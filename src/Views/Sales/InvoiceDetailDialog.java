package Views.Sales;

import Models.Customer;
import Models.Sales.InvoiceItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class InvoiceDetailDialog extends JDialog {
    private JTable tblItems;
    private DefaultTableModel tblModel;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    public InvoiceDetailDialog(Frame owner, List<InvoiceItem> items, int invoiceId, BigDecimal customerDiscountPercent, Customer customer) {
        super(owner, "Chi tiết hóa đơn #" + invoiceId, true);
        setSize(650, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        JLabel lblCustomer = new JLabel("Khách hàng: " + customer == null ? customer.getName() : "Khách lẻ");
        lblCustomer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCustomer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblCustomer, BorderLayout.NORTH);

        String[] columns = {"Tên sản phẩm", "Số lượng", "Đơn giá (đ)", "Thành tiền (đ)"};
        tblModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblItems = new JTable(tblModel);
        tblItems.setRowHeight(24);
        tblItems.getTableHeader().setReorderingAllowed(false);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (InvoiceItem item : items) {
            BigDecimal unitPrice = item.getUnitPrice();
            BigDecimal totalPrice = item.getTotalPrice();
            totalAmount = totalAmount.add(totalPrice);

            tblModel.addRow(new Object[]{
                    item.getProductName(),
                    item.getQuantity(),
                    currencyFormat.format(unitPrice),
                    currencyFormat.format(totalPrice)
            });
        }

        BigDecimal discountAmount = totalAmount.multiply(customerDiscountPercent).divide(BigDecimal.valueOf(100));
        BigDecimal finalTotal = totalAmount.subtract(discountAmount);

        JScrollPane scrollPane = new JScrollPane(tblItems);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new GridLayout(2, 1));

        JPanel panelTotal = new JPanel(new GridLayout(4, 1));
        JLabel lblOriginalTotal = new JLabel("Tổng tiền: " + currencyFormat.format(totalAmount) + " đ");
        JLabel lblDiscount = new JLabel("Giảm giá: " + customerDiscountPercent + "% (-" + currencyFormat.format(discountAmount) + " đ)");
        JLabel lblFinalTotal = new JLabel("Thành tiền sau giảm: " + currencyFormat.format(finalTotal) + " đ");

        lblOriginalTotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDiscount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));

        panelTotal.add(lblOriginalTotal);
        panelTotal.add(lblDiscount);
        panelTotal.add(lblFinalTotal);

        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        panelButton.add(btnClose);

        panelBottom.add(panelTotal);
        panelBottom.add(panelButton);
        panelBottom.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        add(panelBottom, BorderLayout.SOUTH);
    }
}
