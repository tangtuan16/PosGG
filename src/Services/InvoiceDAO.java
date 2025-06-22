package Services;

import Models.OrderStatistic;

import java.sql.*;
import java.util.*;

public class InvoiceDAO {
    private Connection conn;

    public InvoiceDAO(Connection conn) {
        this.conn = conn;
    }

    public List<OrderStatistic> getStatisticBy(String type, Integer year) throws SQLException {
        String groupExpr;
        String labelExpr;

        switch (type) {
            case "Tuần":
                groupExpr = "YEAR(created_at), WEEK(created_at)";
                labelExpr = "CONCAT('Tuần ', WEEK(created_at), '/', YEAR(created_at))";
                break;
            case "Tháng":
                groupExpr = "YEAR(created_at), MONTH(created_at)";
                labelExpr = "CONCAT('Tháng ', MONTH(created_at), '/', YEAR(created_at))";
                break;
            case "Quý":
                groupExpr = "YEAR(created_at), QUARTER(created_at)";
                labelExpr = "CONCAT('Quý ', QUARTER(created_at), '/', YEAR(created_at))";
                break;
            case "Năm":
                groupExpr = "YEAR(created_at)";
                labelExpr = "CONCAT('Năm ', CAST(YEAR(created_at) AS CHAR))";
                break;
            default:
                throw new IllegalArgumentException("Invalid type");
        }

        String sql = "SELECT t.timeGroup, t.total, t.min_date " +
                "FROM (" +
                "    SELECT " + groupExpr + ", " + labelExpr + " AS timeGroup, " +
                "           SUM(final_amount) AS total, MIN(created_at) AS min_date " +
                "    FROM invoices " +
                "    WHERE YEAR(created_at) = ? " +
                "    GROUP BY " + groupExpr + ", " + labelExpr + " " +
                ") t " +
                "ORDER BY t.min_date";

        List<OrderStatistic> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderStatistic(
                            rs.getString("timeGroup"),
                            rs.getDouble("total")
                    ));
                }
            }
        }
        return list;
    }
}