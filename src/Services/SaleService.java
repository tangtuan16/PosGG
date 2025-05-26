package Services;

import Models.Sales.CartItem;
import Models.Customer;
import Models.Product;
import Utils.DBConnection;
import Utils.PdfUtils;
import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

public class SaleService {
    private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);
    private static final BigDecimal MAX_DISCOUNT_PERCENT = BigDecimal.valueOf(10);
    private static final String INVOICE_PDF_PATH = "invoice.pdf";

    private List<CartItem> cart;
    private EmailService emailService;
    private volatile boolean scanning = false;

    public SaleService() {
        this.cart = new ArrayList<>();
        this.emailService = new EmailService();
    }

    public List<Product> loadProducts(String keyword, int page, int size) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE (name LIKE ? OR barcode LIKE ?) AND status = 'active' LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            stmt.setInt(3, size);
            stmt.setInt(4, (page - 1) * size);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(createProductFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new SaleServiceException("Failed to retrieve products: " + e.getMessage(), e);
        }

        return products;
    }

    public List<Product> findByKeys(String keyword, int page, int size) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE (LOWER(name) LIKE ? OR LOWER(barcode)) AND status = 'actvie' LIKE ? LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setInt(3, size);
            stmt.setInt(4, (page - 1) * size);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(createProductFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new SaleServiceException("Failed to retrieve product suggestions: " + e.getMessage(), e);
        }

        return products;
    }

    private Product createProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setUnit(rs.getString("unit"));
        product.setBarcode(rs.getString("barcode"));
        product.setSellingPrice(rs.getBigDecimal("selling_price"));
        product.setQuantity(rs.getInt("quantity"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setImage(rs.getString("image"));
        product.setDiscount(rs.getBigDecimal("discount") != null ? rs.getBigDecimal("discount") : BigDecimal.ZERO);
        return product;
    }

    public boolean addToCart(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return false;
        }
        int stock = product.getQuantity();
        int existingQty = 0;
        for (CartItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                existingQty = item.getQuantity();
                break;
            }
        }
        if (existingQty + quantity > stock) {
            return false;
        }
        for (CartItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(existingQty + quantity);
                return true;
            }
        }
        cart.add(new CartItem(product, quantity));
        return true;
    }

    public boolean updateQuantity(int productId, int newQuantity) {
        for (CartItem item : cart) {
            if (item.getProduct().getId() == productId) {
                int stock = item.getProduct().getQuantity();
                if (newQuantity > stock) {
                    return false;
                }
                if (newQuantity <= 0) {
                    cart.remove(item);
                } else {
                    item.setQuantity(newQuantity);
                }
                return true;
            }
        }
        return false;
    }

    public void removeFromCart(int productId) {
        cart.removeIf(item -> item.getProduct().getId() == productId);
    }

    public List<CartItem> getCart() {
        return new ArrayList<>(cart);
    }

    public void clearCart() {
        cart.clear();
    }

    public BigDecimal calculateFinalPrice(Product product, int quantity) {
        BigDecimal originalPrice = product.getSellingPrice();
        BigDecimal discount = product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO;
        BigDecimal discountAmount = originalPrice.multiply(discount).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        BigDecimal discountedPrice = originalPrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        return discountedPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal checkout(String phoneNumber, int staffId, String paymentMethod, String note, String customerName) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int[] invoiceIdHolder = new int[1];
                BigDecimal finalTotal = processCheckout(conn, phoneNumber, staffId, paymentMethod, note, customerName, invoiceIdHolder);
                conn.commit();
                generateInvoicePDF(cart, invoiceIdHolder[0], finalTotal, getCustomerDiscountPercent(phoneNumber), phoneNumber, paymentMethod, note, customerName);
                this.cart.clear();
                return finalTotal;
            } catch (SQLException | IOException e) {
                conn.rollback();
                throw new SaleServiceException("Thanh toán thất bại: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new SaleServiceException("Không thể kết nối cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    private BigDecimal processCheckout(Connection conn, String phoneNumber, int staffId, String paymentMethod, String note, String customerName, int[] invoiceIdHolder) throws SQLException {
        // Kiểm tra giỏ hàng
        if (cart.isEmpty()) {
            throw new SaleServiceException("Giỏ hàng rỗng, không thể thanh toán", null);
        }

        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, Integer> productQuantityMap = new HashMap<>();

        // Bước 1: Tính tổng tiền và chuẩn bị dữ liệu sản phẩm
        for (CartItem item : cart) {
            Product product = item.getProduct();
            int productId = product.getId();
            int quantity = item.getQuantity();
            BigDecimal itemTotal = calculateFinalPrice(product, quantity);
            total = total.add(itemTotal);
            productQuantityMap.put(productId, quantity);
        }

        // Bước 2: Lấy thông tin khách hàng và tính giảm giá
        Integer customerId = null;
        BigDecimal discountPercent = BigDecimal.ZERO;
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            try {
                Customer customer = findByPhone(phoneNumber);
                if (customer != null) {
                    customerId = customer.getId();
                    BigDecimal totalBill = customer.getTotalBill() != null ? customer.getTotalBill() : BigDecimal.ZERO;
                    if (totalBill.compareTo(MILLION) >= 0) {
                        discountPercent = totalBill.divide(MILLION, 0, RoundingMode.FLOOR).min(MAX_DISCOUNT_PERCENT);
                    }
                }
            } catch (SaleServiceException e) {
                System.err.println("Lỗi khi tìm khách hàng: " + e.getMessage());
            }
        }

        // Bước 3: Áp dụng giảm giá khách hàng
        BigDecimal discountAmount = total.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        BigDecimal finalTotal = total.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        // Bước 4: Thêm hóa đơn
        invoiceIdHolder[0] = insertInvoice(conn, customerId, staffId, total, discountPercent, finalTotal, note, paymentMethod);

        // Bước 5: Thêm chi tiết hóa đơn và cập nhật kho
        insertInvoiceDetailsAndUpdateStock(conn, invoiceIdHolder[0], productQuantityMap);

        // Bước 6: Cập nhật tổng chi tiêu của khách hàng
        if (customerId != null) {
            updateCustomerTotalBill(conn, customerId, finalTotal);
        }

        return finalTotal;
    }

    private int insertInvoice(Connection conn, Integer customerId, int staffId, BigDecimal total, BigDecimal discountPercent, BigDecimal finalTotal, String note, String paymentMethod) throws SQLException {
        String sql = "INSERT INTO invoices (customer_id, staff_id, total_amount, discount, final_amount, note, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, customerId);
            ps.setInt(2, staffId);
            ps.setBigDecimal(3, total);
            ps.setBigDecimal(4, discountPercent);
            ps.setBigDecimal(5, finalTotal);
            ps.setString(6, note);
            ps.setString(7, paymentMethod);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void insertInvoiceDetailsAndUpdateStock(Connection conn, int invoiceId, Map<Integer, Integer> productQuantityMap) throws SQLException {
        String sqlDetail = "INSERT INTO invoice_details (invoice_id, product_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE products SET quantity = quantity - ? WHERE id = ?";

        try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
             PreparedStatement psUpdateStock = conn.prepareStatement(sqlUpdateStock)) {
            for (Map.Entry<Integer, Integer> entry : productQuantityMap.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                Product product = getProductById(productId);
                BigDecimal discountedPrice = calculateFinalPrice(product, 1); // Price for one unit
                BigDecimal totalPrice = discountedPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);

                psDetail.setInt(1, invoiceId);
                psDetail.setInt(2, productId);
                psDetail.setInt(3, quantity);
                psDetail.setBigDecimal(4, discountedPrice);
                psDetail.setBigDecimal(5, totalPrice);
                psDetail.addBatch();

                psUpdateStock.setInt(1, quantity);
                psUpdateStock.setInt(2, productId);
                psUpdateStock.addBatch();
            }
            psDetail.executeBatch();
            psUpdateStock.executeBatch();
        }
    }

    private void updateCustomerTotalBill(Connection conn, int customerId, BigDecimal finalTotal) throws SQLException {
        String sql = "UPDATE customers SET total_bill = total_bill + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, finalTotal);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    private void generateInvoicePDF(List<CartItem> cart, int invoiceId, BigDecimal finalTotal, BigDecimal discountPersen, String phoneNumber, String paymentMethod, String note, String customerName) throws IOException {
        if (cart == null || cart.isEmpty()) {
            throw new IOException("Giỏ hàng rỗng, không thể tạo hóa đơn PDF");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            total = total.add(calculateFinalPrice(item.getProduct(), item.getQuantity()));
        }

        PdfUtils.generateInvoicePDF(
                INVOICE_PDF_PATH,
                cart,
                invoiceId,
                total,
                discountPersen,
                finalTotal,
                paymentMethod,
                note,
                customerName,
                phoneNumber
        );
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
        } catch (SQLException e) {
            throw new SaleServiceException("Failed to find customer by phone: " + e.getMessage(), e);
        }
        return null;
    }

    public Product getProductById(int productId) {
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createProductFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new SaleServiceException("Failed to retrieve product by ID: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean sendInvoiceToEmail(String email, BigDecimal totalAmount, String attachmentPath) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String content = buildInvoiceEmailContent(email);
        try {
            emailService.sendInvoiceWithAttachment(email, "Hóa đơn mua hàng", content, attachmentPath);
            return true;
        } catch (Exception e) {
            throw new SaleServiceException("Failed to send invoice email: " + e.getMessage(), e);
        }
    }

    public String buildInvoiceEmailContent(String emai) {
        return "<h2>Hóa đơn mua hàng</h2>" +
                "<p>Xin chào khách hàng,</p>" +
                "<p>Cảm ơn bạn đã mua hàng tại cửa hàng chúng tôi.</p>" +
                "<p>Chúng tôi rất mong được phục vụ bạn lần sau!</p>" +
                "<br><p>Trân trọng,</p>" +
                "<p>Đội ngũ PosGG</p>";
    }

    public BigDecimal getCustomerDiscountPercent(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            Customer customer = findByPhone(phone);
            if (customer == null) {
                return BigDecimal.ZERO;
            }

            BigDecimal totalBill = customer.getTotalBill() != null ? customer.getTotalBill() : BigDecimal.ZERO;
            if (totalBill.compareTo(MILLION) >= 0) {
                return totalBill.divide(MILLION, 0, RoundingMode.FLOOR).min(MAX_DISCOUNT_PERCENT);
            }
            return BigDecimal.ZERO;
        } catch (SaleServiceException e) {
            System.err.println("Lỗi khi lấy phần trăm giảm giá: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public String startBarcodeScanner(Webcam webcam) {
        scanning = true;
        try {
            MultiFormatReader reader = new MultiFormatReader();
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.ITF,
                    BarcodeFormat.CODABAR
            ));
            reader.setHints(hints);

            while (scanning) {
                BufferedImage image = webcam.getImage();
                if (image == null) continue;

                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
                        new BufferedImageLuminanceSource(image)));

                try {
                    Result result = reader.decode(bitmap);
                    scanning = false;
                    return result.getText();
                } catch (NotFoundException e) {

                }

                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        }
        return null;
    }

    public void stopBarcodeScanner() {
        scanning = false;
    }

    public static class SaleServiceException extends RuntimeException {
        public SaleServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}