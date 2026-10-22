package com.parking.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;

/** Reusable modal used for both entry and exit receipts. */
public class ReceiptDialog extends JDialog {

    public ReceiptDialog(JFrame parent, String title, Map<String, String> fields) {
        super(parent, title, true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(new Color(0x1565C0));
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        int row = 0;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            JLabel key = new JLabel(entry.getKey() + " :");
            key.setFont(new Font("Consolas", Font.BOLD, 14));
            key.setForeground(new Color(0x424242));
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(3, 0, 3, 14);
            body.add(key, gbc);

            JLabel value = new JLabel(entry.getValue());
            value.setFont(new Font("Consolas", Font.PLAIN, 14));
            value.setForeground(new Color(0x212121));
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(3, 0, 3, 0);
            body.add(value, gbc);
            row++;
        }
        add(body, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.setPreferredSize(new Dimension(120, 34));
        ok.setBackground(new Color(0x2196F3));
        ok.setForeground(Color.WHITE);
        ok.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ok.setFocusPainted(false);
        ok.addActionListener(e -> dispose());

        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        footer.add(ok);
        add(footer, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(460, getHeight()));
        setLocationRelativeTo(parent);
    }
}
