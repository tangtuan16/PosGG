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
import java.util.*;
import java.util.List;

public class CategoryFrame extends JFrame {
    public int userId = Session.getInstance().getUser().getId();
    private JTable table;
    private DefaultTableModel categoryModel;
    private JTextField searchField;
    private JComboBox<ComboItem> statusComboBox, createdByComboBox, statusChangeMultiComboBox;
    private JDateChooser fromDateChooser, toDateChooser;
    private JButton createButton, checkAllButton, editButton, deleteButton, trashButton, changeMultiButton, searchButton, clearFilterButton;
    private JButton preButton, nextButton, firstPageButton, lastPageButton;
    private boolean isAllChecked = false; // Biến để theo dõi trạng thái
    private CategoryController categoryController;
    private UserController userController;
    private int idCategorySelect;
    private CategoryTrashFrame trashFrame;
    private EditCategoryFrame editFrame;
    private ArrayList<Category> danhSachDanhMuc;
    private Set<Integer> danhMucChaIds = new HashSet<>();
    private int currentPage = 1;
    private int limit = 20;
    int totalPage;
    private JLabel page = new JLabel();
    private AddCategoryFrame addFrame = null;


    //    // Renderer tùy chỉnh cho cột hình ảnh
//    private static class ImageRenderer extends JLabel implements TableCellRenderer {
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            setHorizontalAlignment(JLabel.CENTER);
//            setOpaque(true);
//            if (value != null && value instanceof String) {
//                String imageUrl = (String) value;
//                try {
//                    URL url = new URL(imageUrl);
//                    BufferedImage image = ImageIO.read(url);
//                    if (image != null) {
//                        Image scaledImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Thay đổi kích thước
//                        setIcon(new ImageIcon(scaledImage));
//                    } else {
//                        setText("No Image");
//                    }
//                } catch (Exception e) {
//                    setText("Error Loading");
//                }
//            } else {
//                setIcon(null);
//                setText("");
//            }
//            return this;
//        }
//    }
    public void view(){
        // Thiết lập JFrame
        setTitle("Quản lý danh mục");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

// Panel chứa toàn bộ phần phía trên (lọc + nút)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel JTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLable = new JLabel("Quản lý danh mục");
        titleLable.setFont(new Font("Arial", Font.BOLD, 30));
        JTitle.add(titleLable);


// Panel bộ lọc (dòng 1)
        JPanel panelRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

// Bộ lọc
        JLabel filterLabel = new JLabel("Bộ lọc");
        panelRow1.add(filterLabel);

// JComboBox với giá trị tùy chỉnh
        statusComboBox = new JComboBox<>();
        statusComboBox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusComboBox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusComboBox.addItem(new ComboItem("active", "Hoạt động"));
        panelRow1.add(statusComboBox);

        createdByComboBox = new JComboBox<>();
        createdByComboBox.addItem(new ComboItem("", "--- Người tạo ---"));
        List<User> adminList = loadAllAccountAdim();
        for (int i = 0; i < adminList.size(); i++) {
            createdByComboBox.addItem(new ComboItem(
                    String.valueOf(adminList.get(i).getId()),
                    String.valueOf(adminList.get(i).getUsername())
            ));
        }
        panelRow1.add(createdByComboBox);

        JLabel startDate = new JLabel("Từ: ");
        panelRow1.add(startDate);

        fromDateChooser = new JDateChooser();
        fromDateChooser.setDateFormatString("dd/MM/yyyy");
        fromDateChooser.setPreferredSize(new Dimension(100, 25));
        panelRow1.add(fromDateChooser);
// Vô hiệu hóa nhập tay
        JTextFieldDateEditor editorFrom = (JTextFieldDateEditor) fromDateChooser.getDateEditor();
        editorFrom.setEditable(false);

        JLabel endDate = new JLabel("đến: ");
        panelRow1.add(endDate);

        toDateChooser = new JDateChooser();
        toDateChooser.setDateFormatString("dd/MM/yyyy");
        toDateChooser.setPreferredSize(new Dimension(100, 25));
        panelRow1.add(toDateChooser);
// Vô hiệu hóa nhập tay
        JTextFieldDateEditor editorTo = (JTextFieldDateEditor) toDateChooser.getDateEditor();
        editorTo.setEditable(false);

// Nút Tìm kiếm và ô tìm kiếm
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(150,25));
        panelRow1.add(searchField);
        searchButton = new JButton("Tìm kiếm");
        panelRow1.add(searchButton);

