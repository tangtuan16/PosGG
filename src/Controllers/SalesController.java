package Controllers;

import Models.SalesModel;
import Views.Statistical.SalesView;
import java.awt.event.ActionListener;
import java.util.List;

public class SalesController {
    private SalesModel model;
    private SalesView view;

    public SalesController(SalesModel model, SalesView view) {
        this.model = model;
        this.view = view;
        initController();
    }

    private void initController() {
        // Khởi tạo danh sách năm
        view.setYears(model.getAvailableYears());

        // Thêm sự kiện cho nút Update
        view.getUpdateButton().addActionListener(e -> updateTable());

        // Cập nhật bảng ban đầu
        updateTable();
    }

    private void updateTable() {
        // Lấy giá trị năm và tháng từ ComboBox
        String selectedYear = (String) view.getYearComboBox().getSelectedItem();
        String selectedMonth = (String) view.getMonthComboBox().getSelectedItem();

        // Lấy dữ liệu từ model và cập nhật bảng
        List<SalesModel.SalesData> salesData = model.getSalesData(selectedYear, selectedMonth);
        view.updateTable(salesData);
    }
}