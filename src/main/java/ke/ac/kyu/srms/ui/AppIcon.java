
package ke.ac.kyu.srms.ui;

import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

public final class AppIcon {

    private AppIcon() {
        // Prevent object creation.
    }

    public static void apply(JFrame frame) {
        try {
            Image icon = ImageIO.read(
                AppIcon.class.getResource("/icons/srms-icon.png")
            );

            if (icon != null) {
                frame.setIconImage(icon);
            }

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Unable to load SRMS application icon: "
                    + e.getMessage());
        }
    }
}