        // Nút Xóa bộ lọc
        clearFilterButton = new JButton("Xóa bộ lọc");
        clearFilterButton.setBackground(new Color(255, 204, 204));
        clearFilterButton.setForeground(Color.RED);
        panelRow1.add(clearFilterButton);

// Panel chứa các nút chức năng (dòng 2)
        JPanel panelRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

// Nút checkall
        checkAllButton = new JButton("Chọn tất cả");
        checkAllButton.setBackground(Color.decode("#F07171"));
        checkAllButton.setPreferredSize(new Dimension(150, 25));
        checkAllButton.setForeground(Color.WHITE);
        panelRow2.add(checkAllButton);

        // chọn trạng thái caaph nhật hàng loạt
        statusChangeMultiComboBox = new JComboBox<>();
        statusChangeMultiComboBox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusChangeMultiComboBox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusChangeMultiComboBox.addItem(new ComboItem("active", "Hoạt động"));
        panelRow2.add(statusChangeMultiComboBox);

        // nút cập nhật hàng loạt
        changeMultiButton = new JButton("Cập nhật hàng loạt");
        changeMultiButton.setPreferredSize(new Dimension(150,25));
        panelRow2.add(changeMultiButton);

// Nút Tạo mới
        createButton = new JButton("+ Tạo mới");
        createButton.setBackground(Color.decode("#007BFF"));
        createButton.setForeground(Color.WHITE);
        panelRow2.add(createButton);

// Nút Sửa
        editButton = new JButton("Sửa");
        editButton.setBackground(Color.decode("#007BFF"));
        editButton.setForeground(Color.WHITE);
        panelRow2.add(editButton);

// Nút Xóa
        deleteButton = new JButton("Xóa");
        deleteButton.setBackground(Color.decode("#FA3E3E"));
        deleteButton.setForeground(Color.WHITE);
        panelRow2.add(deleteButton);

// Nút Thùng rác
        trashButton = new JButton("Thùng rác");
        trashButton.setBackground(Color.decode("#C2C2C2"));
        panelRow2.add(trashButton);

// Thêm cả hai panel vào topPanel và topPanel vào JFrame
        topPanel.add(JTitle);
        topPanel.add(panelRow1);
        topPanel.add(panelRow2);
        add(topPanel, BorderLayout.NORTH);

        // Tạo bảng
        String[] columnNames = {"","id", "Tên danh mục", "Mô tả", "Trạng thái", "Danh mục cha", "Tạo bởi", "Cập nhật bởi"};
        categoryModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tất cả ô đều không cho sửa
            }
        };

        loadAllCategories();
        table = new JTable(categoryModel);
        // ẩn cột id
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setWidth(0);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setMaxWidth(30); // Cột checkbox
//        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value,
//                                                           boolean isSelected, boolean hasFocus,
//                                                           int row, int column) {
//                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//
//                if (!isSelected) {
//                    int categoryId = (int) table.getValueAt(row, 1); // Cột 1 là ID
//                    if (danhMucChaIds.contains(categoryId)) {
//                        c.setBackground(Color.decode("#DBDFE3"));
//                    } else {
//                        c.setBackground(Color.WHITE); // con giữ màu nền mặc định
//                    }
//                } else {
//                    c.setBackground(table.getSelectionBackground());
//                }
//
//                return c;
//            }
//        });
//        table.getColumnModel().getColumn(2).setMaxWidth(150); // Cột ảnh đại diện

        // Thêm renderer cho cột hình ảnh
