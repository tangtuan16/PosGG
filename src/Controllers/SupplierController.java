package Controllers;


import Models.Supplier;
import Services.SupplierService;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class SupplierController {
    private SupplierService supplierService;


    public SupplierController(){
        supplierService = new SupplierService();
    }

    // SupplierFrame
    public int getTotalRecord(){
        return supplierService.getTotalRecord();
    }

    public ArrayList<Supplier> findSupplierPage(int currentPage, int limit) throws SQLException{
        try{
            return supplierService.getSuppliersPage(currentPage,limit);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2253136: Lỗi! Tải dữ liệu thất bại!");
        }
    }

    public ArrayList<Supplier> findAllSupplier() throws SQLException{
        try{
            return supplierService.getAllSuppliers();
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2116166: Lỗi! Tải dữ liệu thất bại!");
        }
    }

    public ArrayList<Supplier> filteredSuppliers(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate, int currentPage, int  limit) throws SQLException{
        try{
            return supplierService.getFilteredSuppliers(searchText,filterStatus,filterCreatedBy, fromDate, toDate, currentPage,limit);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2314136: Lỗi! Tải dữ liệu thất bại!");
        }
    }

    public int getTotalFilterRecord(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate){
        return supplierService.getTotalFilterRecord(searchText,filterStatus,filterCreatedBy, fromDate, toDate);
    }

    public void updateMultiSupplier(int supplierId, String statusRestore, int idUser) throws SQLException{
        try{
            supplierService.updateMultiSupplier(supplierId, statusRestore,idUser);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2220136: Lỗi! Cập nhật thất bại.");
        }
    }



    // addSupplierFrame
    public void addSupplier(String name, String phone, String email, String address, String status, int userId) throws SQLException{
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống!");
        }
        if ((phone == null || phone.trim().isEmpty()) && (email == null || email.trim().isEmpty())) {
            throw new IllegalArgumentException("Phải nhập ít nhất 1 thông tin liên hệ SĐT hoặc email!");
        }
        if (phone != null && !phone.trim().isEmpty() && !phone.matches("[0-9 ]+")) {
            throw new IllegalArgumentException("Số điện thoại chỉ được chứa số. Vui lòng nhập lại!");
        }
        if(status == null && status.trim().isEmpty()){
            throw new IllegalArgumentException("Chưa chọn trạng thái cung cấp!");
        }

        // Kiểm tra email đúng định dạng
        if (email != null && !email.trim().isEmpty()) {
            String emailRegex = "^[A-Za-z0-9+_.-]{1,}@[A-Za-z0-9.-]{1,}\\.[A-Za-z]{2,}$";
            if (!email.matches(emailRegex)) {
                throw new IllegalArgumentException("Email không đúng định dạng. Vui lòng nhập lại!");
            }
        }

        // kiểm tra email hoặc phone đã tồn tại chưa
        if(supplierService.checkPhoneOrEmail(phone, email) > 0){
            throw new SQLException("Email hoặc phone đã tồn tại. Vui lòng kiểm tra lại!");
        }

        try{
            supplierService.addSupplier(name, phone, email, address, status, userId, userId);
        }catch (SQLException ex){
            throw  new SQLException("Error1414146: Lỗi! Thêm nhà cung cấp thất bại!");
        }
    }

    //editSupplierFrame
    public Supplier getOneSupplier(int id) throws SQLException{
        try{
            return supplierService.getOneSupplier(id);
        }catch (SQLException ex){
            throw new SQLException("Error2318136: Lỗi nhà cung cấp không còn tồn tại!");
        }
    }

    public void editSupplier(int id ,String name, String phone, String email, String address, String status, int userId) throws SQLException{
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống!");
        }
        if ((phone == null || phone.trim().isEmpty()) && (email == null || email.trim().isEmpty())) {
            throw new IllegalArgumentException("Phải nhập ít nhất 1 thông tin liên hệ SĐT hoặc email!");
        }
        if (phone != null && !phone.trim().isEmpty() && !phone.matches("[0-9 ]+")) {
            throw new IllegalArgumentException("Số điện thoại chỉ được chứa số và khoảng trắng. Vui lòng nhập lại!");
        }
        if(status == null && status.trim().isEmpty()){
            throw new IllegalArgumentException("Chưa chọn trạng thái cung cấp!");
        }

        // Kiểm tra email đúng định dạng
        if (email != null && !email.trim().isEmpty()) {
            String emailRegex = "^[A-Za-z0-9+_.-]{1,}@[A-Za-z0-9.-]{1,}\\.[A-Za-z]{2,}$";
            if (!email.matches(emailRegex)) {
                throw new IllegalArgumentException("Email không đúng định dạng. Vui lòng nhập lại!");
            }
        }

        // kiểm tra email hoặc phone đã tồn tại chưa
        if(supplierService.checkPhoneOrEmailID(id ,phone, email) > 1){
            throw new SQLException("Email hoặc phone đã tồn tại. Vui lòng kiểm tra lại!");
        }

        try{
            supplierService.editSupplier(id, name, phone, email, address, status, userId);
        }catch (SQLException ex){
            throw  new SQLException("Error1415146: Lỗi! Cập nhật nhà cung cấp thất bại!");
        }
    }


    // delete supplier
    public void deleteSupplier(int id, int userId) throws SQLException{
        try{
            supplierService.deleteSupplier(id, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error2319136: Lỗi! Xóa thất bại.");
        }
    }


    // trang thùng rác
    public ArrayList<Supplier> findAllSupplierTrash(int currentPage, int limit) throws SQLException{
        try{
            return supplierService.getAllSupplierTrash(currentPage,limit);
        }catch (SQLException e){
            throw new SQLException("Error2320136: Lỗi khi tải dữ liệu thùng rác nhà cung cấp!");
        }
    }

    public int getTotalRecordTrash() throws SQLException{
        try {
            return supplierService.getTotalRecordTrash();
        }catch (SQLException e){
            throw new SQLException("Error2321136: Lỗi tải dữ liệu tổng số trang!");
        }
    }

    public ArrayList<Supplier> filteredSuppliersPageTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate, int currentPage, int limit) throws SQLException{
        try{
            return supplierService.getFilteredSuppliersPageTrash(searchText,filterCreatedBy, fromDate, toDate, currentPage, limit);
        }catch (SQLException e){
            throw new SQLException("Error1820136: Lỗi tải dữ liệu khi tìm kiếm!");
        }
    }

    public int getTotalRecordFilterTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) throws SQLException{
        try{
            return supplierService.getTotalRecordFilterTrash(searchText,filterCreatedBy, fromDate, toDate);
        } catch (SQLException e){
            throw new SQLException("Error1839136: Lỗi lấy tổng số trang khi lọc!");
        }
    }

    public void deletePermanentlySupplier(int idSupplier) throws SQLException{
        try{
            supplierService.deletePermanentlySupplier(idSupplier);
        } catch (SQLException e){
            throw new SQLException("Error2226136: Lỗi! Xóa vĩnh viễn thất bại!");
        }
    }


    // lấy cacs nhà cung cấp status = active
    public ArrayList<Supplier> getSupplierActive() throws SQLException{
        try{
            return supplierService.getSupplierActive();
        } catch (SQLException e){
            e.printStackTrace();
            throw new SQLException("Error0741196: Lỗi! Lấy danh mục trong combobox thất bại!");
        }
    }

    // lấy cacs nhà cung cấp status = active, inactive
    public ArrayList<Supplier> getSupplierActiveInactive() throws SQLException{
        try{
            return supplierService.getAllSuppliers();
        } catch (SQLException e){
            e.printStackTrace();
            throw new SQLException("Error0205226: Lỗi! Lấy danh mục trong combobox thất bại!");
        }
    }
}
