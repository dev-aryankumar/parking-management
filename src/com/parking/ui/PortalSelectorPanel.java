package com.parking.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class PortalSelectorPanel extends JPanel {

    public PortalSelectorPanel(MainFrame mainFrame) {
        setLayout(new GridBagLayout());
        setBackground(Theme.BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Smart Parking Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Theme.TEAL_DARK);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 4, 10);
        add(title, gbc);

        JLabel subtitle = new JLabel("Choose a portal to continue");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(Theme.MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 30, 10);
        add(subtitle, gbc);

        JButton userButton = new JButton("User Portal");
        Theme.primary(userButton);
        userButton.setPreferredSize(new Dimension(280, 62));
        userButton.setFont(new Font("Segoe UI", Font.BOLD, 17));
        userButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        userButton.addActionListener(e -> mainFrame.showCard(MainFrame.USER_AUTH));
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(userButton, gbc);

        JButton employeeButton = new JButton("Employee Portal");
        Theme.secondary(employeeButton);
        employeeButton.setPreferredSize(new Dimension(280, 62));
        employeeButton.setFont(new Font("Segoe UI", Font.BOLD, 17));
        employeeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        employeeButton.addActionListener(e -> mainFrame.showCard(MainFrame.EMPLOYEE_LOGIN));
        gbc.gridy = 3;
        add(employeeButton, gbc);
    }
}
