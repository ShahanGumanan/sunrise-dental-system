package com.sunrisedental.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** Shared presentation styles for the application's Swing screens. */
public final class UiTheme {
    public static final Color CANVAS = new Color(0xF2F4F9);
    public static final Color SURFACE = Color.WHITE;
    public static final Color ACCENT = new Color(0x5B6CF2);
    public static final Color ACCENT_DARK = new Color(0x4A58D6);
    public static final Color TEXT = new Color(0x1A1F36);
    public static final Color MUTED = new Color(0x767E93);
    public static final Color BORDER = new Color(0xE0E4EE);
    public static final Color SUCCESS = new Color(0x168A64);
    public static final Color DANGER = new Color(0xD9485F);

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BUTTON = new Font("Segoe UI", Font.BOLD, 12);

    private UiTheme() { }

    public static void styleButton(JButton button) {
        button.setFont(BUTTON);
        button.setForeground(TEXT);
        button.setBackground(new Color(0xF4F5FA));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void stylePrimaryButton(JButton button) {
        styleButton(button);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    public static void styleField(JComponent field) {
        field.setFont(LABEL);
        field.setForeground(TEXT);
        field.setBackground(SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
    }

    public static void styleTitle(JLabel title) {
        title.setFont(TITLE);
        title.setForeground(TEXT);
    }

    public static void styleTable(JTable table) {
        table.setFont(LABEL);
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setSelectionBackground(new Color(0xE8EBFF));
        table.setSelectionForeground(TEXT);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(MUTED);
        header.setBackground(new Color(0xF7F8FC));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
    }

    public static Border surfaceBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 20, 18, 20));
    }
}