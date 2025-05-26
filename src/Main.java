
import Views.Customer.CustomerFrame;
import Views.Sales.SaleFrame;

public class Main {
    public static void main(String[] args) {
        // Khởi chạy giao diện CustomerFrame
//        System.out.println("DB_USER " + System.getenv("DB_USER"));
//        java.awt.EventQueue.invokeLater(() -> {
//            new CustomerFrame().setVisible(true);
//        });
        System.out.println("DB_PASSWORD " + System.getenv("DB_PASSWORD"));
        SaleFrame frame = new SaleFrame();
        frame.setVisible(true);
    }
}