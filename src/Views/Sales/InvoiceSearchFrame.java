package Views.Sales;

import Controllers.InvoiceController;
import Models.Customer;
import Models.Sales.Invoice;
import Models.Sales.InvoiceItem;
import Utils.DateRenderer;
import Utils.FormatVND;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class InvoiceSearchFrame extends JFrame {
    private static final Color PRIMARY_BUTTON_COLOR = new Color(0, 123, 255);

    private JTable tblInvoices;
    private DefaultTableModel tblModel;
    private JDateChooser dateChooserFrom;
    private JDateChooser dateChooserTo;
    private JTextField txtKeyword;
    private JButton btnSearch;
    private JButton btnReset;
    private final InvoiceController invoiceController;

    public InvoiceSearchFrame() {
        invoiceController = new InvoiceController(this);

        setTitle("Tra cứu hóa đơn");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Từ khóa (ID/Khách hàng/SDT):"));
        txtKeyword = new JTextField(15);
        txtKeyword.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(txtKeyword);

        searchPanel.add(new JLabel("Từ ngày (dd/MM/yyyy):"));
        dateChooserFrom = new JDateChooser();
        dateChooserFrom.setDateFormatString("dd/MM/yyyy");
        dateChooserFrom.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(dateChooserFrom);

        searchPanel.add(new JLabel("Đến ngày (dd/MM/yyyy):"));
        dateChooserTo = new JDateChooser();
        dateChooserTo.setDateFormatString("dd/MM/yyyy");
        dateChooserTo.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(dateChooserTo);

        btnSearch = new JButton("Tìm");
        styleButton(btnSearch, PRIMARY_BUTTON_COLOR);
        searchPanel.add(btnSearch);

        btnReset = new JButton("Làm mới");
        styleButton(btnReset, new Color(40,167,69));
        searchPanel.add(btnReset);

        add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Ngày tạo", "Khách hàng", "SĐT", "Nhân viên", "Tổng tiền", "Phương thức", "Trạng thái"};
        tblModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblInvoices = new JTable(tblModel);
        tblInvoices.setAutoCreateRowSorter(true);
        tblInvoices.setRowHeight(25);
        tblInvoices.setFont(new Font("Arial", Font.PLAIN, 14));
        tblInvoices.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tblInvoices.getColumnModel().getColumn(1).setCellRenderer(new DateRenderer());

        add(new JScrollPane(tblInvoices), BorderLayout.CENTER);

        btnSearch.addActionListener(e -> {
            String keyword = txtKeyword.getText().trim();
            Date fromDate = dateChooserFrom.getDate();
            Date toDate = dateChooserTo.getDate();
            invoiceController.search(keyword, fromDate, toDate);
        });

        btnReset.addActionListener(e -> invoiceController.reset());

        tblInvoices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblInvoices.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = tblInvoices.convertRowIndexToModel(selectedRow);
                    int invoiceId = (int) tblModel.getValueAt(modelRow, 0);
                    invoiceController.showInvoiceDetail(invoiceId);
                }
            }
        });

        invoiceController.search("", null, null);
    }

    public void updateTable(List<Invoice> invoices) {
        tblModel.setRowCount(0);
        if (invoices != null) {
            for (Invoice inv : invoices) {
                tblModel.addRow(new Object[]{
                        inv.getId(),
                        inv.getCreatedAt(),
                        inv.getCustomerName() != null ? inv.getCustomerName() : "Khách lẻ",
                        inv.getCustomerPhone() != null ? inv.getCustomerPhone() : "Không có",
                        inv.getStaffName() != null ? inv.getStaffName() : "Không xác định",
                        FormatVND.format(inv.getFinalAmount()),
                        inv.getPaymentMethod() != null ? inv.getPaymentMethod() : "Không xác định",
                        inv.getStatus() != null ? inv.getStatus() : "Không xác định"
                });
            }
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }


    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showInvoiceDetail(int invoiceID, List<InvoiceItem> items, Customer customer, BigDecimal discount) {
        InvoiceDetailDialog detailDialog = new InvoiceDetailDialog(this, items, invoiceID, discount, customer);
        detailDialog.setVisible(true);
        tblInvoices.clearSelection();
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
    }
}