package Views.Categories;

import Controllers.CategoryController;
import Models.Category;
import Models.Session;
import Utils.ComboItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

public class AddCategoryFrame extends JFrame {
    private JComboBox<ComboItem> parentCategoryComboBox, statusCategoryCombobox;
    private int pd = 20;
    private CategoryController categoryController = new CategoryController();
    public int userId = Session.getInstance().getUser().getId();
    private CategoryFrame categoryFrame ;
    private JButton addButton;
    private JTextField nameField;
    private JTextArea descriptionField;


    private void view(){
        setTitle("Tạo danh mục");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Tạo danh mục",JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        add(title, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(Color.decode("#E0E0E0"));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 50, 0, 50);
        gbc.anchor = GridBagConstraints.WEST;  // đặt về phía bên trái của ô
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Cho phép mở rộng ngang
        gbc.weightx = 1.0;  // Chiem 1 o

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Tên danh mục:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(nameLabel, gbc);

        gbc.gridy = 1; gbc.ipady = pd;
        nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(nameField, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.ipady = 0;
        JLabel parentCategoryLabel = new JLabel("Danh mục cha:");
        parentCategoryLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(parentCategoryLabel, gbc);

        gbc.gridy = 1; gbc.ipady = pd;
        parentCategoryComboBox = new JComboBox<>();
        loadParentCategoryComboBox();
        parentCategoryComboBox.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(parentCategoryComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.ipady = 0;
        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(statusLabel, gbc);

        gbc.gridy = 3; gbc.ipady = pd;
        statusCategoryCombobox = new JComboBox<>();
        statusCategoryCombobox.addItem(new ComboItem("", "--- Trạng thái ---"));
        statusCategoryCombobox.addItem(new ComboItem("active", "Hoạt động"));
        statusCategoryCombobox.addItem(new ComboItem("inactive", "Tạm dừng"));
        statusCategoryCombobox.setFont(new Font("Arial", Font.PLAIN, 15));
        mainContent.add(statusCategoryCombobox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.ipady = 0;
        JLabel descriptionCategoryLabel = new JLabel("Mô tả:");
        descriptionCategoryLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        mainContent.add(descriptionCategoryLabel, gbc);

        gbc.gridy = 5; gbc.ipady = 0;
        gbc.gridwidth = 2;  // Chiếm 2 cột
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        descriptionField = new JTextArea();
        descriptionField.setFont(new Font("Arial", Font.PLAIN, 18));
        JScrollPane scrollPane = new JScrollPane(descriptionField);
        mainContent.add(scrollPane, gbc);

        add(mainContent,BorderLayout.CENTER);

        addButton = new JButton("+ Tạo mới");
        addButton.setPreferredSize(new Dimension(0, 50));
        addButton.setBackground(Color.decode("#007BFF"));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.PLAIN, 18));
        add(addButton,BorderLayout.SOUTH);

    }


    public AddCategoryFrame(CategoryFrame categoryFrame){
        this.categoryFrame = categoryFrame;
        view();

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    String nameCategory = nameField.getText().toString().trim();
                    ComboItem idParentComboItem = (ComboItem) parentCategoryComboBox.getSelectedItem();
                    ComboItem statusCategoryComboItem = (ComboItem) statusCategoryCombobox.getSelectedItem();
                    int idParent = 0;
                    String statusCategory = statusCategoryComboItem.getValue();
                    String descriptionCategory = descriptionField.getText().toString().trim();
                    if(idParentComboItem.getValue() != ""){
                        idParent = Integer.parseInt(idParentComboItem.getValue());
                    }

                    categoryController.createNewCategory(nameCategory,descriptionCategory,statusCategory,idParent,userId);

                    if (categoryFrame != null) {
                        categoryFrame.filterCategory(); // load lại bảng khi thêm mới danh mục
                    }
                    loadParentCategoryComboBox();
                    JOptionPane.showMessageDialog(null, "Tạo danh mục thành công!");
                }catch (SQLException | IllegalArgumentException ex){
                    JOptionPane.showMessageDialog(null,ex.getMessage());
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
                parentCategoryComboBox.addItem(new ComboItem(String.valueOf(category.getId()), " |--".repeat(level) + " " + category.getName()));
                hienThiBacDanhMuc(categoryList, String.valueOf(category.getId()), level + 1);
            }
        }
    }


    public void loadParentCategoryComboBox(){
        try{
            parentCategoryComboBox.removeAllItems();
            parentCategoryComboBox.addItem(new ComboItem("", "--- Danh mục cha ---"));
            ArrayList<Category> parentCategoryList = categoryController.findAllCategory();
            hienThiBacDanhMuc(parentCategoryList, "", 0);
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }
}
