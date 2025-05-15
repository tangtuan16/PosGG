package Models;

public class Customers {

    private long id;
    private String name;
    private double totalBill;
    private String phone;
    private String address;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getTotalBill() {
        return totalBill;
    }

    public void setTotalBill(double totalBill) {
        this.totalBill = totalBill;
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

    public Customers(long id, String name, String phone, double totalBill, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.totalBill = totalBill;
        this.address = address;
    }

    public Customers() {}
}
