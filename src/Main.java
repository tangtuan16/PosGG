import Utils.UpdateDiscount;
import Views.Auth.RegisterFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UpdateDiscount productService = new UpdateDiscount();
        productService.scheduleAutoUpdateDiscount();
        String[] options = {"Login", "Register"};
        int choice = JOptionPane.showOptionDialog(null, "Select an option", "Welcome",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 1) {
            new RegisterFrame().setVisible(true);
        } else {
            new Views.Auth.LoginFrame().setVisible(true);
        }
    }
}