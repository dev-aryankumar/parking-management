package com.parking.ui;

import com.parking.config.DBConnection;
import com.parking.dao.FloorDAO;
import com.parking.dao.ParkingSlotDAO;
import com.parking.dao.ParkingTicketDAO;
import com.parking.dao.UserDAO;
import com.parking.dao.VehicleDAO;
import com.parking.model.Employee;
import com.parking.model.Floor;
import com.parking.model.ParkingSlot;
import com.parking.model.ParkingTicket;
import com.parking.model.User;
import com.parking.model.Vehicle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntryGatePanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserDAO userDAO = new UserDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private final ParkingTicketDAO ticketDAO = new ParkingTicketDAO();
    private final FloorDAO floorDAO = new FloorDAO();

    private final JTextField userIdField = new JTextField(14);
    private final JLabel customerLabel = new JLabel("Search a user to begin.");
    private final JComboBox<Vehicle> vehicleCombo = new JComboBox<>();
    private final JRadioButton autoRadio = new JRadioButton("Auto-Assign", true);
    private final JRadioButton manualRadio = new JRadioButton("Manual-Assign");
    private final SlotGridPanel slotGridPanel = new SlotGridPanel();
    private final JButton confirmButton = new JButton("Allocate Slot & Generate Ticket");

    private Employee currentEmployee;
    private User foundUser;

    public EntryGatePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Theme.CARD);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(2, 0, 2, 0);

        // --- Search row -----------------------------------------------------
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        searchRow.setOpaque(false);
        JLabel searchLabel = new JLabel("User ID:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(Theme.TEXT);
        searchRow.add(searchLabel);
        searchRow.add(userIdField);
        JButton searchButton = new JButton("Search");
        Theme.secondary(searchButton);
        searchButton.addActionListener(e -> handleSearch());
        searchRow.add(searchButton);
        addRow(content, gbc, 0, searchRow, 0);

        // --- Customer summary ----------------------------------------------
        JPanel customerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        customerRow.setOpaque(false);
        customerLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        customerLabel.setForeground(Theme.TEAL_DARK);
        customerRow.add(customerLabel);
        addRow(content, gbc, 1, customerRow, 0);

        // --- Vehicle selection ---------------------------------------------
        JPanel vehicleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        vehicleRow.setOpaque(false);
        JLabel vehicleLabel = new JLabel("Vehicle:");
        vehicleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vehicleLabel.setForeground(Theme.TEXT);
        vehicleRow.add(vehicleLabel);
        vehicleCombo.setPreferredSize(new java.awt.Dimension(230, 28));
        vehicleCombo.setFont(Theme.FONT);
        vehicleRow.add(vehicleCombo);
        addRow(content, gbc, 2, vehicleRow, 0);

        // --- Allocation mode ------------------------------------------------
        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        modeRow.setOpaque(false);
        JLabel modeLabel = new JLabel("Allocation:");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modeLabel.setForeground(Theme.TEXT);
        modeRow.add(modeLabel);
        ButtonGroup group = new ButtonGroup();
        group.add(autoRadio);
        group.add(manualRadio);
        autoRadio.setOpaque(false);
        manualRadio.setOpaque(false);
        autoRadio.setForeground(Theme.TEXT);
        manualRadio.setForeground(Theme.TEXT);
        modeRow.add(autoRadio);
        modeRow.add(manualRadio);
        addRow(content, gbc, 3, modeRow, 0);

        // --- Manual slot grid (grows to fill the remaining space) ----------
        slotGridPanel.setVisible(false);
        addRow(content, gbc, 4, slotGridPanel, 0);

        // Filler keeps the form anchored to the top when the grid is hidden.
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        addRow(content, gbc, 5, filler, 1);

        // --- Confirm (pinned below the scroll area so it is always visible) --
        JPanel confirmRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        confirmRow.setOpaque(false);
        confirmRow.setBackground(Theme.CARD);
        Theme.primary(confirmButton);
        confirmButton.addActionListener(e -> handleConfirm());
        confirmRow.add(confirmButton);
        add(confirmRow, BorderLayout.SOUTH);

        add(new JScrollPane(content), BorderLayout.CENTER);

        // --- Wiring ---------------------------------------------------------
        manualRadio.addActionListener(e -> {
            slotGridPanel.setVisible(true);
            updateGridVehicleType();
            slotGridPanel.refreshGrid();
            revalidate();
            repaint();
        });
        autoRadio.addActionListener(e -> {
            slotGridPanel.setVisible(false);
            revalidate();
            repaint();
        });
        vehicleCombo.addActionListener(e -> updateGridVehicleType());
    }

    private void addRow(JPanel target, GridBagConstraints gbc, int row, java.awt.Component component, double weighty) {
        gbc.gridy = row;
        gbc.weighty = weighty;
        gbc.fill = GridBagConstraints.BOTH;
        target.add(component, gbc);
    }

    public void setEmployee(Employee employee) {
        this.currentEmployee = employee;
    }

    private void updateGridVehicleType() {
        Vehicle vehicle = (Vehicle) vehicleCombo.getSelectedItem();
        if (vehicle != null) {
            slotGridPanel.setVehicleType(vehicle.getType());
        }
    }

    private void handleSearch() {
        String userId = userIdField.getText().trim().toUpperCase();
        if (userId.isEmpty()) {
            showWarning("Please enter a User ID.");
            return;
        }

        try {
            User user = userDAO.getUserById(userId);
            if (user == null) {
                resetCustomer();
                showError("User ID not found.");
                return;
            }
            foundUser = user;
            customerLabel.setText("Customer: " + user.getName() + " (" + user.getUserId() + ")");

            vehicleCombo.removeAllItems();
            for (Vehicle vehicle : vehicleDAO.getVehiclesByUserId(userId)) {
                vehicleCombo.addItem(vehicle);
            }
            if (vehicleCombo.getItemCount() == 0) {
                showWarning("This user has no registered vehicles.");
                return;
            }
            vehicleCombo.setSelectedIndex(0);
            updateGridVehicleType();
            slotGridPanel.clearSelection();
            slotGridPanel.refreshGrid();
        } catch (Exception ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    private void handleConfirm() {
        if (currentEmployee == null) {
            showError("No employee session. Please log in again.");
            return;
        }
        Vehicle vehicle = (Vehicle) vehicleCombo.getSelectedItem();
        if (vehicle == null) {
            showWarning("Please select a vehicle.");
            return;
        }

        ParkingSlot chosen;
        try {
            if (autoRadio.isSelected()) {
                chosen = slotDAO.findFirstAvailableSlot(vehicle.getType());
            } else {
                chosen = slotGridPanel.getSelectedSlot();
            }
        } catch (Exception ex) {
            showError("Could not find a slot: " + ex.getMessage());
            return;
        }

        if (chosen == null) {
            showWarning("No available slots for this vehicle type. Select another pillar section or floor.");
            return;
        }

        LocalDateTime entryTime = LocalDateTime.now();
        Connection connection = DBConnection.getConnection();
        try {
            connection.setAutoCommit(false);

            ParkingSlot fresh = slotDAO.getSlotById(chosen.getSlotId());
            if (fresh == null || fresh.isOccupied()) {
                throw new SQLException("Selected slot is no longer available.");
            }
            if (ticketDAO.hasActiveTicket(vehicle.getVehicleNumber())) {
                throw new SQLException("This vehicle already has an active parking session.");
            }

            ParkingTicket ticket = new ParkingTicket();
            ticket.setVehicleNumber(vehicle.getVehicleNumber());
            ticket.setSlotId(fresh.getSlotId());
            ticket.setEmployeeId(currentEmployee.getEmployeeId());
            ticket.setEntryTime(entryTime);
            int ticketId = ticketDAO.insertTicket(ticket);

            slotDAO.updateSlotOccupancy(fresh.getSlotId(), true);
            connection.commit();

            Floor floor = floorDAO.getFloorById(fresh.getFloorId());

            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("Ticket ID", String.valueOf(ticketId));
            fields.put("Customer", foundUser == null ? "-" : foundUser.getName());
            fields.put("Vehicle", vehicle.toString());
            fields.put("Floor", floor == null ? "-" : floor.getFloorNumber());
            fields.put("Pillar", fresh.getPillarSection());
            fields.put("Slot ID", String.valueOf(fresh.getSlotId()));
            fields.put("Entry Time", entryTime.format(DATE_TIME));
            fields.put("Employee", currentEmployee.getUsername());

            new ReceiptDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Parking Entry Receipt", fields).setVisible(true);

            resetForm();
        } catch (Exception ex) {
            rollbackQuietly(connection);
            showError("Could not create the ticket: " + rootMessage(ex));
        } finally {
            restoreAutoCommit(connection);
        }
    }

    private void resetForm() {
        foundUser = null;
        userIdField.setText("");
        customerLabel.setText("Search a user to begin.");
        vehicleCombo.removeAllItems();
        autoRadio.setSelected(true);
        slotGridPanel.setVisible(false);
        slotGridPanel.clearSelection();
        revalidate();
        repaint();
    }

    private void resetCustomer() {
        foundUser = null;
        customerLabel.setText("Search a user to begin.");
        vehicleCombo.removeAllItems();
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
