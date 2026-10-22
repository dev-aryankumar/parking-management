package com.parking.ui;

import com.parking.dao.FloorDAO;
import com.parking.dao.ParkingSlotDAO;
import com.parking.model.Floor;
import com.parking.model.ParkingSlot;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Visual 4x4 grid of pillar sections (A1-A2 ... D7-D8). Green = at least one
 * free slot of the selected vehicle type, red/disabled = full.
 */
public class SlotGridPanel extends JPanel {

    private static final int ROWS = 4;
    private static final int COLS = 4;

    private final ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private final FloorDAO floorDAO = new FloorDAO();

    private final JComboBox<Floor> floorCombo = new JComboBox<>();
    private final JPanel grid = new JPanel(new GridLayout(ROWS, COLS, 8, 8));
    private final JButton[][] buttons = new JButton[ROWS][COLS];

    private final List<ActionListener> selectionListeners = new ArrayList<>();
    private String vehicleType = "4W";
    private ParkingSlot selectedSlot;
    private JButton selectedButton;
    private final javax.swing.border.Border normalBorder =
            BorderFactory.createLineBorder(new Color(0xBDBDBD));
    private final javax.swing.border.Border selectedBorder =
            BorderFactory.createLineBorder(new Color(0x1565C0), 3);

    public SlotGridPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createTitledBorder("Manual Slot Selection"));
        setOpaque(false);

        JPanel floorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        floorRow.setOpaque(false);
        floorRow.add(new JLabel("Floor:"));
        floorRow.add(floorCombo);
        add(floorRow, BorderLayout.NORTH);

        grid.setOpaque(false);
        add(grid, BorderLayout.CENTER);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                final int r = row;
                final int c = col;
                JButton button = new JButton();
                button.setOpaque(true);
                button.setContentAreaFilled(true);
                button.setFocusPainted(false);
                button.setBorder(normalBorder);
                button.addActionListener(e -> handleSectionClick(r, c));
                buttons[row][col] = button;
                grid.add(button);
            }
        }

        floorCombo.addActionListener(e -> {
            selectedSlot = null;
            refreshGrid();
        });

        loadFloors();
    }

    private void loadFloors() {
        try {
            for (Floor floor : floorDAO.getAllFloors()) {
                floorCombo.addItem(floor);
            }
        } catch (Exception ex) {
            JLabel error = new JLabel("Could not load floors: " + ex.getMessage());
            error.setForeground(new Color(0xC62828));
            add(error, BorderLayout.SOUTH);
            return;
        }
        refreshGrid();
    }

    public void setVehicleType(String vehicleType) {
        if (vehicleType != null && !vehicleType.equals(this.vehicleType)) {
            this.vehicleType = vehicleType;
            selectedSlot = null;
            refreshGrid();
        }
    }

    public ParkingSlot getSelectedSlot() {
        return selectedSlot;
    }

    public void clearSelection() {
        selectedSlot = null;
        if (selectedButton != null) {
            selectedButton.setBorder(normalBorder);
            selectedButton = null;
        }
    }

    public void addSlotSelectionListener(ActionListener listener) {
        selectionListeners.add(listener);
    }

    public void refreshGrid() {
        Floor floor = (Floor) floorCombo.getSelectedItem();
        if (floor == null) {
            return;
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String section = sectionName(row, col);
                int available;
                try {
                    available = slotDAO.countAvailableSlots(floor.getFloorId(), section, vehicleType);
                } catch (Exception ex) {
                    available = 0;
                }

                JButton button = buttons[row][col];
                button.setBorder(normalBorder);
                if (available > 0) {
                    button.setEnabled(true);
                    button.setBackground(new Color(0x4CAF50));
                    button.setForeground(Color.WHITE);
                    button.setText("<html><center><b>" + section + "</b><br/>" + available + " free</center></html>");
                } else {
                    button.setEnabled(false);
                    button.setBackground(new Color(0xEF9A9A));
                    button.setForeground(new Color(0x5D0000));
                    button.setText("<html><center><b>" + section + "</b><br/>FULL</center></html>");
                }
            }
        }
        selectedButton = null;
        revalidate();
        repaint();
    }

    private void handleSectionClick(int row, int col) {
        Floor floor = (Floor) floorCombo.getSelectedItem();
        if (floor == null) {
            return;
        }
        String section = sectionName(row, col);
        try {
            List<ParkingSlot> slots = slotDAO.getSlotsByFloorAndPillar(floor.getFloorId(), section, vehicleType);
            if (slots.isEmpty()) {
                return;
            }
            selectedSlot = slots.get(0);
            if (selectedButton != null) {
                selectedButton.setBorder(normalBorder);
            }
            selectedButton = buttons[row][col];
            selectedButton.setBorder(selectedBorder);
            for (ActionListener listener : selectionListeners) {
                listener.actionPerformed(
                        new java.awt.event.ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, section));
            }
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Could not read slots: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private String sectionName(int row, int colPair) {
        char rowChar = (char) ('A' + row);
        int first = colPair * 2 + 1;
        int second = first + 1;
        return "" + rowChar + first + "-" + rowChar + second;
    }
}
