package control;

import entity.Supplier;
import util.DatabaseConnector;
// SupplyItemController is in the same package

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types; 
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.List;
import java.util.Map; 

/**
 * Controller class for managing Supplier data.
 * Handles interactions between the database and supplier-related boundaries.
 * This is a Control class in the ECB pattern.
 */
public class SupplierController {

    private SupplyItemController supplyItemController;

    public SupplierController() {
        this.supplyItemController = new SupplyItemController(); 
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT supplierCode, supplierName, recentSupplyDate FROM Supplier ORDER BY supplierName"; 
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Supplier supplier = new Supplier();
                supplier.setSupplierCode(rs.getString("supplierCode"));
                supplier.setSupplierName(rs.getString("supplierName"));
                java.sql.Date dbDate = rs.getDate("recentSupplyDate");
                if (dbDate != null) {
                    supplier.setRecentSupplyDate(dbDate.toLocalDate());
                } else {
                    supplier.setRecentSupplyDate(null);
                }
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            System.err.println("FATAL DB ERROR fetching all suppliers: " + e.getMessage());
            e.printStackTrace(); 
        }
        return suppliers;
    }
    
    public Supplier getSupplierByCode(String supplierCode) {
        Supplier supplier = null;
        if (supplierCode == null || supplierCode.trim().isEmpty()) {
            return null; 
        }
        String sql = "SELECT supplierCode, supplierName, recentSupplyDate FROM Supplier WHERE supplierCode = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplierCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    supplier = new Supplier();
                    supplier.setSupplierCode(rs.getString("supplierCode"));
                    supplier.setSupplierName(rs.getString("supplierName"));
                    java.sql.Date dbDate = rs.getDate("recentSupplyDate");
                    if (dbDate != null) {
                        supplier.setRecentSupplyDate(dbDate.toLocalDate());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("FATAL DB ERROR fetching supplier by code '" + supplierCode + "': " + e.getMessage());
            e.printStackTrace();
        }
        return supplier;
    }

    public String addSupplier(Supplier supplier) {
        if (supplier == null || supplier.getSupplierCode() == null || supplier.getSupplierCode().trim().isEmpty()) {
            return "Supplier data or code cannot be empty.";
        }
        if (getSupplierByCode(supplier.getSupplierCode()) != null) {
            return "Supplier with code '" + supplier.getSupplierCode() + "' already exists.";
        }
        String sql = "INSERT INTO Supplier (supplierCode, supplierName, recentSupplyDate) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getSupplierCode());
            pstmt.setString(2, supplier.getSupplierName());
            if (supplier.getRecentSupplyDate() != null) {
                pstmt.setDate(3, java.sql.Date.valueOf(supplier.getRecentSupplyDate()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            int affectedRows = pstmt.executeUpdate();
            return (affectedRows > 0) ? null : "Failed to add supplier. No rows affected (unknown database issue).";
        } catch (SQLException e) {
            e.printStackTrace(); 
            return "Database error adding supplier '" + supplier.getSupplierCode() + "': " + e.getMessage();
        }
    }
    
    public String updateSupplier(Supplier supplier) {
        if (supplier == null || supplier.getSupplierCode() == null || supplier.getSupplierCode().trim().isEmpty()) {
            return "Cannot update supplier: supplier data or code is null/empty.";
        }
        if (getSupplierByCode(supplier.getSupplierCode()) == null) { // Check if supplier exists
            return "Cannot update supplier: Supplier with code '" + supplier.getSupplierCode() + "' not found.";
        }
        String sql = "UPDATE Supplier SET supplierName = ?, recentSupplyDate = ? WHERE supplierCode = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getSupplierName());
            if (supplier.getRecentSupplyDate() != null) {
                pstmt.setDate(2, java.sql.Date.valueOf(supplier.getRecentSupplyDate()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }
            pstmt.setString(3, supplier.getSupplierCode()); 
            int affectedRows = pstmt.executeUpdate();
            return (affectedRows > 0) ? null : "Failed to update supplier. Data unchanged or supplier not found.";
        } catch (SQLException e) {
            e.printStackTrace(); 
            return "Database error updating supplier '" + supplier.getSupplierCode() + "': " + e.getMessage();
        }
    }

    public String deleteSupplier(String supplierCode) {
        if (supplierCode == null || supplierCode.trim().isEmpty()) {
            return "Cannot delete supplier: supplier code is null/empty.";
        }
        if (this.supplyItemController == null) { 
            this.supplyItemController = new SupplyItemController();
        }
        if (supplyItemController.isSupplierReferenced(supplierCode)) {
            return "Cannot delete supplier '" + supplierCode + "': This supplier is referenced by inventory items.";
        }
        String sql = "DELETE FROM Supplier WHERE supplierCode = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplierCode);
            int affectedRows = pstmt.executeUpdate();
            return (affectedRows > 0) ? null : "Failed to delete supplier '" + supplierCode + "'. Supplier not found.";
        } catch (SQLException e) {
            e.printStackTrace(); 
            return "Database error deleting supplier '" + supplierCode + "': " + e.getMessage();
        }
    }

    public Map<String, String> deleteMultipleSuppliers(List<String> supplierCodes) {
        Map<String, String> results = new HashMap<>();
        if (supplierCodes == null || supplierCodes.isEmpty()) {
            return results; 
        }
        for (String code : supplierCodes) {
            results.put(code, deleteSupplier(code)); 
        }
        return results;
    }
}