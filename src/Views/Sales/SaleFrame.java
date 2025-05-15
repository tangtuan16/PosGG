package Views.Sales;

import Controllers.SaleController;
import Models.Product;
import Utils.ButtonEditor;
import Utils.ButtonRenderer;
import Utils.FormatVND;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.List;

public class SaleFrame extends JFrame {
    private JTable cartTable, productTable;
    private DefaultTableModel cartModel, productModel;
    private JTextField searchField;
    private JTextField phoneField;
    private JLabel customerNameLabel;
    private JLabel discountLabel;

    private SaleController saleController;

    public SaleFrame() {
        saleController = new SaleController();

        setTitle("Bán hàng");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupSearchBar();
        setupProductTable();
        setupCartTable();
        setupMainSplitPane();
        setupCheckoutButton();

        loadProducts("");
        setVisible(true);
    }

    private void setupSearchBar() {
        JPanel searchPanel = new JPanel();
        JLabel searchLabel = new JLabel("Tìm kiếm sản phẩm: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 18));
        searchPanel.add(searchLabel);

        searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Tìm kiếm");
        styleButton(searchButton, new Color(0, 123, 255));
        searchButton.setFont(new Font("Arial", Font.BOLD, 16));
        searchButton.addActionListener(e -> loadProducts(searchField.getText()));
        searchPanel.add(searchButton);
        JButton ivoicesButton = new JButton("Check hóa đơn");
        styleButton(ivoicesButton, new Color(0, 123, 255));
        ivoicesButton.setFont(new Font("Arial", Font.BOLD, 16));
        ivoicesButton.addActionListener(e -> new InvoiceSearchFrame().setVisible(true));
        searchPanel.add(ivoicesButton);
        add(searchPanel, BorderLayout.NORTH);
    }

    private void setupProductTable() {
        productModel = new DefaultTableModel(new String[]{"ID", "Tên", "Giá bán", "Đơn vị", "Số lượng tồn"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(productModel);
        productTable.setRowHeight(30);
        productTable.setFont(new Font("Arial", Font.PLAIN, 16));
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        productTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = productTable.getSelectedRow();
                Product product = saleController.getProductById((int) productModel.getValueAt(row, 0));
                if (product.getQuantity() <= 0) {
                    JOptionPane.showMessageDialog(productTable, "Sản phẩm đã hết hàng!");
                    return;
                }
                addToCart(product);
            }
        });
    }

    private void setupCartTable() {
        cartModel = new DefaultTableModel(new String[]{"ID", "Tên", "Đơn vị", "Số lượng", "Giảm", "Tăng", "Thành tiền", "Xóa"}, 0);

        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(30);
        cartTable.setFont(new Font("Arial", Font.PLAIN, 16));
        cartTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        cartTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        cartTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        cartTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());

        cartTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(e -> updateQuantity(cartTable.getSelectedRow(), -1)));
        cartTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(e -> updateQuantity(cartTable.getSelectedRow(), 1)));
        cartTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(e -> {
            int row = cartTable.getSelectedRow();
            removeFromCart(row);
        }));
    }

    private void setupMainSplitPane() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel productLabel = new JLabel("Danh sách sản phẩm", SwingConstants.CENTER);
        productLabel.setFont(new Font("Arial", Font.BOLD, 18));
        leftPanel.add(productLabel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel cartLabel = new JLabel("Giỏ hàng của bạn", SwingConstants.CENTER);
        cartLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(cartLabel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setDividerSize(5);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setDividerLocation(0.5);
        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);

        add(mainSplit, BorderLayout.CENTER);
    }

    private void setupCheckoutButton() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));

        JLabel phoneLabel = new JLabel("SĐT khách hàng:");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(phoneLabel);

        phoneField = new JTextField(10);
        phoneField.setFont(new Font("Arial", Font.PLAIN, 16));
        phoneField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkCustomerInfo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkCustomerInfo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkCustomerInfo();
            }
        });
        bottomPanel.add(phoneField);

        customerNameLabel = new JLabel("Tên: ");
        customerNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bottomPanel.add(customerNameLabel);

        discountLabel = new JLabel("Giảm giá: ");
        discountLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bottomPanel.add(discountLabel);

        JButton checkoutButton = new JButton("Thanh toán");
        styleButton(checkoutButton, new Color(0, 123, 255));
        checkoutButton.addActionListener(e -> checkout());
        bottomPanel.add(checkoutButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
    }

    private void loadProducts(String keyword) {
        List<Product> products = saleController.getProducts(keyword);
        productModel.setRowCount(0);
        for (Product product : products) {
            productModel.addRow(new Object[]{
                    product.getId(), product.getName(), FormatVND.format(product.getSellingPrice()),
                    product.getUnit(), product.getQuantity()
            });
        }
    }

    private void addToCart(Product product) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == product.getId()) {
                int currentQty = (int) cartModel.getValueAt(i, 3);
                if (currentQty < product.getQuantity()) {
                    updateQuantity(i, 1);
                    saleController.addToCart(product, 1);
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể mua quá số lượng tồn kho!");
                }
                return;
            }
        }

        saleController.addToCart(product, 1);
        cartModel.addRow(new Object[]{
                product.getId(),
                product.getName(),
                product.getUnit(),
                1,
                "-",
                "+",
                FormatVND.format(product.getSellingPrice()),
                "x"
        });
        updateTotalPrice(cartModel.getRowCount() - 1);
    }


    private void updateQuantity(int row, int amount) {
        int productId = (int) cartModel.getValueAt(row, 0);
        Product product = saleController.getProductById(productId);
        int newQty = Math.max(1, (int) cartModel.getValueAt(row, 3) + amount);
        saleController.updateQuantity(productId, newQty);
        cartModel.setValueAt(newQty, row, 3);
        updateTotalPrice(row);
    }

    private void updateTotalPrice(int row) {
        int productId = (int) cartModel.getValueAt(row, 0);
        Product product = saleController.getProductById(productId);
        int quantity = (int) cartModel.getValueAt(row, 3);
        BigDecimal totalPrice = product.getSellingPrice().multiply(BigDecimal.valueOf(quantity));
        cartModel.setValueAt(FormatVND.format(totalPrice), row, 6);
    }

    private void removeFromCart(int row) {
        int productId = (int) cartModel.getValueAt(row, 0);
        saleController.removeFromCart(productId);
        cartModel.removeRow(row);
    }

    private void checkCustomerInfo() {
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty()) {
            String customerName = saleController.getCustomerNameByPhone(phone);
            customerNameLabel.setText("Tên: " + customerName);

            BigDecimal discount = saleController.getCustomerDiscountPercent(phone);
            discountLabel.setText("Giảm giá: " + FormatVND.format(discount));
        }
    }

    private void checkout() {
        String phoneNumber = phoneField.getText();
        String customerName = saleController.getCustomerNameByPhone(phoneNumber);
        int staffId = 1;
        String paymentMethod = "Cash";
        String note = "Cảm ơn khách hàng !";

        BigDecimal finalAmount = saleController.checkout(phoneNumber, paymentMethod, note, staffId, customerName);
        cartModel.setRowCount(0);
        saleController.clearCart();
        loadProducts(searchField.getText());
        customerNameLabel.setText("");
        discountLabel.setText("");
        phoneField.setText("");
        JOptionPane.showMessageDialog(this, "Thanh toán thành công! Tổng tiền: " + FormatVND.format(finalAmount));
    }
}
