package control;

import entity.SupplyItem;
import entity.Supplier;
import util.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types; 
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.List;
import java.util.Map;   

/**
 * Controller class for managing SupplyItem (inventory) data.
 */
public class SupplyItemController {

    private SupplierController supplierController;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public SupplyItemController() {
    }
    
    public SupplyItemController(SupplierController supplierController) {
        this.supplierController = supplierController;
    }

    private SupplierController getActiveSupplierController() {
        if (this.supplierController == null) {
            this.supplierController = new SupplierController();
        }
        return this.supplierController;
    }
    
    private SupplyItem mapRowToSupplyItemObject(ResultSet rs) throws SQLException {
        SupplyItem item = new SupplyItem();
        item.setBarcode(rs.getString("barcode"));
        item.setProductTitle(rs.getString("productTitle"));
        item.setProductDetails(rs.getString("productDetails"));
        item.setCategory(rs.getString("category"));
        
        double availableUnitsDouble = rs.getDouble("availableUnits");
        item.setAvailableUnits((int) availableUnitsDouble);
        
        double thresholdStockDouble = rs.getDouble("thresholdStock");
        item.setThresholdStock((int) thresholdStockDouble);
        
        String expirationDateStr = rs.getString("expirationDate"); 
        if (expirationDateStr != null && !expirationDateStr.trim().isEmpty() && !expirationDateStr.equalsIgnoreCase("N/A")) {
            try {
                item.setExpirationDate(LocalDate.parse(expirationDateStr, DATE_FORMATTER)); 
            } catch (DateTimeParseException e) {
                item.setExpirationDate(null);
            }
        } else {
            item.setExpirationDate(null); 
        }

        String supplierCode = rs.getString("supplierCode");
        if (supplierCode != null && !supplierCode.trim().isEmpty()) {
            Supplier supplier = getActiveSupplierController().getSupplierByCode(supplierCode);
            if (supplier != null) {
                item.setSupplier(supplier); 
            }
        }
        return item;
    }

