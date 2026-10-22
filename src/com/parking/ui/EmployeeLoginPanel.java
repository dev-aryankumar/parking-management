package com.parking.ui;

import com.parking.dao.EmployeeDAO;
import com.parking.model.Employee;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class EmployeeLoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public EmployeeLoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(24, 36, 24, 36)));

        JLabel title = new JLabel("Employee Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEAL_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 18, 0);
        form.add(title, gbc);

        gbc.gridwidth = 1;
        addRow(form, gbc, 1, "Username", usernameField);
        addRow(form, gbc, 2, "Password", passwordField);

        JButton login = new JButton("Login");
        Theme.primary(login);
        login.addActionListener(e -> handleLogin());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(login, gbc);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.add(form);
        add(wrapper, BorderLayout.CENTER);

        JButton back = new JButton("← Back");
        Theme.secondary(back);
        back.addActionListener(e -> mainFrame.showCard(MainFrame.PORTAL_SELECTOR));
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        footer.setBackground(Theme.BG);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));
        footer.add(back);
        add(footer, BorderLayout.SOUTH);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        JLabel key = new JLabel(label);
        key.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(6, 0, 6, 12);
        form.add(key, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 6, 0);
        form.add(field, gbc);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Employee employee = employeeDAO.authenticate(username, password);
            if (employee == null) {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            passwordField.setText("");
            mainFrame.navigateToEmployeeDashboard(employee);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
