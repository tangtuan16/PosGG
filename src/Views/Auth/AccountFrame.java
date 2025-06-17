package Views.Auth;

import Controllers.UserController;
import Models.Session;
import Models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AccountFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private UserController controller;

    // Trường hiển thị thông tin
    private JTextField idField, usernameField, nameField;
    private JComboBox<String> roleCombo, statusCombo;


    public AccountFrame() {
        setTitle("Quản lý tài khoản ! Chào mừng: " + Session.getInstance().getUser().getName());
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        controller = new UserController();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Panel Trường Thông Tin ---
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("User Info"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 10, 5, 10);
        c.anchor = GridBagConstraints.WEST;

        idField = new JTextField(10);
        idField.setEditable(false);
        usernameField = new JTextField(10);
        nameField = new JTextField(10);
        roleCombo = new JComboBox<>(new String[]{"admin", "staff"});
        statusCombo = new JComboBox<>(new String[]{"pending", "active", "delete"});

        int row = 0;

        c.gridx = 0;
        c.gridy = row;
        infoPanel.add(new JLabel("ID:"), c);
        c.gridx = 1;
        infoPanel.add(idField, c);
        c.gridx = 2;
        infoPanel.add(new JLabel("Username:"), c);
        c.gridx = 3;
        infoPanel.add(usernameField, c);
        row++;

        c.gridx = 0;
        c.gridy = row;
        infoPanel.add(new JLabel("Name:"), c);
        c.gridx = 1;
        infoPanel.add(nameField, c);
        c.gridx = 2;
        infoPanel.add(new JLabel("Role:"), c);
        c.gridx = 3;
        infoPanel.add(roleCombo, c);
        row++;

        c.gridx = 0;
        c.gridy = row;
        infoPanel.add(new JLabel("Status:"), c);
        c.gridx = 1;
        infoPanel.add(statusCombo, c);

        // Thêm infoPanel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(infoPanel, gbc);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Username", "Pass", "Role", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        add(scroll, gbc);

        // --- Buttons ---
        JPanel btnPanel = new JPanel();
        JButton btnUpdate = new JButton("Update");
        JButton btnChangePass = new JButton("Change Password");
        add(btnChangePass);
        add(btnUpdate);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnChangePass);

        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(btnPanel, gbc);

        // --- Events ---
        btnUpdate.addActionListener(e -> updateInfo());
        btnChangePass.addActionListener(e -> openChangePasswordDialog());
        table.getSelectionModel().addListSelectionListener(e -> fillUserInfo());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        List<User> users = controller.getAllUsers();
        for (User user : users) {
            model.addRow(new Object[]{
                    user.getId(),
                    user.getName(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getStatus()
            });
        }
    }

    private void fillUserInfo() {
        int selected = table.getSelectedRow();
        if (selected >= 0) {
            idField.setText(model.getValueAt(selected, 0).toString());
            nameField.setText(model.getValueAt(selected, 1).toString());
            usernameField.setText(model.getValueAt(selected, 2).toString());
            roleCombo.setSelectedItem(model.getValueAt(selected, 4).toString());
            statusCombo.setSelectedItem(model.getValueAt(selected, 5).toString());
        }
    }


    private void updateInfo() {
        try {
            int id = Integer.parseInt(idField.getText());
            String username = usernameField.getText();
            String name = nameField.getText();
            String role = roleCombo.getSelectedItem().toString();
            String status = statusCombo.getSelectedItem().toString();
            String password = Session.getInstance().getUser().getPassword();

            User user = new User(id, name, username, password, role, status);
            if (controller.updateUserInfo(user)) {
                JOptionPane.showMessageDialog(this, "User updated");
                loadData();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    private void openChangePasswordDialog() {
        int selected = table.getSelectedRow();
        if (selected >= 0) {
            int userId = (int) model.getValueAt(selected, 0);
            String username = (String) model.getValueAt(selected, 1);
            new ChangePasswordDialog(this, userId, username).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user.");
        }
    }


    private void clearFields() {
        idField.setText("");
        usernameField.setText("");
        nameField.setText("");
    }
}
