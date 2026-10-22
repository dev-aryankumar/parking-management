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
    private final JPanel center;
    private final JLabel emptyState;
    private User currentUser;

    public UserDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(Theme.TEAL_DARK);

        JButton logout = new JButton("Logout");
        Theme.secondary(logout);
        logout.addActionListener(e -> {
            currentUser = null;
            mainFrame.logout();
        });

        JButton addVehicle = new JButton("+ Add Vehicle");
        Theme.primary(addVehicle);
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
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(Theme.CARD);
        table.setForeground(Theme.TEXT);
        table.setGridColor(Theme.BORDER);
        table.setShowVerticalLines(true);

        // The Windows look-and-feel paints its own table header, so the header
        // labels are drawn with an explicit renderer to guarantee contrast.
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable headerTable, Object value,
                                                                   boolean isSelected, boolean hasFocus,
                                                                   int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(
                        headerTable, value, isSelected, hasFocus, row, column);
                setBackground(Theme.TEAL_DARK);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                return component;
            }
        });

        DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
        centered.setHorizontalAlignment(SwingConstants.CENTER);
        centered.setBackground(Theme.CARD);
        centered.setForeground(Theme.TEXT);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centered);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Theme.CARD);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER), "Active Parking Tickets",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), Theme.TEAL_DARK));

        emptyState = new JLabel("No active parking tickets.", SwingConstants.CENTER);
        emptyState.setForeground(Theme.MUTED);
        emptyState.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        emptyState.setBackground(Theme.CARD);
        emptyState.setOpaque(true);

        center = new JPanel(new java.awt.CardLayout());
        center.setBackground(Theme.CARD);
        center.add(scrollPane, "TABLE");
        center.add(emptyState, "EMPTY");
        add(center, BorderLayout.CENTER);

        JLabel hint = new JLabel("Tickets shown here disappear automatically after the vehicle exits.", SwingConstants.CENTER);
        hint.setForeground(Theme.MUTED);
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
            showEmptyState();
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
        showEmptyState();
    }

    private void showEmptyState() {
        ((java.awt.CardLayout) center.getLayout()).show(center, tableModel.getRowCount() == 0 ? "EMPTY" : "TABLE");
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
