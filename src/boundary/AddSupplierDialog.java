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


public class AddSupplierDialog extends JDialog {

    private static final long serialVersionUID = 1L;
	private JTextField txtSupplierCode;
    private JTextField txtSupplierName;
    private JTextField txtRecentSupplyDate; 

    private JButton btnSave;
    private JButton btnCancel;

    private Supplier newSupplier; 

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; 

    public AddSupplierDialog(JFrame parent) {
        super(parent, "Add New Supplier", true); 
        initComponents();
        pack();
        setLocationRelativeTo(parent); 
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5)); 

        inputPanel.add(new JLabel("Supplier Code:"));
        txtSupplierCode = new JTextField(20);
        inputPanel.add(txtSupplierCode);

        inputPanel.add(new JLabel("Supplier Name:"));
        txtSupplierName = new JTextField(20);
        inputPanel.add(txtSupplierName);

        inputPanel.add(new JLabel("Recent Supply Date (YYYY-MM-DD):"));
        txtRecentSupplyDate = new JTextField(20);
        inputPanel.add(txtRecentSupplyDate);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSave();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
    }

    private void onSave() {
        String supplierCode = txtSupplierCode.getText().trim();
        String supplierName = txtSupplierName.getText().trim();
        String dateStr = txtRecentSupplyDate.getText().trim();

        if (supplierCode.isEmpty() || supplierName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier Code and Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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

        newSupplier = new Supplier(supplierCode, supplierName, recentSupplyDate);
        setVisible(false);
    }

    private void onCancel() {
        newSupplier = null; 
        setVisible(false); 
    }

    public Supplier getNewSupplier() {
        return newSupplier;
    }
}