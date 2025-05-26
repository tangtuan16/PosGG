package Views.Customer;

import Services.CustomerServiceImpl;
import Services.Impl.CustomerService;
import Controllers.CustomerController;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;


public class CustomerFrame extends JFrame {
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> sortComboBox;
    private CustomerService customerService;
    private JButton addButton; // Thêm để Controller truy cập
    private JButton editButton;
    private JButton deleteButton;
    private JButton importExcelButton;
    private JButton exportExcelButton;
    private JButton firstPageButton;
    private JButton previousPageButton;
    private JButton nextPageButton;
    private JButton lastPageButton;
    private JLabel paginationLabel;
    private CustomerController controller; // Lưu tham chiếu đến controller

    public CustomerFrame() {
        // Khởi tạo CustomerService
        customerService = new CustomerServiceImpl();

        // Thiết lập JFrame
        setTitle("Quản lý Khách hàng");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 600);
        setLayout(new BorderLayout());

        // Tạo mô hình bảng
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Mã khách hàng");
        tableModel.addColumn("Tên khách hàng");
        tableModel.addColumn("Tổng đã dùng");
        tableModel.addColumn("Số điện thoại");
        tableModel.addColumn("Địa chỉ");

        // Khởi tạo bảng với mô hình
        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Font cho tiêu đề cột
        JTableHeader header = customerTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));

        // Font cho nội dung bảng
        customerTable.setRowHeight(25);

        // Căn giữa toàn bộ nội dung trong bảng
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < customerTable.getColumnCount(); i++) {
            customerTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Đặt bảng vào JScrollPane
        JScrollPane scrollPane = new JScrollPane(customerTable);

        // Tạo panel chứa các nút công cụ
        JPanel toolPanel = new JPanel(new BorderLayout());

        // Panel bên trái chứa các nút
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Thêm");
        editButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        importExcelButton = new JButton("Nhập Excel");
        exportExcelButton = new JButton("Xuất Excel");

        leftPanel.add(addButton);
        leftPanel.add(editButton);
        leftPanel.add(deleteButton);
        leftPanel.add(importExcelButton);
        leftPanel.add(exportExcelButton);

        // Panel bên phải chứa lọc, tìm kiếm, làm mới
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        String[] sortOptions = {"Mặc định", "Tên A → Z", "Tên Z → A", "Tổng Max → Min", "Tổng Min → Max"};
        sortComboBox = new JComboBox<>(sortOptions);
        searchField = new JTextField("Nhập nội dung tìm kiếm...", 20);
        JButton refreshButton = new JButton("Làm mới");

        // Đặt kích thước cho JComboBox
        sortComboBox.setPreferredSize(new Dimension(150, 25));
        // Đặt kích thước cho Search
        searchField.setPreferredSize(new Dimension(300, 30));

        // Thêm DocumentFilter để chặn dấu cách khi trống
        AbstractDocument document = (AbstractDocument) searchField.getDocument();
        document.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;

                // Kiểm tra nếu văn bản hiện tại trống và ký tự nhập là dấu cách
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                if (currentText.trim().isEmpty() && string.trim().isEmpty()) {
                    return; // Chặn dấu cách
                }

                super.insertString(fb, offset, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;

                // Kiểm tra nếu văn bản hiện tại trống và ký tự nhập là dấu cách
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                if (currentText.trim().isEmpty() && text.trim().isEmpty()) {
                    return; // Chặn dấu cách
                }

                super.replace(fb, offset, length, text, attrs);
            }
        });

        rightPanel.add(sortComboBox);
        rightPanel.add(searchField);
        rightPanel.add(refreshButton);

        // Thêm các panel vào toolPanel
        toolPanel.add(leftPanel, BorderLayout.WEST);
        toolPanel.add(rightPanel, BorderLayout.EAST);

        // Thêm panel phân trang
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        firstPageButton = new JButton("Trang đầu");
        previousPageButton = new JButton("Trang trước");
        paginationLabel = new JLabel("Trang 1/1");
        nextPageButton = new JButton("Trang sau");
        lastPageButton = new JButton("Trang cuối");

        paginationPanel.add(firstPageButton);
        paginationPanel.add(previousPageButton);
        paginationPanel.add(paginationLabel);
        paginationPanel.add(nextPageButton);
        paginationPanel.add(lastPageButton);

        // Thiết lập văn bản mặc định
        searchField.setText("Nhập nội dung tìm kiếm...");

        // Xóa văn bản khi focus vào ô tìm kiếm
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("Nhập nội dung tìm kiếm...")) {
                    searchField.setText("");
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText("Nhập nội dung tìm kiếm...");
                }
            }
        });

        // Thêm sự kiện cho làm mới
        refreshButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            sortComboBox.setSelectedIndex(0);
            searchField.setText("Nhập nội dung tìm kiếm...");
            controller.reset(); // Gọi reset trên controller hiện tại
        });

        // Khởi tạo Controller
        controller = new CustomerController(this, customerService);

        // Thêm các thành phần vào JFrame
        add(toolPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(paginationPanel, BorderLayout.SOUTH);

        // Hiển thị JFrame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Getter để Controller truy cập nút Thêm Sửa Xóa
    public JButton getAddButton() {
        return addButton;
    }
    public JButton getEditButton() {
        return editButton;
    }
    public JButton getDeleteButton() {
        return deleteButton;
    }
    public JTable getCustomerTable() {
        return customerTable;
    }
    public JTextField getSearchField() {
        return searchField;
    }
    public JComboBox<String> getSortComboBox() {
        return sortComboBox;
    }
    public JButton getFirstPageButton() {
        return firstPageButton;
    }

    public JButton getPreviousPageButton() {
        return previousPageButton;
    }

    public JButton getNextPageButton() {
        return nextPageButton;
    }

    public JButton getLastPageButton() {
        return lastPageButton;
    }

    public void updatePaginationInfo(int currentPage, int totalPages) {
        paginationLabel.setText("Trang " + currentPage + "/" + totalPages);
    }

    public JButton getImportExcelButton() {
        return importExcelButton;
    }

    public JButton getExportExcelButton() {
        return exportExcelButton;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomerFrame::new);
    }
}