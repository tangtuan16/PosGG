package Views;

import Utils.FrameManager;
import Views.Auth.LoginFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.URL;

public class HomeFrame extends JFrame {
    private JSplitPane splitPane;

    public HomeFrame() {
        setTitle("POS - Trang Chủ");
        setSize(1000, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel imagePanel = createImagePanel();
        Font buttonFont = new Font("Quicksand", Font.PLAIN, 16);

        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        JButton btnProduct = new JButton("Quản lý sản phẩm");
        btnProduct.setFont(buttonFont);
        JButton btnSale = new JButton("Bán hàng");
        btnSale.setFont(buttonFont);
        JButton btnCustomer = new JButton("Khách hàng thân thiết");
        btnCustomer.setFont(buttonFont);
        JButton btnAccount = new JButton("Quản lý tài khoản");
        btnAccount.setFont(buttonFont);
        JButton btnStatiscal = new JButton("Thống kê, báo cáo");
        btnStatiscal.setFont(buttonFont);
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(buttonFont);

//        if ("admin".equals(user.getRole())) {
//            buttonPanel.add(btnProduct);
//            buttonPanel.add(btnAccount);
//        }

        buttonPanel.add(btnSale);
        buttonPanel.add(btnCustomer);
        buttonPanel.add(btnStatiscal);
        buttonPanel.add(btnLogout);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePanel, buttonPanel);
        splitPane.setDividerLocation(3 * getWidth() / 5);
        splitPane.setDividerSize(0);

        add(splitPane, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation(3 * getWidth() / 5);
            }
        });

        btnProduct.addActionListener(e -> FrameManager.showProductsFrame());
        btnSale.addActionListener(e -> FrameManager.showSaleFrame());
        btnAccount.addActionListener(e -> FrameManager.showAccountFrame());
        btnCustomer.addActionListener(e -> FrameManager.showCusomerFrame());
        btnStatiscal.addActionListener(e -> FrameManager.showStatisticalFrame());
        btnLogout.addActionListener(e -> logout());
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        try {
            URL gifUrl = new URL("https://wallpapers-clan.com/wp-content/uploads/2024/08/bright-moon-in-the-forest-gif-desktop-wallpaper-preview.gif"); // ví dụ 10s
            JLabel gifLabel = new JLabel(new ImageIcon(gifUrl));
            gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(gifLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return panel;
    }


    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void logout() {
        dispose();
        FrameManager.closeAll();
        JOptionPane.showMessageDialog(this, "Bạn đã đăng xuất!");
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}