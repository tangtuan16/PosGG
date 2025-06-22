package Views.Supplier;

import Controllers.SupplierController;
import Models.Session;
import Utils.ComboItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

public class AddSupplierFrame extends JFrame {
    private int pd = 20;
    private JComboBox<ComboItem> statusSupplierCombobox;
    private JButton addSupplierButton;
    JTextField tfAddressSupplier,tfEmaiSupplier,tfPhoneSupplier,tfNameSupplier;
    private SupplierController supplierController = new SupplierController();
    public int userId = Session.getInstance().getUser().getId();
    private SupplierFrame supplierFrame;

    public void view(){
        setTitle("Thêm nhà cung cấp");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));


        // north
        JLabel title = new JLabel("Thêm nhà cung cấp",JLabel.CENTER);
        title.setFont(new Font("Arial",Font.BOLD,30));
        add(title, BorderLayout.NORTH);



        // centre
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.decode("#E0E0E0"));

        // khởi tạo gbc
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 50, 20, 50);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameSupplier = new JLabel("Tên: ");
        nameSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(nameSupplier,gbc);

        gbc.gridy = 1; gbc.ipady = pd;
        tfNameSupplier = new JTextField();
        tfNameSupplier.setPreferredSize(new Dimension(0, 20));
        tfNameSupplier.setMaximumSize(new Dimension(300, 20));
        tfNameSupplier.setFont(new Font("Arial", Font.PLAIN, 15));
        mainPanel.add(tfNameSupplier,gbc);

        gbc.gridy = 2; gbc.ipady = 0;
        JLabel phoneSupplier = new JLabel("Số điện thoại: ");
        phoneSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(phoneSupplier,gbc);

        gbc.gridy = 3; gbc.ipady = pd;
        tfPhoneSupplier = new JTextField();
        tfPhoneSupplier.setFont(new Font("Arial", Font.PLAIN, 15));
        mainPanel.add(tfPhoneSupplier,gbc);

        gbc.gridy = 4; gbc.ipady = 0;
        JLabel emailSupplier = new JLabel("Email: ");
        emailSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(emailSupplier,gbc);

        gbc.gridy = 5; gbc.ipady = pd; gbc.gridwidth = 2;
        tfEmaiSupplier = new JTextField();
        tfEmaiSupplier.setFont(new Font("Arial", Font.PLAIN, 15));
        mainPanel.add(tfEmaiSupplier,gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.ipady = 0; gbc.gridwidth = 1;
        JLabel addressSupplier = new JLabel("Địa chỉ: ");
        addressSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(addressSupplier,gbc);

        gbc.gridy = 1; gbc.ipady = pd;
        tfAddressSupplier = new JTextField();
        tfAddressSupplier.setFont(new Font("Arial", Font.PLAIN, 15));
        mainPanel.add(tfAddressSupplier,gbc);

        gbc.gridy = 2; gbc.ipady = 0;
        JLabel statusSupplier = new JLabel("Trạng thái: ");
        statusSupplier.setFont(new Font("Arial", Font.PLAIN, 18));
        mainPanel.add(statusSupplier,gbc);

        gbc.gridy = 3; gbc.ipady = pd;
        statusSupplierCombobox = new JComboBox<>();
        statusSupplierCombobox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusSupplierCombobox.addItem(new ComboItem("active", "Hoạt động"));
        statusSupplierCombobox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusSupplierCombobox.setFont(new Font("Arial", Font.PLAIN, 15));
        mainPanel.add(statusSupplierCombobox,gbc);

        add(mainPanel,BorderLayout.CENTER);


        // south
        addSupplierButton = new JButton("Thêm");
        addSupplierButton.setPreferredSize(new Dimension(0,50));
        addSupplierButton.setBackground(Color.decode("#007BFF"));
        addSupplierButton.setForeground(Color.WHITE);
        addSupplierButton.setFont(new Font("Arial", Font.PLAIN, 18));
        add(addSupplierButton,BorderLayout.SOUTH);


        setVisible(true);
    }

    public AddSupplierFrame(SupplierFrame supplierFrame){
        this.supplierFrame = supplierFrame;
        view();

        // chỉ cho nhaập số
        tfPhoneSupplier.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume(); // chặn ký tự không phải số
                }
            }
        });

        addSupplierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = tfNameSupplier.getText().trim();
                    String phone = tfPhoneSupplier.getText().trim();
                    String email = tfEmaiSupplier.getText().trim();
                    String address = tfAddressSupplier.getText().trim();
                    ComboItem statusSupplierComboItem = (ComboItem) statusSupplierCombobox.getSelectedItem();
                    String statusSupplier = statusSupplierComboItem.getValue();
                    supplierController.addSupplier(name, phone, email, address, statusSupplier, userId);
                    if(supplierFrame != null){
                        supplierFrame.filterSupplier();
                    }
                    JOptionPane.showMessageDialog(null, "Nhà cung cấp đã được lưu thành công!");
                } catch (IllegalArgumentException | SQLException iae) {
                    JOptionPane.showMessageDialog(null, iae.getMessage());
                }
            }
        });



    }

}
