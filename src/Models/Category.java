package Models;

public class Category {
    private int id;
    private String name;
    private String description;
    private String status;
    private String createdAt;
    private String updatedAt;
    private int parentId; // cho phép null nếu không có cha
    private int createdBy;
    private int updatedBy;
    private String createdByUsername; // Tên người tạo
    private String updatedByUsername; // Tên người cập nhật
    private String parentName; // Thêm thuộc tính để lưu tên danh mục cha

    public Category() {
    }

    public Category(int id, String name, String description, String status,
                    String createdAt, String updatedAt,
                    int parentId, int createdBy, int updatedBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.parentId = parentId;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public Category(int id, String name, String description, String status,
                    String createdAt, String updatedAt,
                    int parentId, int createdBy, int updatedBy,
                    String createdByUsername, String updatedByUsername, String parentName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.parentId = parentId;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdByUsername = createdByUsername;
        this.updatedByUsername = updatedByUsername;
        this.parentName = parentName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getUpdatedByUsername() {
        return updatedByUsername;
    }

    public void setUpdatedByUsername(String updatedByUsername) {
        this.updatedByUsername = updatedByUsername;
    }
// --- Getter & Setter ---

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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

    // --- Optional: toString() for debugging ---
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", parentId=" + parentId +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}

