package com.parking.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class PortalSelectorPanel extends JPanel {

    public PortalSelectorPanel(MainFrame mainFrame) {
        setLayout(new GridBagLayout());
        setBackground(new Color(0xF5F5F5));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Smart Parking Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(0x1565C0));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 4, 10);
        add(title, gbc);

        JLabel subtitle = new JLabel("Choose a portal to continue");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(0x616161));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 30, 10);
        add(subtitle, gbc);

        JButton userButton = portalButton("User Portal", new Color(0x2196F3));
        userButton.addActionListener(e -> mainFrame.showCard(MainFrame.USER_AUTH));
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(userButton, gbc);

        JButton employeeButton = portalButton("Employee Portal", new Color(0x1565C0));
        employeeButton.addActionListener(e -> mainFrame.showCard(MainFrame.EMPLOYEE_LOGIN));
        gbc.gridy = 3;
        add(employeeButton, gbc);
    }

    private JButton portalButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(280, 62));
        button.setFont(new Font("Segoe UI", Font.BOLD, 17));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
}
