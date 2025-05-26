package Services;

import Models.Customer;
import Services.Impl.CustomerService;
import Utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerServiceImpl implements CustomerService {

    private List<Customer> executeQuery(PreparedStatement pstmt) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            Customer customer = new Customer();
            customer.setId((int) rs.getLong("id"));
            customer.setName(rs.getString("name"));
            customer.setTotalBill(BigDecimal.valueOf(rs.getDouble("total_bill")));
            customer.setPhone(rs.getString("phone"));
            customer.setAddress(rs.getString("address"));
            customers.add(customer);
        }
        rs.close();
        pstmt.close();
        return customers;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return getCustomersByPage(1, Integer.MAX_VALUE, "Mặc định");
    }

    @Override
    public void insertCustomer(Customer customer) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            // Kiểm tra trùng số điện thoại
            String checkSql = "SELECT COUNT(*) FROM customers WHERE phone = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, customer.getPhone());
            rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new RuntimeException("Số điện thoại đã tồn tại. Không thể thêm khách hàng.");
            }

            // Nếu không trùng thì thêm mới
            String insertSql = "INSERT INTO customers (name, total_bill, phone, address) VALUES (?, ?, ?, ?)";
            insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, customer.getName());
            insertStmt.setBigDecimal(2, customer.getTotalBill());
            insertStmt.setString(3, customer.getPhone());
            insertStmt.setString(4, customer.getAddress());
            insertStmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khách hàng: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi thêm khách hàng", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (checkStmt != null) checkStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (insertStmt != null) insertStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            DBConnection.closeConnection(conn);
        }
    }

    @Override
    public void updateCustomer(Customer customer) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE customers SET name = ?, total_bill = ?, phone = ?, address = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customer.getName());
            pstmt.setBigDecimal(2, customer.getTotalBill());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getAddress());
            pstmt.setLong(5, customer.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không tìm thấy khách hàng với ID: " + customer.getId());
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật khách hàng: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật khách hàng", e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            DBConnection.closeConnection(conn);
        }
    }

    @Override
    public void deleteCustomers(List<Long> ids) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu giao dịch

            String sql = "DELETE FROM customers WHERE id = ?";
            pstmt = conn.prepareStatement(sql);

            for (Long id : ids) {
                pstmt.setLong(1, id);
                pstmt.addBatch();
            }

            int[] rowsAffected = pstmt.executeBatch();
            int totalRows = 0;
            for (int count : rowsAffected) {
                totalRows += count;
            }

            if (totalRows < ids.size()) {
                throw new SQLException("Một số khách hàng không được xóa do không tìm thấy ID.");
            }

            conn.commit(); // Xác nhận giao dịch
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa nhiều khách hàng: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback(); // Hoàn tác giao dịch nếu có lỗi
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Lỗi khi xóa nhiều khách hàng", e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<Customer> searchCustomers(String keyword) {
        return searchCustomersByPage(keyword, 1, Integer.MAX_VALUE, "Mặc định");
    }

    @Override
    public List<Customer> getCustomersByPage(int page, int pageSize, String sortType) {
        List<Customer> customers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            String orderBy;
            switch (sortType) {
                case "Tên A → Z":
                    orderBy = "name ASC";
                    break;
                case "Tên Z → A":
                    orderBy = "name DESC";
                    break;
                case "Tổng Max → Min":
                    orderBy = "total_bill DESC";
                    break;
                case "Tổng Min → Max":
                    orderBy = "total_bill ASC";
                    break;
                case "Mặc định":
                default:
                    orderBy = "id ASC";
                    break;
            }

            String sql = "SELECT * FROM customers ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, (page - 1) * pageSize);

            customers = executeQuery(pstmt);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy khách hàng theo trang: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy khách hàng theo trang", e);
        } finally {
            DBConnection.closeConnection(conn);
        }

        return customers;
    }

    @Override
    public List<Customer> searchCustomersByPage(String keyword, int page, int pageSize, String sortType) {
        List<Customer> customers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            String orderBy;
            switch (sortType) {
                case "Tên A → Z":
                    orderBy = "name ASC";
                    break;
                case "Tên Z → A":
                    orderBy = "name DESC";
                    break;
                case "Tổng Max → Min":
                    orderBy = "total_bill DESC";
                    break;
                case "Tổng Min → Max":
                    orderBy = "total_bill ASC";
                    break;
                case "Mặc định":
                default:
                    orderBy = "id ASC";
                    break;
            }

            String sql = "SELECT * FROM customers WHERE " +
                    "CAST(id AS CHAR) LIKE ? OR " +
                    "LOWER(name) LIKE ? OR " +
                    "CAST(total_bill AS CHAR) LIKE ? OR " +
                    "phone LIKE ? OR " +
                    "LOWER(address) LIKE ? " +
                    "ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
            pstmt = conn.prepareStatement(sql);
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, likeKeyword);
            pstmt.setString(2, likeKeyword);
            pstmt.setString(3, likeKeyword);
            pstmt.setString(4, likeKeyword);
            pstmt.setString(5, likeKeyword);
            pstmt.setInt(6, pageSize);
            pstmt.setInt(7, (page - 1) * pageSize);

            customers = executeQuery(pstmt);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm khách hàng theo trang: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tìm kiếm khách hàng theo trang", e);
        } finally {
            DBConnection.closeConnection(conn);
        }

        return customers;
    }

    @Override
    public int getTotalCustomers() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int total = 0;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM customers";
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm tổng số khách hàng: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi đếm tổng số khách hàng", e);
        } finally {
            DBConnection.closeConnection(conn);
        }

        return total;
    }

    @Override
    public int getTotalSearchCustomers(String keyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int total = 0;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM customers WHERE " +
                    "CAST(id AS CHAR) LIKE ? OR " +
                    "LOWER(name) LIKE ? OR " +
                    "CAST(total_bill AS CHAR) LIKE ? OR " +
                    "phone LIKE ? OR " +
                    "LOWER(address) LIKE ?";
            pstmt = conn.prepareStatement(sql);
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, likeKeyword);
            pstmt.setString(2, likeKeyword);
            pstmt.setString(3, likeKeyword);
            pstmt.setString(4, likeKeyword);
            pstmt.setString(5, likeKeyword);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm tổng số khách hàng tìm kiếm: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi đếm tổng số khách hàng tìm kiếm", e);
        } finally {
            DBConnection.closeConnection(conn);
        }

        return total;
    }

}