//        table.getColumnModel().getColumn(2).setCellRenderer(new ImageRenderer());

        // Thêm bảng vào JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);


        // phân trang
        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.CENTER)); // căn giữa

        firstPageButton = new JButton("Trang đầu");
        firstPageButton.setPreferredSize(new Dimension(120,25));
        preButton = new JButton("Trang trước");
        preButton.setPreferredSize(new Dimension(120,25));
        totalPage = (int) Math.ceil((double) categoryController.getTotalRecord() / limit);
        if(totalPage > 1){
            totalPage = (int) Math.ceil((double) categoryController.getTotalRecord() / limit);
        }else{
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
    }


    public CategoryFrame() {
        categoryController = new CategoryController();
        userController = new UserController();
        view();

        // nút trang đầu
        firstPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentPage>1){
                    currentPage = 1;
                    filterCategory();
                    page.setText(currentPage + " / " + totalPage);
                }
            }
        });

        // nút trang trước
        preButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                filterCategory();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang tiếp theo
        nextButton.addActionListener(e -> {
            if (currentPage < totalPage) {
                currentPage++;
                filterCategory();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang cuối
        lastPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentPage < totalPage){
                    currentPage = totalPage;
                    filterCategory();
                    page.setText(currentPage + " / " + totalPage);
                }
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
                filterCategory();
            }
        });

        // button reset filter
        clearFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusComboBox.setSelectedIndex(0);
                createdByComboBox.setSelectedIndex(0);
                fromDateChooser.setDate(null);
                toDateChooser.setDate(null);
                searchField.setText("");
                idCategorySelect = 0;
                loadAllCategories();
            }
        });

        // Thêm ActionListener cho statusComboBox
        statusComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterCategory();
            }
        });

        // Thêm ActionListener cho createdByComboBox
        createdByComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterCategory();
            }
        });

        // Thêm sự kiện cho fromDateChooser (bắt đầu từ 00:00:00)
        fromDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterCategory();
            }
        });

        // Thêm sự kiện cho toDateChooser (kết thúc lúc 23:59:59)
        toDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterCategory();
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

        changeMultiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    boolean hasSelectedRow = false;

                    for (int i = 0; i < categoryModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) categoryModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }
                    if(hasSelectedRow){
                        ComboItem item = (ComboItem) statusChangeMultiComboBox.getSelectedItem();
                        String statusItem = item.getValue();
                        if(statusItem != ""){
                            for (int i = 0; i < categoryModel.getRowCount(); i++) {
                                Boolean isChecked = (Boolean) categoryModel.getValueAt(i, 0);
                                if (isChecked != null && isChecked && !statusItem.equals(categoryModel.getValueAt(i,4))) {
                                    int categoryId = (Integer) categoryModel.getValueAt(i, 1);
                                    categoryController.updateMultiCategory(categoryId, statusItem, userId);
                                }
                            }
                            filterCategory();
                            if (trashFrame != null) {
                                trashFrame.loadAllCategoriesTrash();
                            }
                            table.clearSelection();
                            JOptionPane.showMessageDialog(null, "Thay đổi hàng loạt thành công!");
                            idCategorySelect = 0;
                            isAllChecked = false;
                            checkAllButton.setText("Chọn tất cả");
                            statusChangeMultiComboBox.setSelectedItem(new ComboItem("", "--- Trạng thái ---"));
                        }else{
                            JOptionPane.showMessageDialog(null,"Chưa chọn trạng thái thay đổi!");
                        }
                    }else{
                        JOptionPane.showMessageDialog(null, "Hãy chọn ít nhất 1 danh mục");
                    }
                }catch (SQLException ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addFrame != null && addFrame.isDisplayable()) {
                    addFrame.dispose();  // Đóng frame cũ
                }

                // Tạo frame mới
                addFrame = new AddCategoryFrame(CategoryFrame.this);
                addFrame.setVisible(true);

            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int hasSelectedRow = 0;

                for (int i = 0; i < categoryModel.getRowCount(); i++) {
                    Boolean isChecked = (Boolean) categoryModel.getValueAt(i, 0);
                    if (isChecked != null && isChecked) {
                        hasSelectedRow++;
                        if(hasSelectedRow > 1) break;
                        idCategorySelect = (int) categoryModel.getValueAt(i, 1);
                    }
                }

                if(hasSelectedRow == 1){
                    if (editFrame != null && editFrame.isDisplayable()) {
                        editFrame.dispose(); // Đóng frame cũ nếu còn tồn tại
                    }
                    editFrame = new EditCategoryFrame(CategoryFrame.this, idCategorySelect);
                    editFrame.setVisible(true);
                    table.clearSelection();
                } else if(hasSelectedRow==0) {
                    JOptionPane.showMessageDialog(null, "Hãy chọn 1 danh mục!");
                }else{
                    JOptionPane.showMessageDialog(null, "Chỉ chọn 1 danh mục!");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    boolean hasSelectedRow = false;

                    for (int i = 0; i < categoryModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) categoryModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }

                    if(hasSelectedRow){
                        for (int i = 0; i < categoryModel.getRowCount(); i++) {
                            Boolean isChecked = (Boolean) categoryModel.getValueAt(i, 0);
                            if (isChecked != null && isChecked) {
                                int categoryId = (Integer) categoryModel.getValueAt(i, 1);
                                categoryController.deleteCategory(categoryId, userId);
                            }
                        }
                        filterCategory();
                        if (trashFrame != null) {
                            trashFrame.loadAllCategoriesTrash();
                        }
                        table.clearSelection();
                        JOptionPane.showMessageDialog(null, "Xóa thành công!");
                        idCategorySelect = 0;
                        isAllChecked = false;
                        checkAllButton.setText("Chọn tất cả");
                    }else{
                        JOptionPane.showMessageDialog(null, "Hãy chọn ít nhất 1 danh mục!");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }

            }
        });

        trashButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (trashFrame == null || !trashFrame.isDisplayable()) {
                    trashFrame = new CategoryTrashFrame(CategoryFrame.this);
                }
                trashFrame.setVisible(true);
                trashFrame.toFront();
            }
        });


    }

    public void loadAllCategories() {
        try{
//          danhSachDanhMuc = new ArrayList<>();
            ArrayList<Category> categories = categoryController.findCategoryPage(currentPage,limit);
//          hienThiDanhMuc(categories,"");
            categoryModel.setRowCount(0);
            for (Category category : categories) {
                categoryModel.addRow(new Object[]{false,category.getId(),
                        category.getName(),
                        category.getDescription(),
                        category.getStatus(),
                        category.getParentName(),
                        category.getCreatedAt() + " - " + category.getCreatedByUsername(),
                        category.getUpdatedAt() + " - " + category.getUpdatedByUsername()
                });
            }
            totalPage = (int) Math.ceil((double) categoryController.getTotalRecord() / limit);
            if(totalPage <= 1){
                totalPage = 1;
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }

    // phân cấp danh mục
//    public void hienThiDanhMuc(ArrayList<Category> categoryList, String parent) {
//        for (int i = 0; i < categoryList.size(); i++) {
//            Category category = categoryList.get(i);
//            // Trường hợp (parentId = null) in ra terminal = 0
//            if ((category.getParentId() == 0 && parent == "") || (category.getParentId() != null && String.valueOf(category.getParentId()).equals(parent))) {
//                danhSachDanhMuc.add(category);
//
//                // Đánh dấu là danh mục cha
//                if (category.getParentId() == 0) {
//                    danhMucChaIds.add(category.getId());
//                }
//
//                hienThiDanhMuc(categoryList, String.valueOf(category.getId()));
//            }
//        }
//    }

    private void checkAllButton(){
        isAllChecked = !isAllChecked; // Đảo ngược trạng thái
        boolean checkedState = isAllChecked; // Lấy trạng thái mới
        for (int i = 0; i < categoryModel.getRowCount(); i++) {
            categoryModel.setValueAt(checkedState, i, 0); // Cập nhật cột 0 (checkbox)
        }
        if(!isAllChecked){
            checkAllButton.setText("Chọn tất cả");
        }else{
            checkAllButton.setText("Bỏ chọn tất cả");
        }
    }

    private void loadFilterCategories(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) {
        try{
            List<Category> categories = categoryController.filteredCategories(searchText,filterStatus,filterCreatedBy, fromDate, toDate,currentPage,limit);
            categoryModel.setRowCount(0);
            for (Category category : categories) {
                categoryModel.addRow(new Object[]{false,category.getId(),
                        category.getName(),
                        category.getDescription(),
                        category.getStatus(),
                        category.getParentName(),
                        category.getCreatedAt() + " - " + category.getCreatedByUsername(),
                        category.getUpdatedAt() + " - " + category.getUpdatedByUsername()
                });
            }

            totalPage = (int) Math.ceil((double) categoryController.getTotalFilterRecord(searchText,filterStatus,filterCreatedBy, fromDate, toDate) / limit);
            if(totalPage<=1){
                totalPage = 1;
            }
            // sau khi xóa nếu trang hiện tại > tổng số trang thig load lại
            if(totalPage<currentPage){
                currentPage = totalPage;
                filterCategory();
            }
            page.setText(currentPage + " / " + totalPage);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }

    public void filterCategory(){
        Date selectedFromDate = fromDateChooser.getDate();
        Date selectedToDate = toDateChooser.getDate();
        String searchText = searchField.getText().toLowerCase();
        ComboItem selectedStatusItem = (ComboItem) statusComboBox.getSelectedItem();
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

        loadFilterCategories(searchText, selectedStatusItem.getValue(), selectedCreatedByItem.getValue(), sqlFromDate, sqlToDate);
    }

    private List<User> loadAllAccountAdim(){
        return userController.getAllUsers();
    }

}