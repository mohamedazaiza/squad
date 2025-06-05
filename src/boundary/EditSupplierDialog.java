package boundary;

import entity.Supplier; 
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog for editing an existing Supplier.
 */
public class EditSupplierDialog extends JDialog {

    private static final long serialVersionUID = 1L;
	private JTextField txtSupplierCode;
    private JTextField txtSupplierName;
    private JTextField txtRecentSupplyDate; 

    private JButton btnSaveChanges;
    private JButton btnCancel;

    private Supplier supplierToEdit; 
    private Supplier updatedSupplier; 

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; 

    public EditSupplierDialog(JFrame parent, Supplier supplierToEdit) {
        super(parent, "Edit Supplier", true); 
        this.supplierToEdit = supplierToEdit;
        initComponents();
        populateFields();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        inputPanel.add(new JLabel("Supplier Code:"));
        txtSupplierCode = new JTextField(20);
        txtSupplierCode.setEditable(false);
        inputPanel.add(txtSupplierCode);

        inputPanel.add(new JLabel("Supplier Name:"));
        txtSupplierName = new JTextField(20);
        inputPanel.add(txtSupplierName);

        inputPanel.add(new JLabel("Recent Supply Date (YYYY-MM-DD):"));
        txtRecentSupplyDate = new JTextField(20);
        inputPanel.add(txtRecentSupplyDate);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSaveChanges = new JButton("Save Changes");
        btnCancel = new JButton("Cancel");

        buttonPanel.add(btnSaveChanges);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnSaveChanges.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSaveChanges();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
    }

    private void populateFields() {
        if (supplierToEdit != null) {
            txtSupplierCode.setText(supplierToEdit.getSupplierCode());
            txtSupplierName.setText(supplierToEdit.getSupplierName());
            if (supplierToEdit.getRecentSupplyDate() != null) {
                txtRecentSupplyDate.setText(supplierToEdit.getRecentSupplyDate().format(DATE_FORMATTER));
            } else {
                txtRecentSupplyDate.setText("");
            }
        }
    }

    private void onSaveChanges() {
        String supplierName = txtSupplierName.getText().trim();
        String dateStr = txtRecentSupplyDate.getText().trim();

        if (supplierName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate recentSupplyDate = null;
        if (!dateStr.isEmpty()) {
            try {
                recentSupplyDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Create a new supplier object with the updated details, keeping the original code
        updatedSupplier = new Supplier(supplierToEdit.getSupplierCode(), supplierName, recentSupplyDate);
        
        setVisible(false);
    }

    private void onCancel() {
        updatedSupplier = null; // Ensure no supplier is returned
        setVisible(false); 
    }

    public Supplier getUpdatedSupplier() {
        return updatedSupplier;
    }
}