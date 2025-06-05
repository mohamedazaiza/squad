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
 * Dialog for adding a new Supply Item.
 */
public class AddSupplyItemDialog extends JDialog {

    private static final long serialVersionUID = 1L;
	private JTextField txtBarcode;
    private JTextField txtProductTitle;
    private JTextField txtProductDetails;
    private JTextField txtCategory;
    private JTextField txtAvailableUnits;
    private JTextField txtExpirationDate; 
    private JTextField txtThresholdStock;
    private JComboBox<SupplierComboBoxItem> cmbSupplier; // ComboBox for suppliers

    private JButton btnSave;
    private JButton btnCancel;

    private SupplyItem newSupplyItem;
    private SupplierController supplierController;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // Inner class for JComboBox items to display supplier name but store supplier object/code
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
            // Display name and code in ComboBox for clarity
            return supplier != null ? supplier.getSupplierName() + " (" + supplier.getSupplierCode() + ")" : "Select Supplier";
        }
    }


    public AddSupplyItemDialog(JFrame parent) {
        super(parent, "Add New Supply Item", true);
        this.supplierController = new SupplierController(); // To populate supplier dropdown

        initComponents();
        populateSupplierComboBox();
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
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> onCancel());
    }

    private void populateSupplierComboBox() {
        List<Supplier> suppliers = supplierController.getAllSuppliers();
        cmbSupplier.addItem(new SupplierComboBoxItem(null)); // For "Select Supplier" or no supplier option
        if (suppliers != null) {
            for (Supplier s : suppliers) {
                cmbSupplier.addItem(new SupplierComboBoxItem(s));
            }
        }
    }

    private void onSave() {
        String barcode = txtBarcode.getText().trim();
        String productTitle = txtProductTitle.getText().trim();
        String details = txtProductDetails.getText().trim();
        String category = txtCategory.getText().trim();
        String availableUnitsStr = txtAvailableUnits.getText().trim();
        String expDateStr = txtExpirationDate.getText().trim().toUpperCase(); // Convert "n/a" to "N/A"
        String thresholdStockStr = txtThresholdStock.getText().trim();
        
        SupplierComboBoxItem selectedSupplierItem = (SupplierComboBoxItem) cmbSupplier.getSelectedItem();
        Supplier selectedSupplier = (selectedSupplierItem != null) ? selectedSupplierItem.getSupplier() : null;


        if (barcode.isEmpty() || productTitle.isEmpty() || category.isEmpty() || availableUnitsStr.isEmpty() || thresholdStockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Barcode, Title, Category, Units, and Threshold cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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

        newSupplyItem = new SupplyItem(barcode, productTitle, details, category, availableUnits, thresholdStock);
        newSupplyItem.setExpirationDate(expirationDate); 
        if (selectedSupplier != null) {
            newSupplyItem.setSupplier(selectedSupplier);
        }
        
        setVisible(false);
    }

    private void onCancel() {
        newSupplyItem = null;
        setVisible(false);
    }

    public SupplyItem getNewSupplyItem() {
        return newSupplyItem;
    }
}