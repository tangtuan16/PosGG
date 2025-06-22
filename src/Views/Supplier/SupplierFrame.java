package Views.Supplier;

import Controllers.SupplierController;
import Controllers.UserController;
import Models.Session;
import Models.Supplier;
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

public class SupplierFrame extends JFrame {
    public int userId = Session.getInstance().getUser().getId();
    private JTable table;
    private DefaultTableModel supplierModel;
    private JTextField searchField;
    private JComboBox<ComboItem> statusComboBox, createdByComboBox, statusChangeMultiComboBox;
    private JDateChooser fromDateChooser, toDateChooser;
    private JButton createButton, checkAllButton, editButton, deleteButton, trashButton, changeMultiButton, searchButton, clearFilterButton;
    private JButton preButton, nextButton, firstPageButton, lastPageButton;
    private boolean isAllChecked = false; // Biến để theo dõi trạng thái
    private SupplierController supplierController;
    private UserController userController;
    private int idSupplierSelect;
    private SupplierTrashFrame trashFrame;
    private EditSupplierFrame editFrame;
    private int currentPage = 1;
    private int limit = 20;
    int totalPage;
    private JLabel page = new JLabel();

    public void view(){
        // Thiết lập JFrame
        setTitle("Quản lý Nhà cung cấp");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel JTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLable = new JLabel("Quản lý nhà cung cấp");
        titleLable.setFont(new Font("Arial", Font.BOLD, 30));
        JTitle.add(titleLable);

        // Panel bộ lọc (dòng 1)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Bộ lọc
        JLabel filterLabel = new JLabel("Bộ lọc");
        filterPanel.add(filterLabel);

        // JComboBox với giá trị tùy chỉnh
        statusComboBox = new JComboBox<>();
        statusComboBox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusComboBox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusComboBox.addItem(new ComboItem("active", "Hoạt động"));
        filterPanel.add(statusComboBox);

        createdByComboBox = new JComboBox<>();
        createdByComboBox.addItem(new ComboItem("", "--- Người tạo ---"));
        List<User> adminList = loadAllAccountAdim();
        for (int i = 0; i < adminList.size(); i++) {
            createdByComboBox.addItem(new ComboItem(
                    String.valueOf(adminList.get(i).getId()),
                    String.valueOf(adminList.get(i).getUsername())
            ));
        }
        filterPanel.add(createdByComboBox);

        JLabel startDate = new JLabel("Từ: ");
        filterPanel.add(startDate);

        fromDateChooser = new JDateChooser();
        fromDateChooser.setDateFormatString("dd/MM/yyyy");
        fromDateChooser.setPreferredSize(new Dimension(100, 25));
        filterPanel.add(fromDateChooser);
        // Vô hiệu hóa nhập tay
        JTextFieldDateEditor editorFrom = (JTextFieldDateEditor) fromDateChooser.getDateEditor();
        editorFrom.setEditable(false);

        JLabel endDate = new JLabel("đến: ");
        filterPanel.add(endDate);

        toDateChooser = new JDateChooser();
        toDateChooser.setDateFormatString("dd/MM/yyyy");
        toDateChooser.setPreferredSize(new Dimension(100, 25));
        filterPanel.add(toDateChooser);
        // Vô hiệu hóa nhập tay
        JTextFieldDateEditor editorTo = (JTextFieldDateEditor) toDateChooser.getDateEditor();
        editorTo.setEditable(false);

        // Nút Tìm kiếm và ô tìm kiếm
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(150,25));
        filterPanel.add(searchField);
        searchButton = new JButton("Tìm kiếm");
        filterPanel.add(searchButton);

        // Nút Xóa bộ lọc
        clearFilterButton = new JButton("Xóa bộ lọc");
        clearFilterButton.setBackground(new Color(255, 204, 204));
        clearFilterButton.setForeground(Color.RED);
        filterPanel.add(clearFilterButton);

        // Panel chứa các nút chức năng (dòng 2)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Nút checkall
        checkAllButton = new JButton("Chọn tất cả");
        checkAllButton.setBackground(Color.decode("#F07171"));
        checkAllButton.setPreferredSize(new Dimension(150, 25));
        checkAllButton.setForeground(Color.WHITE);
        buttonPanel.add(checkAllButton);

        // chọn trạng thái caaph nhật hàng loạt
        statusChangeMultiComboBox = new JComboBox<>();
        statusChangeMultiComboBox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusChangeMultiComboBox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusChangeMultiComboBox.addItem(new ComboItem("active", "Hoạt động"));
        buttonPanel.add(statusChangeMultiComboBox);

