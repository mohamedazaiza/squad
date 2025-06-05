package boundary;

import control.SupplyItemController;
import entity.SupplyItem;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel; 
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane; 
import javax.swing.BorderFactory;
import javax.swing.Box; 
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.time.LocalDate; 
import java.time.temporal.ChronoUnit; 
import java.util.ArrayList; 
import java.util.List;
import java.util.Map;   
import java.util.Vector;
import java.util.stream.Collectors; 

/**
 * Boundary class for displaying and managing SupplyItem (inventory) data.
 */
public class InventoryBoundary extends JPanel {

    private static final long serialVersionUID = 1L;
    private MainAppFrame mainFrame; 
    private SupplyItemController supplyItemController;

    private JTable inventoryTable;
    private DefaultTableModel tableModel;

    private JButton btnAddItem;
    private JButton btnEditItem;
    private JButton btnDeleteItem;
    private JButton btnRefreshInventory;
    private JButton btnBackToMenu;
    private JButton btnShowLowStock;    
    private JButton btnShowNearExpiry; 

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 22);
    private static final Font TABLE_HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font TABLE_BODY_FONT = new Font("Arial", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("Arial", Font.PLAIN, 13);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(180, 30);
    private static final Dimension ALERT_BUTTON_SIZE = new Dimension(160, 30); 
    private static final Dimension NAV_BUTTON_SIZE = new Dimension(160, 30);
    
    private static final int NEAR_EXPIRATION_DAYS_THRESHOLD = 30; 

    public InventoryBoundary(MainAppFrame frame) {
        this.mainFrame = frame;
        this.supplyItemController = new SupplyItemController(); 

        setLayout(new BorderLayout(10, 15)); 
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); 

        initComponents();
        loadInventoryData(); 
        addListeners();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Manage Inventory (Supply Items)", JLabel.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); 
        add(lblTitle, BorderLayout.NORTH);

        String[] columnNames = {
            "Barcode", "Product Title", "Category", "Available Units", 
            "Expiration Date", "Threshold Stock", "Supplier Code"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        inventoryTable = new JTable(tableModel);
        inventoryTable.setFillsViewportHeight(true);
        inventoryTable.setRowHeight(25);
        inventoryTable.getTableHeader().setFont(TABLE_HEADER_FONT);
        inventoryTable.setFont(TABLE_BODY_FONT);
        inventoryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(100); 
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(200); 
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(120); 
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(100); 
        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(100); 
        inventoryTable.getColumnModel().getColumn(5).setPreferredWidth(100); 
        inventoryTable.getColumnModel().getColumn(6).setPreferredWidth(120); 

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Button Panels ---
        JPanel southPanelContainer = new JPanel(new BorderLayout(10, 5)); 

        // Navigation Button Panel (Back) 
        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
        btnBackToMenu = new JButton("Back to Main Menu");
        btnBackToMenu.setFont(BUTTON_FONT);
        btnBackToMenu.setPreferredSize(NAV_BUTTON_SIZE);
        navButtonPanel.add(btnBackToMenu);
        southPanelContainer.add(navButtonPanel, BorderLayout.WEST);

        JPanel allOtherButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); 

        btnShowLowStock = new JButton("Show Low Stock");
        btnShowNearExpiry = new JButton("Show Near Expiry");
        btnShowLowStock.setFont(BUTTON_FONT);
        btnShowLowStock.setPreferredSize(ALERT_BUTTON_SIZE);
        btnShowNearExpiry.setFont(BUTTON_FONT);
        btnShowNearExpiry.setPreferredSize(ALERT_BUTTON_SIZE);
        
        btnRefreshInventory = new JButton("Refresh List");
        styleActionButton(btnRefreshInventory); 
        
        btnAddItem = new JButton("Add New Item");
        btnEditItem = new JButton("Edit Selected Item");
        btnDeleteItem = new JButton("Delete Selected Item(s)"); 
        styleActionButton(btnAddItem);
        styleActionButton(btnEditItem);
        styleActionButton(btnDeleteItem);

        allOtherButtonsPanel.add(btnDeleteItem);
        allOtherButtonsPanel.add(btnEditItem);
        allOtherButtonsPanel.add(btnAddItem);
        allOtherButtonsPanel.add(Box.createHorizontalStrut(10)); 
        allOtherButtonsPanel.add(btnRefreshInventory);
        allOtherButtonsPanel.add(Box.createHorizontalStrut(10)); 
        allOtherButtonsPanel.add(btnShowNearExpiry);
        allOtherButtonsPanel.add(btnShowLowStock);
        
        southPanelContainer.add(allOtherButtonsPanel, BorderLayout.CENTER); 
        
        add(southPanelContainer, BorderLayout.SOUTH);
    }
    
    private void styleActionButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(ACTION_BUTTON_SIZE);
    }

    private void loadInventoryData() { 
        tableModel.setRowCount(0); 
        List<SupplyItem> items = supplyItemController.getAllSupplyItems();
        if (items != null && !items.isEmpty()) {
            for (SupplyItem item : items) {
                Vector<Object> row = new Vector<>();
                row.add(item.getBarcode());
                row.add(item.getProductTitle());
                row.add(item.getCategory());
                row.add(item.getAvailableUnits());
                row.add(item.getExpirationDate() != null ? item.getExpirationDate().toString() : "N/A");
                row.add(item.getThresholdStock());
                row.add(item.getSupplierCode() != null ? item.getSupplierCode() : "N/A");
                tableModel.addRow(row);
            }
        }
    }
    
    private void checkAndShowLowStockAlerts() {
        List<SupplyItem> lowStockItems = supplyItemController.getLowStockItems();
        if (lowStockItems != null && !lowStockItems.isEmpty()) {
            StringBuilder alertMessage = new StringBuilder("The following items are LOW ON STOCK (at or below threshold):\n\n");
            for (SupplyItem item : lowStockItems) {
                alertMessage.append("- ")
                            .append(item.getProductTitle())
                            .append(" (Barcode: ").append(item.getBarcode()).append(")")
                            .append(" - Available: ").append(item.getAvailableUnits())
                            .append(" / Threshold: ").append(item.getThresholdStock())
                            .append("\n");
            }
            JOptionPane.showMessageDialog(this, alertMessage.toString(), "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No items are currently low on stock.", "Low Stock Alert", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void checkAndShowNearExpirationAlerts() {
        List<SupplyItem> nearExpirationItems = supplyItemController.getNearExpirationItems(NEAR_EXPIRATION_DAYS_THRESHOLD);
        if (nearExpirationItems != null && !nearExpirationItems.isEmpty()) {
            StringBuilder alertMessage = new StringBuilder("The following items are NEARING EXPIRATION (within ")
                                            .append(NEAR_EXPIRATION_DAYS_THRESHOLD).append(" days or expiring today):\n\n");
            LocalDate today = LocalDate.now();
            for (SupplyItem item : nearExpirationItems) {
                long daysUntilExpiry = item.getExpirationDate() != null ? ChronoUnit.DAYS.between(today, item.getExpirationDate()) : -1;
                String expiryInfo;
                if (daysUntilExpiry < 0) { 
                    expiryInfo = "Expired on " + item.getExpirationDate().toString();
                } else if (daysUntilExpiry == 0) {
                    expiryInfo = "Expires TODAY!";
                } else {
                    expiryInfo = "Expires in " + daysUntilExpiry + " day(s) on " + item.getExpirationDate().toString();
                }
                alertMessage.append("- ")
                            .append(item.getProductTitle())
                            .append(" (Barcode: ").append(item.getBarcode()).append(")")
                            .append(" - ").append(expiryInfo)
                            .append("\n");
            }
            JOptionPane.showMessageDialog(this, alertMessage.toString(), "Near Expiration Alert", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No items are nearing expiration within the next " + NEAR_EXPIRATION_DAYS_THRESHOLD + " days.", "Near Expiration Alert", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addListeners() {
        btnBackToMenu.addActionListener(e -> mainFrame.switchPanel(new MainPanel(mainFrame)));
        btnRefreshInventory.addActionListener(e -> loadInventoryData());
        btnShowLowStock.addActionListener(e -> checkAndShowLowStockAlerts());
        btnShowNearExpiry.addActionListener(e -> checkAndShowNearExpirationAlerts());

        btnAddItem.addActionListener(e -> {
            AddSupplyItemDialog addDialog = new AddSupplyItemDialog(mainFrame);
            addDialog.setVisible(true); 
            SupplyItem newSupplyItem = addDialog.getNewSupplyItem();
            if (newSupplyItem != null) {
                String errorMessage = supplyItemController.addSupplyItem(newSupplyItem);
                if (errorMessage == null) { 
                    JOptionPane.showMessageDialog(this, "Supply item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadInventoryData(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add supply item: " + errorMessage, "Error Adding Item", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEditItem.addActionListener(e -> {
            int[] selectedRows = inventoryTable.getSelectedRows();
            if (selectedRows.length == 0) { JOptionPane.showMessageDialog(this, "Please select an item to edit.", "No Item Selected", JOptionPane.WARNING_MESSAGE); return; }
            if (selectedRows.length > 1) { JOptionPane.showMessageDialog(this, "Please select only one item to edit at a time.", "Multiple Items Selected", JOptionPane.WARNING_MESSAGE); return; }
            int modelRow = inventoryTable.convertRowIndexToModel(selectedRows[0]);
            String itemBarcode = (String) tableModel.getValueAt(modelRow, 0); 
            SupplyItem itemToEdit = supplyItemController.getSupplyItemByBarcode(itemBarcode);
            if (itemToEdit != null) {
                EditSupplyItemDialog editDialog = new EditSupplyItemDialog(mainFrame, itemToEdit);
                editDialog.setVisible(true); 
                SupplyItem updatedItem = editDialog.getUpdatedSupplyItem();
                if (updatedItem != null) {
                    String errorMessage = supplyItemController.updateSupplyItem(updatedItem);
                    if (errorMessage == null) { 
                        JOptionPane.showMessageDialog(this, "Supply item updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadInventoryData(); 
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update supply item: " + errorMessage, "Error Updating Item", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Could not retrieve details for item with barcode: " + itemBarcode + "\nIt may have been deleted.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDeleteItem.addActionListener(e -> {
            int[] selectedRowsView = inventoryTable.getSelectedRows(); 
            if (selectedRowsView.length == 0) { JOptionPane.showMessageDialog(this, "Please select one or more items to delete.", "No Item Selected", JOptionPane.WARNING_MESSAGE); return; }
            List<String> itemBarcodesToDelete = new ArrayList<>();
            List<String> itemTitlesForConfirmation = new ArrayList<>();
            for (int viewRow : selectedRowsView) { 
                int modelRow = inventoryTable.convertRowIndexToModel(viewRow);
                itemBarcodesToDelete.add((String) tableModel.getValueAt(modelRow, 0)); 
                itemTitlesForConfirmation.add((String) tableModel.getValueAt(modelRow, 1)); 
            }
            String confirmationMessage;
            if (itemTitlesForConfirmation.size() == 1) {
                confirmationMessage = "Are you sure you want to delete item: '" + itemTitlesForConfirmation.get(0) + "' (Barcode: " + itemBarcodesToDelete.get(0) + ")?";
            } else {
                String titles = itemTitlesForConfirmation.stream().collect(Collectors.joining("', '", "'", "'"));
                confirmationMessage = "Are you sure you want to delete these " + itemTitlesForConfirmation.size() + " items: " + titles + "?";
            }
            int confirm = JOptionPane.showConfirmDialog(this, confirmationMessage,"Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                Map<String, String> deleteResults = supplyItemController.deleteMultipleSupplyItems(itemBarcodesToDelete);
                StringBuilder resultMessageSb = new StringBuilder(); 
                String dialogTitle = "Deletion Result";
                int messageType = JOptionPane.INFORMATION_MESSAGE;
                long successCount = deleteResults.entrySet().stream().filter(entry -> entry.getValue() == null).count();
                long failureCount = deleteResults.size() - successCount;
                 if (failureCount > 0) { 
                    messageType = JOptionPane.WARNING_MESSAGE;
                    dialogTitle = (successCount == 0 && itemBarcodesToDelete.size() == 1) ? "Deletion Failed" : "Deletion Partially Failed";
                    if (successCount == 0 && itemBarcodesToDelete.size() == 1) {
                         resultMessageSb.append("Could not delete item: '").append(itemTitlesForConfirmation.get(0)) 
                                     .append("' (Barcode: ").append(itemBarcodesToDelete.get(0)).append(").\n\n")
                                     .append(deleteResults.get(itemBarcodesToDelete.get(0))); 
                    } else {
                        if (successCount > 0) resultMessageSb.append(successCount).append(" item(s) deleted successfully.\n\n");
                        resultMessageSb.append(failureCount).append(" item(s) failed to delete:\n");
                        for (Map.Entry<String, String> entry : deleteResults.entrySet()) {
                            if (entry.getValue() != null) { 
                                String failedTitle = "Unknown";
                                for(int i=0; i<itemBarcodesToDelete.size(); i++){ 
                                    if(itemBarcodesToDelete.get(i).equals(entry.getKey())){
                                        failedTitle = itemTitlesForConfirmation.get(i);
                                        break;
                                    }
                                }
                                resultMessageSb.append("- '").append(failedTitle).append("' (Code: ").append(entry.getKey()).append("): ").append(entry.getValue()).append("\n");
                            }
                        }
                    }
                } else { resultMessageSb.append(successCount).append(" item(s) deleted successfully.\n"); }
                JOptionPane.showMessageDialog(this, resultMessageSb.toString().trim(), dialogTitle, messageType);
                if (successCount > 0 || selectedRowsView.length > 0) { 
                    loadInventoryData(); 
                }
            }
        });
    }
}