package com.parking.ui;

import com.parking.config.DBConnection;
import com.parking.dao.UserDAO;
import com.parking.dao.VehicleDAO;
import com.parking.model.User;
import com.parking.model.Vehicle;
import com.parking.util.UserIdGenerator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;

public class UserAuthPanel extends JPanel {

    private final MainFrame mainFrame;
    private final UserDAO userDAO = new UserDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    private final JTextField signupName = new JTextField(18);
    private final JPasswordField signupPassword = new JPasswordField(18);
    private final JTextField signupVehicle = new JTextField(18);
    private final JComboBox<String> signupType = new JComboBox<>(new String[]{"2W", "4W"});

    private final JTextField loginUserId = new JTextField(18);
    private final JPasswordField loginPassword = new JPasswordField(18);

    private final JTabbedPane tabs = new JTabbedPane();

    public UserAuthPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        tabs.addTab("Signup", buildSignupPanel());
        tabs.addTab("Login", buildLoginPanel());

        add(tabs, BorderLayout.CENTER);

        JButton back = new JButton("← Back");
        Theme.secondary(back);
        back.addActionListener(e -> mainFrame.showCard(MainFrame.PORTAL_SELECTOR));
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        footer.setBackground(Theme.BG);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));
        footer.add(back);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildSignupPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(24, 36, 24, 36)));

        Font titleFont = new Font("Segoe UI", Font.BOLD, 22);
        JLabel title = new JLabel("Create your parking account");
        title.setFont(titleFont);
        title.setForeground(Theme.TEAL_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 18, 0);
        form.add(title, gbc);

        gbc.gridwidth = 1;
        addRow(form, gbc, 1, "Name", signupName);
        addRow(form, gbc, 2, "Password", signupPassword);
        addRow(form, gbc, 3, "Vehicle Number", signupVehicle);
        addRow(form, gbc, 4, "Vehicle Type", signupType);

        JButton submit = new JButton("Sign Up");
        Theme.primary(submit);
        submit.addActionListener(e -> handleSignup());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(submit, gbc);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.add(form);
        return wrapper;
    }

    private JPanel buildLoginPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(24, 36, 24, 36)));

        JLabel title = new JLabel("Welcome back");
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
        addRow(form, gbc, 1, "User ID", loginUserId);
        addRow(form, gbc, 2, "Password", loginPassword);

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
        return wrapper;
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

    private void stylePrimary(JButton button) {
        Theme.primary(button);
    }

    private void handleSignup() {
        String name = signupName.getText().trim();
        String password = new String(signupPassword.getPassword());
        String vehicleNumber = signupVehicle.getText().trim().toUpperCase();
        String vehicleType = (String) signupType.getSelectedItem();

        if (name.isEmpty() || !name.matches("[A-Za-z ]+") || name.length() > 100) {
            showWarning("Name is required and must contain only letters.");
            return;
        }
        if (password.length() < 4) {
            showWarning("Password must be at least 4 characters.");
            return;
        }
        if (vehicleNumber.isEmpty()) {
            showWarning("Vehicle number is required.");
            return;
        }
        if (!vehicleNumber.matches("[A-Za-z0-9-]+")) {
            showWarning("Vehicle number may contain only letters, digits and hyphens.");
            return;
        }

        Connection connection = DBConnection.getConnection();
        try {
            if (vehicleDAO.vehicleExists(vehicleNumber)) {
                showWarning("Vehicle number already registered.");
                return;
            }

            connection.setAutoCommit(false);
            String userId = UserIdGenerator.generate(connection);
            userDAO.insertUser(new User(userId, name, password));
            vehicleDAO.insertVehicle(new Vehicle(vehicleNumber, userId, vehicleType));
            connection.commit();

            JOptionPane.showMessageDialog(
                    this,
                    "<html><div style='font-size:13px'>"
                            + "<b>Registration successful!</b><br/><br/>"
                            + "Your User ID: <b style='font-size:16px'>" + userId + "</b><br/><br/>"
                            + "<span style='color:#B71C1C'>Save this ID - you need it to log in and at the parking gate.</span>"
                            + "</div></html>",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            signupName.setText("");
            signupPassword.setText("");
            signupVehicle.setText("");
            signupType.setSelectedIndex(0);
            loginUserId.setText(userId);
            loginPassword.setText("");
            tabs.setSelectedIndex(1);
        } catch (Exception ex) {
            rollbackQuietly(connection);
            showError("Registration failed: " + rootMessage(ex));
        } finally {
            restoreAutoCommit(connection);
        }
    }

    private void handleLogin() {
        String userId = loginUserId.getText().trim().toUpperCase();
        String password = new String(loginPassword.getPassword());

        if (userId.isEmpty() || !userId.matches("USR-\\d{4}")) {
            showWarning("Invalid User ID format. Expected USR-XXXX.");
            return;
        }
        if (password.isEmpty()) {
            showWarning("Password is required.");
            return;
        }

        try {
            User user = userDAO.authenticate(userId, password);
            if (user == null) {
                showError("Invalid User ID or Password.");
                return;
            }
            loginPassword.setText("");
            mainFrame.navigateToUserDashboard(user);
        } catch (Exception ex) {
            showError("Login failed: " + rootMessage(ex));
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (Exception ignored) {
            // Best effort.
        }
    }

    private void restoreAutoCommit(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (Exception ignored) {
            // Best effort.
        }
    }
}
