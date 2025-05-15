import Views.Sales.InvoiceSearchFrame;
import Views.Sales.SaleFrame;

import javax.swing.*;
import java.util.TimeZone;

public class Main {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        System.out.println("DB_USER: " + System.getenv("DB_USER"));
        System.out.println("DB_PASS: " + System.getenv("DB_PASS"));
        SwingUtilities.invokeLater(() -> (new SaleFrame()).setVisible(true));
    }
}