        // nút cập nhật hàng loạt
        changeMultiButton = new JButton("Cập nhật hàng loạt");
        changeMultiButton.setPreferredSize(new Dimension(150,25));
        buttonPanel.add(changeMultiButton);

        // Nút thêm nhà cung cấp
        createButton = new JButton("+ Thêm nhà cung cấp");
        createButton.setBackground(Color.decode("#007BFF"));
        createButton.setForeground(Color.WHITE);
        buttonPanel.add(createButton);

        // Nút Sửa
        editButton = new JButton("Sửa");
        editButton.setBackground(Color.decode("#007BFF"));
        editButton.setForeground(Color.WHITE);
        buttonPanel.add(editButton);

        // Nút Xóa
        deleteButton = new JButton("Xóa");
        deleteButton.setBackground(Color.decode("#FA3E3E"));
        deleteButton.setForeground(Color.WHITE);
        buttonPanel.add(deleteButton);

        // Nút Thùng rác
        trashButton = new JButton("Thùng rác");
        trashButton.setBackground(Color.decode("#C2C2C2"));
        buttonPanel.add(trashButton);

        // Thêm cả hai panel vào topPanel và topPanel vào JFrame
        topPanel.add(JTitle);
        topPanel.add(filterPanel);
        topPanel.add(buttonPanel);
        add(topPanel, BorderLayout.NORTH);

