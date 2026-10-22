package com.parking.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Central palette and button styling for the whole application. */
public final class Theme {

    public static final Color TEAL_DARK = new Color(0x0F4C5C);
    public static final Color TEAL = new Color(0x137E7E);
    public static final Color TEAL_HOVER = new Color(0x0F6B6B);
    public static final Color TEAL_LIGHT = new Color(0xE3F2F1);
    public static final Color AMBER = new Color(0xF2A900);
    public static final Color AMBER_LIGHT = new Color(0xFFF3D1);

    public static final Color BG = new Color(0xF6FAFA);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x1E2B2C);
    public static final Color MUTED = new Color(0x5C6B6C);
    public static final Color BORDER = new Color(0xCFDDDD);
    public static final Color ROW_ALT = new Color(0xF2F7F7);

    public static final Color GREEN_BG = new Color(0xCFEFD9);
    public static final Color GREEN_TEXT = new Color(0x0B5D2A);
    public static final Color RED_BG = new Color(0xF8D7D6);
    public static final Color RED_TEXT = new Color(0x8A1C1C);

    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 22);

    private Theme() {
    }

    public static void primary(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setBackground(TEAL);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hover(button, TEAL, TEAL_HOVER);
    }

    public static void secondary(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setBackground(CARD);
        button.setForeground(TEAL_DARK);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hover(button, CARD, TEAL_LIGHT);
    }

    private static void hover(JButton button, Color normal, Color hover) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normal);
            }
        });
    }
}
