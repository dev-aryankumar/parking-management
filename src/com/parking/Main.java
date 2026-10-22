package com.parking;

import com.parking.config.DBConnection;
import com.parking.ui.MainFrame;
import com.parking.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the default Swing look and feel.
        }

        applyThemeDefaults();

        try {
            DBConnection.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Database connection failed!\n\n"
                            + e.getMessage()
                            + "\n\nChecklist:\n"
                            + "  1. MySQL is running.\n"
                            + "  2. schema.sql has been executed (database: smart_parking).\n"
                            + "  3. MYSQL_PASSWORD environment variable is set.\n"
                            + "  4. mysql-connector-j jar is on the classpath.",
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    private static void applyThemeDefaults() {
        UIManager.put("Panel.background", Theme.BG);
        UIManager.put("OptionPane.background", Theme.BG);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT);
        UIManager.put("Label.foreground", Theme.TEXT);
        UIManager.put("Label.font", Theme.FONT);
        UIManager.put("TabbedPane.background", Theme.BG);
        UIManager.put("TabbedPane.foreground", Theme.TEXT);
        UIManager.put("TabbedPane.selected", Theme.TEAL_LIGHT);
        UIManager.put("TabbedPane.font", Theme.FONT_BOLD);
        UIManager.put("Table.background", Theme.CARD);
        UIManager.put("Table.foreground", Theme.TEXT);
        UIManager.put("Table.gridColor", Theme.BORDER);
        UIManager.put("Table.selectionBackground", Theme.TEAL_LIGHT);
        UIManager.put("Table.selectionForeground", Theme.TEXT);
        UIManager.put("TitledBorder.titleColor", Theme.TEAL_DARK);
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(Theme.BORDER));
        UIManager.put("TextField.background", Theme.CARD);
        UIManager.put("TextField.foreground", Theme.TEXT);
        UIManager.put("TextField.caretForeground", Theme.TEXT);
        UIManager.put("PasswordField.background", Theme.CARD);
        UIManager.put("PasswordField.foreground", Theme.TEXT);
        UIManager.put("PasswordField.caretForeground", Theme.TEXT);
        UIManager.put("ComboBox.background", Theme.CARD);
        UIManager.put("ComboBox.foreground", Theme.TEXT);
        UIManager.put("RadioButton.background", Theme.CARD);
        UIManager.put("RadioButton.foreground", Theme.TEXT);
        UIManager.put("CheckBox.background", Theme.CARD);
        UIManager.put("CheckBox.foreground", Theme.TEXT);
    }
}
