package Services;

import Models.Sales.Invoice;
import Models.Sales.InvoiceItem;
import Utils.DBConnection;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InvoiceService {

    public List<Invoice> search(String keyword, String fromDateStr, String toDateStr) {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.*, c.name AS customer_name, c.phone AS customer_phone, u.name AS staff_name " +
                "FROM invoices i " +
                "LEFT JOIN customers c ON i.customer_id = c.id " +
                "LEFT JOIN users u ON i.staff_id = u.id " +
                "WHERE (CAST(i.id AS CHAR) LIKE ? OR CONCAT(c.name, ' ', c.phone) LIKE ?) " +
                "AND i.created_at BETWEEN ? AND ? " +
                "ORDER BY i.created_at DESC";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            Timestamp fromTimestamp = parseStartOfDay(fromDateStr);
            Timestamp toTimestamp = parseEndOfDay(toDateStr);

            ps.setTimestamp(3, fromTimestamp);
            ps.setTimestamp(4, toTimestamp);
            System.out.println("from: " + fromTimestamp + " to: " + toTimestamp);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Invoice inv = new Invoice();
                    inv.setId(rs.getInt("id"));
                    inv.setCustomerId(rs.getInt("customer_id"));
                    inv.setStaffId(rs.getInt("staff_id"));
                    inv.setCreatedAt(rs.getTimestamp("created_at"));
                    inv.setTotalAmount(rs.getBigDecimal("total_amount"));
                    inv.setDiscount(rs.getBigDecimal("discount"));
                    inv.setFinalAmount(rs.getBigDecimal("final_amount"));
                    inv.setNote(rs.getString("note"));
                    inv.setPaymentMethod(rs.getString("payment_method"));
                    inv.setStatus(rs.getString("status"));
                    inv.setCustomerName(rs.getString("customer_name"));
                    inv.setStaffName(rs.getString("staff_name"));
                    inv.setCustomerPhone(rs.getString("customer_phone"));
                    list.add(inv);
                }
            }

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return list;
    }

    private Timestamp parseStartOfDay(String dateStr) throws ParseException {
        return Timestamp.valueOf(dateStr + " 00:00:00");
    }

    private Timestamp parseEndOfDay(String dateStr) throws ParseException {
        return Timestamp.valueOf(dateStr + " 23:59:59");
    }

    public List<InvoiceItem> getInvoiceItems(int invoiceId) {
        List<InvoiceItem> items = new ArrayList<>();
        String sql = "SELECT d.product_id, p.name as product_name, d.quantity, d.unit_price, d.total_price " +
                "FROM invoice_details d " +
                "JOIN products p ON d.product_id = p.id " +
                "WHERE d.invoice_id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, invoiceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = new InvoiceItem();
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    items.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }
}
