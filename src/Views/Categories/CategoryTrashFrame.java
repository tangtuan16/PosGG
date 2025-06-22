package Views.Categories;

import Controllers.CategoryController;
import Controllers.UserController;
import Models.Category;
import Models.Session;
import Models.User;
import Utils.ComboItem;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CategoryTrashFrame extends JFrame {
    private JTable table;
    public int userId = Session.getInstance().getUser().getId();
    private DefaultTableModel categoryTrashModel;
    private JTextField searchField;
    private JComboBox<ComboItem> statusChangeMultiComboBox, createdByComboBox;
    private JDateChooser fromDateChooser, toDateChooser;
    private JButton restoreButton, checkAllButton, deletePermanentlyButton;
    private JButton preButton, nextButton, firstPageButton, lastPageButton;
    private JButton searchButton;
    private JButton clearFilterButton;
    private boolean isAllChecked = false; // Biến để theo dõi trạng thái
    private CategoryController categoryController;
    private UserController userController;
    private int idCategorySelect;
    private CategoryFrame categoryFrame;
    private int currentPage = 1;
    private int limit = 20;
    int totalPage;
    private JLabel page = new JLabel();


    public CategoryTrashFrame(CategoryFrame categoryFrame) {
        this.categoryFrame = categoryFrame;
        categoryController = new CategoryController();
        userController = new UserController();
        setTitle("Thùng rác danh mục");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Wrapper panel cho cả bộ lọc
        JPanel filterWrapper = new JPanel();
        filterWrapper.setLayout(new BoxLayout(filterWrapper, BoxLayout.Y_AXIS)); // xếp dọc 2 dòng

// Panel dòng 1: các bộ lọc
        JPanel filterRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterRow1.add(new JLabel("Bộ lọc"));

        createdByComboBox = new JComboBox<>();
        createdByComboBox.addItem(new ComboItem("", "--- Người xóa ---"));
        List<User> adminList = loadAllAccountAdim();
        for (User user : adminList) {
            createdByComboBox.addItem(new ComboItem(String.valueOf(user.getId()), user.getUsername()));
        }
        filterRow1.add(createdByComboBox);

        filterRow1.add(new JLabel("Ngày xóa:"));
        fromDateChooser = new JDateChooser();
        fromDateChooser.setDateFormatString("dd/MM/yyyy");
        fromDateChooser.setPreferredSize(new Dimension(100, 25));
        ((JTextFieldDateEditor) fromDateChooser.getDateEditor()).setEditable(false);
        filterRow1.add(fromDateChooser);

        filterRow1.add(new JLabel("đến:"));
        toDateChooser = new JDateChooser();
        toDateChooser.setDateFormatString("dd/MM/yyyy");
        toDateChooser.setPreferredSize(new Dimension(100, 25));
        ((JTextFieldDateEditor) toDateChooser.getDateEditor()).setEditable(false);
        filterRow1.add(toDateChooser);

        searchField = new JTextField();
        filterRow1.add(searchField);
        searchField.setPreferredSize(new Dimension(150,25));
        searchButton = new JButton("Tìm kiếm");
        filterRow1.add(searchButton);

        clearFilterButton = new JButton("Xóa bộ lọc");
        clearFilterButton.setBackground(new Color(255, 204, 204));
        clearFilterButton.setForeground(Color.RED);
        filterRow1.add(clearFilterButton);

// Panel dòng 2: các nút hành động
        JPanel filterRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        checkAllButton = new JButton("Chọn tất cả");
        checkAllButton.setBackground(Color.decode("#F07171"));
        checkAllButton.setPreferredSize(new Dimension(150, 25));
        checkAllButton.setForeground(Color.WHITE);
        filterRow2.add(checkAllButton);

        statusChangeMultiComboBox = new JComboBox<>();
        statusChangeMultiComboBox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusChangeMultiComboBox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusChangeMultiComboBox.addItem(new ComboItem("active", "Hoạt động"));
        filterRow2.add(statusChangeMultiComboBox);

        restoreButton = new JButton("Khôi phục");
        restoreButton.setBackground(Color.decode("#007BFF"));
        restoreButton.setForeground(Color.WHITE);
        filterRow2.add(restoreButton);

        deletePermanentlyButton = new JButton("Xóa vĩnh viễn");
        deletePermanentlyButton.setBackground(Color.decode("#FA3E3E"));
        deletePermanentlyButton.setForeground(Color.WHITE);
        filterRow2.add(deletePermanentlyButton);

// Thêm hai dòng vào wrapper
        filterWrapper.add(filterRow1);
        filterWrapper.add(filterRow2);

// Thêm filterWrapper vào JFrame
        add(filterWrapper, BorderLayout.NORTH);

        // Tạo bảng
        String[] columnNames = {"","id", "Tên danh mục", "Mô tả", "Trạng thái", "Danh mục cha", "Tạo bởi", "Cập nhật bởi"};
        categoryTrashModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tất cả ô đều không cho sửa
            }
        };


        loadAllCategoriesTrash();
        table = new JTable(categoryTrashModel);
        // ẩn cột id
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setWidth(0);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setMaxWidth(30); // Cột checkbox

        // Thêm bảng vào JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);


        // phân trang
        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.CENTER)); // căn giữa

        firstPageButton = new JButton("Trang đầu");
        firstPageButton.setPreferredSize(new Dimension(120,25));
        preButton = new JButton("Trang trước");
        preButton.setPreferredSize(new Dimension(120,25));
        totalPage = (int) Math.ceil((double) categoryController.getTotalRecordTrash() / limit);
        if(totalPage<=1){
            totalPage = 1;
        }
        page.setText(currentPage + " / " + totalPage);
        nextButton = new JButton("Trang sau");
        nextButton.setPreferredSize(new Dimension(120,25));
        lastPageButton = new JButton("Trang cuối");
        lastPageButton.setPreferredSize(new Dimension(120,25));

        // Thêm các thành phần
        pagination.add(firstPageButton);
        pagination.add(preButton);
        pagination.add(page);
        pagination.add(nextButton);
        pagination.add(lastPageButton);

        add(pagination, BorderLayout.SOUTH);

        // nút trang đầu
        firstPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterCategoryTrash();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang trước
        preButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                filterCategoryTrash();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang tiếp theo
        nextButton.addActionListener(e -> {
            if (currentPage < totalPage) {
                currentPage++;
                filterCategoryTrash();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang cuối
        lastPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = totalPage;
                filterCategoryTrash();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // xử lý sự kiện khi ấn nút checkAll thì tất cả row trong table tự động checked
        checkAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAllButton();
            }
        });

        // Xử lý sự kiện tìm kiếm (chỉ tìm theo tên danh mục)
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterCategoryTrash();
            }
        });

        // button reset filter
        clearFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createdByComboBox.setSelectedIndex(0);
                fromDateChooser.setDate(null);
                toDateChooser.setDate(null);
                searchField.setText("");
                idCategorySelect = 0;
                loadAllCategoriesTrash();
            }
        });

        // Thêm ActionListener cho createdByComboBox
        createdByComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterCategoryTrash();
            }
        });

        // Thêm sự kiện cho fromDateChooser (bắt đầu từ 00:00:00)
        fromDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterCategoryTrash();
            }
        });

        // Thêm sự kiện cho toDateChooser (kết thúc lúc 23:59:59)
        toDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterCategoryTrash();
            }
        });


        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow(); // Lấy chỉ số dòng được chọn
                if (selectedRow != -1) {
                    if((boolean) table.getValueAt(selectedRow, 0)){
                        table.setValueAt(false, selectedRow, 0);
                    }else{
                        table.setValueAt(true, selectedRow, 0);
                    }
                    idCategorySelect = (int) table.getValueAt(selectedRow, 1);
                } else {
                    System.out.println("Không có dòng nào được chọn.");
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        restoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    boolean hasSelectedRow = false;

                    for (int i = 0; i < categoryTrashModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) categoryTrashModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }

                    if(hasSelectedRow){
                        ComboItem statusRestoreComboBox = (ComboItem) statusChangeMultiComboBox.getSelectedItem();
                        String statusRestore = statusRestoreComboBox.getValue();
                        if(statusRestore != ""){
                            for (int i = 0; i < categoryTrashModel.getRowCount(); i++) {
                                Boolean isChecked = (Boolean) categoryTrashModel.getValueAt(i, 0);
                                if (isChecked != null && isChecked) {
                                    int categoryId = (Integer) categoryTrashModel.getValueAt(i, 1);
                                    categoryController.updateMultiCategory(categoryId, statusRestore,userId);
                                }
                            }
                            filterCategoryTrash();
                            if(categoryFrame != null ){
                                categoryFrame.filterCategory();
                            }
                            JOptionPane.showMessageDialog(null, "Khôi phục thành công!");
                            isAllChecked = false;
                            checkAllButton.setText("Chọn tất cả");
                            statusChangeMultiComboBox.setSelectedItem(new ComboItem("", "--- Trạng thái ---"));
                        }else{
                            JOptionPane.showMessageDialog(null,"Chưa chọn trạng thái khôi phục");
                        }
                    }else{
                        JOptionPane.showMessageDialog(null, "Hãy chọn ít nhất 1 danh mục");
                    }
                }catch (SQLException ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });


        deletePermanentlyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    boolean hasSelectedRow = false;

                    for (int i = 0; i < categoryTrashModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) categoryTrashModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }

                    if(hasSelectedRow){
                        // Hỏi xác nhận
                        int result = JOptionPane.showConfirmDialog(null, "Bạn có chắc chắn muốn xóa vĩnh viễn các danh mục đã chọn không?\nNếu xác nhận xóa thì sẽ không thể khôi phục!", "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (result == JOptionPane.YES_OPTION) {
                            for (int i = 0; i < categoryTrashModel.getRowCount(); i++) {
                                Boolean isChecked = (Boolean) categoryTrashModel.getValueAt(i, 0);
                                if (isChecked != null && isChecked) {
                                    int categoryId = (Integer) categoryTrashModel.getValueAt(i, 1);
                                    categoryController.deletePermanentlyCategory(categoryId);
                                }
                            }

                            filterCategoryTrash();
                            table.clearSelection();
                            JOptionPane.showMessageDialog(null, "Xóa vĩnh viễn thành công!");
                            idCategorySelect = 0;
                        }
                    }else{
                        JOptionPane.showMessageDialog(null, "Hãy chọn ít nhất 1 danh mục!");
                    }
                }catch (SQLException ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });


    }

    public void loadAllCategoriesTrash() {
        try{
            ArrayList<Category> categories = categoryController.findAllCategoryTrash(currentPage, limit);
            categoryTrashModel.setRowCount(0);
            for (Category category : categories) {
                categoryTrashModel.addRow(new Object[]{false,category.getId(),
                        category.getName(),
                        category.getDescription(),
                        category.getStatus(),
                        category.getParentName(),
                        category.getCreatedAt() + " - " + category.getCreatedByUsername(),
                        category.getUpdatedAt() + " - " + category.getUpdatedByUsername()
                });
            }
            totalPage = (int) Math.ceil((double) categoryController.getTotalRecordTrash() / limit);
            if(totalPage<=1){
                totalPage = 1;
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }


    private void checkAllButton(){
        isAllChecked = !isAllChecked; // Đảo ngược trạng thái
        boolean checkedState = isAllChecked; // Lấy trạng thái mới
        for (int i = 0; i < categoryTrashModel.getRowCount(); i++) {
            categoryTrashModel.setValueAt(checkedState, i, 0); // Cập nhật cột 0 (checkbox)
        }
        if(!isAllChecked){
            checkAllButton.setText("Chọn tất cả");
        }else{
            checkAllButton.setText("Bỏ chọn tất cả");
        }
    }


    private void loadFilterCategoriesTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) {
        try{
            ArrayList<Category> categories = categoryController.filteredCategoriesPageTrash(searchText,filterCreatedBy, fromDate, toDate, currentPage, limit);
            categoryTrashModel.setRowCount(0);
            for (Category category : categories) {
                categoryTrashModel.addRow(new Object[]{false,category.getId(),
                        category.getName(),
                        category.getDescription(),
                        category.getStatus(),
                        category.getParentName(),
                        category.getCreatedAt() + " - " + category.getCreatedByUsername(),
                        category.getUpdatedAt() + " - " + category.getUpdatedByUsername()
                });
            }
            totalPage = (int) Math.ceil((double) categoryController.getTotalRecordFilterTrash(searchText,filterCreatedBy, fromDate, toDate) / limit);
            if(totalPage <= 1){
                totalPage = 1;
            }
            // sau khi xóa nếu trang hiện tại > tổng số trang thig load lại
            if(totalPage<currentPage){
                currentPage = totalPage;
                filterCategoryTrash();
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }

    private void filterCategoryTrash(){
        Date selectedFromDate = fromDateChooser.getDate();
        Date selectedToDate = toDateChooser.getDate();
        String searchText = searchField.getText().toLowerCase();
        ComboItem selectedCreatedByItem = (ComboItem) createdByComboBox.getSelectedItem();
        Timestamp sqlToDate = null, sqlFromDate = null;
        if (selectedFromDate != null) {
            // Sử dụng Calendar để đặt giờ về 00:00:00
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(selectedFromDate);
            calendar1.set(Calendar.HOUR_OF_DAY, 0);
            calendar1.set(Calendar.MINUTE, 0);
            calendar1.set(Calendar.SECOND, 0);
            calendar1.set(Calendar.MILLISECOND, 0);
            selectedFromDate = calendar1.getTime();
            sqlFromDate = new Timestamp(selectedFromDate.getTime());
        }

        if (selectedToDate != null) {
            // Sử dụng Calendar để đặt giờ về 23:59:59
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(selectedToDate);
            calendar2.set(Calendar.HOUR_OF_DAY, 23);
            calendar2.set(Calendar.MINUTE, 59);
            calendar2.set(Calendar.SECOND, 59);
            calendar2.set(Calendar.MILLISECOND, 999);
            selectedToDate = calendar2.getTime();
            sqlToDate = new Timestamp(selectedToDate.getTime());

        }

        loadFilterCategoriesTrash(searchText, selectedCreatedByItem.getValue(), sqlFromDate, sqlToDate);
    }


    private List<User> loadAllAccountAdim(){
        UserController userController = new UserController();
        return userController.getAllUsers();

    }
    
}