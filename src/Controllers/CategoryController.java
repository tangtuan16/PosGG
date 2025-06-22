package Controllers;

import Models.Category;
import Services.CategoryService;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class CategoryController {
    private CategoryService categoryService;


    public CategoryController(){
        categoryService = new CategoryService();
    }

    public int getTotalRecord(){
        return categoryService.getTotalRecord();
    }

    public int getTotalFilterRecord(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) throws SQLException{
        try{
            return categoryService.getTotalFilterRecord(searchText,filterStatus,filterCreatedBy, fromDate, toDate);
        }catch (SQLException ex){
            throw new SQLException("Error1250146: Lỗi! Lấy tổng số trang thất bại!");
        }
    }

    public int getTotalRecordTrash(){
        return categoryService.getTotalRecordTrash();
    }

    public void deletePermanentlyCategory(int idCategory) throws SQLException{
        try{
            categoryService.deletePermanentlyCategory(idCategory);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2345136: Lỗi! Xóa vĩnh viễn thất bại!");
        }

    }

    public void updateMultiCategory(int idCategory, String statusRestore, int idUser) throws  SQLException{
        try{
            categoryService.updateMultiCategory(idCategory, statusRestore,idUser);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error2340136: Lỗi! Cập nhật danh mục hàng loạt thất bại!");
        }
    }

    public void deleteCategory(int idCategory, int idUser) throws SQLException{
        try{
            categoryService.deleteCategory(idCategory, idUser);
        } catch (SQLException ex){
            throw new SQLException("Error2333136: Lỗi! Xóa danh mục thất bại!");
        }
    }

    public ArrayList<Category> findCategoryPage(int currentPage, int limit) throws  SQLException{
        try{
            return categoryService.getCategoriesPage(currentPage,limit);
        }catch (SQLException ex){
            ex.printStackTrace();
            throw new SQLException("Error1254146: Lỗi! Lấy danh mục theo trang thất bại!");
        }
    }

    public ArrayList<Category> findAllCategory() throws SQLException{
        try{
            return categoryService.getAllCategories();
        }catch (SQLException ex){
            throw new SQLException("Error1300146: Lỗi! Lấy danh mục thất bại!");
        }
    }

    public ArrayList<Category> findAllCategoryTrash(int currentPage, int limit) throws SQLException{
        try{
            return categoryService.getAllCategoriesTrash(currentPage,limit);
        }catch (SQLException ex){
            throw new SQLException("Error1304146: Lỗi! Lấy danh mục trong thùng rác thất bại");
        }
    }

    public ArrayList<Category> filteredCategoriesPageTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate, int currentPage, int limit) throws SQLException{
        try{
            return categoryService.getFilteredCategoriesPageTrash(searchText,filterCreatedBy, fromDate, toDate, currentPage, limit);
        }catch (SQLException ex){
            throw new SQLException("Error1306146: Lỗi! Lấy danh mục theo bộ lọc trong thùng rác thất bại!");
        }
    }

    public int getTotalRecordFilterTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate){
        return categoryService.getTotalRecordFilterTrash(searchText,filterCreatedBy, fromDate, toDate);
    }

    public ArrayList<Category> filteredCategories(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate, int currentPage, int  limit) throws SQLException {
        try{
            return categoryService.getFilteredCategories(searchText,filterStatus,filterCreatedBy, fromDate, toDate, currentPage,limit);
        }catch (SQLException e){
            throw new SQLException("Error2328136: Lỗi lấy dữ liệu thất bại!");
        }
    }

    public void createNewCategory(String name, String desc, String status, int idParent, int idUser) throws SQLException{
        if(name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }

        if(status == null || status.trim().isEmpty()){
            throw new IllegalArgumentException("Trạng thái danh mục không được để trống");
        }

        //kiểm tra tên danh mục đã tồn tại chưa
        if(categoryService.checkNameExist(name) > 0){
            throw new SQLException("Tên danh mục đã tồn tại! Vui lòng nhập tên khác!");
        }

        try{
            categoryService.createCategory(name,desc,status,idParent,idUser,idUser);
        }catch (SQLException ex){
            throw  new SQLException("Error1309146: Lỗi! Thêm danh mục mới thất bại!");
        }
    }

    public Category getCategory(int idCategory){
        return categoryService.getCategory(idCategory);
    }

    public void updateCategory(int idCategory, String nameCategory, String descriptionCategory, String statusCategory,int idParent, int userId) throws SQLException{
        if(nameCategory == null || nameCategory.trim().isEmpty()){
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }

        if(statusCategory == null || statusCategory.trim().isEmpty()){
            throw new IllegalArgumentException("Trạng thái danh mục không được để trống");
        }

        //kiểm tra tên danh mục đã tồn tại chưa
        if(categoryService.checkNameExistId(idCategory ,nameCategory) > 0){
            throw new SQLException("Tên danh mục đã tồn tại! Vui lòng nhập tên khác!");
        }


        try{
            categoryService.updateCategory(idCategory,nameCategory,descriptionCategory,statusCategory,idParent,userId);
        }catch (SQLException ex){
            throw new SQLException("Error1323146: Lỗi! Cập nhật danh mục thất bại!");
        }
    }
}
