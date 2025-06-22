package Models;
import java.sql.*;
import java.util.*;
import Utils.DBConnection;

public class SalesModel {
    // Lớp lưu trữ thông tin số lượng bán của một sản phẩm
    public static class SalesData {
        private String productName;
        private String monthYear;
        private int quantity;

        public SalesData(String productName, String monthYear, int quantity) {
            this.productName = productName;
            this.monthYear = monthYear;
            this.quantity = quantity;
        }

        public String getProductName() { return productName; }
        public String getMonthYear() { return monthYear; }
        public int getQuantity() { return quantity; }
    }

    // Lấy dữ liệu từ cơ sở dữ liệu, có thể lọc theo năm hoặc tháng/năm
    public List<SalesData> getSalesData(String year, String month) {
        List<SalesData> salesDataList = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.name AS product_name, " +
                        "DATE_FORMAT(i.created_at, '%Y-%m') AS month_year, " +
                        "SUM(id.quantity) AS total_quantity " +
                        "FROM invoice_details id " +
                        "JOIN invoices i ON id.invoice_id = i.id " +
                        "JOIN products p ON id.product_id = p.id "
        );

        List<String> params = new ArrayList<>();
        if (year != null && !year.isEmpty() && !year.equals("All")) {
            sql.append("WHERE YEAR(i.created_at) = ? ");
            params.add(year);
            if (month != null && !month.isEmpty() && !month.equals("All")) {
                sql.append("AND MONTH(i.created_at) = ? ");
                params.add(month);
            }
        }

        sql.append("GROUP BY p.name, DATE_FORMAT(i.created_at, '%Y-%m') ");
        sql.append("ORDER BY month_year, p.name");

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setString(i + 1, params.get(i));
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String productName = rs.getString("product_name");
                        String monthYear = rs.getString("month_year");
                        int quantity = rs.getInt("total_quantity");
                        salesDataList.add(new SalesData(productName, monthYear, quantity));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return salesDataList;
    }

    // Lấy danh sách năm có sẵn trong dữ liệu
    public List<String> getAvailableYears() {
        List<String> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(created_at) AS year FROM invoices ORDER BY year";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    years.add(rs.getString("year"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return years;
    }
}