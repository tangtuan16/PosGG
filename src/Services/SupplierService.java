package Services;

import Models.Supplier;
import Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;

public class SupplierService {

    // lấy tổng số bản ghi
    public int getTotalRecord() {
        int totalRecord = 0;
        String sql = "SELECT COUNT(*) FROM suppliers where status in ('active', 'inactive')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                totalRecord = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalRecord;
    }

    // lấy nhà cung cấp theo phân trang
    public ArrayList<Supplier> getSuppliersPage(int currentPage, int limit) throws SQLException {
        ArrayList<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT " +
                "s.*, " +
                "u1.name AS createdByName, " +
                "u2.name AS updatedByName " +
                "FROM suppliers s " +
                "JOIN users u1 ON u1.id = s.created_by " +
                "JOIN users u2 ON u2.id = s.updated_by " +
                "WHERE s.status IN ('active', 'inactive') " +
                "ORDER BY s.id DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int offset = (currentPage - 1) * limit;
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getInt("created_by"),
                        rs.getString("updated_at"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByName"),
                        rs.getString("updatedByName")
                ));
            }
        }
        return suppliers;
    }

    // lấy tất cả nhà cung cấp
    public ArrayList<Supplier> getAllSuppliers() throws SQLException {
        ArrayList<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT " +
                "s.*, " +
                "u1.name AS createdByName, " +
                "u2.name AS updatedByName " +
                "FROM suppliers s " +
                "JOIN users u1 ON u1.id = s.created_by " +
                "JOIN users u2 ON u2.id = s.updated_by " +
                "WHERE s.status IN ('active', 'inactive') " +
                "ORDER BY s.id DESC ";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getInt("created_by"),
                        rs.getString("updated_at"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByName"),
                        rs.getString("updatedByName")
                ));
            }
        }
        return suppliers;
    }

    // laasy danh sách theo diều kiện bộ lọc
    public ArrayList<Supplier> getFilteredSuppliers(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate, int currentPage, int limit) throws SQLException {
        ArrayList<Supplier> suppliers = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.*, " +
                "u1.name AS createdByName, " +
                "u2.name AS updatedByName " +
                "FROM suppliers s " +
                "JOIN users u1 ON u1.id = s.created_by " +
                "JOIN users u2 ON u2.id = s.updated_by "
        );

        // Danh sách để lưu các điều kiện WHERE
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Điều kiện lọc theo tên (searchText)
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("(s.name LIKE ? OR s.phone LIKE ? OR s.email LIKE ?)");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
        }

        // Điều kiện lọc theo trạng thái (filterStatus)
        if (filterStatus != null && !filterStatus.trim().isEmpty()) {
            conditions.add("s.status = ?");
            parameters.add(filterStatus);
        } else {
            conditions.add("(s.status = ? OR s.status = ?)");
            parameters.add("active");
            parameters.add("inactive");
        }

        // Điều kiện lọc theo người tạo (filterCreatedBy)
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("s.created_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Điều kiện lọc theo ngày tạo (fromDate)
        if (fromDate != null) {
            conditions.add("s.created_at >= ?");
            parameters.add(fromDate);
        }

        // Điều kiện lọc theo ngày tạo (toDate)
        if (toDate != null) {
            conditions.add("s.created_at <= ?");
            parameters.add(toDate);
        }

        // Thêm các điều kiện WHERE nếu có
        if (!conditions.isEmpty()) {
            sql.append(" where ");
            sql.append(String.join(" and ", conditions));
        }

        // Sắp xếp
        sql.append(" ORDER BY s.id DESC");

        // Tính OFFSET
        int offset = (currentPage - 1) * limit;

        // Thêm LIMIT và OFFSET
        sql.append(" LIMIT ? OFFSET ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Gán giá trị cho các tham số trong PreparedStatement
            int index = 1;
            for (Object param : parameters) {
                stmt.setObject(index++, param);
            }

            // Gán LIMIT và OFFSET
            stmt.setInt(index++, limit);
            stmt.setInt(index, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getInt("created_by"),
                        rs.getString("updated_at"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByName"),
                        rs.getString("updatedByName")
                ));
            }
        }
        return suppliers;
    }

    // laasy số bản ghi theo diều kiện bộ lọc
    public int getTotalFilterRecord(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) {
        ArrayList<Supplier> suppliers = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.*, " +
                        "u1.name AS createdByName, " +
                        "u2.name AS updatedByName " +
                        "FROM suppliers s " +
                        "JOIN users u1 ON u1.id = s.created_by " +
                        "JOIN users u2 ON u2.id = s.updated_by "
        );

        // Danh sách để lưu các điều kiện WHERE
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Điều kiện lọc theo tên (searchText)
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("(s.name LIKE ? or s.phone LIKE ? or s.email LIKE ? )");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
        }

        // Điều kiện lọc theo trạng thái (filterStatus)
        if (filterStatus != null && !filterStatus.trim().isEmpty()) {
            conditions.add("s.status = ?");
            parameters.add(filterStatus);
        } else {
            conditions.add("(s.status = ? OR s.status = ?)");
            parameters.add("active");
            parameters.add("inactive");
        }

        // Điều kiện lọc theo người tạo (filterCreatedBy)
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("s.created_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Điều kiện lọc theo ngày tạo (fromDate)
        if (fromDate != null) {
            conditions.add("s.created_at >= ?");
            parameters.add(fromDate);
        }

        // Điều kiện lọc theo ngày tạo (toDate)
        if (toDate != null) {
            conditions.add("s.created_at <= ?");
            parameters.add(toDate);
        }

        // Thêm các điều kiện WHERE nếu có
        if (!conditions.isEmpty()) {
            sql.append(" where ");
            sql.append(String.join(" and ", conditions));
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Gán giá trị cho các tham số trong PreparedStatement
            int index = 1;
            for (Object param : parameters) {
                stmt.setObject(index++, param);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getInt("created_by"),
                        rs.getString("updated_at"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByName"),
                        rs.getString("updatedByName")
                ));
            }

            DBConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers.size();
    }

    public void updateMultiSupplier(int supplierId, String statusRestore, int idUser) throws SQLException{
        String sql = "UPDATE suppliers SET " +
                "status = ?, " +
                "updated_at = NOW(), " +
                "updated_by = ? " +
                "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statusRestore);
            stmt.setInt(2, idUser);
            stmt.setInt(3, supplierId);

            stmt.executeUpdate();
        }
    }



    // AddSupplierFrame
    public void addSupplier(String name, String phone, String email, String address, String status, int createdBy, int updatedBy) throws SQLException{
        String sql = "INSERT INTO suppliers (name, phone, email, address, status, created_at, updated_at, created_by, updated_by) VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setString(5, status);
            stmt.setInt(6, createdBy);
            stmt.setInt(7, updatedBy);

            stmt.executeUpdate();
        }
    }

    // hàm kiểm tra trung lặp email với phone khi thêm nhà cung cấp
    public int checkPhoneOrEmail(String phone, String email) throws SQLException{
        String sql = "SELECT COUNT(*) FROM suppliers WHERE phone = ? OR email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // lấy giá trị COUNT(*)
                }
            }
        }
        return 0;
    }


    // lấy 1 nhà cung caaps
    public Supplier getOneSupplier(int id) throws SQLException{
        String sql = "select * from suppliers where id = ?";
        try(Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1,id);
            try (ResultSet rs = stmt.executeQuery()) { // Lấy kết quả trả về
                if (rs.next()) {
                    return new Supplier(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getString("status"),
                            rs.getString("created_at"),
                            rs.getInt("created_by"),
                            rs.getString("updated_at"),
                            rs.getInt("updated_by")
                    );
                }
            }
        }
        return null;
    }

    // kieerm tra phone với email tồn tại ngoại trừ nhà cung cấp đang chọn
    public int checkPhoneOrEmailID(int id, String phone, String email) throws SQLException{
        String sql = "select count(*) from suppliers where (phone = ? OR email = ?) and id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            stmt.setString(2, email);
            stmt.setInt(3, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // lấy giá trị count(*)
                }
            }
        }
        return 0;
    }

    // cập nhật nh cung cấp
    public void editSupplier(int id, String name, String phone, String email, String address, String status, int updatedBy) throws SQLException{
        String sql = "update suppliers set name = ?, phone = ?, email = ?, address = ?, status = ?, updated_at = NOW(), updated_by = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setString(5, status);
            stmt.setInt(6, updatedBy);
            stmt.setInt(7, id);

            stmt.executeUpdate();
        }
    }

    // xóa danh mục tạm thời
    public void deleteSupplier(int id, int userId) throws SQLException{
        String sql = "update suppliers set status = ?, updated_by = ?, updated_at = NOW() where id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1,"deleted");
            stmt.setInt(2,userId);
            stmt.setInt(3,id);

            stmt.executeUpdate();
        }
    }

    // lấy các nhà cung cấp trong thùng rác
    public ArrayList<Supplier> getAllSupplierTrash(int currentPage, int limit) throws SQLException{
        ArrayList<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT " +
                "s.*, " +
                "u1.name AS createdByName, " +
                "u2.name AS updatedByName " +
                "FROM suppliers s " +
                "JOIN users u1 ON u1.id = s.created_by " +
                "JOIN users u2 ON u2.id = s.updated_by " +
                "WHERE s.status = ? " +
                "ORDER BY s.updated_at DESC " +
                "LIMIT ? OFFSET ?";
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1,"deleted");
            int offset = (currentPage - 1) * limit;
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getInt("created_by"),
                        rs.getString("updated_at"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByName"),
                        rs.getString("updatedByName")
                ));
            }
            return suppliers;
        }
    }

    public int getTotalRecordTrash() throws SQLException{
        int totalRecordTrash = 0;
        String sql = "select count(*) from suppliers where status = 'deleted' ";

        try(Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                totalRecordTrash = rs.getInt(1);
            }
            return  totalRecordTrash;
        }
    }

    public ArrayList<Supplier> getFilteredSuppliersPageTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate, int currentPage, int limit) throws SQLException{
        ArrayList<Supplier> suppliers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s.*, u1.name AS createdByName, u2.name AS updatedByName " +
                        "FROM suppliers s " +
                        "JOIN users u1 ON u1.id = s.created_by " +
                        "JOIN users u2 ON u2.id = s.updated_by " +
                        "WHERE s.status in ('deleted')");

        // Danh sách điều kiện bổ sung
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Điều kiện lọc theo tên (searchText)
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("(s.name LIKE ? OR s.phone LIKE ? OR s.email LIKE ?)");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
        }

        // Lọc theo người tạo
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("s.updated_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Lọc theo ngày tạo từ
        if (fromDate != null) {
            conditions.add("s.updated_at >= ?");
            parameters.add(fromDate);
        }

        // Lọc theo ngày tạo đến
        if (toDate != null) {
            conditions.add("s.updated_at <= ?");
            parameters.add(toDate);
        }

        // Gắn các điều kiện vào câu SQL
        for (String condition : conditions) {
            sql.append(" and ").append(condition);
        }

        // Sắp xếp theo thời gian tạo giảm dần
        sql.append(" order by s.updated_at DESC");

        // Tính OFFSET
        int offset = (currentPage - 1) * limit;

        // Thêm LIMIT và OFFSET vào cuối
        sql.append(" limit ? offset ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            // Gán các tham số cho điều kiện WHERE
            for (Object param : parameters) {
                stmt.setObject(index++, param);
            }

            // Gán LIMIT và OFFSET
            stmt.setInt(index++, limit);
            stmt.setInt(index, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getInt("created_by"),
                        rs.getString("updated_at"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByName"),
                        rs.getString("updatedByName")
                ));
            }
            return suppliers;
        }
    }

    public int getTotalRecordFilterTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) throws SQLException{
        ArrayList<Supplier> suppliers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT " +
                "s.*, " +
                "u1.name AS createdByName, " +
                "u2.name AS updatedByName " +
                "FROM suppliers s " +
                "JOIN users u1 ON u1.id = s.created_by " +
                "JOIN users u2 ON u2.id = s.updated_by " +
                "WHERE s.status = 'deleted' ");

        // Danh sách điều kiện bổ sung
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Điều kiện lọc theo tên (searchText)
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("(s.name LIKE ? OR s.phone LIKE ? OR s.email LIKE ?)");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
        }

        // Lọc theo người tạo
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("s.created_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Lọc theo ngày tạo từ
        if (fromDate != null) {
            conditions.add("s.created_at >= ?");
            parameters.add(fromDate);
        }

        // Lọc theo ngày tạo đến
        if (toDate != null) {
            conditions.add("s.created_at <= ?");
            parameters.add(toDate);
        }

        // Gắn các điều kiện vào câu SQL
        for (String condition : conditions) {
            sql.append(" AND ").append(condition);
        }


        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            // Gán các tham số cho điều kiện WHERE
            for (Object param : parameters) {
                stmt.setObject(index++, param);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getInt("created_by"),
                        rs.getString("updated_at"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByName"),
                        rs.getString("updatedByName")
                ));
            }
            return suppliers.size();
        }
    }

    public void deletePermanentlySupplier(int id) throws SQLException{
        String sql = "delete from suppliers where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();
        }
    }


    public ArrayList<Supplier> getSupplierActive() throws SQLException{
        ArrayList<Supplier> suppliers = new ArrayList<>();
        String sql = "select * from suppliers where status = ?";
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1,"active");
            ResultSet rs = stmt.executeQuery();

            while(rs.next()){
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getInt("created_by"),
                        rs.getString("updated_at"),
                        rs.getInt("updated_by")
                ));
            }
            return suppliers;
        }
    }
}
