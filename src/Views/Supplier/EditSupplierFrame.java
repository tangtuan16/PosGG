package Views.Supplier;

import Controllers.SupplierController;
import Models.Session;
import Models.Supplier;
import Utils.ComboItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class EditSupplierFrame extends JFrame {
    private int pd = 20;
    private JComboBox<ComboItem> statusSupplierCombobox;
    private JButton editSupplierButton;
    JTextField tfAddressSupplier,tfEmaiSupplier,tfPhoneSupplier,tfNameSupplier;
    private SupplierController supplierController = new SupplierController();
    public int userId = Session.getInstance().getUser().getId();
    private SupplierFrame supplierFrame;
    private int idSupplier;

    public void view(){
        setTitle("Cập nhật nhà cung cấp");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        // north
        JLabel title = new JLabel("Cập nhật nhà cung cấp",JLabel.CENTER);
        title.setFont(new Font("Arial",Font.BOLD,30));
        add(title, BorderLayout.NORTH);

        // centre
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.decode("#E0E0E0"));

        // khởi tạo gbc
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 50, 20, 50);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameSupplier = new JLabel("Tên: ");
        nameSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(nameSupplier,gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.ipady = pd;
        tfNameSupplier = new JTextField();
        tfNameSupplier.setPreferredSize(new Dimension(0, 20));
        tfNameSupplier.setMaximumSize(new Dimension(300, 20));
        tfNameSupplier.setFont(new Font("Arial", Font.PLAIN, 15));
        mainPanel.add(tfNameSupplier,gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.ipady = 0;
        JLabel addressSupplier = new JLabel("Địa chỉ: ");
        addressSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(addressSupplier,gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.ipady = pd;
        tfAddressSupplier = new JTextField();
        tfAddressSupplier.setFont(new Font("Arial", Font.PLAIN, 15));
        mainPanel.add(tfAddressSupplier,gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.ipady = 0;
        JLabel phoneSupplier = new JLabel("Số điện thoại: ");
        phoneSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(phoneSupplier,gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.ipady = pd;
        tfPhoneSupplier = new JTextField();
        tfPhoneSupplier.setFont(new Font("Arial", Font.PLAIN, 15));
        mainPanel.add(tfPhoneSupplier,gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.ipady = 0;
        JLabel statusSupplier = new JLabel("Trạng thái: ");
        statusSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(statusSupplier,gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.ipady = pd;
        statusSupplierCombobox = new JComboBox<>();
        statusSupplierCombobox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusSupplierCombobox.addItem(new ComboItem("active", "Hoạt động"));
        statusSupplierCombobox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusSupplierCombobox.setFont(new Font("Arial", Font.PLAIN, 15));
        mainPanel.add(statusSupplierCombobox,gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.ipady = 0;
        JLabel emailSupplier = new JLabel("Email: ");
        emailSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(emailSupplier,gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.ipady = pd; gbc.gridwidth = 2;
        tfEmaiSupplier = new JTextField();
        tfEmaiSupplier.setFont(new Font("Arial", Font.PLAIN, 15));
//        JScrollPane emailScrollPane = new JScrollPane(tfEmaiSupplier);
//        emailScrollPane.setPreferredSize(new Dimension(300, 40));
        mainPanel.add(tfEmaiSupplier, gbc);

        add(mainPanel,BorderLayout.CENTER);

        // south
        editSupplierButton = new JButton("Cập nhật");
        editSupplierButton.setPreferredSize(new Dimension(0,50));
        editSupplierButton.setBackground(Color.decode("#007BFF"));
        editSupplierButton.setForeground(Color.WHITE);
        editSupplierButton.setFont(new Font("Arial", Font.PLAIN, 18));
        add(editSupplierButton,BorderLayout.SOUTH);

    }

    public EditSupplierFrame(SupplierFrame supplierFrame, int idSupplier){
        view();
        this.supplierFrame = supplierFrame;
        this.idSupplier = idSupplier;
        loadSupplier(idSupplier);

        editSupplierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = tfNameSupplier.getText().trim();
                    String phone = tfPhoneSupplier.getText().trim();
                    String email = tfEmaiSupplier.getText().trim();
                    String address = tfAddressSupplier.getText().trim();
                    ComboItem statusSupplierComboItem = (ComboItem) statusSupplierCombobox.getSelectedItem();
                    String statusSupplier = statusSupplierComboItem.getValue();
                    supplierController.editSupplier(idSupplier,name, phone, email, address, statusSupplier, userId);
                    if(supplierFrame != null){
                        supplierFrame.filterSupplier();
                    }
                    JOptionPane.showMessageDialog(null, "Nhà cung cấp cập nhật thành công!");
                    dispose();
                } catch (IllegalArgumentException | SQLException iae) {
                    JOptionPane.showMessageDialog(null, iae.getMessage());
                }
            }
        });
    }

    public void loadSupplier(int idSupplier){
        try{
            Supplier supplier = supplierController.getOneSupplier(idSupplier);
            tfNameSupplier.setText(supplier.getName());
            tfAddressSupplier.setText(supplier.getAddress());
            tfEmaiSupplier.setText(supplier.getEmail());
            tfPhoneSupplier.setText(supplier.getPhone());

            String status = supplier.getStatus();
            String label = "";
            if ("active".equals(status)) {
                label = "Hoạt động";
            } else if ("inactive".equals(status)) {
                label = "Tạm dừng";
            }
            statusSupplierCombobox.setSelectedItem(new ComboItem(status, label));

        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
}
