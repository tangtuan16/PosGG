package Models.Sales;

import Models.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal discountPercent = product.getDiscount() == null ? BigDecimal.ZERO : product.getDiscount();
        BigDecimal discountedPrice = product.getSellingPrice()
                .multiply(BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100))))
                .setScale(2, RoundingMode.HALF_UP);
        return discountedPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }


    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
