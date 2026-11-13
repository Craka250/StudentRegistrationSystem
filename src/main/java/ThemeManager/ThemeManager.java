
package ke.ac.kyu.srms.config;

import java.awt.Color;
import java.awt.Font;

public final class ThemeManager {

    private ThemeManager() {
        // Prevent object creation
    }

    // Application colours
    public static final Color PRIMARY =
            new Color(37, 99, 235);

    public static final Color PRIMARY_DARK =
            new Color(29, 78, 216);

    public static final Color SIDEBAR =
            new Color(15, 23, 42);

    public static final Color SIDEBAR_HOVER =
            new Color(30, 41, 59);

    public static final Color BACKGROUND =
            new Color(245, 247, 251);

    public static final Color SURFACE =
            Color.WHITE;

    public static final Color TEXT_PRIMARY =
            new Color(30, 41, 59);

    public static final Color TEXT_SECONDARY =
            new Color(100, 116, 139);

    public static final Color SUCCESS =
            new Color(22, 163, 74);

    public static final Color WARNING =
            new Color(217, 119, 6);

    public static final Color DANGER =
            new Color(220, 38, 38);

    // Typography
    public static final Font FONT_REGULAR =
            new Font("Segoe UI", Font.PLAIN, 14);

    public static final Font FONT_MEDIUM =
            new Font("Segoe UI", Font.BOLD, 14);

    public static final Font FONT_HEADING =
            new Font("Segoe UI", Font.BOLD, 24);

    public static final Font FONT_SUBHEADING =
            new Font("Segoe UI", Font.BOLD, 17);
}
