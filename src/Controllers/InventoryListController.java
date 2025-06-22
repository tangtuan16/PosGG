package Controllers;

import Models.InventoryListModel;
import Views.Statistical.InventoryListView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InventoryListController {
    private InventoryListModel model;
    private InventoryListView view;

    public InventoryListController(InventoryListModel model, InventoryListView view) {
        this.model = model;
        this.view = view;
        initController();
    }

    private void initController() {
        // Thêm sự kiện cho nút Refresh
        view.getRefreshButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTable("");
            }
        });

        // Thêm sự kiện cho nút Search
        view.getSearchButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTable(view.getSearchField().getText());
            }
        });

        // Thêm sự kiện Enter cho thanh tìm kiếm
        view.getSearchField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateTable(view.getSearchField().getText());
                }
            }
        });

        // Cập nhật bảng ban đầu
        updateTable("");
    }

    private void updateTable(String searchTerm) {
        // Lấy dữ liệu từ model và cập nhật bảng
        view.updateTable(model.getProductData(searchTerm));
    }
}