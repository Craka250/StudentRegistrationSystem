
package ke.ac.kyu.srms;

import com.formdev.flatlaf.FlatLightLaf;
import ke.ac.kyu.srms.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {

    public static void main(String[] args) {

        // Configure the application appearance
        FlatLightLaf.setup();

        // Global UI defaults
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);

        // Start the application on Swing's Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
