package Models;

import java.math.BigDecimal;

public class Customer {
    private int id;
    private String name;
    private BigDecimal totalBill;
    private String phone;
    private String address;

    public Customer() {
    }

    public Customer(int id, String name, BigDecimal totalBill, String phone, String address) {
        this.id = id;
        this.name = name;
        this.totalBill = totalBill;
        this.phone = phone;
        this.address = address;
    }

    public Customer(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.totalBill = BigDecimal.ZERO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getTotalBill() {
        return totalBill;
    }

    public void setTotalBill(BigDecimal totalBill) {
        this.totalBill = totalBill;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDiscountPercent() {
        if (totalBill == null) return 0;
        int percent = totalBill.divide(BigDecimal.valueOf(1_000_000), 0, java.math.RoundingMode.FLOOR).intValue();
        return Math.min(percent, 10);
    }
}
