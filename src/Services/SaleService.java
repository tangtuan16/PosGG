package Services;

import Models.Sales.CartItem;
import Models.Customer;
import Models.Product;
import Utils.DBConnection;
import Utils.PdfUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleService {

    public List<Product> getFilteredProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setUnit(rs.getString("unit"));
                    product.setSellingPrice(rs.getBigDecimal("selling_price"));
                    product.setQuantity(rs.getInt("quantity"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setImage(rs.getString("image"));
                    products.add(product);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public BigDecimal getProductPrice(int productId) {
        String sql = "SELECT selling_price FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("selling_price");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public int getQuantity(int productId) {
        String sql = "SELECT quantity FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public BigDecimal checkout(List<CartItem> cart, String phoneNumber, int staffId, String paymentMethod, String note, String customerName) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal finalTotal;
        BigDecimal discountPercent = BigDecimal.ZERO;
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Integer customerId = null;
            BigDecimal customerTotalBill = BigDecimal.ZERO;

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                PreparedStatement customerStmt = conn.prepareStatement("SELECT id, total_bill FROM customers WHERE phone = ?");
                customerStmt.setString(1, phoneNumber);
                ResultSet rs = customerStmt.executeQuery();
                if (rs.next()) {
                    customerId = rs.getInt("id");
                    customerTotalBill = rs.getBigDecimal("total_bill");
                }
                rs.close();
                customerStmt.close();
            }

            // Tổng hợp số lượng từng sản phẩm trong giỏ hàng
            Map<Integer, Integer> productQuantityMap = new HashMap<>();
            Map<Integer, Product> productMap = new HashMap<>();
            for (CartItem item : cart) {
                total = total.add(item.getTotalPrice());
                int productId = item.getProduct().getId();
                productQuantityMap.put(productId, productQuantityMap.getOrDefault(productId, 0) + item.getQuantity());
                productMap.put(productId, item.getProduct());
            }

            // Tính phần trăm giảm giá dựa trên tổng tiền đã mua của khách hàng
            if (customerTotalBill.compareTo(BigDecimal.valueOf(1_000_000)) >= 0) {
                discountPercent = new BigDecimal(customerTotalBill.divide(BigDecimal.valueOf(1_000_000), RoundingMode.FLOOR).intValue());
                if (discountPercent.compareTo(BigDecimal.valueOf(10)) > 0) {
                    discountPercent = BigDecimal.valueOf(10);
                }
            }

            BigDecimal discountAmount = total.multiply(discountPercent).divide(BigDecimal.valueOf(100));
            finalTotal = total.subtract(discountAmount);

            // Kiểm tra tồn kho từng sản phẩm với lock FOR UPDATE
            for (Map.Entry<Integer, Integer> entry : productQuantityMap.entrySet()) {
                int productId = entry.getKey();
                int requiredQuantity = entry.getValue();
                System.out.println("Required quantity: " + requiredQuantity);

                PreparedStatement stockStmt = conn.prepareStatement("SELECT quantity FROM products WHERE id = ? FOR UPDATE");
                stockStmt.setInt(1, productId);
                ResultSet rsStock = stockStmt.executeQuery();

                if (rsStock.next()) {
                    int stock = rsStock.getInt("quantity");
                    System.out.println("Stock: " + stock);
                    if (stock < requiredQuantity) {
                        rsStock.close();
                        stockStmt.close();
                        throw new SQLException("Sản phẩm id " + productId + " không đủ hàng trong kho.");
                    }
                } else {
                    rsStock.close();
                    stockStmt.close();
                    throw new SQLException("Sản phẩm id " + productId + " không tồn tại.");
                }
                rsStock.close();
                stockStmt.close();
            }

            // Tạo hóa đơn trong DB
            PreparedStatement invoiceStmt = conn.prepareStatement("""
            INSERT INTO invoices (customer_id, staff_id, total_amount, discount, final_amount, note, payment_method)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, Statement.RETURN_GENERATED_KEYS);

            if (customerId != null) {
                invoiceStmt.setInt(1, customerId);
            } else {
                invoiceStmt.setNull(1, Types.INTEGER);
            }
            invoiceStmt.setInt(2, staffId);
            invoiceStmt.setBigDecimal(3, total);
            invoiceStmt.setBigDecimal(4, discountPercent);
            invoiceStmt.setBigDecimal(5, finalTotal);
            invoiceStmt.setString(6, note);
            invoiceStmt.setString(7, paymentMethod);
            invoiceStmt.executeUpdate();

            ResultSet rsInvoice = invoiceStmt.getGeneratedKeys();
            rsInvoice.next();
            int invoiceId = rsInvoice.getInt(1);

            rsInvoice.close();
            invoiceStmt.close();

            // Thêm chi tiết hóa đơn và trừ kho gộp theo sản phẩm
            for (Map.Entry<Integer, Integer> entry : productQuantityMap.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();

                Product product = productMap.get(productId);
                BigDecimal unitPrice = product.getSellingPrice();
                BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

                PreparedStatement detailStmt = conn.prepareStatement("""
                INSERT INTO invoice_details (invoice_id, product_id, quantity, unit_price, total_price)
                VALUES (?, ?, ?, ?, ?)
                """);
                detailStmt.setInt(1, invoiceId);
                detailStmt.setInt(2, productId);
                detailStmt.setInt(3, quantity);
                detailStmt.setBigDecimal(4, unitPrice);
                detailStmt.setBigDecimal(5, totalPrice);
                detailStmt.executeUpdate();
                detailStmt.close();

                PreparedStatement updateStockStmt = conn.prepareStatement("UPDATE products SET quantity = quantity - ? WHERE id = ?");
                updateStockStmt.setInt(1, quantity);
                updateStockStmt.setInt(2, productId);
                updateStockStmt.executeUpdate();
                updateStockStmt.close();
            }

            if (customerId != null) {
                PreparedStatement updateCustomer = conn.prepareStatement("UPDATE customers SET total_bill = total_bill + ? WHERE id = ?");
                updateCustomer.setBigDecimal(1, finalTotal);
                updateCustomer.setInt(2, customerId);
                updateCustomer.executeUpdate();
                updateCustomer.close();
            }
            conn.commit();

            try {
                PdfUtils.generateInvoicePDF("invoice.pdf", cart, total, discountPercent, finalTotal, paymentMethod, note, customerName, phoneNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return finalTotal;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }

        return BigDecimal.ZERO;
    }



    public Customer findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer c = new Customer();
                    c.setId(rs.getInt("id"));
                    c.setName(rs.getString("name"));
                    c.setPhone(rs.getString("phone"));
                    c.setAddress(rs.getString("address"));
                    c.setTotalBill(rs.getBigDecimal("total_bill"));
                    return c;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
