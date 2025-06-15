package Views.Sales;

import Controllers.SaleController;
import Models.Product;
import Models.Sales.CartItem;
import Utils.ButtonEditor;
import Utils.ButtonRenderer;
import Utils.FormatVND;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.List;

import static java.awt.Toolkit.getDefaultToolkit;

public class SaleFrame extends JFrame {
    private static final int SEARCH_FIELD_WIDTH = 30;
    private static final int SUGGESTION_MAX_HEIGHT = 200;
    private static final int SUGGESTION_ITEM_HEIGHT = 30;
    private static final int MAX_SUGGESTIONS = 10;
    private static final Color PRIMARY_BUTTON_COLOR = new Color(0, 123, 255);
    private static final Color PAGINATION_BUTTON_COLOR = new Color(108, 117, 125);

    private JTable cartTable, productTable;
    private DefaultTableModel cartModel, productModel;
    private JTextField searchField;
    private JTextField phoneField;
    private JLabel customerNameLabel;
    private JLabel discountLabel;
    private JLabel cartSummaryLabel;
    private JPanel leftPanel;
    private WebcamPanel webcamPanel;
    private Webcam webcam;
    private JWindow suggestionWindow;
    private AWTEventListener clickOutsideListener;
    private int currentPage = 1;
    private final int pageSize = 20;
    private JButton prevButton, nextButton, scanButton, stopScanButton;
    private final SaleController controller;

    public SaleFrame() {
        controller = new SaleController(this);

        setTitle("Bán hàng");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupSearchBar();
        setupProductTable();
        setupCartTable();
        setupPaginationPanel();
        setupMainSplitPane();
        setupBottomPanel();

        controller.loadProducts("", currentPage, pageSize);
        updateCartSummary();
        setVisible(true);
    }

    private void setupSearchBar() {
        JPanel searchPanel = new JPanel();
        JLabel searchLabel = new JLabel("Tìm kiếm sản phẩm: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 18));
        searchPanel.add(searchLabel);

        searchField = new JTextField(SEARCH_FIELD_WIDTH);
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchPanel.add(searchField);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                controller.getProductSuggestions(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                controller.getProductSuggestions(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                controller.getProductSuggestions(searchField.getText());
            }
        });

        JButton searchButton = new JButton("Tìm kiếm");
        styleButton(searchButton, PRIMARY_BUTTON_COLOR);
        searchButton.addActionListener(e -> {
            currentPage = 1;
            controller.loadProducts(searchField.getText(), currentPage, pageSize);
            hideSuggestionWindow();
        });

        searchField.addActionListener(e -> {
            currentPage = 1;
            controller.loadProducts(searchField.getText(), currentPage, pageSize);
            hideSuggestionWindow();
        });

        searchPanel.add(searchButton);

        scanButton = new JButton("Quét mã vạch");
        styleButton(scanButton, PRIMARY_BUTTON_COLOR);
        scanButton.addActionListener(e -> {
            if (webcam == null) {
                webcam = Webcam.getDefault();
            }

            if (webcam.isOpen()) {
                webcam.close();
            }
            Dimension d = new Dimension(176, 144);
            webcam.setViewSize(d);
            stopScanButton.setVisible(true);
            webcamPanel = new WebcamPanel(webcam);
            searchPanel.add(webcamPanel, BorderLayout.CENTER);
            searchPanel.revalidate();
            searchPanel.repaint();
            webcam.open();

            controller.startBarcodeScanner(webcam, code -> {
                if (code != null) {
                    SwingUtilities.invokeLater(() -> {
                        searchField.setText(code);
                        currentPage = 1;
                        controller.loadProducts(code, currentPage, pageSize);

                        if (webcam.isOpen()) webcam.close();
                        if (webcamPanel != null) {
                            searchPanel.remove(webcamPanel);
                            webcamPanel = null;
                        }
                        searchPanel.revalidate();
                        searchPanel.repaint();
                        stopScanButton.setVisible(false);
                    });
                }
            });
        });

        searchPanel.add(scanButton);
        stopScanButton = new JButton("Dừng quét");
        stopScanButton.setVisible(false);
        styleButton(stopScanButton, new Color(244, 67, 54));
        stopScanButton.addActionListener(e -> {
            controller.stopBarcodeScanner();
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
            if (webcamPanel != null) {
                searchPanel.remove(webcamPanel);
                webcamPanel = null;
                searchPanel.revalidate();
                searchPanel.repaint();
            }
            stopScanButton.setVisible(false);
        });

        searchPanel.add(stopScanButton);

        JButton invoicesButton = new JButton("Check hóa đơn");
        styleButton(invoicesButton, PRIMARY_BUTTON_COLOR);
        invoicesButton.addActionListener(e -> new InvoiceSearchFrame().setVisible(true));
        searchPanel.add(invoicesButton);

        add(searchPanel, BorderLayout.NORTH);
    }

