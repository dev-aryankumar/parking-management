package com.parking;

import com.parking.config.DBConnection;
import com.parking.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the default Swing look and feel.
        }

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
}
