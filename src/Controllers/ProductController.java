package Controllers;

import Models.Product;
import Services.ProductService;
import Utils.CloudinaryConfig;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ProductController {
    private ProductService productService;


    public ProductController(){
        productService = new ProductService();
    }

    public int getTotalRecord(){
        return productService.getTotalRecord();
    }

    public int getTotalRecordTrash(){
        return productService.getTotalRecordTrash();
    }

    public ArrayList<Product> findProductPage(int currentPage, int limit) throws SQLException {
        try{
            return productService.getProductsPage(currentPage,limit);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error1632166: Lỗi! Tải sản phẩm thất bại!");
        }
    }

    public ArrayList<Product> filteredProducts(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate,String filterCategory, String filterSupplier, String filterRange, int currentPage, int  limit) throws SQLException{
        try{
            return productService.getFilteredProducts(searchText,filterStatus,filterCreatedBy, fromDate, toDate, filterCategory, filterSupplier, filterRange, currentPage,limit);
        }catch (SQLException ex){
            throw new SQLException("Error1939166: Lỗi! Tải dữ liệu sản phẩm thất bại!");
        }
    }

    public int getTotalFilterRecord(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate,String filterCategory, String filterSupplier) throws SQLException{
        try{
            return productService.getTotalFilterRecord(searchText,filterStatus,filterCreatedBy, fromDate, toDate, filterCategory, filterSupplier);
        } catch (SQLException e) {
            throw new SQLException("Error2013166: Lỗi! Tải số trang thất bại!");
        }
    }


    public void updateMultiProduct(int idProduct, String statusRestore, int idUser) throws  SQLException{
        try{
            productService.updateMultiProduct(idProduct, statusRestore,idUser);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2234166: Lỗi! Cập nhật sản phẩm hàng loạt thất bại!");
        }
    }

    public void addProduct(String name, String categoryId, String supplierId,
                           String originalPrice, String sellingPrice, String quantity,
                           String unit, String status, int createdBy, int updatedBy,
                           String barcode, File selectedFile, String minQuantity,
                           String discount, String useByDateStr) throws SQLException{
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }

        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục.");
        }

        if (supplierId == null || supplierId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp.");
        }

        if (!isNumericOnly(originalPrice)) {
            throw new IllegalArgumentException("Giá gốc chỉ được chứa các chữ số.");
        }

        if (!isNumericOnly(sellingPrice)) {
            throw new IllegalArgumentException("Giá bán chỉ được chứa các chữ số.");
        }

        double originalP = Double.parseDouble(originalPrice);
        double sellingP = Double.parseDouble(sellingPrice);

        if (sellingP <= originalP) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn giá gốc.");
        }

        if (!quantity.matches("\\d+")) {
            throw new IllegalArgumentException("Số lượng phải là số nguyên.");
        }

        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đơn vị.");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn trạng thái.");
        }

        if (barcode == null || barcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã vạch không được để trống.");
        }

        if (!minQuantity.matches("\\d+")) {
            throw new IllegalArgumentException("Số lượng tối thiểu phải là số nguyên.");
        }

        if (!discount.trim().isEmpty()) {
            if (!isNumeric(discount)) {
                throw new IllegalArgumentException("Giảm giá không hợp lệ.");
            }

            double discountValue = Double.parseDouble(discount);
            if (discountValue < 0 || discountValue > 50) {
                throw new IllegalArgumentException("Giảm giá phải từ 0 đến 50%.");
            }
        }

        if (useByDateStr != null && !useByDateStr.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setLenient(false);
                Date selectedDate = sdf.parse(useByDateStr);

                // So sánh với ngày hiện tại (bỏ giờ)
                Date today = new Date();
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date currentDate = sdf.parse(sdf.format(today)); // Reset về 00:00

                if (selectedDate.before(currentDate)) {
                    throw new IllegalArgumentException("Hạn sử dụng phải lớn hơn hoặc bằng ngày hiện tại.");
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("Định dạng hạn sử dụng không hợp lệ.");
            }
        }



        try{
            String imageUrl="";
            if(selectedFile != null){
                imageUrl = uploadToCloudinary(selectedFile);
            }
            int qty = Integer.parseInt(quantity);
            int minQty = Integer.parseInt(minQuantity);
            double discountValue = discount.trim().isEmpty() ? 0 : Double.parseDouble(discount);


            if(productService.checkDuplicate(barcode, useByDateStr) >= 1){
//                phát hiện có sp đã trùng thì hỏi xem họ quyết định thêm mới hay gộp số lượng
                String[] options = {"Cập nhật", "Thêm mới", "Hủy"};
                int choice = JOptionPane.showOptionDialog(
                        null,
                        "Sản phẩm với mã vạch và hạn sử dụng này đã tồn tại.\n"
                                + "Bạn có muốn cập nhật số lượng sản phẩm hiện có không?",
                        "Xác nhận gộp sản phẩm",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0] // default button = "Cập nhật"
                );
                Product product = productService.findOneProduct(barcode, useByDateStr);
                if (choice == 0) {
                    productService.updateProduct(product.getId(),
                            name, Integer.parseInt(categoryId), Integer.parseInt(supplierId),
                            originalP, sellingP, qty + product.getQuantity(), unit, status,createdBy,
                            barcode, imageUrl, minQty, discountValue, useByDateStr
                    );
                    JOptionPane.showMessageDialog(null,"Cập nhật thành công!");
                } else if (choice == 1) {
                    productService.addProduct(
                            name, Integer.parseInt(categoryId), Integer.parseInt(supplierId),
                            originalP, sellingP, qty, unit, status,createdBy, updatedBy,
                            barcode, imageUrl, minQty, discountValue, useByDateStr
                    );
                    JOptionPane.showMessageDialog(null,"Thêm mới thành công!");
                } else {
                    return;
                }
            }else{
                productService.addProduct(
                        name, Integer.parseInt(categoryId), Integer.parseInt(supplierId),
                        originalP, sellingP, qty, unit, status,
                        createdBy, updatedBy,
                        barcode, imageUrl, minQty, discountValue, useByDateStr
                );
                JOptionPane.showMessageDialog(null,"Thêm mới thành công!");
            }
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2205206: Lỗi! Thêm sản phẩm thất bại!");
        }
    }

    private boolean isNumericOnly(String str) {
        return str.matches("\\d+(\\.\\d+)?");  // Cho phép cả số thập phân như "123.45"
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String uploadToCloudinary(File selectedFile) {
        String imageUrl = "";
        if(selectedFile!= null){
            try {
                Cloudinary cloudinary = CloudinaryConfig.getCloudinary();
                Map uploadResult = cloudinary.uploader().upload(selectedFile, ObjectUtils.emptyMap());
                imageUrl = uploadResult.get("secure_url").toString();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return imageUrl;
    }

    public Product findOneProduct(int id) throws SQLException{
        try{
            return productService.getOneProduct(id);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Lỗi hiển thị sản phẩm sửa");
        }
    }

    public void editProduct(int id, String name, String categoryId, String supplierId,
                            String originalPrice, String sellingPrice, String quantity,
                            String unit, String status, int updatedBy,String imageUrlOld, File selectedFile, String minQuantity,
                            String discount, String useByDateStr) throws SQLException{
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }

        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục.");
        }

        if (supplierId == null || supplierId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp.");
        }

        if (!isNumericOnly(originalPrice)) {
            throw new IllegalArgumentException("Giá gốc chỉ được chứa các chữ số.");
        }

        if (!isNumericOnly(sellingPrice)) {
            throw new IllegalArgumentException("Giá bán chỉ được chứa các chữ số.");
        }

        double originalP = Double.parseDouble(originalPrice);
        double sellingP = Double.parseDouble(sellingPrice);

        if (sellingP <= originalP) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn giá gốc.");
        }

        if (!quantity.matches("\\d+")) {
            throw new IllegalArgumentException("Số lượng phải là số nguyên.");
        }

        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đơn vị.");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn trạng thái.");
        }

        if (!minQuantity.matches("\\d+")) {
            throw new IllegalArgumentException("Số lượng tối thiểu phải là số nguyên.");
        }

        if (!discount.trim().isEmpty()) {
            if (!isNumeric(discount)) {
                throw new IllegalArgumentException("Giảm giá không hợp lệ.");
            }

            double discountValue = Double.parseDouble(discount);
            if (discountValue < 0 || discountValue > 50) {
                throw new IllegalArgumentException("Giảm giá phải từ 0 đến 50%.");
            }
        }

        if (useByDateStr != null && !useByDateStr.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setLenient(false);
                Date selectedDate = sdf.parse(useByDateStr);

                // So sánh với ngày hiện tại (bỏ giờ)
                Date today = new Date();
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date currentDate = sdf.parse(sdf.format(today)); // Reset về 00:00

                if (selectedDate.before(currentDate)) {
                    throw new IllegalArgumentException("Hạn sử dụng phải lớn hơn hoặc bằng ngày hiện tại.");
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("Định dạng hạn sử dụng không hợp lệ.");
            }
        }


        try{
            String imageUrl="";
            if(selectedFile != null){
                imageUrl = uploadToCloudinary(selectedFile);
            }else{
                imageUrl = imageUrlOld;
            }
            int qty = Integer.parseInt(quantity);
            int minQty = Integer.parseInt(minQuantity);
            double discountValue = discount.trim().isEmpty() ? 0 : Double.parseDouble(discount);


            productService.updateOneProduct(
                    id, name, Integer.parseInt(categoryId), Integer.parseInt(supplierId),
                    originalP, sellingP, qty, unit, status, updatedBy,
                    imageUrl, minQty, discountValue, useByDateStr
            );
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2205206: Lỗi! Thêm sản phẩm thất bại!");
        }


    }

    public void deleteProduct(int id, int userId) throws SQLException{
        try{
            productService.deleteOneProduct(id, userId);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error1323226: Lỗi! Xóa sản phẩm thất bại");
        }
    }


    // trang thùng rác
    public ArrayList<Product> findProductPageTrash(int currentPage, int limit) throws SQLException{
        try{
            return productService.getProductPageTrash(currentPage,limit);
        }catch (SQLException e){
            throw new SQLException("Error2320136: Lỗi khi tải dữ liệu thùng rác sản phẩm!");
        }
    }

    public ArrayList<Product> filteredProductsTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate,String filterCategory, String filterSupplier, String filterArrange, int currentPage, int  limit) throws SQLException{
        try{
            return productService.getFilteredProductsTrash(searchText,filterCreatedBy, fromDate, toDate, filterCategory, filterSupplier, filterArrange, currentPage,limit);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2005226: Lỗi! Tải dữ liệu sản phẩm trong thùng rác thất bại!");
        }
    }

    public int getTotalFilterRecordTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate,String filterCategory, String filterSupplier) throws SQLException{
        try{
            return productService.getTotalFilterRecordTrash(searchText,filterCreatedBy, fromDate, toDate, filterCategory, filterSupplier);
        } catch (SQLException e) {
            throw new SQLException("Error2013226: Lỗi! Tải số trang trong thùng rác sản phẩm thất bại!");
        }
    }

    public void deletePermanentlyProduct(int idProduct) throws SQLException{
        try{
            productService.deletePermanentlyProduct(idProduct);
        } catch (SQLException e){
            throw new SQLException("Error2110226: Lỗi! Xóa vĩnh viễn sản phẩm thất bại!");
        }
    }


}
