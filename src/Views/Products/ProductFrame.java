package Views.Products;

import Controllers.CategoryController;
import Controllers.ProductController;
import Controllers.SupplierController;
import Controllers.UserController;
import Models.*;
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

public class ProductFrame extends JFrame {
    public int userId = Session.getInstance().getUser().getId();
    private JTable table;
    private DefaultTableModel productModel;
    private JTextField searchField;
    private JComboBox<ComboItem> statusComboBox, createdByComboBox, arrangeComboBox, statusChangeMultiComboBox, supplierComboBox, categoryComboBox;
    private JDateChooser fromDateChooser, toDateChooser;
    private JButton createButton,createExcelButton, checkAllButton, editButton, deleteButton, trashButton, changeMultiButton, searchButton, clearFilterButton;
    private JButton preButton, nextButton, firstPageButton, lastPageButton;
    private boolean isAllChecked = false;
    private ProductController productController;
    private CategoryController categoryController;
    private SupplierController supplierController;
    private UserController userController;
    private int idProductSelect;
    private ProductTrashFrame trashFrame;
    private EditProductFrame editFrame;
    private ArrayList<Product> danhSachSanPham;
    private int currentPage = 1;
    private int limit = 20;
    int totalPage;
    private JLabel page = new JLabel();
    private AddProductFrame addFrame = null;


    public void view(){
        setTitle("Quản lý sản phẩm");
        setSize(1500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel JTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLable = new JLabel("Quản lý sản phẩm");
        titleLable.setFont(new Font("Arial", Font.BOLD, 30));
        JTitle.add(titleLable);

        // Panel bộ lọc (dòng 1)
        JPanel panelRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Bộ lọc
        JLabel filterLabel = new JLabel("Bộ lọc");
        panelRow1.add(filterLabel);

        
        // trạng thái
        statusComboBox = new JComboBox<>();
        statusComboBox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusComboBox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusComboBox.addItem(new ComboItem("active", "Hoạt động"));
        panelRow1.add(statusComboBox);


        // danh mục
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem(new ComboItem("", "--- Danh mục ---"));
        ArrayList<Category> categories = loadAllCategory();
        for (int i = 0; i < categories.size(); i++) {
            categoryComboBox.addItem(new ComboItem(
                    String.valueOf(categories.get(i).getId()),
                    String.valueOf(categories.get(i).getName())
            ));
        }
        panelRow1.add(categoryComboBox);


        // nhà cung cấp
        supplierComboBox = new JComboBox<>();
        supplierComboBox.addItem(new ComboItem("", "--- Nhà cung cấp ---"));
        ArrayList<Supplier> suppliers = loadAllSupplier();
        for (int i = 0; i < suppliers.size(); i++) {
            supplierComboBox.addItem(new ComboItem(
                    String.valueOf(suppliers.get(i).getId()),
                    String.valueOf(suppliers.get(i).getName())
            ));
        }
        panelRow1.add(supplierComboBox);


        
        // người tạo
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

        
        // lọc theo ngày tạo
        JLabel startDate = new JLabel("Ngày tạo: ");
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
        
        
        // tìm kiếm
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


        // dòng 2
        JPanel panelRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));


        // Nút checkall
        checkAllButton = new JButton("Chọn tất cả");
        checkAllButton.setBackground(Color.decode("#F07171"));
        checkAllButton.setPreferredSize(new Dimension(150, 25));
        checkAllButton.setForeground(Color.WHITE);
        panelRow2.add(checkAllButton);


