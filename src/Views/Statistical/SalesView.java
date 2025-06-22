package Views.Statistical;

import Models.SalesModel.SalesData;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class SalesView extends JFrame {
    private JTable salesTable;
    private JScrollPane tableScrollPane;
    private JComboBox<String> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JButton updateButton;
    private TableRowSorter<DefaultTableModel> sorter;

    public SalesView(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Panel điều khiển
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // ComboBox cho năm
        yearComboBox = new JComboBox<>();
        yearComboBox.addItem("All");
        controlPanel.add(new JLabel("Năm: "));
        controlPanel.add(yearComboBox);

        // ComboBox cho tháng
        monthComboBox = new JComboBox<>();
        monthComboBox.addItem("All");
        for (int i = 1; i <= 12; i++) {
            monthComboBox.addItem(String.format("%02d", i));
        }
        controlPanel.add(new JLabel("Tháng: "));
        controlPanel.add(monthComboBox);

        // Nút cập nhật
        updateButton = new JButton("Tải thống kê");
        controlPanel.add(updateButton);

        // Thêm panel điều khiển vào giao diện
        add(controlPanel, BorderLayout.NORTH);

        // Tạo bảng
        String[] columnNames = {"Tên sản phẩm", "Tháng/Năm", "Số lượng"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Integer.class; // Cột "Số lượng" là kiểu Integer
                return String.class;
            }
        };
        salesTable = new JTable(tableModel);

        // Thêm sorter để hỗ trợ sắp xếp
        sorter = new TableRowSorter<>(tableModel);
        salesTable.setRowSorter(sorter);

        tableScrollPane = new JScrollPane(salesTable);
        add(tableScrollPane, BorderLayout.CENTER);
    }

    // Cập nhật danh sách năm
    public void setYears(List<String> years) {
        yearComboBox.removeAllItems();
        yearComboBox.addItem("All");
        for (String year : years) {
            yearComboBox.addItem(year);
        }
    }

    // Cập nhật dữ liệu bảng
    public void updateTable(List<SalesData> salesData) {
        DefaultTableModel tableModel = (DefaultTableModel) salesTable.getModel();
        tableModel.setRowCount(0); // Xóa dữ liệu cũ

        for (SalesData data : salesData) {
            Object[] row = {
                    data.getProductName(),
                    data.getMonthYear(),
                    data.getQuantity()
            };
            tableModel.addRow(row);
        }
    }

    // Getter cho JComboBox và JButton
    public JComboBox<String> getYearComboBox() { return yearComboBox; }
    public JComboBox<String> getMonthComboBox() { return monthComboBox; }
    public JButton getUpdateButton() { return updateButton; }
}