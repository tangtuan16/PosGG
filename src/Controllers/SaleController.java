package Controllers;

import Models.Sales.CartItem;
import Models.Customer;
import Models.Product;
import Services.SaleService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleController {

    private SaleService saleService;
    private List<CartItem> cart;

    public SaleController() {
        this.saleService = new SaleService();
        this.cart = new ArrayList<>();
    }

    public List<Product> getProducts(String keyword) {
        return saleService.getFilteredProducts(keyword);
    }

    public void addToCart(Product product, int quantity) {
        if (product != null && quantity > 0) {
            for (CartItem item : cart) {
                if (item.getProduct().getId() == product.getId()) {
                    item.setQuantity(item.getQuantity() + quantity);
                    return;
                }
            }
            cart.add(new CartItem(product, quantity));
        }
    }

    public void removeFromCart(int productId) {
        cart.removeIf(item -> item.getProduct().getId() == productId);
    }

    public void updateQuantity(int productId, int newQuantity) {
        for (CartItem item : cart) {
            if (item.getProduct().getId() == productId) {
                if (newQuantity <= 0) {
                    removeFromCart(productId);
                } else {
                    item.setQuantity(newQuantity);
                }
                return;
            }
        }
    }

    public Product getProductById(int productId) {
        List<Product> products = saleService.getFilteredProducts("");
        for (Product product : products) {
            if (product.getId() == productId) {
                return product;
            }
        }
        return null;
    }

    public String getCustomerNameByPhone(String phone) {
        Customer customer = saleService.findByPhone(phone);
        return customer != null ? customer.getName() : null;
    }

    public BigDecimal getCustomerDiscountPercent(String phone) {
        Customer customer = saleService.findByPhone(phone);
        if (customer != null) {
            BigDecimal totalBill = customer.getTotalBill();
            BigDecimal discount = totalBill.divide(BigDecimal.valueOf(1_000_000), 0, BigDecimal.ROUND_FLOOR);
            return discount.min(BigDecimal.valueOf(10));
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    public BigDecimal checkout(String phoneNumber, String paymentMethod, String note, int staffId, String customerName) {
        return saleService.checkout(cart, phoneNumber, staffId, paymentMethod, note, customerName);
    }

    public List<CartItem> getCart() {
        return cart;
    }

    public void clearCart() {
        cart.clear();
    }
}
