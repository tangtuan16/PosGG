package Services;

import Models.Category;
import Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;

public class CategoryService {

    // lấy số bản ghi đang trong thuùng rác
    // lấy tổng số bản ghi
    public int getTotalRecordTrash() {
        int totalRecordTrash = 0;
        String sql = "select COUNT(*) from categories where status = 'deleted'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                totalRecordTrash = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalRecordTrash;
    }

    // lấy tổng số bản ghi
    public int getTotalRecord() {
        int totalRecord = 0;
        String sql = "select COUNT(*) from categories where status in ('active', 'inactive')";

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

    // xóa vĩnh viễn
    public void deletePermanentlyCategory(int id) throws SQLException{
        String sql = "DELETE from categories where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();
        }
    }

    public void updateMultiCategory(int idCategory, String statusRestore, int idUser) throws SQLException{
        String sql = "update categories SET " +
                "status = ?, " +
                "updated_at = NOW(), " +
                "updated_by = ? " +
                "where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statusRestore);
            stmt.setInt(2, idUser);
            stmt.setInt(3, idCategory);

            stmt.executeUpdate();
        }
    }

    // laasy tất cả danh mục đang bị xóa mềm
    public ArrayList<Category> getAllCategoriesTrash(int currentPage, int limit) throws SQLException {
        ArrayList<Category> categories = new ArrayList<>();
        String sql = "select " +
                "    c.*, " +
                "    u1.name AS createdByUsername, " +
                "    u2.name AS updatedByUsername, " +
                "    CASE " +
                "        WHEN c.parent_id IS NULL THEN NULL " +
                "        WHEN pc.name IS NULL THEN 'Unknown' " +
                "        ELSE pc.name " +
                "    END AS parentName " +
                "from categories c " +
                "join users u1 ON u1.id = c.created_by " +
                "join users u2 ON u2.id = c.updated_by " +
                "LEFT join categories pc ON pc.id = c.parent_id " +
                "where c.status = ? " +
                "ORDER BY c.updated_at DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "deleted");
            int offset = (currentPage - 1) * limit;
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("parent_id"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByUsername"),
                        rs.getString("updatedByUsername"),
                        rs.getString("parentName")));
            }
        }
        return categories;
    }

    // xóa danh mục
    public  void deleteCategory(int id, int idUser) throws SQLException{
        String sql = "update categories set " +
                "status = ?, " +
                "updated_at = NOW(), " +
                "updated_by = ? " +
                "where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "deleted");
            stmt.setInt(2, idUser);
            stmt.setInt(3, id);

            stmt.executeUpdate();
        }
    }

    // sửa 1 danh mục theo id
    public void updateCategory(int id, String name, String description, String status, int idParent, int userId) throws SQLException {
        String sql = "update categories set " +
                "name = ?, " +
                "description = ?, " +
                "status = ?, " +
                "updated_at = NOW(), " +
                "parent_id = ?, " +
                "updated_by = ? " +
                "where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, status);

            // Gán NULL nếu idParent = 0
            if (idParent == 0) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, idParent);
            }

            stmt.setInt(5, userId); // updated_by
            stmt.setInt(6, id);     // where id =

            stmt.executeUpdate();
        }
    }


    // lấy 1 danh mục theo id danh mục
    public Category getCategory(int id){
        Category category = new Category();
        String sql = "select c.*, u1.name AS createdByUsername, u2.name AS updatedByUsername, pc.name AS parentName " +
                "from categories c " +
                "join users u1 ON u1.id = c.created_by " +
                "join users u2 ON u2.id = c.updated_by " +
                "LEFT join categories pc ON pc.id = c.parent_id " +
                "where c.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id); // Thay thế dấu hỏi chấm bằng giá trị id

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                category = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("parent_id"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByUsername"),
                        rs.getString("updatedByUsername"),
                        rs.getString("parentName")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category;
    }


    // lấy danh mục theo phân trang
    public ArrayList<Category> getCategoriesPage(int currentPage, int limit) throws SQLException {
        ArrayList<Category> categories = new ArrayList<>();
        String sql = "select " +
                "    c.*, " +
                "    u1.name AS createdByUsername, " +
                "    u2.name AS updatedByUsername, " +
                "    CASE " +
                "        WHEN c.parent_id IS NULL THEN NULL " +
                "        WHEN pc.name IS NULL THEN 'Unknown' " +
                "        ELSE pc.name " +
                "    END AS parentName " +
                "from categories c " +
                "join users u1 ON u1.id = c.created_by " +
                "join users u2 ON u2.id = c.updated_by " +
                "LEFT join categories pc ON pc.id = c.parent_id " +
                "where c.status IN ('active', 'inactive') " +
                "ORDER BY c.created_at DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int offset = (currentPage - 1) * limit;
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("parent_id"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByUsername"),
                        rs.getString("updatedByUsername"),
                        rs.getString("parentName")
                ));
            }
        }
        return categories;
    }


    // laasy tất cả danh mục
    public ArrayList<Category> getAllCategories() throws SQLException {
        ArrayList<Category> categories = new ArrayList<>();
        String sql = "select " +
                "    c.*, " +
                "    u1.username AS createdByUsername, " +
                "    u2.username AS updatedByUsername, " +
                "    CASE " +
                "        WHEN c.parent_id IS NULL THEN NULL " +
                "        WHEN pc.name IS NULL THEN 'Unknown' " +
                "        ELSE pc.name " +
                "    END AS parentName " +
                "from categories c " +
                "join users u1 ON u1.id = c.created_by " +
                "join users u2 ON u2.id = c.updated_by " +
                "LEFT join categories pc ON pc.id = c.parent_id " +
                "where c.status IN ('active', 'inactive') ";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("parent_id"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByUsername"),
                        rs.getString("updatedByUsername"),
                        rs.getString("parentName")));
            }
        }
        return categories;
    }

    // lấy tổng số bản ghi khi lọc
    public int getTotalFilterRecord(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) throws SQLException {
        ArrayList<Category> categories = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select c.*, u1.username AS createdByUsername, u2.username AS updatedByUsername, pc.name AS parentName " +
                "from categories c " +
                "join users u1 ON u1.id = c.created_by " +
                "join users u2 ON u2.id = c.updated_by " +
                "LEFT join categories pc ON pc.id = c.parent_id ");

        // Danh sách để lưu các điều kiện where
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Điều kiện lọc theo tên (searchText)
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("c.name LIKE ?");
            parameters.add("%" + searchText + "%");
        }

        // Điều kiện lọc theo trạng thái (filterStatus)
        if (filterStatus != null && !filterStatus.trim().isEmpty()) {
            conditions.add("c.status = ?");
            parameters.add(filterStatus);
        } else {
            conditions.add("(c.status = ? OR c.status = ?)");
            parameters.add("active");
            parameters.add("inactive");
        }

        // Điều kiện lọc theo người tạo (filterCreatedBy)
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("c.created_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Điều kiện lọc theo created_at (fromDate)
        if (fromDate != null) {
            conditions.add("c.created_at >= ?");
            parameters.add(fromDate);
        }

        // Điều kiện lọc theo created_at (toDate)
        if (toDate != null) {
            conditions.add("c.created_at <= ?");
            parameters.add(toDate);
        }

        // Thêm các điều kiện vào câu SQL nếu có
        if (!conditions.isEmpty()) {
            sql.append(" where ");
            sql.append(String.join(" AND ", conditions));
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Gán giá trị cho các tham số trong PreparedStatement
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String parentName = rs.getString("parentName");
                if (parentName == null) {
                    parentName = "";
                }

                categories.add(new Category(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("parent_id"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByUsername"),
                        rs.getString("updatedByUsername"),
                        rs.getString("parentName")));

            }
        }
        return categories.size();
    }


    // laasy danh sách theo diều kiện bộ lọc
    public ArrayList<Category> getFilteredCategories(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate, int currentPage, int limit) throws SQLException {
        ArrayList<Category> categories = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select c.*, u1.name AS createdByUsername, u2.name AS updatedByUsername, pc.name AS parentName " +
                "from categories c " +
                "join users u1 ON u1.id = c.created_by " +
                "join users u2 ON u2.id = c.updated_by " +
                "LEFT join categories pc ON pc.id = c.parent_id ");

        // Danh sách để lưu các điều kiện where
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Điều kiện lọc theo tên (searchText)
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("c.name LIKE ?");
            parameters.add("%" + searchText + "%");
        }

        // Điều kiện lọc theo trạng thái (filterStatus)
        if (filterStatus != null && !filterStatus.trim().isEmpty()) {
            conditions.add("c.status = ?");
            parameters.add(filterStatus);
        } else {
            conditions.add("(c.status = ? OR c.status = ?)");
            parameters.add("active");
            parameters.add("inactive");
        }

        // Điều kiện lọc theo người tạo (filterCreatedBy)
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("c.created_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Điều kiện lọc theo ngày tạo (fromDate)
        if (fromDate != null) {
            conditions.add("c.created_at >= ?");
            parameters.add(fromDate);
        }

        // Điều kiện lọc theo ngày tạo (toDate)
        if (toDate != null) {
            conditions.add("c.created_at <= ?");
            parameters.add(toDate);
        }

        // Thêm các điều kiện where nếu có
        if (!conditions.isEmpty()) {
            sql.append(" where ");
            sql.append(String.join(" AND ", conditions));
        }

        // Sắp xếp
        sql.append(" ORDER BY c.updated_at DESC");

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
                String parentName = rs.getString("parentName");
                if (parentName == null) {
                    parentName = "";
                }

                categories.add(new Category(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("parent_id"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByUsername"),
                        rs.getString("updatedByUsername"),
                        parentName));
            }
        }
        return categories;
    }

    // laasy danh sách theo diều kiện bộ lọc trong thùng rác
    public ArrayList<Category> getFilteredCategoriesPageTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate, int currentPage, int limit) throws SQLException {
        ArrayList<Category> categories = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select c.*, u1.name AS createdByUsername, u2.name AS updatedByUsername, pc.name AS parentName " +
                "from categories c " +
                "join users u1 ON u1.id = c.created_by " +
                "join users u2 ON u2.id = c.updated_by " +
                "LEFT join categories pc ON pc.id = c.parent_id " +
                "where c.status = 'deleted'");

        // Danh sách điều kiện bổ sung
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Lọc theo tên
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("c.name LIKE ?");
            parameters.add("%" + searchText + "%");
        }

        // Lọc theo người xóa
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("c.updated_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Lọc theo ngày tạo từ
        if (fromDate != null) {
            conditions.add("c.updated_at >= ?");
            parameters.add(fromDate);
        }

        // Lọc theo ngày tạo đến
        if (toDate != null) {
            conditions.add("c.updated_at <= ?");
            parameters.add(toDate);
        }

        // Gắn các điều kiện vào câu SQL
        for (String condition : conditions) {
            sql.append(" AND ").append(condition);
        }

        // Sắp xếp theo thời gian tạo giảm dần
        sql.append(" ORDER BY c.updated_at DESC");

        // Tính OFFSET
        int offset = (currentPage - 1) * limit;

        // Thêm LIMIT và OFFSET vào cuối
        sql.append(" LIMIT ? OFFSET ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            // Gán các tham số cho điều kiện where
            for (Object param : parameters) {
                stmt.setObject(index++, param);
            }

            // Gán LIMIT và OFFSET
            stmt.setInt(index++, limit);
            stmt.setInt(index, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String parentName = rs.getString("parentName");
                if (parentName == null) {
                    parentName = "";
                }

                categories.add(new Category(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("parent_id"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByUsername"),
                        rs.getString("updatedByUsername"),
                        parentName));
            }
        }
        return categories;
    }

    // laasy tổng bản ghi theo diều kiện bộ lọc trong thùng rác
    public int getTotalRecordFilterTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) {
        ArrayList<Category> categories = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select c.*, u1.name AS createdByUsername, u2.name AS updatedByUsername, pc.name AS parentName " +
                "from categories c " +
                "join users u1 ON u1.id = c.created_by " +
                "join users u2 ON u2.id = c.updated_by " +
                "LEFT join categories pc ON pc.id = c.parent_id " +
                "where c.status = 'deleted'");

        // Danh sách để lưu các điều kiện where
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Điều kiện lọc theo tên (searchText)
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("c.name LIKE ?");
            parameters.add("%" + searchText + "%");
        }

        // Điều kiện lọc theo người tạo (filterCreatedBy)
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("c.created_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Điều kiện lọc theo created_at (fromDate)
        if (fromDate != null) {
            conditions.add("c.created_at >= ?");
            parameters.add(fromDate);
        }

        // Điều kiện lọc theo created_at (toDate)
        if (toDate != null) {
            conditions.add("c.created_at <= ?");
            parameters.add(toDate);
        }

        // Gắn các điều kiện vào câu SQL
        for (String condition : conditions) {
            sql.append(" AND ").append(condition);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Gán giá trị cho các tham số trong PreparedStatement
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String parentName = rs.getString("parentName");
                if (parentName == null) {
                    parentName = "";
                }

                categories.add(new Category(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("parent_id"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("createdByUsername"),
                        rs.getString("updatedByUsername"),
                        rs.getString("parentName")));

            }
            DBConnection.closeConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories.size();
    }


    // Lưu danh mục mới
    public void createCategory(String name, String description, String status, int idParent, int createdBy, int updatedBy) throws SQLException{
        String sql = "INSERT INTO categories (name, description, status, created_at, updated_at, parent_id, created_by, updated_by)" +
                "VALUES (?, ?, ?, NOW(), NOW(), ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); // Tùy cách bạn kết nối DB
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, status);
            // Nếu idParent == 0 thì gán NULL cho parent_id
            if (idParent == 0) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, idParent);
            }
            stmt.setInt(5, createdBy);
            stmt.setInt(6, updatedBy);

            stmt.executeUpdate();
        }
    }

    // hàm kiểm tra trung lặp name  khi thêm danh mục mới
    public int checkNameExist(String name) throws SQLException{
        String sql = "select count(*) from categories where name = ? ";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // lấy giá trị COUNT(*)
                }
            }
        }
        return 0;
    }

    // kieerm tra name tồn tại ngoại trừ nhà cung cấp đang chọn
    public int checkNameExistId(int id, String name) throws SQLException{
        String sql = "select count(*) from categories where name = ? and id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // lấy giá trị count(*)
                }
            }
        }
        return 0;
    }
}
