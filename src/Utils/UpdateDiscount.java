package Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateDiscount {
    // Gọi hàm này một lần để bắt đầu lịch cập nhật mỗi ngày
    public void scheduleAutoUpdateDiscount() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            updateDiscount();
        }, 0, 1, TimeUnit.DAYS); // chạy ngay lập tức và sau đó mỗi 1 ngày
    }

    // Hàm thực hiện cập nhật discount
    private void updateDiscount() {
        String sql = "UPDATE products " +
                "SET discount = 10 " +
                "WHERE use_by_date IS NOT NULL " +
                "AND DATEDIFF(use_by_date, NOW()) < 3 " +
                "AND discount < 10";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int affectedRows = stmt.executeUpdate();
            System.out.println("[TỰ ĐỘNG] Đã cập nhật giảm giá cho " + affectedRows + " sản phẩm sắp hết hạn.");

        } catch (SQLException e) {
            System.err.println("Lỗi khi tự động cập nhật giảm giá: " + e.getMessage());
        }
    }
}
