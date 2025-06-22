package Controllers;

import Models.Customer;
import Services.Impl.CustomerService;
import Views.Customer.CustomerFrame;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CustomerController {
    private CustomerFrame view;
    private CustomerService customerService;
    private int currentPage = 1;
    private final int pageSize = 5;
    private String currentKeyword = "";
    private String currentSortType = "Mặc định";
    private boolean isSearchMessageShown = false;

    public CustomerController(CustomerFrame view, CustomerService customerService) {
        this.view = view;
        this.customerService = customerService;

        setupButtonListeners();
        setupSearchListener();
        setupSortListener();
        setupPaginationListeners();
        updateTable();
    }

    private void setupButtonListeners() {
        view.getAddButton().addActionListener(e -> view.showAddCustomerDialog());
        view.getEditButton().addActionListener(e -> view.showEditCustomerDialog());
        view.getDeleteButton().addActionListener(e -> deleteCustomers());
        view.getImportExcelButton().addActionListener(e -> importCSV());
        view.getExportExcelButton().addActionListener(e -> exportCSV());
    }

    private void setupSearchListener() {
        view.getSearchField().addActionListener(e -> searchCustomers());
    }

    private void setupSortListener() {
        view.getSortComboBox().addActionListener(e -> sortCustomers());
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }

    public void reset() {
        currentPage = 1;
        currentKeyword = "";
        currentSortType = "Mặc định";
        view.getSortComboBox().setSelectedIndex(0);
        view.getSearchField().setText("Nhập nội dung tìm kiếm...");
        updateTable();
    }

    // New method: Handle adding a customer
    public void addCustomer(String name, String totalBillText, String phone, String address, JDialog dialog) {
        if (name.isEmpty() || totalBillText.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng điền đầy đủ thông tin!");
        }

        double totalBill;
        try {
            totalBill = Double.parseDouble(totalBillText);
            if (totalBill < 0) {
                throw new IllegalArgumentException("Tổng đã dùng phải là số không âm!");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Tổng đã dùng phải là số hợp lệ!");
        }

        if (!isValidPhoneNumber(phone)) {
            throw new IllegalArgumentException("Số điện thoại phải có đúng 10 chữ số!");
        }

        Customer newCustomer = new Customer();
        newCustomer.setName(name);
        newCustomer.setTotalBill(BigDecimal.valueOf(totalBill));
        newCustomer.setPhone(phone);
        newCustomer.setAddress(address);

        customerService.insertCustomer(newCustomer);
        JOptionPane.showMessageDialog(dialog, "Thêm khách hàng thành công!");
        updateTable();
    }

    // New method: Handle editing a customer
    public void editCustomer(long customerId, String name, String totalBillText, String phone, String address, JDialog dialog) {
        if (name.isEmpty() || totalBillText.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng điền đầy đủ thông tin!");
        }

        double totalBill;
        try {
            totalBill = Double.parseDouble(totalBillText);
            if (totalBill < 0) {
                throw new IllegalArgumentException("Tổng đã dùng phải là số không âm!");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Tổng đã dùng phải là số hợp lệ!");
        }

        if (!isValidPhoneNumber(phone)) {
            throw new IllegalArgumentException("Số điện thoại phải có đúng 10 chữ số!");
        }

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId((int) customerId);
        updatedCustomer.setName(name);
        updatedCustomer.setTotalBill(BigDecimal.valueOf(totalBill));
        updatedCustomer.setPhone(phone);
        updatedCustomer.setAddress(address);

        customerService.updateCustomer(updatedCustomer);
        JOptionPane.showMessageDialog(dialog, "Cập nhật khách hàng thành công!");
        updateTable();
    }

    private void importCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file CSV để nhập");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });

        int result = fileChooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (CSVReader csvReader = new CSVReader(new FileReader(selectedFile))) {
                String[] headers = null;
                try {
                    headers = csvReader.readNext();
                } catch (CsvValidationException e) {
                    throw new RuntimeException(e);
                }
                if (headers == null || headers.length != 5 || !headers[0].equals("ID") || !headers[1].equals("Tên") ||
                        !headers[2].equals("Tổng đã dùng") || !headers[3].equals("Số điện thoại") || !headers[4].equals("Địa chỉ")) {
                    JOptionPane.showMessageDialog(view, "File CSV không đúng định dạng! Cần có 5 cột: ID, Tên, Tổng đã dùng, Số điện thoại, Địa chỉ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                List<Customer> newCustomers = new ArrayList<>();
                String[] row;
                int rowNum = 1;
                while ((row = csvReader.readNext()) != null) {
                    rowNum++;
                    try {
                        String name = row[1].trim();
                        double totalBill = Double.parseDouble(row[2].trim());
                        String phone = row[3].trim();
                        String address = row[4].trim();

                        if (phone.length() != 10 || !phone.matches("\\d+")) {
                            JOptionPane.showMessageDialog(view, "Dòng " + rowNum + ": Số điện thoại phải có đúng 10 chữ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        Customer customer = new Customer();
                        customer.setName(name);
                        customer.setTotalBill(BigDecimal.valueOf(totalBill));
                        customer.setPhone(phone);
                        customer.setAddress(address);
                        newCustomers.add(customer);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(view, "Dòng " + rowNum + ": Tổng đã dùng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        JOptionPane.showMessageDialog(view, "Dòng " + rowNum + ": Thiếu cột dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                for (Customer customer : newCustomers) {
                    customerService.insertCustomer(customer);
                }
                JOptionPane.showMessageDialog(view, "Nhập " + newCustomers.size() + " khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                currentPage = 1;
                updateTable();
            } catch (IOException | CsvValidationException e) {
                JOptionPane.showMessageDialog(view, "Lỗi khi đọc file CSV: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu tệp CSV");
        fileChooser.setSelectedFile(new File("khachhang.csv"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "Tệp CSV (*.csv)";
            }
        });

        int result = fileChooser.showSaveDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
            }

            List<Customer> danhSachKhachHang;
            if (currentKeyword.isEmpty()) {
                danhSachKhachHang = customerService.getAllCustomers();
            } else {
                danhSachKhachHang = customerService.searchCustomers(currentKeyword);
            }

            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(selectedFile), "UTF-8");
                 CSVWriter csvWriter = new CSVWriter(writer)) {

                writer.write('\uFEFF');
                String[] tieuDe = {"Mã KH", "Tên khách hàng", "Tổng đã chi", "Số điện thoại", "Địa chỉ"};
                csvWriter.writeNext(tieuDe);

                for (Customer khachHang : danhSachKhachHang) {
                    String[] dong = {
                            String.valueOf(khachHang.getId()),
                            khachHang.getName(),
                            String.valueOf(khachHang.getTotalBill()),
                            khachHang.getPhone(),
                            khachHang.getAddress()
                    };
                    csvWriter.writeNext(dong);
                }

                JOptionPane.showMessageDialog(view, "Xuất tệp CSV thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(view, "Đã xảy ra lỗi khi xuất tệp CSV: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setupPaginationListeners() {
        view.getFirstPageButton().addActionListener(e -> {
            currentPage = 1;
            updateTable();
        });

        view.getPreviousPageButton().addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTable();
            }
        });

        view.getNextPageButton().addActionListener(e -> {
            int totalPages = getTotalPages();
            if (currentPage < totalPages) {
                currentPage++;
                updateTable();
            }
        });

        view.getLastPageButton().addActionListener(e -> {
            currentPage = getTotalPages();
            updateTable();
        });
    }

    private int getTotalPages() {
        int totalRecords = currentKeyword.isEmpty() ?
                customerService.getTotalCustomers() :
                customerService.getTotalSearchCustomers(currentKeyword);
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    private void deleteCustomers() {
        int[] selectedRows = view.getCustomerTable().getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn ít nhất một khách hàng để xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Long> customerIds = new ArrayList<>();
        StringBuilder customerNames = new StringBuilder();
        for (int row : selectedRows) {
            long id = (long) view.getCustomerTable().getValueAt(row, 0);
            String name = (String) view.getCustomerTable().getValueAt(row, 1);
            customerIds.add(id);
            customerNames.append(name).append(" (ID: ").append(id).append("), ");
        }
        if (customerNames.length() > 0) {
            customerNames.setLength(customerNames.length() - 2);
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Bạn có chắc chắn muốn xóa các khách hàng: " + customerNames + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                customerService.deleteCustomers(customerIds);
                JOptionPane.showMessageDialog(view, "Xóa " + customerIds.size() + " khách hàng thành công!");
                if (view.getCustomerTable().getRowCount() == 0 && currentPage > 1) {
                    currentPage--;
                }
                updateTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Lỗi khi xóa khách hàng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchCustomers() {
        currentKeyword = view.getSearchField().getText().trim();
        if (currentKeyword.equals("Nhập nội dung tìm kiếm...")) {
            currentKeyword = "";
        }
        currentPage = 1;
        isSearchMessageShown = false;
        updateTable();
    }

    private void sortCustomers() {
        currentSortType = (String) view.getSortComboBox().getSelectedItem();
        currentPage = 1;
        isSearchMessageShown = false;
        updateTable();
    }

    private void updateTable() {
        try {
            List<Customer> customers;
            if (currentKeyword.isEmpty()) {
                customers = customerService.getCustomersByPage(currentPage, pageSize, currentSortType);
            } else {
                customers = customerService.searchCustomersByPage(currentKeyword, currentPage, pageSize, currentSortType);
            }

            DefaultTableModel tableModel = (DefaultTableModel) view.getCustomerTable().getModel();
            DecimalFormat df = new DecimalFormat("#,###.## VND");

            tableModel.setRowCount(0);

            for (Customer customer : customers) {
                tableModel.addRow(new Object[]{
                        customer.getId(),
                        customer.getName(),
                        df.format(customer.getTotalBill()),
                        customer.getPhone(),
                        customer.getAddress()
                });
            }

            int totalPages = getTotalPages();
            view.updatePaginationInfo(currentPage, totalPages);

            view.getFirstPageButton().setEnabled(currentPage > 1);
            view.getPreviousPageButton().setEnabled(currentPage > 1);
            view.getNextPageButton().setEnabled(currentPage < totalPages);
            view.getLastPageButton().setEnabled(currentPage < totalPages);

            if (customers.isEmpty() && !currentKeyword.isEmpty() && !isSearchMessageShown) {
                isSearchMessageShown = true;
                JOptionPane.showMessageDialog(view, "Không tìm thấy khách hàng nào khớp với từ khóa: " + currentKeyword, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Lỗi khi tải dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}