package com.parking.ui;

import com.parking.dao.ParkingTicketDAO;
import com.parking.dao.VehicleDAO;
import com.parking.model.ActiveTicketView;
import com.parking.model.User;
import com.parking.model.Vehicle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;

public class UserDashboardPanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MainFrame mainFrame;
    private final ParkingTicketDAO ticketDAO = new ParkingTicketDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    private final JLabel headerLabel = new JLabel(" ");
    private final DefaultTableModel tableModel;
    private final JTable table;
    private User currentUser;

    public UserDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xF5F5F5));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(0x1565C0));

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            currentUser = null;
            mainFrame.logout();
        });

        JButton addVehicle = new JButton("+ Add Vehicle");
        addVehicle.setBackground(new Color(0x2196F3));
        addVehicle.setForeground(Color.WHITE);
        addVehicle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addVehicle.setFocusPainted(false);
        addVehicle.addActionListener(e -> openAddVehicleDialog());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(headerLabel, BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        topRight.add(addVehicle);
        topRight.add(logout);
        top.add(topRight, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Ticket ID", "Vehicle Number", "Vehicle Type", "Floor", "Pillar Section", "Entry Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
        centered.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centered);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Active Parking Tickets"));
        add(scrollPane, BorderLayout.CENTER);

        JLabel hint = new JLabel("Tickets shown here disappear automatically after the vehicle exits.", SwingConstants.CENTER);
        hint.setForeground(new Color(0x616161));
        add(hint, BorderLayout.SOUTH);
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            headerLabel.setText("Welcome, " + user.getName() + " (" + user.getUserId() + ")");
        }
        refreshTickets();
    }

    private void refreshTickets() {
        tableModel.setRowCount(0);
        if (currentUser == null) {
            return;
        }
        try {
            for (ActiveTicketView view : ticketDAO.getActiveTicketViews(currentUser.getUserId())) {
                tableModel.addRow(new Object[]{
                        view.getTicketId(),
                        view.getVehicleNumber(),
                        view.getVehicleType(),
                        view.getFloorNumber(),
                        view.getPillarSection(),
                        view.getEntryTime() == null ? "" : view.getEntryTime().format(DATE_TIME)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load active tickets: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddVehicleDialog() {
        if (currentUser == null) {
            return;
        }
        JTextField numberField = new JTextField(15);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"2W", "4W"});

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Vehicle Number"));
        form.add(numberField);
        form.add(new JLabel("Vehicle Type"));
        form.add(typeCombo);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String number = numberField.getText().trim().toUpperCase();
        String type = (String) typeCombo.getSelectedItem();

        if (number.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vehicle number is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!number.matches("[A-Za-z0-9-]+")) {
            JOptionPane.showMessageDialog(this,
                    "Vehicle number may contain only letters, digits and hyphens.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (vehicleDAO.vehicleExists(number)) {
                JOptionPane.showMessageDialog(this, "This vehicle is already registered.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            vehicleDAO.insertVehicle(new Vehicle(number, currentUser.getUserId(), type));
            JOptionPane.showMessageDialog(this, "Vehicle added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not add vehicle: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
