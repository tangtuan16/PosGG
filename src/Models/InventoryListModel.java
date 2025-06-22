package Models;

import java.sql.*;
import java.util.*;
import Utils.DBConnection;

public class InventoryListModel {
    // Lớp lưu trữ thông tin sản phẩm
    public static class ProductData {
        private String productName;
        private Timestamp createdAt;
        private int quantity;

        public ProductData(String productName, Timestamp createdAt, int quantity) {
            this.productName = productName;
            this.createdAt = createdAt;
            this.quantity = quantity;
        }

        public String getProductName() { return productName; }
        public Timestamp getCreatedAt() { return createdAt; }
        public int getQuantity() { return quantity; }
    }

    // Lấy dữ liệu sản phẩm chưa từng được bán, hỗ trợ tìm kiếm theo tên
    public List<ProductData> getProductData(String searchTerm) {
        List<ProductData> productDataList = new ArrayList<>();
        String sql = "SELECT p.name, p.created_at, p.quantity " +
                "FROM products p " +
                "LEFT JOIN invoice_details id ON p.id = id.product_id " +
                "WHERE id.product_id IS NULL " +
                (searchTerm != null && !searchTerm.trim().isEmpty() ? "AND p.name LIKE ? " : "") +
                "ORDER BY p.created_at, p.name";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    stmt.setString(1, "%" + searchTerm.trim() + "%");
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String productName = rs.getString("name");
                        Timestamp createdAt = rs.getTimestamp("created_at");
                        int quantity = rs.getInt("quantity");
                        productDataList.add(new ProductData(productName, createdAt, quantity));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return productDataList;
    }
}