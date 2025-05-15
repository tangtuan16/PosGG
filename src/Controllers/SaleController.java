package Controllers;

import Models.Sales.CartItem;
import Models.Customer;
import Models.Product;
import Services.EmailService;
import Services.SaleService;
import Utils.FormatVND;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleController {

    private SaleService saleService;
    private EmailService emailService;
    private List<CartItem> cart;

    public SaleController() {
        this.saleService = new SaleService();
        this.emailService = new EmailService();
        this.cart = new ArrayList<>();
    }

    public List<Product> getProducts(String keyword) {
        return saleService.getFilteredProducts(keyword);
    }

    public boolean addToCart(Product product, int quantity) {
        if (product != null && quantity > 0) {
            int stock = product.getQuantity();
            int existingQty = 0;
            for (CartItem item : cart) {
                if (item.getProduct().getId() == product.getId()) {
                    existingQty = item.getQuantity();
                    break;
                }
            }
            if (existingQty + quantity > stock) {
                JOptionPane.showMessageDialog(null, "Không đủ hàng trong kho để thêm sản phẩm " + product.getName());
                return false;
            }

            for (CartItem item : cart) {
                if (item.getProduct().getId() == product.getId()) {
                    item.setQuantity(existingQty + quantity);
                    return true;
                }
            }
            cart.add(new CartItem(product, quantity));
            return true;
        }
        return false;
    }

    public boolean updateQuantity(int productId, int newQuantity) {
        for (CartItem item : cart) {
            if (item.getProduct().getId() == productId) {
                int stock = item.getProduct().getQuantity();
                if (newQuantity > stock) {
                    JOptionPane.showMessageDialog(null, "Không đủ hàng trong kho để cập nhật sản phẩm " + item.getProduct().getName());
                    return false;
                }
                if (newQuantity <= 0) {
                    removeFromCart(productId);
                    System.out.println("Product: " + item.getProduct().getName() + " quantity: " + newQuantity);
                } else {
                    item.setQuantity(newQuantity);
                }
                return true;
            }
        }
        return false;
    }

    public void removeFromCart(int productId) {
        cart.removeIf(item -> item.getProduct().getId() == productId);
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

    public BigDecimal checkout(String phoneNumber, String paymentMethod, String note, int staffId, String customerName) {
        return saleService.checkout(cart, phoneNumber, staffId, paymentMethod, note, customerName);
    }


    public void clearCart() {
        cart.clear();
    }

    public boolean sendInvoiceToEmail(String email, BigDecimal totalAmount, String attachmentPath) {
        if (email == null || email.isEmpty()) return false;
        String content = buildInvoiceEmailContent(email, totalAmount);
        try {
            emailService.sendInvoiceWithAttachment(email, "Hóa đơn mua hàng", content, attachmentPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String buildInvoiceEmailContent(String email, BigDecimal totalAmount) {
        return "<h2>Hóa đơn mua hàng</h2>" +
                "<p>Xin chào khách hàng,</p>" +
                "<p>Cảm ơn bạn đã mua hàng tại cửa hàng chúng tôi.</p>" +
                "<p><b>Tổng tiền:</b> " + FormatVND.format(totalAmount) + "</p>" +
                "<p>Chúng tôi rất mong được phục vụ bạn lần sau!</p>" +
                "<br><p>Trân trọng,</p>" +
                "<p>Đội ngũ PosGG</p>";
    }

}
