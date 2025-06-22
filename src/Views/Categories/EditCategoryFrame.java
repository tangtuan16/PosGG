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

public class EditCategoryFrame extends JFrame {
    private JComboBox<ComboItem> parentCategoryComboBox, statusCategoryCombobox;
    private int pd = 20;
    private CategoryController categoryController = new CategoryController();;
    public int userId = Session.getInstance().getUser().getId();
    private CategoryFrame categoryFrame;
    private JButton editButton;
    private JTextField nameField;
    private JTextArea descriptionField;
    private int idCategoryGlobal;


    private void initComponents(){
        setTitle("Sửa danh mục");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Sửa danh mục",JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        add(title, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(Color.decode("#E0E0E0"));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 50, 0, 50);
        gbc.anchor = GridBagConstraints.WEST;  // đặt về phía bên trái của ô
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Cho phép mở rộng ngang
        gbc.weightx = 1.0;  // Cho phép chiếm không gian rộng hơn

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

        editButton = new JButton("Cập nhật");
        editButton.setPreferredSize(new Dimension(0, 50));
        editButton.setBackground(Color.decode("#007BFF"));
        editButton.setForeground(Color.WHITE);
        editButton.setFont(new Font("Arial", Font.PLAIN, 18));
        add(editButton,BorderLayout.SOUTH);

    }


    public EditCategoryFrame(CategoryFrame categoryFrame, int idCategory){
        this.categoryFrame = categoryFrame;
        idCategoryGlobal = idCategory;

        Category category = getCategory(idCategory);
        initComponents();
        nameField.setText(category.getName().trim());
        String status = category.getStatus();
        String label = "";

        if ("active".equals(status)) {
            label = "Hoạt động";
        } else if ("inactive".equals(status)) {
            label = "Tạm dừng";
        }
        statusCategoryCombobox.setSelectedItem(new ComboItem(status, label));
        parentCategoryComboBox.setSelectedItem(new ComboItem(String.valueOf(category.getParentId()),category.getParentName()));
        descriptionField.setText(category.getDescription());
        editButton.addActionListener(new ActionListener() {
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
                    if(!nameCategory.isEmpty()){
                        if(!statusCategory.isEmpty()){
                            categoryController.updateCategory(idCategory,nameCategory,descriptionCategory,statusCategory,idParent,userId);
                            JOptionPane.showMessageDialog(null, "Sửa danh mục thành công!");
                            if (categoryFrame != null) {
                                categoryFrame.filterCategory(); // load lại bảng khi thêm mới danh mục
                            }
                            loadParentCategoryComboBox();
                            dispose();
                        }else{
                            JOptionPane.showMessageDialog(null, "Chưa chọn trạng thái danh mục!");
                        }
                    }else{
                        JOptionPane.showMessageDialog(null, "Chưa nhập tên danh mục!");
                    }
                }catch (SQLException ex){
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
            if (((category.getParentId() == 0 && parent == "") || (category.getParentId() != null && String.valueOf(category.getParentId()).equals(parent))) && category.getId() != idCategoryGlobal) {
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

    public Category getCategory(int idCategory){
        return categoryController.getCategory(idCategory);
    }
}
