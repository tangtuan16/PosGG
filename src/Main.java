
import Controllers.InventoryListController;
import Controllers.SalesController;
import Controllers.StatisticController;
import Models.InventoryListModel;
import Models.SalesModel;
import Services.InvoiceDAO;
import Views.Customer.CustomerFrame;
import Views.Sales.SaleFrame;
import Views.Statistical.InventoryListView;
import Views.Statistical.SalesView;
import Views.Statistical.StatisticView;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
//        // Cus Hai
//        System.out.println("DB_USER " + System.getenv("DB_USER"));
//        java.awt.EventQueue.invokeLater(() -> {
//            new CustomerFrame().setVisible(true);
//        });
//
////        //Sale Tuan
//        System.out.println("DB_PASSWORD " + System.getenv("DB_PASSWORD"));
//        SaleFrame frame = new SaleFrame();
//        frame.setVisible(true);
//
//        //Thong ke
//        SwingUtilities.invokeLater(() -> {
//            try {
//                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos_gg", "root", "123");
//                Services.InvoiceDAO dao = new Services.InvoiceDAO(conn);
//                Views.Statistical.StatisticView view = new Views.Statistical.StatisticView();
//                Controllers.StatisticController controller = new Controllers.StatisticController(dao, view);
//                view.setVisible(true);
//            } catch (SQLException e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(null, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
//            }
//        });
//        SwingUtilities.invokeLater(() -> {
//            SalesModel model = new SalesModel();
//            SalesView view = new SalesView("Thống kê sản phẩm đã bán");
//            SalesController controller = new SalesController(model, view);
//            view.setVisible(true);
//        });
//        SwingUtilities.invokeLater(() -> {
//            InventoryListModel model = new InventoryListModel();
//            InventoryListView view = new InventoryListView("Thống kê hàng tồn ");
//            InventoryListController controller = new InventoryListController(model, view);
//            view.setVisible(true);
//        });
    }
}