        arrangeComboBox = new JComboBox<>();
        arrangeComboBox.addItem(new ComboItem("", "--- Sắp xếp ---"));
        arrangeComboBox.addItem(new ComboItem("tên tăng dần", "Tên [A-Z]"));
        arrangeComboBox.addItem(new ComboItem("tên giảm dần", "Tên [Z-A]"));
        arrangeComboBox.addItem(new ComboItem("giá bán tăng dần", "Giá bán \uD83E\uDC1D"));
        arrangeComboBox.addItem(new ComboItem("giá bán giảm dần", "Giá bán \uD83E\uDC1F"));
        arrangeComboBox.addItem(new ComboItem("giảm giá tăng dần", "Giảm giá \uD83E\uDC1D"));
        arrangeComboBox.addItem(new ComboItem("giảm giá giảm dần", "Giảm giá \uD83E\uDC1F"));
        arrangeComboBox.addItem(new ComboItem("hạn sử dụng tăng dần", "Hạn sử dụng \uD83E\uDC1D"));
        arrangeComboBox.addItem(new ComboItem("hạn sử dụng giảm dần", "Hạn sử dụng \uD83E\uDC1F"));
        panelRow2.add(arrangeComboBox);



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

        // Nút Nhập excel
        createExcelButton = new JButton("+ Tạo mới (Excel)");
        createExcelButton.setBackground(Color.decode("#107C41"));
        createExcelButton.setForeground(Color.WHITE);
        panelRow2.add(createExcelButton);


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


        // Nút xem thùng rác
        trashButton = new JButton("Thùng rác");
        trashButton.setBackground(Color.decode("#C2C2C2"));
        panelRow2.add(trashButton);

        topPanel.add(JTitle);
        topPanel.add(panelRow1);
        topPanel.add(panelRow2);
        add(topPanel, BorderLayout.NORTH);


