package boundary;

import control.SupplierController; 
import entity.Supplier;
import entity.SupplyItem;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dialog for editing an existing Supply Item.
 */
public class EditSupplyItemDialog extends JDialog {

    private static final long serialVersionUID = 1L;
	private JTextField txtBarcode;
    private JTextField txtProductTitle;
    private JTextField txtProductDetails;
    private JTextField txtCategory;
    private JTextField txtAvailableUnits;
    private JTextField txtExpirationDate; 
    private JTextField txtThresholdStock;
    private JComboBox<SupplierComboBoxItem> cmbSupplier;

    private JButton btnSaveChanges;
    private JButton btnCancel;

    private SupplyItem itemToEdit;
    private SupplyItem updatedSupplyItem; 
    private SupplierController supplierController;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static class SupplierComboBoxItem {
        private Supplier supplier;

        public SupplierComboBoxItem(Supplier supplier) {
            this.supplier = supplier;
        }

        public Supplier getSupplier() {
            return supplier;
        }

        @Override
        public String toString() {
            return supplier != null ? supplier.getSupplierName() + " (" + supplier.getSupplierCode() + ")" : "Select Supplier";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SupplierComboBoxItem that = (SupplierComboBoxItem) obj;
            if (supplier == null) {
                return that.supplier == null;
            }
            return supplier.equals(that.supplier);
        }

        @Override
        public int hashCode() {
            return supplier != null ? supplier.hashCode() : 0;
        }
    }

    public EditSupplyItemDialog(JFrame parent, SupplyItem itemToEdit) {
        super(parent, "Edit Supply Item", true);
        this.itemToEdit = itemToEdit;
        this.supplierController = new SupplierController();

        initComponents();
        populateFields();
        pack();
        setMinimumSize(new Dimension(450, getSize().height));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 5, 5));

        inputPanel.add(new JLabel("Barcode:"));
        txtBarcode = new JTextField(20);
        txtBarcode.setEditable(false); // Barcode is PK, not editable
        inputPanel.add(txtBarcode);

        inputPanel.add(new JLabel("Product Title:"));
        txtProductTitle = new JTextField(20);
        inputPanel.add(txtProductTitle);

        inputPanel.add(new JLabel("Product Details:"));
        txtProductDetails = new JTextField(20);
        inputPanel.add(txtProductDetails);

        inputPanel.add(new JLabel("Category:"));
        txtCategory = new JTextField(20);
        inputPanel.add(txtCategory);

        inputPanel.add(new JLabel("Available Units:"));
        txtAvailableUnits = new JTextField(20);
        inputPanel.add(txtAvailableUnits);

        inputPanel.add(new JLabel("Expiration Date (YYYY-MM-DD or N/A):"));
        txtExpirationDate = new JTextField(20);
        inputPanel.add(txtExpirationDate);

        inputPanel.add(new JLabel("Threshold Stock:"));
        txtThresholdStock = new JTextField(20);
        inputPanel.add(txtThresholdStock);

        inputPanel.add(new JLabel("Supplier:"));
        cmbSupplier = new JComboBox<>();
        inputPanel.add(cmbSupplier);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSaveChanges = new JButton("Save Changes");
        btnCancel = new JButton("Cancel");

        buttonPanel.add(btnSaveChanges);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        btnSaveChanges.addActionListener(e -> onSaveChanges());
        btnCancel.addActionListener(e -> onCancel());
    }

    private void populateFields() {
        if (itemToEdit != null) {
            txtBarcode.setText(itemToEdit.getBarcode());
            txtProductTitle.setText(itemToEdit.getProductTitle());
            txtProductDetails.setText(itemToEdit.getProductDetails() != null ? itemToEdit.getProductDetails() : "");
            txtCategory.setText(itemToEdit.getCategory());
            txtAvailableUnits.setText(String.valueOf(itemToEdit.getAvailableUnits()));
            txtThresholdStock.setText(String.valueOf(itemToEdit.getThresholdStock()));

            if (itemToEdit.getExpirationDate() != null) {
                txtExpirationDate.setText(itemToEdit.getExpirationDate().format(DATE_FORMATTER));
            } else {
                txtExpirationDate.setText("N/A"); // Display "N/A" if null
            }

            // Populate and select supplier in ComboBox
            List<Supplier> suppliers = supplierController.getAllSuppliers();
            cmbSupplier.addItem(new SupplierComboBoxItem(null)); // Default "Select Supplier"
            Supplier currentItemSupplier = itemToEdit.getSupplier();
            SupplierComboBoxItem itemToSelect = null;

            for (Supplier s : suppliers) {
                SupplierComboBoxItem comboBoxItem = new SupplierComboBoxItem(s);
                cmbSupplier.addItem(comboBoxItem);
                if (currentItemSupplier != null && currentItemSupplier.equals(s)) {
                    itemToSelect = comboBoxItem;
                }
            }
            if (itemToSelect != null) {
                cmbSupplier.setSelectedItem(itemToSelect);
            } else if (currentItemSupplier == null && cmbSupplier.getItemCount() > 0) {
                // If item has no supplier, select the "Select Supplier" default option
                cmbSupplier.setSelectedIndex(0);
            }
        }
    }

    private void onSaveChanges() {
        String productTitle = txtProductTitle.getText().trim();
        String details = txtProductDetails.getText().trim();
        String category = txtCategory.getText().trim();
        String availableUnitsStr = txtAvailableUnits.getText().trim();
        String expDateStr = txtExpirationDate.getText().trim().toUpperCase();
        String thresholdStockStr = txtThresholdStock.getText().trim();
        
        SupplierComboBoxItem selectedSupplierItem = (SupplierComboBoxItem) cmbSupplier.getSelectedItem();
        Supplier selectedSupplier = (selectedSupplierItem != null) ? selectedSupplierItem.getSupplier() : null;

        if (productTitle.isEmpty() || category.isEmpty() || availableUnitsStr.isEmpty() || thresholdStockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, Category, Units, and Threshold cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int availableUnits;
        try {
            availableUnits = Integer.parseInt(availableUnitsStr);
            if (availableUnits < 0) throw new NumberFormatException("Units cannot be negative.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number for Available Units.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int thresholdStock;
        try {
            thresholdStock = Integer.parseInt(thresholdStockStr);
            if (thresholdStock < 0) throw new NumberFormatException("Threshold cannot be negative.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number for Threshold Stock.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate expirationDate = null;
        if (!expDateStr.isEmpty() && !expDateStr.equals("N/A")) {
            try {
                expirationDate = LocalDate.parse(expDateStr, DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Expiration Date format. Use YYYY-MM-DD or N/A.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        updatedSupplyItem = new SupplyItem(itemToEdit.getBarcode(), productTitle, details, category, availableUnits, thresholdStock);
        updatedSupplyItem.setExpirationDate(expirationDate);
        if (selectedSupplier != null) {
            updatedSupplyItem.setSupplier(selectedSupplier);
        } else {
            updatedSupplyItem.setSupplier(null);
        }
        
        setVisible(false);
    }

    private void onCancel() {
        updatedSupplyItem = null;
        setVisible(false);
    }

    public SupplyItem getUpdatedSupplyItem() {
        return updatedSupplyItem;
    }
}