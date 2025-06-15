package Controllers;

import DTO.CheckoutResult;
import Models.Product;
import Models.Sales.CartItem;
import Services.SaleService;
import Utils.FormatVND;
import Views.Sales.SaleFrame;
import com.github.sarxos.webcam.Webcam;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SaleController {
    private final SaleService saleService;
    private final SaleFrame view;

    public SaleController(SaleFrame view) {
        this.saleService = new SaleService();
        this.view = view;
    }

    public void loadProducts(String keyword, int page, int pageSize) {
        try {
            List<Product> products = saleService.getProducts(keyword, page, pageSize);
            view.displayProducts(products, page, pageSize);
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi tải sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void getProductSuggestions(String keyword) {
        try {
            List<Product> suggestions = saleService.getProducts(keyword, 1, 10);
            view.displaySuggestions(suggestions);
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi tải gợi ý sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addToCart(Product product, int quantity) {
        try {
            boolean success = saleService.addToCart(product, quantity);
            if (success) {
                view.updateCart();
            } else {
                JOptionPane.showMessageDialog(view, "Không đủ hàng trong kho để thêm sản phẩm " + product.getName(), "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi thêm sản phẩm vào giỏ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateCartQuantity(int productId, int newQuantity) {
        try {
            boolean success = saleService.updateQuantity(productId, newQuantity);
            if (success) {
                view.updateCart();
            } else {
                JOptionPane.showMessageDialog(view, "Không đủ hàng trong kho để cập nhật số lượng", "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi cập nhật số lượng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void removeFromCart(int productId) {
        try {
            saleService.removeFromCart(productId);
            view.updateCart();
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi xóa sản phẩm khỏi giỏ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Product getProductById(int productId) {
        try {
            return saleService.getProductById(productId);
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi lấy thông tin sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public void checkCustomerInfo(String phone) {
        try {
            String customerName = getCustomerName(phone);
            BigDecimal discount = getCustomerDiscountPercent(phone);
            view.updateCustomerInfo(customerName, discount);
        } catch (SaleService.SaleServiceException e) {
            view.updateCustomerInfo("", BigDecimal.ZERO);
            JOptionPane.showMessageDialog(view, "Lỗi kiểm tra thông tin khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getCustomerName(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        try {
            var customer = saleService.findByPhone(phone);
            return customer != null ? customer.getName() : "";
        } catch (SaleService.SaleServiceException e) {
            return "";
        }
    }

    private BigDecimal getCustomerDiscountPercent(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return saleService.getCustomerDiscountPercent(phone);
        } catch (SaleService.SaleServiceException e) {
            return BigDecimal.ZERO;
        }
    }

    public void checkout(String phoneNumber, String paymentMethod, String note, int staffId, String customerName) {
        try {
            CheckoutResult result = saleService.checkout(phoneNumber, staffId, paymentMethod, note, customerName);
            view.clearCart();
            view.updateCustomerInfo("", BigDecimal.ZERO);
            JOptionPane.showMessageDialog(view, "Thanh toán thành công! Tổng tiền: " + FormatVND.format(result.getFinalTotal()), "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi thanh toán: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void sendInvoiceToEmail(String email, BigDecimal totalAmount, String attachmentPath) {
        try {
            boolean success = saleService.sendInvoiceToEmail(email, totalAmount, attachmentPath);
            if (success) {
                JOptionPane.showMessageDialog(view, "Đã gửi hóa đơn đến email: " + email, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view, "Gửi email thất bại! Vui lòng kiểm tra lại địa chỉ email.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi gửi hóa đơn qua email: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearCart() {
        try {
            saleService.clearCart();
            view.clearCart();
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi xóa giỏ hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public int getCartItemCount() {
        try {
            return saleService.getCart().stream().mapToInt(CartItem::getQuantity).sum();
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi lấy số lượng sản phẩm trong giỏ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    public BigDecimal getCartTotal() {
        try {
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem item : saleService.getCart()) {
                total = total.add(saleService.calculateFinalPrice(item.getProduct(), item.getQuantity()));
            }
            return total;
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi tính tổng tiền giỏ hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return BigDecimal.ZERO;
        }
    }

    public List<CartItem> getCart() {
        try {
            return saleService.getCart();
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi lấy giỏ hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    public BigDecimal calculateCartItemPrice(Product product, int quantity) {
        try {
            return saleService.calculateFinalPrice(product, quantity);
        } catch (SaleService.SaleServiceException e) {
            JOptionPane.showMessageDialog(view, "Lỗi tính giá sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return BigDecimal.ZERO;
        }
    }

    public void startBarcodeScanner(Webcam webcam, Consumer<String> callback) {
        new Thread(() -> {
            String codes = saleService.startBarcodeScanner(webcam);
            if (codes != null) {
                callback.accept(codes);
            }
        }).start();
    }


    public void stopBarcodeScanner() {
        saleService.stopBarcodeScanner();
    }
}