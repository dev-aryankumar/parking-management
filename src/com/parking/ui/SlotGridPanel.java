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
import javax.swing.border.TitledBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Visual 4x4 grid of pillar sections (A1-A2 ... D7-D8).
 * Available sections are mint cards with dark green text; full sections are
 * rose cards with dark red text. The buttons paint themselves, so the Windows
 * look-and-feel cannot wash out the colours.
 */
public class SlotGridPanel extends JPanel {

    private static final int ROWS = 4;
    private static final int COLS = 4;

    private final ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private final FloorDAO floorDAO = new FloorDAO();

    private final JComboBox<Floor> floorCombo = new JComboBox<>();
    private final JPanel grid = new JPanel(new GridLayout(ROWS, COLS, 6, 6));
    private final SectionButton[][] buttons = new SectionButton[ROWS][COLS];
    private final boolean[][] selectable = new boolean[ROWS][COLS];

    private final List<ActionListener> selectionListeners = new ArrayList<>();
    private String vehicleType = "4W";
    private ParkingSlot selectedSlot;
    private SectionButton selectedButton;

    public SlotGridPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(Theme.CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Theme.BORDER), "Manual Slot Selection",
                        TitledBorder.LEFT, TitledBorder.TOP, Theme.FONT_BOLD, Theme.TEAL_DARK),
                BorderFactory.createEmptyBorder(2, 8, 8, 8)));

        JPanel floorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        floorRow.setBackground(Theme.CARD);
        JLabel floorLabel = new JLabel("Floor:");
        floorLabel.setForeground(Theme.TEXT);
        floorLabel.setFont(Theme.FONT_BOLD);
        floorRow.add(floorLabel);
        floorCombo.setFont(Theme.FONT);
        floorCombo.setPreferredSize(new java.awt.Dimension(110, 26));
        floorRow.add(floorCombo);
        add(floorRow, BorderLayout.NORTH);

        grid.setBackground(Theme.CARD);
        add(grid, BorderLayout.CENTER);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                final int r = row;
                final int c = col;
                SectionButton button = new SectionButton();
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
            error.setForeground(Theme.RED_TEXT);
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
            selectedButton.setSelectedState(false);
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

                SectionButton button = buttons[row][col];
                selectable[row][col] = available > 0;
                button.setSelectedState(false);
                if (available > 0) {
                    button.apply(Theme.GREEN_BG, Theme.GREEN_TEXT, true);
                    button.setText("<html><center><b>" + section + "</b><br/>"
                            + "<span style='font-size:9px'>" + available + " free</span></center></html>");
                } else {
                    button.apply(Theme.RED_BG, Theme.RED_TEXT, false);
                    button.setText("<html><center><b>" + section + "</b><br/>"
                            + "<span style='font-size:9px'>FULL</span></center></html>");
                }
            }
        }
        selectedButton = null;
        revalidate();
        repaint();
    }

    private void handleSectionClick(int row, int col) {
        if (!selectable[row][col]) {
            return;
        }
        Floor floor = (Floor) floorCombo.getSelectedItem();
        if (floor == null) {
            return;
        }
        String section = sectionName(row, col);
        try {
            List<ParkingSlot> slots = slotDAO.getSlotsByFloorAndPillar(floor.getFloorId(), section, vehicleType);
            if (slots.isEmpty()) {
                refreshGrid();
                return;
            }
            selectedSlot = slots.get(0);
            if (selectedButton != null) {
                selectedButton.setSelectedState(false);
            }
            selectedButton = buttons[row][col];
            selectedButton.setSelectedState(true);
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

    /** Self-painting card button so the fill colour always shows. */
    private static final class SectionButton extends JButton {

        private Color fill = Theme.GREEN_BG;
        private Color outline = Theme.GREEN_TEXT;
        private boolean selected;

        SectionButton() {
            setContentAreaFilled(false);
            setOpaque(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void apply(Color fill, Color text, boolean selectable) {
            this.fill = fill;
            this.outline = text;
            setForeground(text);
            setCursor(Cursor.getPredefinedCursor(
                    selectable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        void setSelectedState(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

            g2.setColor(selected ? Theme.AMBER : Theme.BORDER);
            g2.setStroke(new BasicStroke(selected ? 3f : 1f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            g2.dispose();

            super.paintComponent(g);
        }
    }
}
