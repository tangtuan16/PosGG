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

public class SupplierTrashFrame extends JFrame {
    private SupplierFrame supplierFrame;
    private JComboBox<ComboItem> createdByComboBox, statusChangeMultiComboBox;
    private JDateChooser fromDateChooser, toDateChooser;
    private JTextField searchField;
    private JButton clearFilterButton, searchButton, checkAllButton, deletePermanentlyButton, restoreButton;
    private DefaultTableModel supplierTrashModel;
    private JTable table;
    private JButton preButton, nextButton, firstPageButton, lastPageButton;
    private int totalPage;
    private int currentPage = 1;
    private int limit = 20;
    private JLabel page = new JLabel();
    private SupplierController supplierController;
    private boolean isAllChecked = false; // Biến để theo dõi trạng thái
    private int idSupplierSelect;
    public int userId = Session.getInstance().getUser().getId();


    public void view(){
        setTitle("Thùng rác nhà cung cấp");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel title = new JLabel("Thùng rác nhà cung cấp");
        title.setFont(new Font("Arial", Font.BOLD,30));
        titlePanel.add(title);

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel,BoxLayout.Y_AXIS));

        JPanel filterRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterRow1.add(new JLabel("Bộ lọc"));

        createdByComboBox = new JComboBox<>();
        createdByComboBox.addItem(new ComboItem("", "--- Người xóa ---"));
        List<User> adminList = loadAllAccountAdim();
        for (User user : adminList) {
            createdByComboBox.addItem(new ComboItem(String.valueOf(user.getId()), user.getUsername()));
        }
        filterRow1.add(createdByComboBox);

        filterRow1.add(new JLabel("Từ:"));
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

        filterPanel.add(titlePanel);
        filterPanel.add(filterRow1);
        filterPanel.add(filterRow2);

        add(filterPanel, BorderLayout.NORTH);


        // Tạo bảng
        String[] columnNames = {"","id", "Tên danh mục", "Mô tả", "Trạng thái", "Danh mục cha", "Tạo bởi", "Cập nhật bởi"};
        supplierTrashModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tất cả ô đều không cho sửa
            }
        };

        loadAllSuppliersTrash();
        table = new JTable(supplierTrashModel);
        // ẩn cột id
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setWidth(0);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setMaxWidth(30); // Cột checkbox

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);


        // phân trang
        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.CENTER)); // căn giữa

        firstPageButton = new JButton("Trang đầu");
        firstPageButton.setPreferredSize(new Dimension(120,25));
        preButton = new JButton("Trang trước");
        preButton.setPreferredSize(new Dimension(120,25));
        totalPage = getTotalPage();
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
    }

    public SupplierTrashFrame(SupplierFrame supplierFrame){
        this.supplierFrame = supplierFrame;
        supplierController = new SupplierController();
        view();

        // nút trang đầu
        firstPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterSupplierTrash();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang trước
        preButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                filterSupplierTrash();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang tiếp theo
        nextButton.addActionListener(e -> {
            if (currentPage < totalPage) {
                currentPage++;
                filterSupplierTrash();
                page.setText(currentPage + " / " + totalPage);
            }
        });

        // nút trang cuối
        lastPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = totalPage;
                filterSupplierTrash();
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
                filterSupplierTrash();
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
                idSupplierSelect = 0;
                loadAllSuppliersTrash();
            }
        });

        // Thêm ActionListener cho createdByComboBox
        createdByComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                filterSupplierTrash();
            }
        });

        // Thêm sự kiện cho fromDateChooser (bắt đầu từ 00:00:00)
        fromDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterSupplierTrash();
            }
        });

        // Thêm sự kiện cho toDateChooser (kết thúc lúc 23:59:59)
        toDateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentPage = 1;
                filterSupplierTrash();
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

        restoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    boolean hasSelectedRow = false;

                    for (int i = 0; i < supplierTrashModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) supplierTrashModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow = true;
                            break;
                        }
                    }

                    if(hasSelectedRow){
                        ComboItem statusRestoreComboBox = (ComboItem) statusChangeMultiComboBox.getSelectedItem();
                        String statusRestore = statusRestoreComboBox.getValue();
                        if(statusRestore != ""){
                            for (int i = 0; i < supplierTrashModel.getRowCount(); i++) {
                                Boolean isChecked = (Boolean) supplierTrashModel.getValueAt(i, 0);
                                if (isChecked != null && isChecked) {
                                    int supplierId = (Integer) supplierTrashModel.getValueAt(i, 1);
                                    supplierController.updateMultiSupplier(supplierId, statusRestore,userId);
                                }
                            }
                            filterSupplierTrash();
                            if(supplierFrame != null){
                                supplierFrame.filterSupplier();
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
                    int hasSelectedRow = 0;
                    for (int i = 0; i < supplierTrashModel.getRowCount(); i++) {
                        Boolean isChecked = (Boolean) supplierTrashModel.getValueAt(i, 0);
                        if (isChecked != null && isChecked) {
                            hasSelectedRow++;
                        }
                    }

                    if(hasSelectedRow > 0){
                        // Hỏi xác nhận
                        int result = JOptionPane.showConfirmDialog(null, "Bạn có chắc chắn muốn xóa vĩnh viễn " + hasSelectedRow + " nhà cung cấp đã chọn không?\nNếu xác nhận xóa thì sẽ không thể khôi phục!", "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (result == JOptionPane.YES_OPTION) {
                            for (int i = 0; i < supplierTrashModel.getRowCount(); i++) {
                                Boolean isChecked = (Boolean) supplierTrashModel.getValueAt(i, 0);
                                if (isChecked != null && isChecked) {
                                    int supplierId = (Integer) supplierTrashModel.getValueAt(i, 1);
                                    supplierController.deletePermanentlySupplier(supplierId);
                                }
                            }
                            filterSupplierTrash();
                            table.clearSelection();
                            JOptionPane.showMessageDialog(null, "Xóa vĩnh viễn thành công!");
                            idSupplierSelect = 0;
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

    public void loadAllSuppliersTrash() {
        try{
            ArrayList<Supplier> suppliers = supplierController.findAllSupplierTrash(currentPage, limit);
            supplierTrashModel.setRowCount(0);
            for (Supplier supplier : suppliers) {
                supplierTrashModel.addRow(new Object[]{false,supplier.getId(),
                        supplier.getName(),
                        supplier.getPhone(),
                        supplier.getEmail(),
                        supplier.getAddress(),
                        supplier.getStatus(),
                        supplier.getCreatedAt() + " - " + supplier.getCreatedByName(),
                        supplier.getUpdatedAt() + " - " + supplier.getUpdatedByName()
                });
            }
            totalPage = getTotalPage();
            if(totalPage<=1){
                totalPage = 1;
            }
            // sau khi xóa nếu trang hiện tại > tổng số trang thig load lại
            if(totalPage<currentPage){
                currentPage = totalPage;
                loadAllSuppliersTrash();
            }
            page.setText(currentPage + " / " + totalPage);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private List<User> loadAllAccountAdim(){
        UserController userController = new UserController();
        return userController.getAllUsers();
    }

    private int getTotalPage(){
        try{
            return (int) Math.ceil((double) supplierController.getTotalRecordTrash() / limit);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,e.getMessage());
        }
        return 0;
    }

    private void checkAllButton(){
        isAllChecked = !isAllChecked; // Đảo ngược trạng thái
        boolean checkedState = isAllChecked; // Lấy trạng thái mới
        for (int i = 0; i < supplierTrashModel.getRowCount(); i++) {
            supplierTrashModel.setValueAt(checkedState, i, 0); // Cập nhật cột 0 (checkbox)
        }
        if(!isAllChecked){
            checkAllButton.setText("Chọn tất cả");
        }else{
            checkAllButton.setText("Bỏ chọn tất cả");
        }
    }

    private void loadFilterSuppliersTrash(String searchText, String filterCreatedBy, Timestamp fromDate, Timestamp toDate) {
        try{
            ArrayList<Supplier> suppliers = supplierController.filteredSuppliersPageTrash(searchText,filterCreatedBy, fromDate, toDate, currentPage, limit);
            supplierTrashModel.setRowCount(0);
            for (Supplier supplier : suppliers) {
                supplierTrashModel.addRow(new Object[]{false,supplier.getId(),
                        supplier.getName(),
                        supplier.getPhone(),
                        supplier.getEmail(),
                        supplier.getAddress(),
                        supplier.getStatus(),
                        supplier.getCreatedAt() + " - " + supplier.getCreatedByName(),
                        supplier.getUpdatedAt() + " - " + supplier.getUpdatedByName()
                });
            }
            totalPage = (int) Math.ceil((double) supplierController.getTotalRecordFilterTrash(searchText,filterCreatedBy, fromDate, toDate) / limit);
            if(totalPage <= 1){
                totalPage = 1;
            }
            // sau khi xóa nếu trang hiện tại > tổng số trang thig load lại
            if(totalPage<currentPage){
                currentPage = totalPage;
                filterSupplierTrash();
            }
            page.setText(currentPage + " / " + totalPage);
        }catch (SQLException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void filterSupplierTrash(){
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

      loadFilterSuppliersTrash(searchText, selectedCreatedByItem.getValue(), sqlFromDate, sqlToDate);
    }
}
