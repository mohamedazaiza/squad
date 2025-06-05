package view;
import javax.swing.SwingUtilities;
import javax.swing.UIManager; 
import boundary.MainAppFrame; 

public class Main {

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Nimbus L&F not found, using default. Error: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainAppFrame mainFrame = new MainAppFrame(); 
                mainFrame.setVisible(true);
            }
        });
    }
}