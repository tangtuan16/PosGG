package Controllers;

import Models.OrderStatistic;
import Services.InvoiceDAO;
import Views.Statistical.StatisticView;
import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatisticController {
    private InvoiceDAO dao;
    private StatisticView view;

    public StatisticController(InvoiceDAO dao, StatisticView view) {
        this.dao = dao;
        this.view = view;

        if (view != null) {
            view.setController(this);
        } else {
            System.err.println("StatisticView is null in StatisticController constructor");
        }
    }

    public void loadStatistics(String type, Integer year) {
        if (view == null) {
            System.err.println("StatisticView is not initialized");
            return;
        }
        try {
            List<OrderStatistic> stats = dao.getStatisticBy(type, year);
            if (stats == null) {
                stats = new ArrayList<>();
            }
            view.updateTable(stats);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi tải thống kê: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Loại thống kê không hợp lệ: " + type, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}