package Views.Sales;

import Controllers.InvoiceController;
import Models.Sales.Invoice;
import Models.Sales.InvoiceItem;
import Utils.DateRenderer;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class InvoiceSearchFrame extends JFrame {
    private JTable tblInvoices;
    private DefaultTableModel tblModel;

    private JDateChooser dateChooserFrom;
    private JDateChooser dateChooserTo;
    private JTextField txtKeyword;
    private JButton btnSearch;

    private InvoiceController invoiceController;

    public InvoiceSearchFrame() {
        invoiceController = new InvoiceController();

        setTitle("Tra cứu hóa đơn");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Từ khóa (Khách hàng/SDT):"));
        txtKeyword = new JTextField(15);
        searchPanel.add(txtKeyword);

        searchPanel.add(new JLabel("Từ ngày (dd/MM/yyyy):"));
        dateChooserFrom = new JDateChooser();
        dateChooserFrom.setDateFormatString("dd/MM/yyyy");
        searchPanel.add(dateChooserFrom);

        searchPanel.add(new JLabel("Đến ngày (dd/MM/yyyy):"));
        dateChooserTo = new JDateChooser();
        dateChooserTo.setDateFormatString("dd/MM/yyyy");
        searchPanel.add(dateChooserTo);


        btnSearch = new JButton("Tìm");
        searchPanel.add(btnSearch);

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
        tblInvoices.getColumnModel().getColumn(1).setCellRenderer(new DateRenderer());

        add(new JScrollPane(tblInvoices), BorderLayout.CENTER);

        btnSearch.addActionListener((ActionEvent e) -> {
            onSearch();
        });

        tblInvoices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblInvoices.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = tblInvoices.convertRowIndexToModel(selectedRow);
                    int invoiceId = (int) tblModel.getValueAt(modelRow, 0);
                    showInvoiceDetail(invoiceId);
                }
            }
        });
    }

    private void onSearch() {
        String keyword = txtKeyword.getText().trim();
        Date fromDate = dateChooserFrom.getDate();
        Date toDate = dateChooserTo.getDate();
        if (fromDate != null && toDate != null && toDate.before(fromDate)) {
            JOptionPane.showMessageDialog(this,
                    "Ngày kết thúc không được nhỏ hơn ngày bắt đầu.",
                    "Lỗi chọn ngày",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (fromDate == null) {
            fromDate = new Date(0);
        }
        if (toDate == null) {
            toDate = new Date();
        }
        String formattedFrom = sdf.format(fromDate);
        String formattedTo = sdf.format(toDate);
        List<Invoice> results = invoiceController.searchInvoices(keyword, formattedFrom, formattedTo);
        updateTable(results);
    }

    public void updateTable(List<Invoice> invoices) {
        tblModel.setRowCount(0);
        for (Invoice inv : invoices) {
            tblModel.addRow(new Object[]{
                    inv.getId(),
                    inv.getCreatedAt(),
                    inv.getCustomerName(),
                    inv.getCustomerPhone(),
                    inv.getStaffName(),
                    inv.getFinalAmount(),
                    inv.getPaymentMethod(),
                    inv.getStatus()
            });
        }
    }

    private void showInvoiceDetail(int invoiceId) {
        List<InvoiceItem> items = invoiceController.getInvoiceItems(invoiceId);
        InvoiceDetailDialog detailDialog = new InvoiceDetailDialog(this, items, invoiceId);
        detailDialog.setVisible(true);
        tblInvoices.clearSelection();
    }

}
