package Utils;

import Views.Auth.AccountFrame;
import Views.Customer.CustomerFrame;
import Views.Products.ProductFrame;
import Views.Sales.SaleFrame;
import Views.Statistical.StatisticalFrame;

public class FrameManager {
    private static AccountFrame accountFrame;
    private static CustomerFrame customerFrame;
    private static ProductFrame productFrame;
    private static SaleFrame saleFrame;
    private static StatisticalFrame statFrame;

    public static void showAccountFrame() {
        if (accountFrame == null || !accountFrame.isDisplayable()) {
            accountFrame = new AccountFrame();
        }
        accountFrame.setVisible(true);
        accountFrame.toFront();
    }

    public static void showCusomerFrame() {
        if (customerFrame == null || !customerFrame.isDisplayable()) {
            customerFrame = new CustomerFrame();
        }
        customerFrame.setVisible(true);
        customerFrame.toFront();
    }

    public static void showProductsFrame() {
        if (productFrame == null || !productFrame.isDisplayable()) {
            productFrame = new ProductFrame();
        }
        productFrame.setVisible(true);
        productFrame.toFront();
    }

    public static void showSaleFrame() {
        if (saleFrame == null || !saleFrame.isDisplayable()) {
            saleFrame = new SaleFrame();
        }
        saleFrame.setVisible(true);
        saleFrame.toFront();
    }

    public static void showStatisticalFrame() {
        if (statFrame == null || !statFrame.isDisplayable()) {
            statFrame = new StatisticalFrame();
        }
        statFrame.setVisible(true);
        statFrame.toFront();
    }

    public static void closeAll() {
        if (accountFrame != null) accountFrame.dispose();
        if (customerFrame != null) customerFrame.dispose();
        if (productFrame != null) productFrame.dispose();
        if (saleFrame != null) saleFrame.dispose();
        if (statFrame != null) statFrame.dispose();
    }
}