        // Tạo bảng
        String[] columnNames = {"","id", "Tên sản phẩm","Ảnh", "Danh mục","Nhà cung cấp","Giá nhập", "Giá bán","Đơn vị","Còn",  "Trạng thái", "Tạo bởi", "Barcode","SL tối thiểu", "Giảm giá","HSD"};
        productModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tất cả ô đều không cho sửa
            }
        };

        loadAllProduct();
        table = new JTable(productModel);
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
        totalPage = (int) Math.ceil((double) productController.getTotalRecord() / limit);
        if(totalPage < 1){
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



    public ProductFrame(){
        productController = new ProductController();
        categoryController = new CategoryController();
        supplierController = new SupplierController();
        userController = new UserController();
        view();


        // nút trang đầu
        firstPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentPage>1){
                    currentPage = 1;
                    filterProduct();
                    page.setText(currentPage + " / " + totalPage);
                }
            }
        });

        // nút trang trước
        preButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                filterProduct();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang tiếp theo
        nextButton.addActionListener(e -> {
            if (currentPage < totalPage) {
                currentPage++;
                filterProduct();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang cuối
        lastPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentPage < totalPage){
                    currentPage = totalPage;
                    filterProduct();
                    page.setText(currentPage + " / " + totalPage);
                }
            }
        });

        checkAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAllButton();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProduct();
            }
        });


        // button reset filter
        clearFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusComboBox.setSelectedIndex(0);
                categoryComboBox.setSelectedIndex(0);
                supplierComboBox.setSelectedIndex(0);
                createdByComboBox.setSelectedIndex(0);
                fromDateChooser.setDate(null);
                toDateChooser.setDate(null);
                searchField.setText("");
                arrangeComboBox.setSelectedItem(0);
                idProductSelect = 0;
                loadAllProduct();
            }
        });

        // Thêm ActionListener cho statusComboBox
        statusComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProduct();
            }
        });


        // Thêm ActionListener cho createdByComboBox
        createdByComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProduct();
            }
        });


        // Thêm ActionListener cho categoryComboBox
        categoryComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProduct();
            }
        });


        // Thêm ActionListener cho supplierComboBox
        supplierComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProduct();
            }
        });

        // Thêm sự kiện cho fromDateChooser (bắt đầu từ 00:00:00)
        fromDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterProduct();
            }
        });

        // Thêm sự kiện cho toDateChooser (kết thúc lúc 23:59:59)
        toDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterProduct();
            }
        });

        // Thêm ActionListener cho arrangeComboBox
        arrangeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProduct();
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
//                    idProductSelect = (int) table.getValueAt(selectedRow, 1);
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

                    for (int i = 0; i < productModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) productModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }

                    if(hasSelectedRow){
                        ComboItem item = (ComboItem) statusChangeMultiComboBox.getSelectedItem();
                        String statusItem = item.getValue();
                        if(statusItem != ""){
                            for (int i = 0; i < productModel.getRowCount(); i++) {
                                Boolean isChecked = (Boolean) productModel.getValueAt(i, 0);
                                if (isChecked != null && isChecked && !statusItem.equals(productModel.getValueAt(i,11))) {
                                    int productId = (Integer) productModel.getValueAt(i, 1);
                                    productController.updateMultiProduct(productId, statusItem, userId);
                                }
                            }
                            filterProduct();
                            table.clearSelection();
                            JOptionPane.showMessageDialog(null, "Thay đổi hàng loạt thành công!");
                            idProductSelect = 0;
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
                addFrame = new AddProductFrame(ProductFrame.this);
                addFrame.setVisible(true);
            }
        });

        createExcelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int hasSelectedRow = 0;

                for (int i = 0; i < productModel.getRowCount(); i++) {
                    Boolean isChecked = (Boolean) productModel.getValueAt(i, 0);
                    if (isChecked != null && isChecked) {
                        hasSelectedRow++;
                        if(hasSelectedRow > 1) break;
                        idProductSelect = (int) productModel.getValueAt(i, 1);
                    }
                }

                if(hasSelectedRow == 1){
                    if (editFrame != null && editFrame.isDisplayable()) {
                        editFrame.dispose(); // Đóng frame cũ nếu còn tồn tại
                    }
                    editFrame = new EditProductFrame(ProductFrame.this, idProductSelect);
                    editFrame.setVisible(true);
                    table.clearSelection();
                } else if(hasSelectedRow==0) {
                    JOptionPane.showMessageDialog(null, "Hãy chọn 1 sản phẩm!");
                }else{
                    JOptionPane.showMessageDialog(null, "Chỉ chọn 1 sản phẩm!");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    boolean hasSelectedRow = false;

                    for (int i = 0; i < productModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) productModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }

                    if(hasSelectedRow){
                        JOptionPane.showMessageDialog(null, "Chỉ xóa những sản phẩm có số lượng tồn kho = 0");
                        for (int i = 0; i < productModel.getRowCount(); i++) {
                            Boolean isChecked = (Boolean) productModel.getValueAt(i, 0);
                            int quantityProduct = (Integer) productModel.getValueAt(i, 9);
                            if (isChecked != null && isChecked && quantityProduct <= 0) {
                                int idProduct = (Integer) productModel.getValueAt(i, 1);
                                productController.deleteProduct(idProduct, userId);
                                JOptionPane.showMessageDialog(null, "Xóa thành công!");
                            }
                        }
                        filterProduct();
                        if (trashFrame != null) {
                            trashFrame.filterProductTrash();
                        }
                        table.clearSelection();
                        idProductSelect = 0;
                        isAllChecked = false;
                        checkAllButton.setText("Chọn tất cả");
                    }else{
                        JOptionPane.showMessageDialog(null, "Hãy chọn ít nhất 1 sản phẩm!");
                    }
                }catch (SQLException ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });

        trashButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (trashFrame == null || !trashFrame.isDisplayable()) {
                    trashFrame = new ProductTrashFrame(ProductFrame.this);
                }
                trashFrame.setVisible(true);
                trashFrame.toFront();
            }
        });


    }



    public List<User> loadAllAccountAdim(){
        return userController.getAllUsers();
    }

    public void loadAllProduct(){
        try{
            ArrayList<Product> products = productController.findProductPage(currentPage,limit);
            productModel.setRowCount(0);
            for (Product product : products) {
                productModel.addRow(new Object[]{false,
                        product.getId(),
                        product.getName(),
                        product.getImage(),
                        product.getCategoryName(),
                        product.getSupplierName(),
                        product.getOriginalPrice(),
                        product.getSellingPrice(),
                        product.getUnit(),
                        product.getQuantity(),
                        product.getStatus(),
                        product.getCreatedAt() + " - " + product.getCreatedByUsername(),
                        product.getBarcode(),
                        product.getMinimumQuantity(),
                        product.getDiscount(),
                        product.getUseByDate(),
                });
            }
            totalPage = (int) Math.ceil((double) productController.getTotalRecord() / limit);
            if(totalPage <= 1){
                totalPage = 1;
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }

    public ArrayList<Category> loadAllCategory(){
        try{
            ArrayList<Category> categories = categoryController.findAllCategory();
            return categories;
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
        return null;
    }

    public ArrayList<Supplier> loadAllSupplier(){
        try{
            ArrayList<Supplier> suppliers = supplierController.findAllSupplier();
            return suppliers;
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
        return null;
    }

    public void filterProduct(){
        ComboItem selectedStatusItem = (ComboItem) statusComboBox.getSelectedItem();
        ComboItem selectedCategoryItem = (ComboItem) categoryComboBox.getSelectedItem();
        ComboItem selectedSupplierItem = (ComboItem) supplierComboBox.getSelectedItem();
        ComboItem selectedCreatedByItem = (ComboItem) createdByComboBox.getSelectedItem();
        Date selectedFromDate = fromDateChooser.getDate();
        Date selectedToDate = toDateChooser.getDate();
        String searchText = searchField.getText().toLowerCase();
        ComboItem selectedRangeItem = (ComboItem) arrangeComboBox.getSelectedItem();

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
        System.out.println(sqlFromDate);
        System.out.println(sqlToDate);
        loadFilterProduct(searchText, selectedStatusItem.getValue(), selectedCreatedByItem.getValue(), sqlFromDate, sqlToDate,selectedCategoryItem.getValue(),selectedSupplierItem.getValue(),selectedRangeItem.getValue());
    }

    private void loadFilterProduct(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate, String filterCategory, String filterSupplier, String filterRange) {
        try{
            ArrayList<Product> products = productController.filteredProducts(searchText,filterStatus,filterCreatedBy, fromDate, toDate, filterCategory, filterSupplier, filterRange,currentPage,limit);
            productModel.setRowCount(0);
            for (Product product : products) {
                productModel.addRow(new Object[]{false,
                        product.getId(),
                        product.getName(),
                        product.getImage(),
                        product.getCategoryName(),
                        product.getSupplierName(),
                        product.getOriginalPrice(),
                        product.getSellingPrice(),
                        product.getUnit(),
                        product.getQuantity(),
                        product.getStatus(),
                        product.getCreatedAt() + " - " + product.getCreatedByUsername(),
                        product.getBarcode(),
                        product.getMinimumQuantity(),
                        product.getDiscount(),
                        product.getUseByDate(),
                });
            }

            totalPage = (int) Math.ceil((double) productController.getTotalFilterRecord(searchText,filterStatus,filterCreatedBy, fromDate, toDate, filterCategory, filterSupplier) / limit);
            if(totalPage <= 1){
                totalPage = 1;
            }
            // sau khi xóa nếu trang hiện tại > tổng số trang thig load lại
            if(totalPage<currentPage){
                currentPage = totalPage;
                filterProduct();
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }

    private void checkAllButton(){
        isAllChecked = !isAllChecked; // Đảo ngược trạng thái
        boolean checkedState = isAllChecked; // Lấy trạng thái mới
        for (int i = 0; i < productModel.getRowCount(); i++) {
            productModel.setValueAt(checkedState, i, 0); // Cập nhật cột 0 (checkbox)
        }
        if(!isAllChecked){
            checkAllButton.setText("Chọn tất cả");
        }else{
            checkAllButton.setText("Bỏ chọn tất cả");
        }
    }


}
