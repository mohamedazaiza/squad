package boundary;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The main navigation panel displayed within the MainAppFrame.
 * Contains buttons to access different parts of the application.
 * Redesigned for a luxurious, modern, and fancy appearance with gradients.
 * This is a Boundary class in the ECB pattern.
 */
public class MainPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // --- Luxurious Style Constants ---
    private static final Color COLOR_BACKGROUND_START = new Color(20, 30, 48); // Deep dark blue
    private static final Color COLOR_BACKGROUND_END = new Color(36, 59, 85);   // Slightly lighter dark blue
    private static final Color COLOR_TITLE_TEXT = new Color(220, 180, 100); // Soft Gold
    private static final Color COLOR_SUBTEXT = new Color(180, 190, 200);    // Light grey/blue
    private static final Color COLOR_BUTTON_TEXT = Color.WHITE;

    // Button Colors
    private static final Color COLOR_BUTTON_BG_START = new Color(60, 90, 120); // Steel Blue
    private static final Color COLOR_BUTTON_BG_END = new Color(40, 70, 100);   // Darker Steel Blue
    private static final Color COLOR_BUTTON_HOVER_BG_START = new Color(80, 110, 140); // Lighter Steel Blue for hover
    private static final Color COLOR_BUTTON_HOVER_BG_END = new Color(50, 80, 110);   // Darker Lighter Steel Blue

    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 32);
    private static final Font FONT_WELCOME = new Font("SansSerif", Font.ITALIC, 19);
    private static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 15);

    private static final Dimension BUTTON_SIZE = new Dimension(280, 65);
    private static final int BUTTON_CORNER_RADIUS = 20;

    private static final int GLOBAL_PADDING = 30;
    private static final int VERTICAL_SPACING_TITLE_BLOCK = 40;
    private static final int VERTICAL_SPACING_WELCOME_BUTTONS = 50;
    private static final int BUTTON_PANEL_GAP = 30;


    private JButton btnManageSuppliers;
    private JButton btnManageInventory;
    private JButton btnImportXmlData;

    private MainAppFrame mainFrame;

    public MainPanel(MainAppFrame frame) {
        this.mainFrame = frame;
        initComponents();
        addListeners();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(
                0, 0, COLOR_BACKGROUND_START,
                0, getHeight(), COLOR_BACKGROUND_END);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0,0));
        setOpaque(false); // We are painting our own background
        setBorder(BorderFactory.createEmptyBorder(GLOBAL_PADDING, GLOBAL_PADDING, GLOBAL_PADDING, GLOBAL_PADDING));

        // --- Title ---
        JLabel lblTitle = new JLabel("DentalCare System Menu", JLabel.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TITLE_TEXT);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle, BorderLayout.CENTER);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, VERTICAL_SPACING_TITLE_BLOCK, 0));
        add(titlePanel, BorderLayout.NORTH);

        // --- Center Container for Welcome Message and Buttons ---
        JPanel centerWrapperPanel = new JPanel();
        centerWrapperPanel.setLayout(new BoxLayout(centerWrapperPanel, BoxLayout.Y_AXIS));
        centerWrapperPanel.setOpaque(false);

        centerWrapperPanel.add(Box.createVerticalGlue());

        // Welcome Message
        JLabel lblWelcome = new JLabel("Welcome to the DentalCare Management System!", JLabel.CENTER);
        lblWelcome.setFont(FONT_WELCOME);
        lblWelcome.setForeground(COLOR_SUBTEXT);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapperPanel.add(lblWelcome);
        centerWrapperPanel.add(Box.createRigidArea(new Dimension(0, VERTICAL_SPACING_WELCOME_BUTTONS)));

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, BUTTON_PANEL_GAP, BUTTON_PANEL_GAP / 2));
        buttonPanel.setOpaque(false);

        btnManageSuppliers = new GradientButton("Manage Suppliers");
        btnManageInventory = new GradientButton("Manage Inventory");
        btnImportXmlData = new GradientButton("Import Supir XML Data");

        buttonPanel.add(btnManageSuppliers);
        buttonPanel.add(btnManageInventory);
        buttonPanel.add(btnImportXmlData);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerWrapperPanel.add(buttonPanel);
        centerWrapperPanel.add(Box.createVerticalGlue());

        add(centerWrapperPanel, BorderLayout.CENTER);
    }

    // Custom JButton class for gradient background and rounded corners
    private static class GradientButton extends JButton {
        private static final long serialVersionUID = 1L;
        private boolean isHovering = false;

        public GradientButton(String text) {
            super(text);
            setFont(FONT_BUTTON);
            setForeground(COLOR_BUTTON_TEXT);
            setPreferredSize(BUTTON_SIZE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false); // We'll paint our own background

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovering = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovering = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color startColor = isHovering ? COLOR_BUTTON_HOVER_BG_START : COLOR_BUTTON_BG_START;
            Color endColor = isHovering ? COLOR_BUTTON_HOVER_BG_END : COLOR_BUTTON_BG_END;

            GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
            g2d.setPaint(gp);
            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS));

            g2d.dispose();
            super.paintComponent(g); // Paint text and other default stuff
        }
    }


    private void addListeners() {
        btnManageSuppliers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainFrame != null) {
                    mainFrame.switchPanel(new SupplierBoundary(mainFrame));
                }
            }
        });

        btnManageInventory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainFrame != null) {
                    mainFrame.switchPanel(new InventoryBoundary(mainFrame));
                }
            }
        });

        btnImportXmlData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Import Supir XML Data button clicked.");
                String xmlPathToUse = util.Constants.SUPIR_XML_FILE_PATH;

                control.XmlImportController xmlImportController = new control.XmlImportController();
                try {
                    control.ImportResult importResult = xmlImportController.importDataFromXml(xmlPathToUse);

                    // Basic styling for JTextArea to somewhat blend
                    JTextArea textArea = new JTextArea(importResult.toString());
                    textArea.setEditable(false);
                    textArea.setWrapStyleWord(true);
                    textArea.setLineWrap(true);
                    textArea.setCaretPosition(0);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Monospaced for structured data
                    textArea.setBackground(new Color(230, 230, 240)); // Light lavender gray
                    textArea.setForeground(new Color(51, 51, 51)); // Dark gray text
                    textArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new Dimension(550, 350)); // Slightly larger
                    scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180,180,190), 1)); // Subtle border

                    JOptionPane.showMessageDialog(mainFrame,
                                                  scrollPane,
                                                  "XML Import Result",
                                                  (importResult.isOverallSuccess() &&
                                                   importResult.getSuppliersFailed() == 0 &&
                                                   importResult.getItemsFailed() == 0)
                                                   ? JOptionPane.INFORMATION_MESSAGE
                                                   : JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                     // Keep original error dialog styling for now, or apply similar custom styling if needed
                    JTextArea errorTextArea = new JTextArea("Error during XML import process: " + ex.getMessage() +
                                                           "\nExpected XML at: " + xmlPathToUse +
                                                           "\n\nStack Trace:\n" + getStackTraceString(ex));
                    errorTextArea.setEditable(false);
                    errorTextArea.setWrapStyleWord(true);
                    errorTextArea.setLineWrap(true);
                    errorTextArea.setCaretPosition(0);
                    errorTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    errorTextArea.setBackground(new Color(255, 230, 230)); // Light error red
                    errorTextArea.setForeground(Color.RED.darker());
                    errorTextArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                    JScrollPane errorScrollPane = new JScrollPane(errorTextArea);
                    errorScrollPane.setPreferredSize(new Dimension(600, 400));


                    JOptionPane.showMessageDialog(mainFrame,
                                                  errorScrollPane,
                                                  "XML Import Error",
                                                  JOptionPane.ERROR_MESSAGE);
                    // ex.printStackTrace(); // Already included in text area
                }
            }
        });
    }

    // Helper to get stack trace as string for the error dialog
    private String getStackTraceString(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}