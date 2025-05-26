package Services.Impl;

import Models.Customer;
import java.util.List;

public interface CustomerService {
    List<Customer> getAllCustomers();
    List<Customer> searchCustomers(String keyword);
    void insertCustomer(Customer customer);
    void updateCustomer(Customer customer);
    void deleteCustomers(List<Long> ids);
    List<Customer> getCustomersByPage(int page, int pageSize, String sortType);
    List<Customer> searchCustomersByPage(String keyword, int page, int pageSize, String sortType);
    int getTotalCustomers();
    int getTotalSearchCustomers(String keyword);
}