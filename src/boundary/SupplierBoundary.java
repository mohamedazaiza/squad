package boundary;

import control.SupplierController;
import entity.Supplier;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane; 
import javax.swing.BorderFactory;
import javax.swing.Box; // For spacing
// ActionListener is used by lambda expressions
// import java.awt.event.ActionEvent; 
// import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.util.ArrayList; 
import java.util.List;
import java.util.Map; 
import java.util.Vector;
import java.util.stream.Collectors; 

/**
 * Boundary class for displaying and managing Supplier data.
 */
public class SupplierBoundary extends JPanel {

    private static final long serialVersionUID = 1L;
    private MainAppFrame mainFrame; 
    private SupplierController supplierController;

    private JTable supplierTable;
    private DefaultTableModel tableModel;

    private JButton btnAddSupplier;
    private JButton btnEditSupplier;
    private JButton btnDeleteSupplier;
    private JButton btnRefreshSuppliers;
    private JButton btnBackToMenu;

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 22);
    private static final Font TABLE_HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font TABLE_BODY_FONT = new Font("Arial", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("Arial", Font.PLAIN, 13);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(180, 30);
    private static final Dimension NAV_BUTTON_SIZE = new Dimension(160, 30);

    public SupplierBoundary(MainAppFrame frame) {
        this.mainFrame = frame;
        this.supplierController = new SupplierController();

        setLayout(new BorderLayout(10, 15)); 
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); 

        initComponents();
        loadSupplierData();
        addListeners();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Manage Suppliers", JLabel.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); 
        add(lblTitle, BorderLayout.NORTH);

        String[] columnNames = {"Supplier Code", "Supplier Name", "Recent Supply Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        supplierTable = new JTable(tableModel);
        supplierTable.setFillsViewportHeight(true); 
        supplierTable.setRowHeight(25);
        supplierTable.getTableHeader().setFont(TABLE_HEADER_FONT);
        supplierTable.setFont(TABLE_BODY_FONT);
        supplierTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(supplierTable);
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

        JPanel allActionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); 

        btnRefreshSuppliers = new JButton("Refresh List");
        styleActionButton(btnRefreshSuppliers);
        
        btnAddSupplier = new JButton("Add New Supplier");
        btnEditSupplier = new JButton("Edit Selected Supplier");
        btnDeleteSupplier = new JButton("Delete Selected Supplier(s)"); 
        styleActionButton(btnAddSupplier);
        styleActionButton(btnEditSupplier);
        styleActionButton(btnDeleteSupplier);

        allActionButtonsPanel.add(btnDeleteSupplier);
        allActionButtonsPanel.add(btnEditSupplier);
        allActionButtonsPanel.add(btnAddSupplier);
        allActionButtonsPanel.add(Box.createHorizontalStrut(10)); // Spacer
        allActionButtonsPanel.add(btnRefreshSuppliers);
        
        southPanelContainer.add(allActionButtonsPanel, BorderLayout.CENTER); 
        
        add(southPanelContainer, BorderLayout.SOUTH);
    }
    
    private void styleActionButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(ACTION_BUTTON_SIZE);
    }

    private void loadSupplierData() {
        tableModel.setRowCount(0);
        List<Supplier> suppliers = supplierController.getAllSuppliers();
        if (suppliers != null && !suppliers.isEmpty()) {
            for (Supplier supplier : suppliers) {
                Vector<Object> row = new Vector<>();
                row.add(supplier.getSupplierCode());
                row.add(supplier.getSupplierName());
                row.add(supplier.getRecentSupplyDate() != null ? supplier.getRecentSupplyDate().toString() : "N/A");
                tableModel.addRow(row);
            }
        }
    }

    private void addListeners() {
        btnBackToMenu.addActionListener(e -> mainFrame.switchPanel(new MainPanel(mainFrame)));
        btnRefreshSuppliers.addActionListener(e -> loadSupplierData());

        btnAddSupplier.addActionListener(e -> {
            AddSupplierDialog addDialog = new AddSupplierDialog(mainFrame);
            addDialog.setVisible(true); 
            Supplier newSupplier = addDialog.getNewSupplier(); 
            if (newSupplier != null) {
                String errorMessage = supplierController.addSupplier(newSupplier);
                if (errorMessage == null) { 
                    JOptionPane.showMessageDialog(this, "Supplier added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSupplierData(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add supplier: " + errorMessage, "Error Adding Supplier", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEditSupplier.addActionListener(e -> {
            int[] selectedRows = supplierTable.getSelectedRows();
            if (selectedRows.length == 0) { JOptionPane.showMessageDialog(this, "Please select a supplier to edit.", "No Supplier Selected", JOptionPane.WARNING_MESSAGE); return; }
            if (selectedRows.length > 1) { JOptionPane.showMessageDialog(this, "Please select only one supplier to edit at a time.", "Multiple Suppliers Selected", JOptionPane.WARNING_MESSAGE); return; }
            int modelRow = supplierTable.convertRowIndexToModel(selectedRows[0]);
            String supplierCode = (String) tableModel.getValueAt(modelRow, 0);
            Supplier supplierToEdit = supplierController.getSupplierByCode(supplierCode);
            if (supplierToEdit != null) {
                EditSupplierDialog editDialog = new EditSupplierDialog(mainFrame, supplierToEdit);
                editDialog.setVisible(true); 
                Supplier updatedSupplier = editDialog.getUpdatedSupplier();
                if (updatedSupplier != null) {
                    String errorMessage = supplierController.updateSupplier(updatedSupplier);
                    if (errorMessage == null) { 
                        JOptionPane.showMessageDialog(this, "Supplier updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadSupplierData(); 
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update supplier: " + errorMessage, "Error Updating Supplier", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Could not retrieve details for supplier code: " + supplierCode + "\nIt may have been deleted.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDeleteSupplier.addActionListener(e -> {
            int[] selectedRowsView = supplierTable.getSelectedRows(); 
            if (selectedRowsView.length == 0) { JOptionPane.showMessageDialog(this, "Please select one or more suppliers to delete.", "No Supplier Selected", JOptionPane.WARNING_MESSAGE); return; }
            List<String> supplierCodesToDelete = new ArrayList<>();
            List<String> supplierNamesForConfirmation = new ArrayList<>();
            for (int viewRow : selectedRowsView) {
                int modelRow = supplierTable.convertRowIndexToModel(viewRow);
                supplierCodesToDelete.add((String) tableModel.getValueAt(modelRow, 0));
                supplierNamesForConfirmation.add((String) tableModel.getValueAt(modelRow, 1));
            }
            String confirmationMessage;
            if (supplierNamesForConfirmation.size() == 1) {
                confirmationMessage = "Are you sure you want to delete supplier: '" + supplierNamesForConfirmation.get(0) + "' (Code: " + supplierCodesToDelete.get(0) + ")?";
            } else {
                String names = supplierNamesForConfirmation.stream().collect(Collectors.joining("', '", "'", "'"));
                confirmationMessage = "Are you sure you want to delete these " + supplierNamesForConfirmation.size() + " suppliers: " + names + "?";
            }
            int confirm = JOptionPane.showConfirmDialog(this, confirmationMessage, "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                Map<String, String> deleteResults = supplierController.deleteMultipleSuppliers(supplierCodesToDelete);
                StringBuilder resultMessageSb = new StringBuilder();
                String dialogTitle = "Deletion Result";
                int messageType = JOptionPane.INFORMATION_MESSAGE;
                long successCount = deleteResults.entrySet().stream().filter(entry -> entry.getValue() == null).count();
                long failureCount = deleteResults.size() - successCount;

                if (failureCount > 0) {
                    messageType = JOptionPane.WARNING_MESSAGE;
                    dialogTitle = (successCount == 0 && supplierCodesToDelete.size() == 1) ? "Deletion Prevented/Failed" : "Deletion Partially Failed";
                    if (successCount == 0 && supplierCodesToDelete.size() == 1) {
                         resultMessageSb.append("Could not delete supplier: '").append(supplierNamesForConfirmation.get(0))
                                     .append("' (Code: ").append(supplierCodesToDelete.get(0)).append(").\n\n")
                                     .append(deleteResults.get(supplierCodesToDelete.get(0))); 
                    } else {
                        if (successCount > 0) resultMessageSb.append(successCount).append(" supplier(s) deleted successfully.\n\n");
                        resultMessageSb.append(failureCount).append(" supplier(s) failed to delete:\n");
                        for (Map.Entry<String, String> entry : deleteResults.entrySet()) {
                            if (entry.getValue() != null) { 
                                String failedName = "Unknown";
                                for(int i=0; i<supplierCodesToDelete.size(); i++){ 
                                    if(supplierCodesToDelete.get(i).equals(entry.getKey())){
                                        failedName = supplierNamesForConfirmation.get(i);
                                        break;
                                    }
                                }
                                resultMessageSb.append("- '").append(failedName).append("' (Code: ").append(entry.getKey()).append("): ").append(entry.getValue()).append("\n");
                            }
                        }
                    }
                } else { 
                    resultMessageSb.append(successCount).append(" supplier(s) deleted successfully.\n");
                }
                JOptionPane.showMessageDialog(this, resultMessageSb.toString().trim(), dialogTitle, messageType);
                if (successCount > 0 || selectedRowsView.length > 0) { 
                    loadSupplierData(); 
                }
            }
        });
    }
}