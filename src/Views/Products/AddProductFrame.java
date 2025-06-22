package Views.Products;

import Controllers.CategoryController;
import Controllers.ProductController;
import Controllers.SupplierController;
import Models.Category;
import Models.Session;
import Models.Supplier;
import Utils.ComboItem;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddProductFrame extends JFrame {
    public int userId = Session.getInstance().getUser().getId();
    private ProductFrame productFrame;
    private int pd = 20;
    private JTextField nameField,discountField,unitField,sellingPriceField,  originalPriceField, minQuantityField, barcodeField, quantityField;
    private JComboBox<ComboItem> categoryComboBox,supplierCombobox,statusProductCombobox ;
    private CategoryController categoryController;
    private JButton removeImageButton, imageButton;
    private ImageIcon image;
    private SupplierController supplierController;
    private JButton addButton;
    private File selectedFile;
    private BufferedImage originalImage;
    private JLabel imageLabel;
    private JDateChooser useByDateField;
    private ProductController productController;

    public void view(){
        setTitle("Thêm sản phẩm");
        setSize(1200,800);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        JLabel title = new JLabel("Tạo sản phẩm",JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        add(title, BorderLayout.NORTH);


        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(Color.decode("#E0E0E0"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,50,0,50);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // tên sản phẩm
        gbc.gridx = 0; gbc.gridy = 0;  gbc.ipady = 0;
        JLabel nameLabel = new JLabel("Tên sản phẩm:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(nameLabel, gbc);

        gbc.gridy = 1; gbc.ipady = pd;
        nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(nameField, gbc);


        //danh mục
        gbc.gridy = 2; gbc.ipady = 0;
        JLabel categoryLabel = new JLabel("Danh mục:");
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(categoryLabel, gbc);

        gbc.gridy = 3; gbc.ipady = pd;
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setPreferredSize(new Dimension(0, 25));
        categoryComboBox.setMaximumSize(new Dimension(600, 25));
        loadCategoryComboBox();
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(categoryComboBox, gbc);


        // giá gốc
        gbc.gridy = 4;  gbc.ipady = 0;
        JLabel originalPriceLabel = new JLabel("Giá gốc:");
        originalPriceLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(originalPriceLabel, gbc);

        gbc.gridy = 5; gbc.ipady = pd;
        originalPriceField = new JTextField();
        originalPriceField.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(originalPriceField, gbc);


        // số lượng
        gbc.gridy = 6; gbc.ipady = 0;
        JLabel quantityLabel = new JLabel("Số lượng:");
        quantityLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(quantityLabel, gbc);

        gbc.gridy = 7; gbc.ipady = pd;
        quantityField = new JTextField();
        quantityField.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(quantityField, gbc);


        // mã vạch
        gbc.gridy = 8;  gbc.ipady = 0;
        JLabel barcodeLabel = new JLabel("Mã vạch:");
        barcodeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(barcodeLabel, gbc);

        gbc.gridy = 9; gbc.ipady = pd;
        barcodeField = new JTextField();
        barcodeField.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(barcodeField, gbc);


        // soo lượng tối thiểu
        gbc.gridy = 10;  gbc.ipady = 0;
        JLabel minQuantityLabel = new JLabel("Số lượng tối thiểu:");
        minQuantityLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(minQuantityLabel, gbc);

        gbc.gridy = 11; gbc.ipady = pd;
        minQuantityField = new JTextField();
        minQuantityField.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(minQuantityField, gbc);


        // trạng thái
        gbc.gridy = 12;  gbc.ipady = 0;
        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(statusLabel, gbc);

        gbc.gridy = 13; gbc.ipady = pd;
        statusProductCombobox = new JComboBox<>();
        statusProductCombobox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusProductCombobox.addItem(new ComboItem("active", "Hoạt động"));
        statusProductCombobox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusProductCombobox.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(statusProductCombobox, gbc);


        // hình ảnh
        gbc.gridheight = 4; gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.BOTH;
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(0, 150));
        imagePanel.setMaximumSize(new Dimension(600, 150));
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(5,1,10,10));
        buttonPanel.add(new JLabel());
        imageButton = new JButton("Chọn ảnh:");
        imageButton.setFont(new Font("Arial", Font.PLAIN, 18));
        buttonPanel.add(imageButton); // Đặt bên phải
        buttonPanel.add(new JLabel());
        removeImageButton = new JButton("Xoá ảnh");
        removeImageButton.setFont(new Font("Arial", Font.PLAIN, 18));
        buttonPanel.add(removeImageButton);
        buttonPanel.add(new JLabel());
        imagePanel.add(buttonPanel,BorderLayout.EAST);

        mainContent.add(imagePanel, gbc);



        // nhà cung cấp
        gbc.gridy = 4; gbc.gridheight = 1; gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel supplierLabel = new JLabel("Nhà cung cấp:");
        supplierLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(supplierLabel, gbc);

        gbc.gridy = 5; gbc.ipady = pd;
        supplierCombobox = new JComboBox<>();
        supplierCombobox.addItem(new ComboItem("", "--- Nhà cung cấp ---"));
        ArrayList<Supplier> suppliers = loadSuppliersComboBox();
        for (Supplier s : suppliers){
            supplierCombobox.addItem(new ComboItem(String.valueOf(s.getId()), s.getName()));
        }
        supplierCombobox.setPreferredSize(new Dimension(0, 25));
        supplierCombobox.setMaximumSize(new Dimension(600, 25));
        supplierCombobox.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(supplierCombobox, gbc);


        // giá bán
        gbc.gridy = 6; gbc.ipady = 0;
        JLabel sellingPriceLabel = new JLabel("Giá bán:");
        sellingPriceLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(sellingPriceLabel, gbc);

        gbc.gridy = 7; gbc.ipady = pd;
        sellingPriceField = new JTextField();
        sellingPriceField.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(sellingPriceField, gbc);


        //đơn vị
        gbc.gridy = 8;  gbc.ipady = 0;
        JLabel unitLabel = new JLabel("Đơn vị:");
        unitLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(unitLabel, gbc);

        gbc.gridy = 9; gbc.ipady = pd;
        unitField = new JTextField();
        unitField.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(unitField, gbc);


        // hạn sử dụng
        gbc.gridy = 10;  gbc.ipady = 0;
        JLabel useByDateLabel = new JLabel("Hạn sử dụng:");
        useByDateLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(useByDateLabel, gbc);

        gbc.gridy = 11; gbc.ipady = pd;
        useByDateField = new JDateChooser();
        useByDateField.setDateFormatString("dd/MM/yyyy");
        useByDateField.setFont(new Font("Arial", Font.PLAIN, 15));
        // Vô hiệu hóa nhập tay
        JTextFieldDateEditor editorFrom = (JTextFieldDateEditor) useByDateField.getDateEditor();
        editorFrom.setEditable(false);
        mainContent.add(useByDateField, gbc);


        // giảm giá
        gbc.gridy = 12;  gbc.ipady = 0;
        JLabel discountLabel = new JLabel("Giảm giá:");
        discountLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(discountLabel, gbc);

        gbc.gridy = 13; gbc.ipady = pd;
        discountField = new JTextField();
        discountField.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(discountField, gbc);


        add(mainContent,BorderLayout.CENTER);


        addButton = new JButton("+ Tạo mới");
        addButton.setPreferredSize(new Dimension(0, 50));
        addButton.setBackground(Color.decode("#007BFF"));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.PLAIN, 18));
        add(addButton,BorderLayout.SOUTH);
    }

    public AddProductFrame(ProductFrame productFrame){
        this.productFrame = productFrame;
        categoryController = new CategoryController();
        supplierController = new SupplierController();
        productController = new ProductController();
        view();

        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseImage();
            }
        });

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (originalImage != null) {
                    showFullImage(originalImage);
                }
            }
        });

        removeImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Xoá ảnh khỏi label
                imageLabel.setIcon(null);

                // Reset biến ảnh
                selectedFile = null;
                originalImage = null;
            }
        });

        quantityField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume(); // chặn ký tự không phải số
                }
            }
        });

        originalPriceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String text = originalPriceField.getText();

                // Cho phép số hoặc dấu chấm (chỉ 1 dấu chấm)
                if (!Character.isDigit(c) && c != '.') {
                    e.consume();
                } else if (c == '.' && text.contains(".")) {
                    e.consume(); // chặn dấu . thứ 2
                }
            }
        });

        sellingPriceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String text = sellingPriceField.getText();

                if (!Character.isDigit(c) && c != '.') {
                    e.consume();
                } else if (c == '.' && text.contains(".")) {
                    e.consume();
                }
            }
        });

        discountField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume(); // chặn ký tự không phải số
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    String name = nameField.getText();
                    String categoryId = ((ComboItem) categoryComboBox.getSelectedItem()).getValue();
                    String originalPrice = originalPriceField.getText();
                    String quantity = quantityField.getText();
                    String barcode = barcodeField.getText();
                    String minQuantity = minQuantityField.getText();
                    String status = ((ComboItem) statusProductCombobox.getSelectedItem()).getValue();
                    String supplierId = ((ComboItem) supplierCombobox.getSelectedItem()).getValue();
                    String sellingPrice = sellingPriceField.getText();
                    String unit = unitField.getText();
                    String discount = discountField.getText();

                    Date useByDate = useByDateField.getDate(); // java.util.Date
                    String useByDateStr = null;
                    if (useByDate != null) {
                        SimpleDateFormat sdfForDB = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
                        useByDateStr = sdfForDB.format(useByDate);
                    }

                    productController.addProduct(name,categoryId,supplierId, originalPrice, sellingPrice, quantity, unit, status, userId, userId, barcode, selectedFile, minQuantity, discount, useByDateStr);
                    if(productFrame != null){
                        productFrame.filterProduct();
                    }
                }catch (IllegalArgumentException | SQLException ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });
    }



    // phân cấp danh mục
    public void hienThiBacDanhMuc(ArrayList<Category> categoryList, String parent, int level) {
        for (int i = 0; i < categoryList.size(); i++) {
            Category category = categoryList.get(i);
            // Trường hợp (parentId = null) in ra terminal = 0
            if ((category.getParentId() == 0 && parent == "") || (category.getParentId() != null && String.valueOf(category.getParentId()).equals(parent))) {
                categoryComboBox.addItem(new ComboItem(String.valueOf(category.getId()), " |--".repeat(level) + " " + category.getName()));
                hienThiBacDanhMuc(categoryList, String.valueOf(category.getId()), level + 1);
            }
        }
    }


    public void loadCategoryComboBox(){
        try{
            categoryComboBox.removeAllItems();
            categoryComboBox.addItem(new ComboItem("", "--- Chọn danh mục ---"));
            ArrayList<Category> parentCategoryList = categoryController.findAllCategory();
            hienThiBacDanhMuc(parentCategoryList, "", 0);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }

    // chỉ lấy những nhà cung cấp status = active
    public ArrayList<Supplier> loadSuppliersComboBox() {
        ArrayList<Supplier> suppliers = null;
        try{
            suppliers = supplierController.getSupplierActive();
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
        return suppliers;
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(selectedFile); // Gán vào biến global

                // Resize để hiển thị trong label
                Image scaledImage = getScaledImage(originalImage, 120, 120);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Không thể đọc ảnh!");
            }
        }
    }

    // Hàm resize ảnh đúng tỉ lệ
    private Image getScaledImage(BufferedImage srcImg, int maxWidth, int maxHeight) {
        int originalWidth = srcImg.getWidth();
        int originalHeight = srcImg.getHeight();

        double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        return srcImg.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }

    private void showFullImage(BufferedImage img) {
        int dialogWidth = 1000;
        int dialogHeight = 800;

        // Resize ảnh theo đúng tỉ lệ trong khung 600x600
        Image scaled = getScaledImage(img, dialogWidth, dialogHeight);
        ImageIcon icon = new ImageIcon(scaled);

        // Hiển thị ảnh trong label
        JLabel imgLabel = new JLabel(icon);
        imgLabel.setHorizontalAlignment(JLabel.CENTER);
        imgLabel.setVerticalAlignment(JLabel.CENTER);

        JDialog dialog = new JDialog(this, "Ảnh phóng to", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(imgLabel);

        dialog.setSize(dialogWidth, dialogHeight);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

}