        // Tạo bảng
        String[] columnNames = {"","id", "Tên nhà cung cấp", "Số điện thoại", "Email", "Địa chỉ", "trạng thái","Ngày thêm", "Ngày cập nhật"};
        supplierModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // ngăn chặn chỉnh sửa ở table
            }
        };

        loadAllSuppliers();
        table = new JTable(supplierModel);
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
        totalPage = (int) Math.ceil((double) supplierController.getTotalRecord() / limit);
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

    public SupplierFrame(){
        supplierController = new SupplierController();
        userController = new UserController();

        view();

        // nút trang đầu
        firstPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentPage>1){
                    currentPage = 1;
                    filterSupplier();
                    page.setText(currentPage + " / " + totalPage);
                }
            }
        });

        // nút trang trước
        preButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                filterSupplier();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang tiếp theo
        nextButton.addActionListener(e -> {
            if (currentPage < totalPage) {
                currentPage++;
                filterSupplier();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang cuối
        lastPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentPage < totalPage){
                    currentPage = totalPage;
                    filterSupplier();
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
                filterSupplier();
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
                idSupplierSelect = 0;
                loadAllSuppliers();
            }
        });

        // Thêm ActionListener cho statusComboBox
        statusComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterSupplier();
            }
        });

        // Thêm ActionListener cho createdByComboBox
        createdByComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterSupplier();
            }
        });

        // Thêm sự kiện cho fromDateChooser (bắt đầu từ 00:00:00)
        fromDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterSupplier();
            }
        });

        // Thêm sự kiện cho toDateChooser (kết thúc lúc 23:59:59)
        toDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterSupplier();
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
                    idSupplierSelect = (int) table.getValueAt(selectedRow, 1);
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

                    for (int i = 0; i < supplierModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) supplierModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }
                    if(hasSelectedRow){
                        ComboItem item = (ComboItem) statusChangeMultiComboBox.getSelectedItem();
                        String statusItem = item.getValue();
                        if(statusItem != ""){
                            for (int i = 0; i < supplierModel.getRowCount(); i++) {
                                Boolean isChecked = (Boolean) supplierModel.getValueAt(i, 0);
                                if (isChecked != null && isChecked && !statusItem.equals(supplierModel.getValueAt(i,6))) {
                                    int supplierId = (Integer) supplierModel.getValueAt(i, 1);
                                    supplierController.updateMultiSupplier(supplierId, statusItem, userId);
                                }
                            }
                            filterSupplier();
                            table.clearSelection();
                            JOptionPane.showMessageDialog(null, "Thay đổi hàng loạt thành công!");
                            idSupplierSelect = 0;
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
                    JOptionPane.showMessageDialog(null,ex.getMessage());
                }
            }
        });

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddSupplierFrame addFrame = new AddSupplierFrame(SupplierFrame.this);
                addFrame.setVisible(true);
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int hasSelectedRow = 0;

                for (int i = 0; i < supplierModel.getRowCount(); i++) {
                    Boolean isChecked = (Boolean) supplierModel.getValueAt(i, 0);
                    if (isChecked != null && isChecked) {
                        hasSelectedRow++;
                        if(hasSelectedRow > 1) break;
                        idSupplierSelect = (int) supplierModel.getValueAt(i, 1);
                    }
                }

                if(hasSelectedRow == 1){
                    if (editFrame != null && editFrame.isDisplayable()) {
                        editFrame.dispose(); // Đóng frame cũ nếu còn tồn tại
                    }
                    editFrame = new EditSupplierFrame(SupplierFrame.this, idSupplierSelect);
                    editFrame.setVisible(true);
                    table.clearSelection();
                } else if(hasSelectedRow==0) {
                    JOptionPane.showMessageDialog(null, "Hãy chọn 1 nhà cung cấp!");
                }else{
                    JOptionPane.showMessageDialog(null, "Chỉ chọn 1 nhà cung cấp!");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    boolean hasSelectedRow = false;

                    for (int i = 0; i < supplierModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) supplierModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }

                    if(hasSelectedRow){
                        for (int i = 0; i < supplierModel.getRowCount(); i++) {
                            Boolean isChecked = (Boolean) supplierModel.getValueAt(i, 0);
                            if (isChecked != null && isChecked) {
                                int supplierId = (Integer) supplierModel.getValueAt(i, 1);
                                supplierController.deleteSupplier(supplierId, userId);
                            }
                        }
                        filterSupplier();
                        if (trashFrame != null) {
                            trashFrame.loadAllSuppliersTrash();
                        }
                        table.clearSelection();
                        JOptionPane.showMessageDialog(null, "Xóa thành công!");
                        idSupplierSelect = 0;
                        isAllChecked = false;
                        checkAllButton.setText("Chọn tất cả");
                    }else{
                        JOptionPane.showMessageDialog(null, "Hãy chọn ít nhất 1 danh mục!");
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
                    trashFrame = new SupplierTrashFrame(SupplierFrame.this);
                }
                trashFrame.setVisible(true);
                trashFrame.toFront();
            }
        });
    }

    public void loadAllSuppliers() {
        try{
            ArrayList<Supplier> suppliers = supplierController.findSupplierPage(currentPage,limit);
            supplierModel.setRowCount(0);
            for (Supplier supplier : suppliers) {
                supplierModel.addRow(new Object[]{false,supplier.getId(),
                        supplier.getName(),
                        supplier.getPhone(),
                        supplier.getEmail(),
                        supplier.getAddress(),
                        supplier.getStatus(),
                        supplier.getCreatedAt() + " - " + supplier.getCreatedByName(),
                        supplier.getUpdatedAt() + " - " + supplier.getUpdatedByName()
                });
            }
            totalPage = (int) Math.ceil((double) supplierController.getTotalRecord() / limit);
            if(totalPage <= 1){
                totalPage = 1;
            }
            // sau khi xóa nếu trang hiện tại > tổng số trang thig load lại
            if(totalPage<currentPage){
                currentPage = totalPage;
                loadAllSuppliers();
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }

    private void checkAllButton(){
        isAllChecked = !isAllChecked; // Đảo ngược trạng thái
        boolean checkedState = isAllChecked; // Lấy trạng thái mới
        for (int i = 0; i < supplierModel.getRowCount(); i++) {
            supplierModel.setValueAt(checkedState, i, 0); // Cập nhật cột 0 (checkbox)
        }
        if(!isAllChecked){
            checkAllButton.setText("Chọn tất cả");
        }else{
            checkAllButton.setText("Bỏ chọn tất cả");
        }
    }

    private void loadFilterSuppliers(String searchText, String filterStatus, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) {
        try{
            ArrayList<Supplier> suppliers = supplierController.filteredSuppliers(searchText,filterStatus,filterCreatedBy, fromDate, toDate,currentPage,limit);
            supplierModel.setRowCount(0);
            for (Supplier supplier : suppliers) {
                supplierModel.addRow(new Object[]{false,supplier.getId(),
                        supplier.getName(),
                        supplier.getPhone(),
                        supplier.getEmail(),
                        supplier.getAddress(),
                        supplier.getStatus(),
                        supplier.getCreatedAt() + " - " + supplier.getCreatedByName(),
                        supplier.getUpdatedAt() + " - " + supplier.getUpdatedByName()
                });
            }

            totalPage = (int) Math.ceil((double) supplierController.getTotalFilterRecord(searchText,filterStatus,filterCreatedBy, fromDate, toDate) / limit);
            if(totalPage<=1){
                totalPage = 1;
            }
            // sau khi xóa nếu trang hiện tại > tổng số trang thig load lại
            if(totalPage<currentPage){
                currentPage = totalPage;
                filterSupplier();
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }

    public void filterSupplier(){
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

        loadFilterSuppliers(searchText, selectedStatusItem.getValue(), selectedCreatedByItem.getValue(), sqlFromDate, sqlToDate);
    }

    private List<User> loadAllAccountAdim(){
        return userController.getAllUsers();
    }


}
