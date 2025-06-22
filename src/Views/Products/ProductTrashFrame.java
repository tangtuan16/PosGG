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

public class ProductTrashFrame extends JFrame {
    private ProductFrame productFrame;
    public int userId = Session.getInstance().getUser().getId();
    private JTable table;
    private DefaultTableModel productTrashModel;
    private JTextField searchField;
    private JComboBox<ComboItem> deletedByComboBox, arrangeComboBox, statusChangeMultiComboBox, supplierComboBox, categoryComboBox;
    private JDateChooser fromDateChooser, toDateChooser;
    private JButton checkAllButton, deletePermanentlyButton, restoreButton, searchButton, clearFilterButton;
    private JButton preButton, nextButton, firstPageButton, lastPageButton;
    private boolean isAllChecked = false;
    private ProductController productController;
    private CategoryController categoryController;
    private SupplierController supplierController;
    private UserController userController;
    private int idProductSelect;
    private ArrayList<Product> danhSachSanPham;
    private int currentPage = 1;
    private int limit = 20;
    int totalPage;
    private JLabel page = new JLabel();

    public void view(){
        setTitle("Thùng rác sản phẩm");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel JTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLable = new JLabel("Thùng rác sản phẩm");
        titleLable.setFont(new Font("Arial", Font.BOLD, 30));
        JTitle.add(titleLable);

        // Panel bộ lọc (dòng 1)
        JPanel panelRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Bộ lọc
        JLabel filterLabel = new JLabel("Bộ lọc");
        panelRow1.add(filterLabel);

        // danh mục
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem(new ComboItem("", "--- Danh mục ---"));
        ArrayList<Category> categories = productFrame.loadAllCategory();
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
        ArrayList<Supplier> suppliers = productFrame.loadAllSupplier();
        for (int i = 0; i < suppliers.size(); i++) {
            supplierComboBox.addItem(new ComboItem(
                    String.valueOf(suppliers.get(i).getId()),
                    String.valueOf(suppliers.get(i).getName())
            ));
        }
        panelRow1.add(supplierComboBox);

        // người xóa
        deletedByComboBox = new JComboBox<>();
        deletedByComboBox.addItem(new ComboItem("", "--- Người xóa ---"));
        List<User> adminList = productFrame.loadAllAccountAdim();
        for (int i = 0; i < adminList.size(); i++) {
            deletedByComboBox.addItem(new ComboItem(
                    String.valueOf(adminList.get(i).getId()),
                    String.valueOf(adminList.get(i).getUsername())
            ));
        }
        panelRow1.add(deletedByComboBox);


        // lọc theo ngày xóa
        JLabel startDate = new JLabel("Ngày xóa: ");
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

        // chọn trạng thái khôi phục hàng loạt
        statusChangeMultiComboBox = new JComboBox<>();
        statusChangeMultiComboBox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusChangeMultiComboBox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusChangeMultiComboBox.addItem(new ComboItem("active", "Hoạt động"));
        panelRow2.add(statusChangeMultiComboBox);

        // nút khôi phục
        restoreButton = new JButton("Khôi phục");
        restoreButton.setBackground(Color.decode("#007BFF"));
        restoreButton.setForeground(Color.WHITE);
        panelRow2.add(restoreButton);


        // Nút Xóa vĩnh viễn
        deletePermanentlyButton = new JButton("Xóa vĩnh viễn");
        deletePermanentlyButton.setBackground(Color.decode("#FA3E3E"));
        deletePermanentlyButton.setForeground(Color.WHITE);
        panelRow2.add(deletePermanentlyButton);

        topPanel.add(JTitle);
        topPanel.add(panelRow1);
        topPanel.add(panelRow2);
        add(topPanel, BorderLayout.NORTH);

        // Tạo bảng
        String[] columnNames = {"","id", "Tên sản phẩm", "Danh mục","Nhà cung cấp","Giá nhập", "Giá bán","Đơn vị",  "Trạng thái", "Xóa bởi", "Barcode","HSD"};
        productTrashModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tất cả ô đều không cho sửa
            }
        };

        loadAllProductTrash();
        table = new JTable(productTrashModel);
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
        totalPage = (int) Math.ceil((double) productController.getTotalRecordTrash() / limit);
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

    public ProductTrashFrame(ProductFrame productFrame){
        this.productFrame = productFrame;
        productController = new ProductController();
        view();

        // nút trang đầu
        firstPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentPage>1){
                    currentPage = 1;
                    filterProductTrash();
                    page.setText(currentPage + " / " + totalPage);
                }
            }
        });

        // nút trang trước
        preButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                filterProductTrash();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang tiếp theo
        nextButton.addActionListener(e -> {
            if (currentPage < totalPage) {
                currentPage++;
                filterProductTrash();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang cuối
        lastPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentPage < totalPage){
                    currentPage = totalPage;
                    filterProductTrash();
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
                filterProductTrash();
            }
        });


        // button reset filter
        clearFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                categoryComboBox.setSelectedIndex(0);
                supplierComboBox.setSelectedIndex(0);
                deletedByComboBox.setSelectedIndex(0);
                fromDateChooser.setDate(null);
                toDateChooser.setDate(null);
                searchField.setText("");
                arrangeComboBox.setSelectedItem(0);
                idProductSelect = 0;
                loadAllProductTrash();
            }
        });



        // Thêm ActionListener cho createdByComboBox
        deletedByComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProductTrash();
            }
        });


        // Thêm ActionListener cho categoryComboBox
        categoryComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProductTrash();
            }
        });


        // Thêm ActionListener cho supplierComboBox
        supplierComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProductTrash();
            }
        });

        // Thêm sự kiện cho fromDateChooser (bắt đầu từ 00:00:00)
        fromDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterProductTrash();
            }
        });

        // Thêm sự kiện cho toDateChooser (kết thúc lúc 23:59:59)
        toDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterProductTrash();
            }
        });

        // Thêm ActionListener cho arrangeComboBox
        arrangeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterProductTrash();
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

        restoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    boolean hasSelectedRow = false;

                    for (int i = 0; i < productTrashModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) productTrashModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }

                    if(hasSelectedRow){
                        ComboItem item = (ComboItem) statusChangeMultiComboBox.getSelectedItem();
                        String statusItem = item.getValue();
                        if(statusItem != ""){
                            for (int i = 0; i < productTrashModel.getRowCount(); i++) {
                                Boolean isChecked = (Boolean) productTrashModel.getValueAt(i, 0);
                                if (isChecked != null && isChecked && !statusItem.equals(productTrashModel.getValueAt(i,11))) {
                                    int productId = (Integer) productTrashModel.getValueAt(i, 1);
                                    productController.updateMultiProduct(productId, statusItem, userId);
                                }
                            }
                            filterProductTrash();
                            table.clearSelection();
                            if(productFrame != null){
                                productFrame.filterProduct();
                            }
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

        deletePermanentlyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    int hasSelectedRow = 0;
                    for (int i = 0; i < productTrashModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) productTrashModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow++;
                        }
                    }

                    if(hasSelectedRow > 0){
                        // Hỏi xác nhận
                        int result = JOptionPane.showConfirmDialog(null, "Bạn có chắc chắn muốn xóa vĩnh viễn " + hasSelectedRow + " sản phẩm đã chọn không?\nNếu xác nhận xóa thì sẽ không thể tìm lại!", "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (result == JOptionPane.YES_OPTION) {
                            for (int i = 0; i < productTrashModel.getRowCount(); i++) {
                                Boolean isChecked = (Boolean) productTrashModel.getValueAt(i, 0);
                                if (isChecked != null && isChecked) {
                                    int idProduct = (Integer) productTrashModel.getValueAt(i, 1);
                                    productController.deletePermanentlyProduct(idProduct);
                                }
                            }
                            filterProductTrash();
                            table.clearSelection();
                            JOptionPane.showMessageDialog(null, "Xóa vĩnh viễn thành công!");
                            idProductSelect = 0;
                        }
                    }else{
                        JOptionPane.showMessageDialog(null, "Hãy chọn ít nhất 1 danh mục!");
                    }
                }catch (SQLException ex){
                    JOptionPane.showMessageDialog(null,ex.getMessage());
                }
            }
        });
    }

    private void checkAllButton(){
        isAllChecked = !isAllChecked; // Đảo ngược trạng thái
        boolean checkedState = isAllChecked; // Lấy trạng thái mới
        for (int i = 0; i < productTrashModel.getRowCount(); i++) {
            productTrashModel.setValueAt(checkedState, i, 0); // Cập nhật cột 0 (checkbox)
        }
        if(!isAllChecked){
            checkAllButton.setText("Chọn tất cả");
        }else{
            checkAllButton.setText("Bỏ chọn tất cả");
        }
    }

    public void loadAllProductTrash(){
        try{
            ArrayList<Product> products = productController.findProductPageTrash(currentPage,limit);
            productTrashModel.setRowCount(0);
            for (Product product : products) {
                productTrashModel.addRow(new Object[]{false,
                        product.getId(),
                        product.getName(),
                        product.getCategoryName(),
                        product.getSupplierName(),
                        product.getOriginalPrice(),
                        product.getSellingPrice(),
                        product.getUnit(),
                        product.getStatus(),
                        product.getUpdatedAt() + " - " + product.getCreatedByUsername(),
                        product.getBarcode(),
                        product.getUseByDate()
                });
            }
            totalPage = (int) Math.ceil((double) productController.getTotalRecordTrash() / limit);
            if(totalPage <= 1){
                totalPage = 1;
            }
            // sau khi xóa nếu trang hiện tại > tổng số trang thig load lại
            if(totalPage<currentPage){
                currentPage = totalPage;
                loadAllProductTrash();
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }


    public void filterProductTrash(){
        ComboItem selectedCategoryItem = (ComboItem) categoryComboBox.getSelectedItem();
        ComboItem selectedSupplierItem = (ComboItem) supplierComboBox.getSelectedItem();
        ComboItem selectedUpdatedByItem = (ComboItem) deletedByComboBox.getSelectedItem();
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

        loadFilterProductTrash(searchText, selectedUpdatedByItem.getValue(), sqlFromDate, sqlToDate,selectedCategoryItem.getValue(),selectedSupplierItem.getValue(),selectedRangeItem.getValue());
    }

    private void loadFilterProductTrash(String searchText, String filterDeletedBy, Timestamp fromDate, Timestamp toDate, String filterCategory, String filterSupplier, String filterArrange) {
        try{
            System.out.println(filterDeletedBy);
            ArrayList<Product> products = productController.filteredProductsTrash(searchText,filterDeletedBy, fromDate, toDate, filterCategory, filterSupplier, filterArrange,currentPage,limit);
            productTrashModel.setRowCount(0);
            for (Product product : products) {
                productTrashModel.addRow(new Object[]{false,
                        product.getId(),
                        product.getName(),
                        product.getCategoryName(),
                        product.getSupplierName(),
                        product.getOriginalPrice(),
                        product.getSellingPrice(),
                        product.getUnit(),
                        product.getStatus(),
                        product.getUpdatedAt() + " - " + product.getCreatedByUsername(),
                        product.getBarcode(),
                        product.getUseByDate()
                });
            }

            totalPage = (int) Math.ceil((double) productController.getTotalFilterRecordTrash(searchText,filterDeletedBy, fromDate, toDate, filterCategory, filterSupplier) / limit);
            if(totalPage <= 1){
                totalPage = 1;
            }
            // sau khi xóa nếu trang hiện tại > tổng số trang thig load lại
            if(totalPage<currentPage){
                currentPage = totalPage;
                filterProductTrash();
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }
}
