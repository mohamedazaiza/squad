package control; 

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private boolean overallSuccess = true;
    private int suppliersAdded = 0;
    private int suppliersUpdated = 0;
    private int suppliersFailed = 0;
    private int itemsAdded = 0;
    private int itemsUpdated = 0;
    private int itemsFailed = 0;
    private List<String> messages = new ArrayList<>();

    public boolean isOverallSuccess() {
        return overallSuccess;
    }

    public void setOverallSuccess(boolean overallSuccess) {
        this.overallSuccess = overallSuccess;
    }

    public int getSuppliersAdded() {
        return suppliersAdded;
    }

    public void incrementSuppliersAdded() {
        this.suppliersAdded++;
    }

    public int getSuppliersUpdated() {
        return suppliersUpdated;
    }

    public void incrementSuppliersUpdated() {
        this.suppliersUpdated++;
    }

    public int getSuppliersFailed() {
        return suppliersFailed;
    }

    public void incrementSuppliersFailed() {
        this.suppliersFailed++;
        this.overallSuccess = false; 
    }

    public int getItemsAdded() {
        return itemsAdded;
    }

    public void incrementItemsAdded() {
        this.itemsAdded++;
    }

    public int getItemsUpdated() {
        return itemsUpdated;
    }

    public void incrementItemsUpdated() {
        this.itemsUpdated++;
    }

    public int getItemsFailed() {
        return itemsFailed;
    }

    public void incrementItemsFailed() {
        this.itemsFailed++;
        this.overallSuccess = false;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }
    
    public void addErrorMessage(String message) {
        this.messages.add("ERROR: " + message);
        this.overallSuccess = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("XML Import ").append(overallSuccess ? "Completed (Possibly with individual errors)" : "Failed").append("\n");
        sb.append("-------------------------------------\n");
        sb.append("Suppliers:\n");
        sb.append("  Added: ").append(suppliersAdded).append("\n");
        sb.append("  Updated: ").append(suppliersUpdated).append("\n");
        sb.append("  Failed: ").append(suppliersFailed).append("\n");
        sb.append("Supply Items:\n");
        sb.append("  Added: ").append(itemsAdded).append("\n");
        sb.append("  Updated: ").append(itemsUpdated).append("\n");
        sb.append("  Failed: ").append(itemsFailed).append("\n");
        sb.append("-------------------------------------\n");
        if (!messages.isEmpty()) {
            sb.append("Messages/Errors:\n");
            for (String msg : messages) {
                sb.append("  - ").append(msg).append("\n");
            }
        }
        return sb.toString();
    }
}