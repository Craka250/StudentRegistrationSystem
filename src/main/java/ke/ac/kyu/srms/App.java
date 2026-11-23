
package ke.ac.kyu.srms;

import com.formdev.flatlaf.FlatLightLaf;
import ke.ac.kyu.srms.ui.LoginFrame;
import ke.ac.kyu.srms.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {

    public static void main(String[] args) {

        FlatLightLaf.setup();

        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);

        SwingUtilities.invokeLater(() -> {

            LoginFrame loginFrame = new LoginFrame(admin -> {

                MainFrame mainFrame = new MainFrame();

                mainFrame.setVisible(true);

            });

            loginFrame.setVisible(true);
        });
    }
}
