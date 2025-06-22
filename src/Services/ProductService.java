package Services;

import Models.Product;
import Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class ProductService {

    // lấy tổng số bản ghi
    public int getTotalRecord() {
        int totalRecord = 0;
        String sql = "select count(*) from products where status in ('active', 'inactive')";

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

    // lấy tổng số bản ghi trong thugfn rác
    public int getTotalRecordTrash() {
        int totalRecord = 0;
        String sql = "select count(*) from products where status in ('deleted')";

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


    // lấy danh mục theo phân trang
    public ArrayList<Product> getProductsPage(int currentPage, int limit) throws SQLException {
        ArrayList<Product> products = new ArrayList<>();
        String sql = "SELECT " +
                "  p.*, " +
                "  c.name AS categoryName, " +
                "  c.status as categoryStatus, " +
                "  s.name AS supplierName, " +
                "  s.status AS supplierStatus, " +
                "  u.username AS createdByUsername " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                "LEFT JOIN users u ON p.created_by = u.id " +
                "where p.status IN ('active', 'inactive') " +
                "ORDER BY p.created_at DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int offset = (currentPage - 1) * limit;
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String categoryStatus = "";
                if(Objects.equals(rs.getString("categoryStatus"), "deleted")){
                    categoryStatus = "[Đã xóa]";
                } else if (Objects.equals(rs.getString("categoryStatus"), "inactive")) {
                    categoryStatus = "[Tạm ngừng]";
                }
                String supplierStatus = "";
                if(Objects.equals(rs.getString("supplierStatus"), "deleted")){
                    supplierStatus = "[Đã xóa]";
                } else if (Objects.equals(rs.getString("supplierStatus"), "inactive")) {
                    supplierStatus = "[Tạm ngừng]";
                }
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getInt("supplier_id"),
                        rs.getBigDecimal("original_price"),
                        rs.getBigDecimal("selling_price"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("barcode"),
                        rs.getString("image"),
                        rs.getInt("minimum_quantity"),
                        rs.getBigDecimal("discount"),
                        rs.getString("use_by_date"),
                        rs.getString("categoryName") + categoryStatus,
                        rs.getString("supplierName") + supplierStatus,
                        rs.getString("createdByUsername")
                ));
            }
        }
        return products;
    }

    // laasy danh sách theo diều kiện bộ lọc
    public ArrayList<Product> getFilteredProducts(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate,String filterCategory, String filterSupplier, String filterRange, int currentPage, int limit) throws SQLException {
        ArrayList<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "  p.*, " +
                        "  c.name AS categoryName, " +
                        "  c.status as categoryStatus, " +
                        "  s.name AS supplierName, " +
                        "  s.status AS supplierStatus, " +
                        "  u.username AS createdByUsername " +
                        "FROM products p " +
                        "LEFT JOIN categories c ON p.category_id = c.id " +
                        "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                        "LEFT JOIN users u ON p.created_by = u.id "
        );

        // Danh sách để lưu các điều kiện WHERE
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Điều kiện lọc theo tên (searchText)
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("(p.name LIKE ? OR p.barcode LIKE ? )");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
        }

        // Điều kiện lọc theo trạng thái (filterStatus)
        if (filterStatus != null && !filterStatus.trim().isEmpty()) {
            conditions.add("p.status = ?");
            parameters.add(filterStatus);
        } else {
            conditions.add("(p.status = ? OR p.status = ?)");
            parameters.add("active");
            parameters.add("inactive");
        }

        // Điều kiện lọc theo người tạo (filterCreatedBy)
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("p.created_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Điều kiện lọc theo ngày tạo (fromDate)
        if (fromDate != null) {
            conditions.add("p.created_at >= ?");
            parameters.add(fromDate);
        }

        // Điều kiện lọc theo ngày tạo (toDate)
        if (toDate != null) {
            conditions.add("p.created_at <= ?");
            parameters.add(toDate);
        }

        // Điều kiện lọc theo danh mục(filterCategory)
        if (filterCategory != null && !filterCategory.trim().isEmpty()) {
            try {
                int categoryId = Integer.parseInt(filterCategory);
                conditions.add("p.category_id = ?");
                parameters.add(categoryId);
            } catch (NumberFormatException e) {
                System.out.println("Invalid category_id ID format: " + filterCategory);
            }
        }

        // Điều kiện lọc theo nhà cung cấp(filterSupplier)
        if (filterSupplier != null && !filterSupplier.trim().isEmpty()) {
            try {
                int supplierId = Integer.parseInt(filterSupplier);
                conditions.add("p.supplier_id = ?");
                parameters.add(supplierId);
            } catch (NumberFormatException e) {
                System.out.println("Invalid supplier_id ID format: " + filterSupplier);
            }
        }

        // Thêm các điều kiện WHERE nếu có
        if (!conditions.isEmpty()) {
            sql.append(" where ");
            sql.append(String.join(" and ", conditions));
        }

        // Sắp xếp
        if (Objects.equals(filterRange, "tên tăng dần")) {
            sql.append(" ORDER BY p.name ASC");
        } else if (Objects.equals(filterRange, "tên giảm dần")) {
            sql.append(" ORDER BY p.name DESC");
        } else if (Objects.equals(filterRange, "giá bán tăng dần")) {
            sql.append(" ORDER BY p.selling_price ASC");
        } else if (Objects.equals(filterRange, "giá bán giảm dần")) {
            sql.append(" ORDER BY p.selling_price DESC");
        } else if (Objects.equals(filterRange, "giảm giá tăng dần")) {
            sql.append(" ORDER BY p.discount ASC");
        } else if (Objects.equals(filterRange, "giảm giá giảm dần")) {
            sql.append(" ORDER BY p.discount DESC");
        } else if (Objects.equals(filterRange, "hạn sử dụng tăng dần")) {
            sql.append(" ORDER BY p.use_by_date ASC");
        } else if (Objects.equals(filterRange, "hạn sử dụng giảm dần")) {
            sql.append(" ORDER BY p.use_by_date DESC");
        }else if (Objects.equals(filterRange, "")){
            sql.append(" ORDER BY p.created_at DESC");
        }


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
                String categoryStatus = "";
                if(Objects.equals(rs.getString("categoryStatus"), "deleted")){
                    categoryStatus = "[Đã xóa]";
                } else if (Objects.equals(rs.getString("categoryStatus"), "inactive")) {
                    categoryStatus = "[Tạm ngừng]";
                }
                String supplierStatus = "";
                if(Objects.equals(rs.getString("supplierStatus"), "deleted")){
                    supplierStatus = "[Đã xóa]";
                } else if (Objects.equals(rs.getString("supplierStatus"), "inactive")) {
                    supplierStatus = "[Tạm ngừng]";
                }
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getInt("supplier_id"),
                        rs.getBigDecimal("original_price"),
                        rs.getBigDecimal("selling_price"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("barcode"),
                        rs.getString("image"),
                        rs.getInt("minimum_quantity"),
                        rs.getBigDecimal("discount"),
                        rs.getString("use_by_date"),
                        rs.getString("categoryName") + categoryStatus,
                        rs.getString("supplierName") + supplierStatus,
                        rs.getString("createdByUsername")
                ));
            }
        }
        return products;
    }

    public int getTotalFilterRecord(String searchText, String filterStatus, String filterCreatedBy,
                                    Timestamp fromDate, Timestamp toDate,
                                    String filterCategory, String filterSupplier) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "select count(*) as total from products p " +
                        "left join categories c on p.category_id = c.id " +
                        "left join suppliers s on p.supplier_id = s.id " +
                        "left join users u on p.created_by = u.id "
        );

        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // searchText: tên hoặc mã vạch
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("(p.name LIKE ? OR p.barcode LIKE ?)");
            parameters.add("%" + searchText.trim() + "%");
            parameters.add("%" + searchText.trim() + "%");
        }

        // trạng thái sản phẩm
        if (filterStatus != null && !filterStatus.trim().isEmpty()) {
            conditions.add("p.status = ?");
            parameters.add(filterStatus);
        } else {
            conditions.add("(p.status = ? OR p.status = ?)");
            parameters.add("active");
            parameters.add("inactive");
        }

        // người tạo
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int creatorId = Integer.parseInt(filterCreatedBy);
                conditions.add("p.created_by = ?");
                parameters.add(creatorId);
            } catch (NumberFormatException ignored) {}
        }

        // ngày tạo
        if (fromDate != null) {
            conditions.add("p.created_at >= ?");
            parameters.add(fromDate);
        }
        if (toDate != null) {
            conditions.add("p.created_at <= ?");
            parameters.add(toDate);
        }

        // danh mục
        if (filterCategory != null && !filterCategory.trim().isEmpty()) {
            try {
                int categoryId = Integer.parseInt(filterCategory);
                conditions.add("p.category_id = ?");
                parameters.add(categoryId);
            } catch (NumberFormatException ignored) {}
        }

        // nhà cung cấp
        if (filterSupplier != null && !filterSupplier.trim().isEmpty()) {
            try {
                int supplierId = Integer.parseInt(filterSupplier);
                conditions.add("p.supplier_id = ?");
                parameters.add(supplierId);
            } catch (NumberFormatException ignored) {}
        }

        // nối các điều kiện WHERE
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            for (Object param : parameters) {
                stmt.setObject(index++, param);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }


    public void updateMultiProduct(int idProduct, String statusRestore, int idUser) throws SQLException{
        String sql = "update products set " +
                "status = ?, " +
                "updated_at = NOW(), " +
                "updated_by = ? " +
                "where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statusRestore);
            stmt.setInt(2, idUser);
            stmt.setInt(3, idProduct);

            stmt.executeUpdate();
        }
    }


    public void addProduct(String name, int categoryId, int supplierId,
                           double originalPrice, double sellingPrice, int quantity,
                           String unit, String status, int createdBy, int updatedBy,
                           String barcode, String imageUrl, int minQuantity,
                           double discount, String useByDate) throws SQLException{
        String sql = "insert into products (name, category_id, supplier_id, original_price,selling_price,quantity, unit, status, created_at, updated_at, created_by, updated_by,barcode,image,minimum_quantity,discount,use_by_date) " +
                "values (?,?,?,?,?,?,?,?,NOW(),NOW(),?,?,?,?,?,?,?) ";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setInt(3, supplierId);
            stmt.setDouble(4, originalPrice);
            stmt.setDouble(5, sellingPrice);
            stmt.setInt(6, quantity);
            stmt.setString(7, unit);
            stmt.setString(8, status);
            stmt.setInt(9, createdBy);
            stmt.setInt(10, updatedBy);
            stmt.setString(11, barcode);
            stmt.setString(12, imageUrl);
            stmt.setInt(13, minQuantity);
            stmt.setDouble(14, discount);
            stmt.setString(15, useByDate);

            stmt.executeUpdate();
        }
    }

    public int checkDuplicate(String barcode, String useByDate) throws SQLException{
        String sql = "select count(*) from products where barcode = ? and use_by_date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, barcode);
            stmt.setString(2, useByDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // lấy giá trị COUNT(*)
                }
            }
        }
        return 0;
    }

    // kiểm tra trùng khi sửa sp
    public int checkDuplicateId(String barcode, Timestamp useByDate, int idProduct) throws SQLException{
        String sql = "select count(*) from products where barcode = ? and use_by_date = ? and id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, barcode);
            stmt.setTimestamp(2, useByDate);
            stmt.setInt(3, idProduct);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // lấy giá trị COUNT(*)
                }
            }
        }
        return 0;
    }

    public void updateProduct(int id, String name, int categoryId, int supplierId,
                              double originalPrice, double sellingPrice, int quantity,
                              String unit, String status, int updatedBy,
                              String barcode, String imageUrl, int minQuantity,
                              double discount, String useByDate) throws SQLException{
        String sql = "update products set name = ?, category_id = ?, supplier_id = ?, original_price = ?, " +
                "selling_price = ?, quantity = ?, unit = ?, status = ?, updated_at = NOW(), " +
                "updated_by = ?, barcode = ?, image = ?, minimum_quantity = ?, discount = ? , use_by_date = ? " +
                "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setInt(3, supplierId);
            stmt.setDouble(4, originalPrice);
            stmt.setDouble(5, sellingPrice);
            stmt.setInt(6, quantity);
            stmt.setString(7, unit);
            stmt.setString(8, status);
            stmt.setInt(9, updatedBy);
            stmt.setString(10, barcode);
            stmt.setString(11, imageUrl);
            stmt.setInt(12, minQuantity);
            stmt.setDouble(13, discount);
            stmt.setString(14, useByDate);
            stmt.setInt(15, id);

            stmt.executeUpdate();
        }

    }

    public Product findOneProduct(String barcode, String useByDate) throws SQLException{
        String sql = "select * from products where barcode = ? and use_by_date = ?";
        try(Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, barcode);
            stmt.setString(2, useByDate);

            try (ResultSet rs = stmt.executeQuery()) { // Lấy kết quả trả về
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("category_id"),
                            rs.getInt("supplier_id"),
                            rs.getBigDecimal("original_price"),
                            rs.getBigDecimal("selling_price"),
                            rs.getInt("quantity"),
                            rs.getString("unit"),
                            rs.getString("status"),
                            rs.getString("created_at"),
                            rs.getString("updated_at"),
                            rs.getInt("created_by"),
                            rs.getInt("updated_by"),
                            rs.getString("barcode"),
                            rs.getString("image"),
                            rs.getInt("minimum_quantity"),
                            rs.getBigDecimal("discount"),
                            rs.getString("use_by_date")
                    );
                }
            }
        }
        return null;
    }

    public Product getOneProduct(int id) throws SQLException{
        String sql = "SELECT " +
                "  p.*, " +
                "  c.name AS categoryName, " +
                "  c.status as categoryStatus, " +
                "  s.name AS supplierName, " +
                "  s.status AS supplierStatus, " +
                "  u.username AS createdByUsername " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                "LEFT JOIN users u ON p.created_by = u.id " +
                "where p.id = ?";
        try(Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) { // Lấy kết quả trả về
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("category_id"),
                            rs.getInt("supplier_id"),
                            rs.getBigDecimal("original_price"),
                            rs.getBigDecimal("selling_price"),
                            rs.getInt("quantity"),
                            rs.getString("unit"),
                            rs.getString("status"),
                            rs.getString("created_at"),
                            rs.getString("updated_at"),
                            rs.getInt("created_by"),
                            rs.getInt("updated_by"),
                            rs.getString("barcode"),
                            rs.getString("image"),
                            rs.getInt("minimum_quantity"),
                            rs.getBigDecimal("discount"),
                            rs.getString("use_by_date"),
                            rs.getString("categoryName"),
                            rs.getString("supplierName"),
                            rs.getString("createdByUsername")
                    );
                }
            }
        }
        return null;
    }

    public void updateOneProduct(int id, String name, int categoryId, int supplierId,
                              double originalPrice, double sellingPrice, int quantity,
                              String unit, String status, int updatedBy, String imageUrl,
                                 int minQuantity, double discount, String useByDate) throws SQLException{
        String sql = "update products set name = ?, category_id = ?, supplier_id = ?, original_price = ?, " +
                "selling_price = ?, quantity = ?, unit = ?, status = ?, updated_at = NOW(), " +
                "updated_by = ?, image = ?, minimum_quantity = ?, discount = ? , use_by_date = ? " +
                "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setInt(3, supplierId);
            stmt.setDouble(4, originalPrice);
            stmt.setDouble(5, sellingPrice);
            stmt.setInt(6, quantity);
            stmt.setString(7, unit);
            stmt.setString(8, status);
            stmt.setInt(9, updatedBy);
            stmt.setString(10, imageUrl);
            stmt.setInt(11, minQuantity);
            stmt.setDouble(12, discount);
            stmt.setString(13, useByDate);
            stmt.setInt(14, id);

            stmt.executeUpdate();
        }

    }

    public void deleteOneProduct(int id, int userId) throws SQLException{
        String sql = "update products set status = ?, updated_by = ?, updated_at = NOW() where id = ?";
        try(Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1,"deleted");
            stmt.setInt(2,userId);
            stmt.setInt(3,id);

            stmt.executeUpdate();
        }
    }


    public ArrayList<Product> getProductPageTrash(int currentPage, int limit) throws SQLException{
        ArrayList<Product> products = new ArrayList<>();
        String sql = "SELECT " +
                "  p.*, " +
                "  c.name AS categoryName, " +
                "  c.status as categoryStatus, " +
                "  s.name AS supplierName, " +
                "  s.status AS supplierStatus, " +
                "  u.username AS updatedByUsername " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                "LEFT JOIN users u ON p.updated_by = u.id " +
                "where p.status IN ('deleted') " +
                "ORDER BY p.updated_at DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int offset = (currentPage - 1) * limit;
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String categoryStatus = "";
                if(Objects.equals(rs.getString("categoryStatus"), "deleted")){
                    categoryStatus = "[Đã xóa]";
                } else if (Objects.equals(rs.getString("categoryStatus"), "inactive")) {
                    categoryStatus = "[Tạm ngừng]";
                }
                String supplierStatus = "";
                if(Objects.equals(rs.getString("supplierStatus"), "deleted")){
                    supplierStatus = "[Đã xóa]";
                } else if (Objects.equals(rs.getString("supplierStatus"), "inactive")) {
                    supplierStatus = "[Tạm ngừng]";
                }
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getInt("supplier_id"),
                        rs.getBigDecimal("original_price"),
                        rs.getBigDecimal("selling_price"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("barcode"),
                        rs.getString("image"),
                        rs.getInt("minimum_quantity"),
                        rs.getBigDecimal("discount"),
                        rs.getString("use_by_date"),
                        rs.getString("categoryName") + categoryStatus,
                        rs.getString("supplierName") + supplierStatus,
                        rs.getString("updatedByUsername")
                ));
            }
        }
        return products;
    }

    // laasy danh sách theo diều kiện bộ lọc
    public ArrayList<Product> getFilteredProductsTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate,String filterCategory, String filterSupplier, String filterArrange, int currentPage, int limit) throws SQLException {
        ArrayList<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "  p.*, " +
                        "  c.name AS categoryName, " +
                        "  c.status as categoryStatus, " +
                        "  s.name AS supplierName, " +
                        "  s.status AS supplierStatus, " +
                        "  u.name AS updatedByUsername " +
                        "FROM products p " +
                        "LEFT JOIN categories c ON p.category_id = c.id " +
                        "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                        "LEFT JOIN users u ON p.updated_by = u.id "
        );

        // Danh sách để lưu các điều kiện WHERE
        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // Điều kiện lọc theo tên (searchText)
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("(p.name LIKE ? OR p.barcode LIKE ? )");
            parameters.add("%" + searchText + "%");
            parameters.add("%" + searchText + "%");
        }

        conditions.add(" p.status = ? ");
        parameters.add("deleted");

        // Điều kiện lọc theo người tạo (filterCreatedBy)
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int createdById = Integer.parseInt(filterCreatedBy);
                conditions.add("p.updated_by = ?");
                parameters.add(createdById);
            } catch (NumberFormatException e) {
                System.out.println("Invalid created_by ID format: " + filterCreatedBy);
            }
        }

        // Điều kiện lọc theo ngày tạo (fromDate)
        if (fromDate != null) {
            conditions.add("p.updated_at >= ?");
            parameters.add(fromDate);
        }

        // Điều kiện lọc theo ngày tạo (toDate)
        if (toDate != null) {
            conditions.add("p.updated_at <= ?");
            parameters.add(toDate);
        }

        // Điều kiện lọc theo danh mục(filterCategory)
        if (filterCategory != null && !filterCategory.trim().isEmpty()) {
            try {
                int categoryId = Integer.parseInt(filterCategory);
                conditions.add("p.category_id = ?");
                parameters.add(categoryId);
            } catch (NumberFormatException e) {
                System.out.println("Invalid category_id ID format: " + filterCategory);
            }
        }

        // Điều kiện lọc theo nhà cung cấp(filterSupplier)
        if (filterSupplier != null && !filterSupplier.trim().isEmpty()) {
            try {
                int supplierId = Integer.parseInt(filterSupplier);
                conditions.add("p.supplier_id = ?");
                parameters.add(supplierId);
            } catch (NumberFormatException e) {
                System.out.println("Invalid supplier_id ID format: " + filterSupplier);
            }
        }

        // Thêm các điều kiện WHERE nếu có
        if (!conditions.isEmpty()) {
            sql.append(" where ");
            sql.append(String.join(" and ", conditions));
        }

        // Sắp xếp
        if (Objects.equals(filterArrange, "tên tăng dần")) {
            sql.append(" ORDER BY p.name ASC");
        } else if (Objects.equals(filterArrange, "tên giảm dần")) {
            sql.append(" ORDER BY p.name DESC");
        } else if (Objects.equals(filterArrange, "giá bán tăng dần")) {
            sql.append(" ORDER BY p.selling_price ASC");
        } else if (Objects.equals(filterArrange, "giá bán giảm dần")) {
            sql.append(" ORDER BY p.selling_price DESC");
        } else if (Objects.equals(filterArrange, "giảm giá tăng dần")) {
            sql.append(" ORDER BY p.discount ASC");
        } else if (Objects.equals(filterArrange, "giảm giá giảm dần")) {
            sql.append(" ORDER BY p.discount DESC");
        } else if (Objects.equals(filterArrange, "hạn sử dụng tăng dần")) {
            sql.append(" ORDER BY p.use_by_date ASC");
        } else if (Objects.equals(filterArrange, "hạn sử dụng giảm dần")) {
            sql.append(" ORDER BY p.use_by_date DESC");
        }else if (Objects.equals(filterArrange, "")){
            sql.append(" ORDER BY p.created_at DESC");
        }


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
                String categoryStatus = "";
                if(Objects.equals(rs.getString("categoryStatus"), "deleted")){
                    categoryStatus = "[Đã xóa]";
                } else if (Objects.equals(rs.getString("categoryStatus"), "inactive")) {
                    categoryStatus = "[Tạm ngừng]";
                }
                String supplierStatus = "";
                if(Objects.equals(rs.getString("supplierStatus"), "deleted")){
                    supplierStatus = "[Đã xóa]";
                } else if (Objects.equals(rs.getString("supplierStatus"), "inactive")) {
                    supplierStatus = "[Tạm ngừng]";
                }
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getInt("supplier_id"),
                        rs.getBigDecimal("original_price"),
                        rs.getBigDecimal("selling_price"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("updated_at"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getString("barcode"),
                        rs.getString("image"),
                        rs.getInt("minimum_quantity"),
                        rs.getBigDecimal("discount"),
                        rs.getString("use_by_date"),
                        rs.getString("categoryName") + categoryStatus,
                        rs.getString("supplierName") + supplierStatus,
                        rs.getString("updatedByUsername")
                ));
            }
        }
        return products;
    }

    public int getTotalFilterRecordTrash(String searchText, String filterCreatedBy,
                                    Timestamp fromDate, Timestamp toDate,
                                    String filterCategory, String filterSupplier) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "select count(*) as total from products p " +
                        "left join categories c on p.category_id = c.id " +
                        "left join suppliers s on p.supplier_id = s.id " +
                        "left join users u on p.updated_by = u.id "
        );

        ArrayList<String> conditions = new ArrayList<>();
        ArrayList<Object> parameters = new ArrayList<>();

        // searchText: tên hoặc mã vạch
        if (searchText != null && !searchText.trim().isEmpty()) {
            conditions.add("(p.name LIKE ? OR p.barcode LIKE ?)");
            parameters.add("%" + searchText.trim() + "%");
            parameters.add("%" + searchText.trim() + "%");
        }

        conditions.add(" p.status = ? ");
        parameters.add("deleted");

        // người xóa
        if (filterCreatedBy != null && !filterCreatedBy.trim().isEmpty()) {
            try {
                int creatorId = Integer.parseInt(filterCreatedBy);
                conditions.add("p.updated_by = ?");
                parameters.add(creatorId);
            } catch (NumberFormatException ignored) {}
        }

        // ngày tạo
        if (fromDate != null) {
            conditions.add("p.updated_at >= ?");
            parameters.add(fromDate);
        }
        if (toDate != null) {
            conditions.add("p.updated_at <= ?");
            parameters.add(toDate);
        }

        // danh mục
        if (filterCategory != null && !filterCategory.trim().isEmpty()) {
            try {
                int categoryId = Integer.parseInt(filterCategory);
                conditions.add("p.category_id = ?");
                parameters.add(categoryId);
            } catch (NumberFormatException ignored) {}
        }

        // nhà cung cấp
        if (filterSupplier != null && !filterSupplier.trim().isEmpty()) {
            try {
                int supplierId = Integer.parseInt(filterSupplier);
                conditions.add("p.supplier_id = ?");
                parameters.add(supplierId);
            } catch (NumberFormatException ignored) {}
        }

        // nối các điều kiện WHERE
        if (!conditions.isEmpty()) {
            sql.append(" where ").append(String.join(" and ", conditions));
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            for (Object param : parameters) {
                stmt.setObject(index++, param);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    // xóa vĩnh viễn sản phẩm
    public void deletePermanentlyProduct(int id) throws SQLException{
        String sql = "delete from products where id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();
        }
    }
}
