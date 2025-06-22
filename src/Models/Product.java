package Models;

import java.math.BigDecimal;

public class Product {
    private String categoryName;
    private String createdByUsername;
    private String supplierName;
    private int id;
    private String name;
    private int categoryId;
    private int supplierId;
    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private int quantity;
    private String unit;
    private String status;
    private String createdAt;
    private String updatedAt;
    private int createdBy;
    private int updatedBy;
    private String barcode;
    private String image;
    private int minimumQuantity;
    private BigDecimal discount;
    private String useByDate;


    public Product() {
    }

    public Product(int id, String name, int categoryId, int supplierId, BigDecimal originalPrice,
                   BigDecimal sellingPrice, int quantity, String unit, String status,
                   String createdAt, String updatedAt, int createdBy, int updatedBy,
                   String barcode, String image, int minimumQuantity, BigDecimal discount, String useByDate) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.originalPrice = originalPrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.unit = unit;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.barcode = barcode;
        this.image = image;
        this.minimumQuantity = minimumQuantity;
        this.discount = discount;
        this.useByDate = useByDate;
    }

    public Product(int id, String name, int categoryId, int supplierId, BigDecimal originalPrice,
                   BigDecimal sellingPrice, int quantity, String unit, String status,
                   String createdAt, String updatedAt, int createdBy, int updatedBy,
                   String barcode, String image, int minimumQuantity, BigDecimal discount,
                   String useByDate, String categoryName, String supplierName, String createdByUsername) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.originalPrice = originalPrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.unit = unit;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.barcode = barcode;
        this.image = image;
        this.minimumQuantity = minimumQuantity;
        this.discount = discount;
        this.useByDate = useByDate;
        this.categoryName = categoryName;
        this.supplierName = supplierName;
        this.createdByUsername = createdByUsername;
    }

    // GETTERS & SETTERS

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getUseByDate() {
        return useByDate;
    }

    public void setUseByDate(String useByDate) {
        this.useByDate = useByDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}