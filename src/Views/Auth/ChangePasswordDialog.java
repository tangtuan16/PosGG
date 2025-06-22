package Views.Auth;

import Controllers.UserController;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private JPasswordField newPassField, confirmPassField;
    private JButton btnChange, btnCancel;
    private int userId;

    public ChangePasswordDialog(JFrame parent, int userId, String username) {
        super(parent, "Đổi mật khẩu cho: " + username, true);
        this.userId = userId;
        setSize(400, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        newPassField = new JPasswordField(20);
        confirmPassField = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1; add(newPassField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Xác nhậ mật khẩu:"), gbc);
        gbc.gridx = 1; add(confirmPassField, gbc);

        btnChange = new JButton("Change");
        btnCancel = new JButton("Cancel");

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnChange);
        btnPanel.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnPanel, gbc);

        btnCancel.addActionListener(e -> dispose());
        btnChange.addActionListener(e -> changePassword());
    }

    private void changePassword() {
        String newPass = new String(newPassField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không khớp !");
            return;
        }

        String hashed = BCrypt.hashpw(newPass, BCrypt.gensalt());
        UserController controller = new UserController();
        if (controller.updatePassword(userId, hashed)) {
            JOptionPane.showMessageDialog(this, "Sửa thành công !");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi ! Vui lòng thửu lại sau !");
        }
    }
}