    public List<SupplyItem> getAllSupplyItems() {
        List<SupplyItem> supplyItems = new ArrayList<>();
        String sql = "SELECT barcode, productTitle, productDetails, category, " +
                     "availableUnits, expirationDate, thresholdStock, supplierCode " +
                     "FROM SupplyItem ORDER BY productTitle"; 

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                supplyItems.add(mapRowToSupplyItemObject(rs)); // Use helper method
            }
        } catch (SQLException e) {
            System.err.println("FATAL DB ERROR fetching all supply items: " + e.getMessage());
            e.printStackTrace();
        }
        return supplyItems;
    }

    public String addSupplyItem(SupplyItem item) {
        if (item == null || item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
            return "Supply item data or barcode cannot be empty.";
        }
        if (getSupplyItemByBarcode(item.getBarcode()) != null) {
            return "Supply item with barcode '" + item.getBarcode() + "' already exists.";
        }
        
        String actualSupplierCode = null;
        if (item.getSupplier() != null && item.getSupplier().getSupplierCode() != null) {
            actualSupplierCode = item.getSupplier().getSupplierCode();
        } else if (item.getSupplierCode() != null && !item.getSupplierCode().trim().isEmpty()){ 
            actualSupplierCode = item.getSupplierCode();
        }

        if (actualSupplierCode != null) {
            if (getActiveSupplierController().getSupplierByCode(actualSupplierCode) == null) {
                return "Cannot add item: Supplier with code '" + actualSupplierCode + "' does not exist.";
            }
        }

        String sql = "INSERT INTO SupplyItem (barcode, productTitle, productDetails, category, " +
                     "availableUnits, expirationDate, thresholdStock, supplierCode) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getBarcode());
            pstmt.setString(2, item.getProductTitle());
            pstmt.setString(3, item.getProductDetails());
            pstmt.setString(4, item.getCategory());
            pstmt.setInt(5, item.getAvailableUnits());
            if (item.getExpirationDate() != null) {
                pstmt.setString(6, item.getExpirationDate().format(DATE_FORMATTER));
            } else {
                pstmt.setString(6, "N/A"); 
            }
            pstmt.setInt(7, item.getThresholdStock()); 
            
            if (actualSupplierCode != null) {
                 pstmt.setString(8, actualSupplierCode);
            } else {
                pstmt.setNull(8, Types.VARCHAR);
            }

            int affectedRows = pstmt.executeUpdate();
            return (affectedRows > 0) ? null : "Failed to add supply item. No rows affected.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error adding supply item '" + item.getBarcode() + "': " + e.getMessage();
        }
    }

    public SupplyItem getSupplyItemByBarcode(String barcode) {
        SupplyItem item = null;
        if (barcode == null || barcode.trim().isEmpty()) return null;
        String sql = "SELECT barcode, productTitle, productDetails, category, " +
                     "availableUnits, expirationDate, thresholdStock, supplierCode " +
                     "FROM SupplyItem WHERE barcode = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, barcode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    item = mapRowToSupplyItemObject(rs); 
                }
            }
        } catch (SQLException e) {
            System.err.println("FATAL DB ERROR fetching item by barcode '" + barcode + "': " + e.getMessage());
            e.printStackTrace();
        }
        return item;
    }

    public String updateSupplyItem(SupplyItem item) {
        if (item == null || item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
            return "Cannot update supply item: item data or barcode is null/empty.";
        }
        // Check if item to update actually exists
        SupplyItem existingItem = getSupplyItemByBarcode(item.getBarcode());
        if (existingItem == null) {
             return "Cannot update supply item: Item with barcode '" + item.getBarcode() + "' not found.";
        }
        
        String actualSupplierCode = null;
        if (item.getSupplier() != null && item.getSupplier().getSupplierCode() != null) {
            actualSupplierCode = item.getSupplier().getSupplierCode();
        } else if (item.getSupplierCode() != null && !item.getSupplierCode().trim().isEmpty()){
            actualSupplierCode = item.getSupplierCode();
        }

        if (actualSupplierCode != null) {
            if (getActiveSupplierController().getSupplierByCode(actualSupplierCode) == null) {
                return "Cannot update item: Supplier with code '" + actualSupplierCode + "' does not exist.";
            }
        }

        String sql = "UPDATE SupplyItem SET productTitle = ?, productDetails = ?, category = ?, " +
                     "availableUnits = ?, expirationDate = ?, thresholdStock = ?, supplierCode = ? " +
                     "WHERE barcode = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getProductTitle());
            pstmt.setString(2, item.getProductDetails());
            pstmt.setString(3, item.getCategory());
            pstmt.setInt(4, item.getAvailableUnits());
            if (item.getExpirationDate() != null) {
                pstmt.setString(5, item.getExpirationDate().format(DATE_FORMATTER));
            } else {
                pstmt.setString(5, "N/A"); 
            }
            pstmt.setInt(6, item.getThresholdStock());
            
            if (actualSupplierCode != null) {
                pstmt.setString(7, actualSupplierCode);
            } else {
                pstmt.setNull(7, Types.VARCHAR); 
            }
            
            pstmt.setString(8, item.getBarcode()); 
            int affectedRows = pstmt.executeUpdate();
            return (affectedRows > 0) ? null : "Failed to update supply item. Data unchanged or item not found.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error updating supply item '" + item.getBarcode() + "': " + e.getMessage();
        }
    }

    public String deleteSupplyItem(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return "Cannot delete supply item: barcode is null/empty.";
        }
        String sql = "DELETE FROM SupplyItem WHERE barcode = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, barcode);
            int affectedRows = pstmt.executeUpdate();
            return (affectedRows > 0) ? null : "Failed to delete supply item. Item not found.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error deleting supply item '" + barcode + "': " + e.getMessage();
        }
    }

    public Map<String, String> deleteMultipleSupplyItems(List<String> barcodes) {
        Map<String, String> results = new HashMap<>();
        if (barcodes == null || barcodes.isEmpty()) return results;
        for (String barcode : barcodes) {
            results.put(barcode, deleteSupplyItem(barcode)); 
        }
        return results;
    }

    public boolean isSupplierReferenced(String supplierCode) {
        if (supplierCode == null || supplierCode.trim().isEmpty()) return false; 
        String sql = "SELECT COUNT(*) AS reference_count FROM SupplyItem WHERE supplierCode = ?";
        try (Connection conn = DatabaseConnector.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplierCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("reference_count") > 0;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return true; 
        }
        return false;
    }

    public List<SupplyItem> getLowStockItems() {
        List<SupplyItem> lowStockItems = new ArrayList<>();
        String sql = "SELECT * FROM SupplyItem WHERE availableUnits <= thresholdStock AND thresholdStock > 0 ORDER BY productTitle";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lowStockItems.add(mapRowToSupplyItemObject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching low stock items: " + e.getMessage());
            e.printStackTrace(); 
        }
        return lowStockItems;
    }
    
    public List<SupplyItem> getNearExpirationItems(int daysInAdvance) {
        List<SupplyItem> nearExpirationItems = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(daysInAdvance);
        List<SupplyItem> allItems = getAllSupplyItems(); 
        for (SupplyItem item : allItems) {
            if (item.getExpirationDate() != null) { 
                if (!item.getExpirationDate().isBefore(today) && 
                    !item.getExpirationDate().isAfter(targetDate)) {
                    nearExpirationItems.add(item);
                }
            }
        }
        nearExpirationItems.sort((item1, item2) -> {
            if (item1.getExpirationDate() == null && item2.getExpirationDate() == null) return 0;
            if (item1.getExpirationDate() == null) return 1; 
            if (item2.getExpirationDate() == null) return -1; 
            return item1.getExpirationDate().compareTo(item2.getExpirationDate());
        });
        return nearExpirationItems;
    }
}