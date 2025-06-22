package Models;

public class OrderStatistic {
    private String timeGroup;
    private double totalAmount;

    public OrderStatistic(String timeGroup, double totalAmount) {
        this.timeGroup = timeGroup;
        this.totalAmount = totalAmount;
    }

    public String getTimeGroup() {
        return timeGroup;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}