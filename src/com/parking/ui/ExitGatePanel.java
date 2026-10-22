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
import com.parking.util.FeeCalculator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExitGatePanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String RUPEE = "\u20B9";

    private final UserDAO userDAO = new UserDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private final ParkingTicketDAO ticketDAO = new ParkingTicketDAO();
    private final FloorDAO floorDAO = new FloorDAO();

    private final JTextField userIdField = new JTextField(15);
    private final JTextArea summaryArea = new JTextArea(12, 46);
    private final JButton confirmButton = new JButton("Confirm Exit & Payment");

    private Employee currentEmployee;
    private User currentUser;
    private ParkingTicket currentTicket;
    private Vehicle currentVehicle;
    private ParkingSlot currentSlot;
    private Floor currentFloor;

    public ExitGatePanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel();
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchRow.setOpaque(false);
        JLabel searchLabel = new JLabel("User ID:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchRow.add(searchLabel);
        searchRow.add(userIdField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchRow.add(searchButton);
        top.add(searchRow);

        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        summaryArea.setBackground(new Color(0xFAFAFA));
        summaryArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        summaryArea.setText("Search a user to view their active parking session.");
        top.add(new JScrollPane(summaryArea));

        add(top, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        footer.setOpaque(false);
        confirmButton.setBackground(new Color(0x2196F3));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmButton.setFocusPainted(false);
        confirmButton.addActionListener(e -> handleConfirm());
        footer.add(confirmButton);
        add(footer, BorderLayout.SOUTH);

        resetSession();
    }

    public void setEmployee(Employee employee) {
        this.currentEmployee = employee;
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
                resetSession();
                showError("User ID not found.");
                return;
            }

            ParkingTicket ticket = ticketDAO.getMostRecentActiveTicketByUserId(userId);
            if (ticket == null) {
                resetSession();
                JOptionPane.showMessageDialog(this,
                        "No active parking session found for this user.",
                        "No Active Session", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            ParkingSlot slot = slotDAO.getSlotById(ticket.getSlotId());
            if (slot == null) {
                resetSession();
                showError("The slot for this ticket no longer exists.");
                return;
            }
            Floor floor = floorDAO.getFloorById(slot.getFloorId());
            Vehicle vehicle = vehicleDAO.getVehicleByNumber(ticket.getVehicleNumber());
            if (vehicle == null) {
                resetSession();
                showError("The vehicle for this ticket no longer exists.");
                return;
            }

            currentUser = user;
            currentTicket = ticket;
            currentVehicle = vehicle;
            currentSlot = slot;
            currentFloor = floor;

            renderSummary(LocalDateTime.now());
        } catch (Exception ex) {
            resetSession();
            showError("Search failed: " + ex.getMessage());
        }
    }

    private void renderSummary(LocalDateTime now) {
        long completedHours = FeeCalculator.getCompletedHours(currentTicket.getEntryTime(), now);
        BigDecimal fee = FeeCalculator.calculateFee(currentTicket.getEntryTime(), now, currentVehicle.getType());

        StringBuilder text = new StringBuilder();
        text.append("Ticket ID      : ").append(currentTicket.getTicketId()).append('\n');
        text.append("Customer       : ").append(currentUser.getName())
                .append(" (").append(currentUser.getUserId()).append(")\n");
        text.append("Vehicle        : ").append(currentVehicle.toString()).append('\n');
        text.append("Floor          : ").append(currentFloor == null ? "-" : currentFloor.getFloorNumber()).append('\n');
        text.append("Pillar         : ").append(currentSlot.getPillarSection()).append('\n');
        text.append("Entry Time     : ").append(format(currentTicket.getEntryTime())).append('\n');
        text.append("Current Time   : ").append(format(now)).append('\n');
        text.append("Duration       : ")
                .append(FeeCalculator.formatDuration(currentTicket.getEntryTime(), now)).append('\n');
        text.append("Completed Hrs  : ").append(completedHours).append('\n');
        text.append("Rate           : ").append(RUPEE)
                .append(FeeCalculator.getRate(currentVehicle.getType()).toPlainString())
                .append("/hr (").append(currentVehicle.getType()).append(")\n");
        text.append("----------------------------------------\n");
        text.append("TOTAL FEE      : ").append(RUPEE).append(fee.toPlainString()).append('\n');

        summaryArea.setText(text.toString());
        summaryArea.setCaretPosition(0);
    }

    private void handleConfirm() {
        if (currentTicket == null) {
            showWarning("Search for an active parking session first.");
            return;
        }
        if (currentEmployee == null) {
            showError("No employee session. Please log in again.");
            return;
        }

        LocalDateTime exitTime = LocalDateTime.now();
        BigDecimal fee = FeeCalculator.calculateFee(
                currentTicket.getEntryTime(), exitTime, currentVehicle.getType());

        Connection connection = DBConnection.getConnection();
        try {
            connection.setAutoCommit(false);

            boolean updated = ticketDAO.updateTicketExit(currentTicket.getTicketId(), exitTime, fee);
            if (!updated) {
                throw new SQLException("This ticket is no longer active.");
            }
            slotDAO.updateSlotOccupancy(currentTicket.getSlotId(), false);
            connection.commit();

            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("Ticket ID", String.valueOf(currentTicket.getTicketId()));
            fields.put("Customer", currentUser.getName());
            fields.put("Vehicle", currentVehicle.toString());
            fields.put("Floor", currentFloor == null ? "-" : currentFloor.getFloorNumber());
            fields.put("Pillar", currentSlot.getPillarSection());
            fields.put("Entry Time", format(currentTicket.getEntryTime()));
            fields.put("Exit Time", format(exitTime));
            fields.put("Duration", FeeCalculator.formatDuration(currentTicket.getEntryTime(), exitTime));
            fields.put("Completed Hrs", String.valueOf(
                    FeeCalculator.getCompletedHours(currentTicket.getEntryTime(), exitTime)));
            fields.put("Rate", RUPEE + FeeCalculator.getRate(currentVehicle.getType()).toPlainString()
                    + "/hr (" + currentVehicle.getType() + ")");
            fields.put("TOTAL FEE", RUPEE + fee.toPlainString());
            fields.put("Status", "PAYMENT SETTLED");

            new ReceiptDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Parking Exit Receipt", fields).setVisible(true);

            resetSession();
        } catch (Exception ex) {
            rollbackQuietly(connection);
            showError("Could not complete the exit: " + rootMessage(ex));
        } finally {
            restoreAutoCommit(connection);
        }
    }

    private String format(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME);
    }

    private void resetSession() {
        currentUser = null;
        currentTicket = null;
        currentVehicle = null;
        currentSlot = null;
        currentFloor = null;
        if (summaryArea != null) {
            summaryArea.setText("Search a user to view their active parking session.");
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
