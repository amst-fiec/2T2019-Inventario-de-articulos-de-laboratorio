package amst.g1.labsec.models;

import androidx.annotation.NonNull;

public class Device {

    private String id;
    private String name;
    private String brand;
    private String model;
    private String borrower;
    private String state;
    private String returnDate;

    public Device() {
        this.state = "Available";
    }

    public Device(String id, String name, String brand, String model) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
    }

    public Device(String id, String name, String brand, String model, String state) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBorrower() {
        return borrower;
    }

    public void setBorrower(String borrower) {
        this.borrower = borrower;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isValid() {
        boolean validName = name != null && !name.equals("");
        boolean validBrand = brand != null && !brand.equals("");
        boolean validModel = model != null && !model.equals("");
        return validName && validBrand && validModel;
    }

    @NonNull
    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", borrower='" + borrower + '\'' +
                ", state='" + state + '\'' +
                ", returnDate=" + returnDate +
                '}';
    }
}
