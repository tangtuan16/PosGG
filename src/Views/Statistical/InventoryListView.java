package Views.Statistical;

import Models.InventoryListModel;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class InventoryListView extends JFrame {
    private JTable productTable;
    private JScrollPane scrollPane;
    private JButton refreshButton;
    private JButton searchButton;
    private JTextField searchField;

    public InventoryListView(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Panel điều khiển
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // Thanh tìm kiếm
        searchField = new JTextField(20);
        controlPanel.add(new JLabel("Search Product: "));
        controlPanel.add(searchField);

        // Nút tìm kiếm
        searchButton = new JButton("Search");
        controlPanel.add(searchButton);

        // Nút làm mới
        refreshButton = new JButton("Refresh");
        controlPanel.add(refreshButton);

        // Thêm panel điều khiển vào giao diện
        add(controlPanel, BorderLayout.NORTH);

        // Tạo bảng
        productTable = new JTable();
        productTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"Product Name", "Created At", "Quantity"}
        ));
        scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Cập nhật bảng
    public void updateTable(java.util.List<InventoryListModel.ProductData> productDataList) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) productTable.getModel();
        model.setRowCount(0); // Xóa dữ liệu cũ

        for (InventoryListModel.ProductData data : productDataList) {
            model.addRow(new Object[]{
                    data.getProductName(),
                    data.getCreatedAt() != null ? dateFormat.format(data.getCreatedAt()) : "N/A",
                    data.getQuantity()
            });
        }

        // Hiển thị thông báo nếu không có dữ liệu
        if (productDataList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data available", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Getter cho các thành phần
    public JButton getRefreshButton() { return refreshButton; }
    public JButton getSearchButton() { return searchButton; }
    public JTextField getSearchField() { return searchField; }
}