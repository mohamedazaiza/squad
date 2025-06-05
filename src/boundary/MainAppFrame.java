package boundary;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

/**
 * The main application window (JFrame) for the DentalCare system.
 * This is a Boundary class in the ECB pattern.
 */
public class MainAppFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel currentPanel;

    public MainAppFrame() throws HeadlessException {
        super("DentalCare Management System");

        initComponents();
        setupFrame();
        addWindowStateBehavior();

    }

    private void initComponents() {
        getContentPane().setLayout(new BorderLayout());
        MainPanel initialMainPanel = new MainPanel(this);
        currentPanel = initialMainPanel;
        getContentPane().add(currentPanel, BorderLayout.CENTER);
    }

    private void setupFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 700));
        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setResizable(false);
    }

    private void addWindowStateBehavior() {
        addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if ((e.getOldState() & JFrame.MAXIMIZED_BOTH) != 0 && e.getNewState() == JFrame.NORMAL) {
                    if (getWidth() <= 0 || getHeight() <= 0) {
                        pack();
                    }
                    setLocationRelativeTo(null);
                } else if (e.getNewState() == JFrame.NORMAL && e.getOldState() == JFrame.ICONIFIED) {
                    if (getWidth() <= 0 || getHeight() <= 0) {
                        pack();
                    }
                    setLocationRelativeTo(null);
                }
            }
        });
    }

    public void switchPanel(JPanel newPanel) {
        if (currentPanel != null) {
            getContentPane().remove(currentPanel);
        }
        currentPanel = newPanel;
        getContentPane().add(currentPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainAppFrame frame = new MainAppFrame();
            frame.setVisible(true);
        });
    }
}
