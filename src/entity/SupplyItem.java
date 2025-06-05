package entity;

import java.time.LocalDate;
import java.util.Objects;

public class SupplyItem {

    private String barcode; 
    private String productTitle;
    private String productDetails;
    private String category;
    private int availableUnits;
    private LocalDate expirationDate; 
    private int thresholdStock;
    private Supplier supplier; 

    public SupplyItem() {
    }
    
    public SupplyItem(String barcode, String productTitle, String productDetails, String category,
                      int availableUnits, int thresholdStock) {
        this.barcode = barcode;
        this.productTitle = productTitle;
        this.productDetails = productDetails;
        this.category = category;
        this.availableUnits = availableUnits;
        this.thresholdStock = thresholdStock;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getAvailableUnits() {
        return availableUnits;
    }

    public void setAvailableUnits(int availableUnits) {
        this.availableUnits = availableUnits;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public int getThresholdStock() {
        return thresholdStock;
    }

    public void setThresholdStock(int thresholdStock) {
        this.thresholdStock = thresholdStock;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        if (supplier != null && !supplier.getSuppliedItems().contains(this)) {
            supplier.addSupplyItem(this); // Maintain bidirectional relationship
        }
    }

    public String getSupplierCode() {
        return (this.supplier != null) ? this.supplier.getSupplierCode() : null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplyItem that = (SupplyItem) o;
        return Objects.equals(barcode, that.barcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcode);
    }

    @Override
    public String toString() {
        return "SupplyItem{" +
               "barcode='" + barcode + '\'' +
               ", productTitle='" + productTitle + '\'' +
               ", availableUnits=" + availableUnits +
               ", expirationDate=" + expirationDate +
               ", thresholdStock=" + thresholdStock +
               ", supplierCode=" + (supplier != null ? supplier.getSupplierCode() : "N/A") +
               '}';
    }
}