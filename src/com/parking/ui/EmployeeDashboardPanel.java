package com.parking.ui;

import com.parking.model.Employee;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

public class EmployeeDashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final JLabel headerLabel = new JLabel(" ");
    private final EntryGatePanel entryGatePanel = new EntryGatePanel();
    private final ExitGatePanel exitGatePanel = new ExitGatePanel();

    public EmployeeDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5F5F5));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(0x1565C0));

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> mainFrame.logout());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(headerLabel, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(logout);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("Entry Gate", entryGatePanel);
        tabs.addTab("Exit Gate", exitGatePanel);
        add(tabs, BorderLayout.CENTER);
    }

    public void setEmployee(Employee employee) {
        if (employee != null) {
            headerLabel.setText("Employee Dashboard - " + employee.getUsername() + " (" + employee.getRole() + ")");
        }
        entryGatePanel.setEmployee(employee);
        exitGatePanel.setEmployee(employee);
    }
}
