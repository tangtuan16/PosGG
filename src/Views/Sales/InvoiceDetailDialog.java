package Views.Sales;

import Models.Sales.InvoiceItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InvoiceDetailDialog extends JDialog {
    private JTable tblItems;
    private DefaultTableModel tblModel;

    public InvoiceDetailDialog(Frame owner, List<InvoiceItem> items, int invoiceId) {
        super(owner, "Chi tiết hóa đơn #" + invoiceId, true);
        setSize(600, 400);
        setLocationRelativeTo(owner);

        String[] columns = {"Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
        tblModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItems = new JTable(tblModel);

        for (InvoiceItem item : items) {
            tblModel.addRow(new Object[]{
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()
            });
        }

        add(new JScrollPane(tblItems), BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());

        JPanel panelBottom = new JPanel();
        panelBottom.add(btnClose);

        add(panelBottom, BorderLayout.SOUTH);
    }
}
