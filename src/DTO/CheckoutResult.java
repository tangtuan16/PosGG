package DTO;

import java.math.BigDecimal;

public class CheckoutResult {
    private BigDecimal finalTotal;
    private BigDecimal discountPercent;
    private int invoiceId;

    public CheckoutResult(BigDecimal finalTotal, BigDecimal discountPercent, int invoiceId) {
        this.finalTotal = finalTotal;
        this.discountPercent = discountPercent;
        this.invoiceId = invoiceId;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }
}