    private void setupProductTable() {
        productModel = new DefaultTableModel(new String[]{"ID", "Tên", "Giá bán", "Barcode", "Số lượng tồn", "Giảm giá"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(productModel);
        productTable.setRowHeight(31);
        productTable.setFont(new Font("Arial", Font.PLAIN, 14));

        JTableHeader productHeader = productTable.getTableHeader();
        productHeader.setFont(new Font("Arial", Font.PLAIN, 16));
        productHeader.setBackground(new Color(230, 230, 250));
        productHeader.setForeground(Color.black);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer();
        renderer1.setHorizontalAlignment(JLabel.LEFT);
        productTable.getColumnModel().getColumn(1).setCellRenderer(renderer1);

        productTable.getColumnModel().getColumn(0).setPreferredWidth(25);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);

        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = productTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = productTable.convertRowIndexToModel(viewRow);
                    int productId = (int) productModel.getValueAt(modelRow, 0);
                    Product product = controller.getProductById(productId);
                    if (product == null) {
                        return;
                    }
                    if (product.getQuantity() <= 0) {
                        JOptionPane.showMessageDialog(SaleFrame.this, "Sản phẩm đã hết hàng!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    controller.addToCart(product, 1);
                    updateCartSummary();
                }
            }
        });
    }

    private void setupCartTable() {
        cartModel = new DefaultTableModel(new String[]{"ID", "Tên", "Barcode", "Số lượng", "Giảm", "Tăng", "Thành tiền", "Xóa"}, 0);

        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(30);
        cartTable.setFont(new Font("Arial", Font.PLAIN, 14));
        cartTable.getTableHeader().setFont(new Font("Arial", Font.PLAIN, 16));
        cartTable.getTableHeader().setBackground(new Color(230, 230, 250));
        cartTable.getTableHeader().setForeground(Color.black);


        cartTable.getColumnModel().getColumn(0).setPreferredWidth(25);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(30);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(30);
        cartTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(7).setPreferredWidth(30);

        cartTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        cartTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        cartTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());

        cartTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                int productId = (int) cartModel.getValueAt(row, 0);
                int currentQty = (int) cartModel.getValueAt(row, 3);
                controller.updateCartQuantity(productId, currentQty - 1);
                updateCartSummary();
            }
        }));
        cartTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                int productId = (int) cartModel.getValueAt(row, 0);
                int currentQty = (int) cartModel.getValueAt(row, 3);
                controller.updateCartQuantity(productId, currentQty + 1);
                updateCartSummary();
            }
        }));
        cartTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                int productId = (int) cartModel.getValueAt(row, 0);
                controller.removeFromCart(productId);
                updateCartSummary();
            }
        }));
    }

    private void setupPaginationPanel() {
        prevButton = new JButton("← Trước");
        styleButton(prevButton, PAGINATION_BUTTON_COLOR);
        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                controller.loadProducts(searchField.getText(), currentPage, pageSize);
            }
        });

        nextButton = new JButton("Tiếp →");
        styleButton(nextButton, PAGINATION_BUTTON_COLOR);
        nextButton.addActionListener(e -> {
            currentPage++;
            controller.loadProducts(searchField.getText(), currentPage, pageSize);
        });

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.add(prevButton);
        paginationPanel.add(nextButton);

        leftPanel = new JPanel(new BorderLayout());
        JLabel productLabel = new JLabel("Danh sách sản phẩm", SwingConstants.CENTER);
        productLabel.setFont(new Font("Arial", Font.BOLD, 20));
        leftPanel.add(productLabel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        leftPanel.add(paginationPanel, BorderLayout.SOUTH);
    }

    private void setupMainSplitPane() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel cartLabel = new JLabel("Giỏ hàng của bạn", SwingConstants.CENTER);
        cartLabel.setFont(new Font("Arial", Font.BOLD, 20));
        rightPanel.add(cartLabel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        rightPanel.add(cartSummaryLabel = new JLabel("Số lượng: 0 | Tổng tiền: 0", SwingConstants.CENTER), BorderLayout.SOUTH);
        cartSummaryLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setDividerSize(5);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setDividerLocation(0.5);
        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void setupBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));

        JLabel phoneLabel = new JLabel("SĐT khách hàng:");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(phoneLabel);

        phoneField = new JTextField(10);
        phoneField.setFont(new Font("Arial", Font.PLAIN, 16));
        phoneField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                controller.checkCustomerInfo(phoneField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                controller.checkCustomerInfo(phoneField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                controller.checkCustomerInfo(phoneField.getText());
            }
        });
        bottomPanel.add(phoneField);

        customerNameLabel = new JLabel("Tên: ");
        customerNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bottomPanel.add(customerNameLabel);

        discountLabel = new JLabel("Giảm giá: ");
        discountLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bottomPanel.add(discountLabel);

        JLabel paymentLabel = new JLabel("Phương thức thanh toán:");
        paymentLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(paymentLabel);

        String[] paymentMethods = {"Tiền mặt", "Thẻ tín dụng", "Chuyển khoản"};
        JComboBox<String> paymentComboBox = new JComboBox<>(paymentMethods);
        paymentComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        bottomPanel.add(paymentComboBox);

        JButton clearCartButton = new JButton("Xóa giỏ hàng");
        styleButton(clearCartButton, new Color(223, 0, 69));
        clearCartButton.addActionListener(e -> {
            controller.clearCart();
            controller.loadProducts("", currentPage, pageSize);
            updateCartSummary();
        });
        bottomPanel.add(clearCartButton);

        JButton checkoutButton = new JButton("Thanh toán");
        styleButton(checkoutButton, new Color(40, 167, 69));
        checkoutButton.addActionListener(e -> {
            if (cartModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Giỏ hàng rỗng, vui lòng thêm sản phẩm!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String phoneNumber = phoneField.getText().trim();
            String customerName = customerNameLabel.getText().replace("Tên: ", "").trim();
            int staffId = 1;
            String paymentMethod = (String) paymentComboBox.getSelectedItem();
            String note = "Cảm ơn khách hàng!";
            controller.checkout(phoneNumber, paymentMethod, note, staffId, customerName);

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có muốn gửi hóa đơn qua email không?",
                    "Gửi hóa đơn",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                String inputEmail = JOptionPane.showInputDialog(
                        this,
                        "Nhập địa chỉ email để gửi hóa đơn:",
                        "Nhập Email",
                        JOptionPane.PLAIN_MESSAGE
                );

                if (inputEmail != null && !inputEmail.trim().isEmpty()) {
                    if (!inputEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                        JOptionPane.showMessageDialog(this, "Email không đúng định dạng!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    BigDecimal totalAmount = controller.getCartTotal();
                    controller.sendInvoiceToEmail(inputEmail.trim(), totalAmount, "invoice.pdf");
                }
            }
        });
        bottomPanel.add(checkoutButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
    }

    public void displayProducts(List<Product> products, int currentPage, int pageSize) {
        try {
            productModel.setRowCount(0);
            for (Product product : products) {
                BigDecimal finalPrice = product.getSellingPrice();
                productModel.addRow(new Object[]{
                        product.getId(),
                        product.getName(),
                        FormatVND.format(finalPrice),
                        product.getBarcode(),
                        product.getQuantity(),
                        product.getDiscount().stripTrailingZeros().toPlainString() + "%"
                });
            }
            prevButton.setEnabled(currentPage > 1);
            nextButton.setEnabled(products.size() == pageSize);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi hiển thị sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void displaySuggestions(List<Product> suggestions) {
        try {
            if (suggestionWindow != null) {
                suggestionWindow.setVisible(false);
                suggestionWindow.dispose();
            }

            if (suggestions.isEmpty() || searchField.getText().trim().isEmpty()) {
                return;
            }

            suggestionWindow = new JWindow();
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            panel.setBackground(Color.WHITE);

            List<Product> limitedSuggestions = suggestions.subList(0, Math.min(suggestions.size(), MAX_SUGGESTIONS));
            for (Product product : limitedSuggestions) {
                String name = product.getName();
                JLabel label = new JLabel(name);
                label.setFont(new Font("Arial", Font.PLAIN, 14));
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                label.setOpaque(true);
                label.setBackground(Color.WHITE);

                label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        label.setBackground(new Color(220, 220, 250));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        label.setBackground(Color.WHITE);
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        searchField.setText(name);
                        hideSuggestionWindow();
                        controller.addToCart(product, 1);
                        updateCartSummary();
                    }
                });

                panel.add(label);
            }

            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setPreferredSize(new Dimension(searchField.getWidth(), Math.min(SUGGESTION_MAX_HEIGHT, limitedSuggestions.size() * SUGGESTION_ITEM_HEIGHT)));
            scrollPane.setBorder(null);
            suggestionWindow.getContentPane().add(scrollPane);

            Point location = searchField.getLocationOnScreen();
            suggestionWindow.setLocation(location.x, location.y + searchField.getHeight());
            suggestionWindow.pack();
            suggestionWindow.setVisible(true);

            if (clickOutsideListener != null) {
                getDefaultToolkit().removeAWTEventListener(clickOutsideListener);
            }

            clickOutsideListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                        Component clicked = SwingUtilities.getDeepestComponentAt(
                                me.getComponent(), me.getX(), me.getY()
                        );

                        if (clicked == null || (!SwingUtilities.isDescendingFrom(clicked, suggestionWindow)
                                && clicked != searchField)) {
                            hideSuggestionWindow();
                        }
                    }
                }
            };

            getDefaultToolkit().addAWTEventListener(clickOutsideListener, AWTEvent.MOUSE_EVENT_MASK);

            searchField.registerKeyboardAction(e -> hideSuggestionWindow(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi hiển thị gợi ý sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateCart() {
        try {
            cartModel.setRowCount(0);
            for (CartItem item : controller.getCart()) {
                Product product = item.getProduct();
                BigDecimal totalPrice = controller.calculateCartItemPrice(product, item.getQuantity());
                cartModel.addRow(new Object[]{
                        product.getId(),
                        product.getName(),
                        product.getBarcode(),
                        item.getQuantity(),
                        "-",
                        "+",
                        FormatVND.format(totalPrice),
                        "x"
                });
            }
            updateCartSummary();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật giỏ hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateCustomerInfo(String customerName, BigDecimal discount) {
        try {
            customerNameLabel.setText("Tên: " + (customerName != null ? customerName : ""));
            discountLabel.setText("Giảm giá: " + discount.stripTrailingZeros().toPlainString() + "%");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật thông tin khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearCart() {
        try {
            cartModel.setRowCount(0);
            phoneField.setText("");
            updateCustomerInfo("", BigDecimal.ZERO);
            updateCartSummary();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa giỏ hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void updateCartSummary() {
        try {
            int itemCount = controller.getCartItemCount();
            BigDecimal total = controller.getCartTotal();
            cartSummaryLabel.setText("Số lượng: " + itemCount + " | Tổng tiền: " + FormatVND.format(total));
        } catch (Exception e) {
            cartSummaryLabel.setText("Số lượng: 0 | Tổng tiền: 0");
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật thông tin giỏ hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hideSuggestionWindow() {
        if (suggestionWindow != null) {
            suggestionWindow.setVisible(false);
            getDefaultToolkit().removeAWTEventListener(clickOutsideListener);
            suggestionWindow.dispose();
            suggestionWindow = null;
        }
    }

}