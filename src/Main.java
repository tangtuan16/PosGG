import Views.HomeFrame;
import Views.Sales.InvoiceSearchFrame;
import Views.Sales.SaleFrame;

import javax.swing.*;
import java.util.TimeZone;

public class Main {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SwingUtilities.invokeLater(() -> (new HomeFrame()).setVisible(true));
    }
}