package Views.Auth;

import Controllers.UserController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterFrame extends JFrame {
    private JTextField usernameField, nameField, roleField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;

    private UserController controller;

    public RegisterFrame() {
        controller = new UserController();
        initUI();
    }

    private void initUI() {
        setTitle("Register");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 20, 200, 25);
        add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(20, 60, 80, 25);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 60, 200, 25);
        add(passwordField);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(20, 100, 80, 25);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(100, 100, 200, 25);
        add(nameField);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(20, 140, 80, 25);
        add(roleLabel);

        String[] roles = {"admin", "staff"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setBounds(100, 140, 200, 25);
        add(roleComboBox);


        roleField = new JTextField();
        roleField.setBounds(100, 140, 200, 25);
        add(roleField);

        registerButton = new JButton("Register");
        registerButton.setBounds(120, 180, 100, 25);
        add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRegisterClicked();
            }
        });
    }

    private void onRegisterClicked() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String name = nameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        boolean success = controller.register(username, password, name, role);
        if (success) {
            JOptionPane.showMessageDialog(this, "Register successful!");
            this.dispose();
            new LoginFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Register failed! Username may already exist.");
        }
    }
}
