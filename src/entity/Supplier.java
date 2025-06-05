package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Supplier {

    private String supplierCode; 
    private String supplierName;
    private LocalDate recentSupplyDate;
    private List<SupplyItem> suppliedItems; 

    public Supplier() {
        this.suppliedItems = new ArrayList<>();
    }

    public Supplier(String supplierCode, String supplierName, LocalDate recentSupplyDate) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.recentSupplyDate = recentSupplyDate;
        this.suppliedItems = new ArrayList<>();
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDate getRecentSupplyDate() {
        return recentSupplyDate;
    }

    public void setRecentSupplyDate(LocalDate recentSupplyDate) {
        this.recentSupplyDate = recentSupplyDate;
    }

    public List<SupplyItem> getSuppliedItems() {
        return suppliedItems;
    }

    public void setSuppliedItems(List<SupplyItem> suppliedItems) {
        this.suppliedItems = suppliedItems;
    }

    public void addSupplyItem(SupplyItem item) {
        if (item != null) {
            this.suppliedItems.add(item);
            if (item.getSupplier() != this) { 
                item.setSupplier(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return Objects.equals(supplierCode, supplier.supplierCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierCode);
    }

    @Override
    public String toString() {
        return "Supplier{" +
               "supplierCode='" + supplierCode + '\'' +
               ", supplierName='" + supplierName + '\'' +
               ", recentSupplyDate=" + recentSupplyDate +
               '}';
    }
}