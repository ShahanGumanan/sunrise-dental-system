package com.sunrisedental.view;

import com.sunrisedental.util.SessionManager;
import com.sunrisedental.view.appointment.AppointmentFormPanel;
import com.sunrisedental.view.billing.BillFormPanel;
import com.sunrisedental.view.patient.PatientFormPanel;
import com.sunrisedental.view.patient.PatientListPanel;
import com.sunrisedental.view.admin.UserManagementPanel;
import com.sunrisedental.view.admin.TreatmentManagementPanel;
import com.sunrisedental.view.dentist.DentistSchedulePanel;
import com.sunrisedental.view.report.RevenueReportPanel;
import com.sunrisedental.view.appointment.AppointmentDirectoryPanel;
import com.sunrisedental.view.help.HelpPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;


public class MainFrame extends JFrame {

    private static final Color SIDEBAR_BG      = new Color(0x17182B);
    private static final Color SIDEBAR_BG_2    = new Color(0x1F2140);
    private static final Color SIDEBAR_TEXT    = new Color(0xB6BAD1);
    private static final Color SIDEBAR_TEXT_ON = Color.WHITE;
    private static final Color SIDEBAR_HOVER   = new Color(255, 255, 255, 26);
    private static final Color ACCENT          = new Color(0x5B6CF2);
    private static final Color ACCENT_DARK     = new Color(0x4A58D6);
    private static final Color CANVAS_BG       = new Color(0xF2F4F9);
    private static final Color HEADER_BG       = Color.WHITE;
    private static final Color BORDER_LIGHT    = new Color(0xE4E7F0);
    private static final Color TEXT_PRIMARY    = new Color(0x1A1F36);
    private static final Color TEXT_SECOND     = new Color(0x767E93);
    private static final Color DANGER          = new Color(0xE5484D);
    private static final Color DANGER_DARK     = new Color(0xC93E42);

    private static final Font FONT_APP_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_USER      = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_NAV       = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SECTION   = new Font("Segoe UI", Font.BOLD, 11);

    private JPanel rootPanel;
    private CardLayout cardLayout;

    // App Layout Panels
    private JPanel appPanel;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private JPanel sidebarPanel;
    private BillFormPanel billFormPanel;
    private DashboardPanel dashboardPanel;

    private final List<NavButton> navButtons = new ArrayList<>();
    private NavButton activeNavButton;

    public MainFrame() {
        setTitle("Sunrise Dental Clinic System");
        setSize(1100, 660);
        setMinimumSize(new Dimension(860, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Root layout handles switching between "Login" and "App"
        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        // 1. Add Login Panel
        rootPanel.add(new LoginPanel(this), "LOGIN");

        add(rootPanel);
    }

    // Called by LoginPanel upon success
    public void loadApplication() {
        buildAppLayout();
        rootPanel.add(appPanel, "APP");
        cardLayout.show(rootPanel, "APP");
    }

    private void buildAppLayout() {
        appPanel = new JPanel(new BorderLayout());
        appPanel.setBackground(CANVAS_BG);

        appPanel.add(buildHeader(), BorderLayout.NORTH);
        appPanel.add(buildSidebar(), BorderLayout.WEST);
        appPanel.add(buildContentArea(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT),
                BorderFactory.createEmptyBorder(14, 22, 14, 20)));

        JLabel titleLabel = new JLabel("Sunrise Dental Clinic System");
        titleLabel.setFont(FONT_APP_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.WEST);

        // Right side: user chip + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        String fullName = SessionManager.getCurrentUser().getFullName();
        JPanel userChip = buildUserChip(fullName);
        right.add(userChip);

        RoundedButton logoutBtn = new RoundedButton("Logout", new Color(0xF4F5FA), new Color(0xEDEFF7), DANGER);
        logoutBtn.setForeground(TEXT_PRIMARY);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        logoutBtn.addActionListener(e -> {
            SessionManager.logout();
            cardLayout.show(rootPanel, "LOGIN"); // Go back to login
            appPanel = null; // Clear memory
            navButtons.clear();
            activeNavButton = null;
        });
        right.add(logoutBtn);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildUserChip(String fullName) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chip.setOpaque(false);

        String initials = initialsOf(fullName);
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), ACCENT_DARK);
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel nameLabel = new JLabel(fullName == null ? "User" : fullName);
        nameLabel.setFont(FONT_USER);
        nameLabel.setForeground(TEXT_PRIMARY);

        chip.add(avatar);
        chip.add(nameLabel);
        return chip;
    }

    private String initialsOf(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    private JComponent buildSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));
        sidebarPanel.setPreferredSize(new Dimension(230, 0));

        // Brand mark at the top of the sidebar
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel brandBadge = new JLabel("SD", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), ACCENT_DARK);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        brandBadge.setPreferredSize(new Dimension(34, 34));
        brandBadge.setForeground(Color.WHITE);
        brandBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel brandText = new JLabel("Sunrise Dental");
        brandText.setForeground(SIDEBAR_TEXT_ON);
        brandText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brand.add(brandBadge);
        brand.add(brandText);
        sidebarPanel.add(brand);
        sidebarPanel.add(Box.createVerticalStrut(24));

        JLabel sectionLabel = new JLabel("MENU");
        sectionLabel.setFont(FONT_SECTION);
        sectionLabel.setForeground(new Color(0x565A78));
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 0));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(sectionLabel);

        // Content Area (For switching between Dashboard, Appointments, etc.)
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        dashboardPanel = new DashboardPanel();
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(new AppointmentFormPanel(), "NEW_APPOINTMENT");

        JTabbedPane patientTabs = new JTabbedPane();
        patientTabs.addTab("Register Patient", new PatientFormPanel());
        patientTabs.addTab("View All Patients", new PatientListPanel());
        contentPanel.add(patientTabs, "PATIENTS");
        billFormPanel = new BillFormPanel();
        contentPanel.add(billFormPanel, "BILLING");
        contentPanel.add(new UserManagementPanel(), "USERS");
        contentPanel.add(new DentistSchedulePanel(), "SCHEDULE");
        contentPanel.add(new RevenueReportPanel(), "REVENUE_REPORT");
        contentPanel.add(new AppointmentDirectoryPanel(false, appointment -> {
            billFormPanel.loadAppointment(appointment);
            contentCardLayout.show(contentPanel, "BILLING");
        }), "APPOINTMENT_LIST");
        contentPanel.add(new HelpPanel(), "HELP");
        contentPanel.add(new TreatmentManagementPanel(), "TREATMENTS");

        // Add Sidebar Buttons based on Role
        addNavButton("Dashboard", "DASHBOARD");

        if (SessionManager.hasRole("admin") || SessionManager.hasRole("receptionist")) {
            addNavButton("Book Appointment", "NEW_APPOINTMENT");
            addNavButton("Appt Directory", "APPOINTMENT_LIST");
            addNavButton("Patients", "PATIENTS");
            addNavButton("Billing", "BILLING");
        }

        if (SessionManager.hasRole("admin")) {
            addNavButton("Manage Staff", "USERS");
            addNavButton("Manage Treatments", "TREATMENTS");
            addNavButton("Revenue Report", "REVENUE_REPORT");
        }

        if (SessionManager.hasRole("dentist")) {
            addNavButton("My Schedule", "SCHEDULE");
            contentPanel.add(new AppointmentDirectoryPanel(true), "DENTIST_APPOINTMENTS");
            addNavButton("Appointments", "DENTIST_APPOINTMENTS");
        }

        addNavButton("System Help", "HELP");

        sidebarPanel.add(Box.createVerticalGlue());

        if (!navButtons.isEmpty()) {
            activeNavButton = navButtons.get(0);
            activeNavButton.setActive(true);
        }

        return sidebarPanel;
    }

    private void addNavButton(String title, String cardName) {
        NavButton btn = new NavButton(title);
        btn.addActionListener(e -> {
            if (activeNavButton != null) activeNavButton.setActive(false);
            activeNavButton = btn;
            btn.setActive(true);

            if ("DASHBOARD".equals(cardName) && dashboardPanel != null) {
                dashboardPanel.refresh();
            }
            contentCardLayout.show(contentPanel, cardName);
        });
        navButtons.add(btn);
        sidebarPanel.add(btn);
        sidebarPanel.add(Box.createVerticalStrut(4));
    }

    private JComponent buildContentArea() {
        JPanel canvas = new JPanel(new BorderLayout());
        canvas.setBackground(CANVAS_BG);
        canvas.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        canvas.add(contentPanel, BorderLayout.CENTER);
        return canvas;
    }

    private static class NavButton extends JButton {
        private boolean active = false;
        private float hoverAlpha = 0f;
        private Timer animator;

        NavButton(String text) {
            super("  " + text);
            setFont(FONT_NAV);
            setForeground(SIDEBAR_TEXT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!active) animateTo(1f);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!active) animateTo(0f);
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            setForeground(active ? SIDEBAR_TEXT_ON : SIDEBAR_TEXT);
            hoverAlpha = 0f;
            repaint();
        }

        private void animateTo(float target) {
            if (animator != null && animator.isRunning()) animator.stop();
            animator = new Timer(12, null);
            animator.addActionListener(e -> {
                float step = 0.2f;
                if (hoverAlpha < target) hoverAlpha = Math.min(target, hoverAlpha + step);
                else if (hoverAlpha > target) hoverAlpha = Math.max(target, hoverAlpha - step);
                repaint();
                if (hoverAlpha == target) animator.stop();
            });
            animator.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), ACCENT_DARK);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (hoverAlpha > 0f) {
                g2.setColor(new Color(255, 255, 255, (int) (26 * hoverAlpha)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Small rounded button used for the header actions (e.g. Logout), with a hover color transition. */
    private static class RoundedButton extends JButton {
        private final Color base;
        private final Color hoverColor;
        private final Color textHover;
        private float hoverAlpha = 0f;
        private Timer animator;

        RoundedButton(String text, Color base, Color hoverColor, Color textHover) {
            super(text);
            this.base = base;
            this.hoverColor = hoverColor;
            this.textHover = textHover;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animateTo(1f);
                    setForeground(textHover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    animateTo(0f);
                    setForeground(TEXT_PRIMARY);
                }
            });
        }

        private void animateTo(float target) {
            if (animator != null && animator.isRunning()) animator.stop();
            animator = new Timer(12, null);
            animator.addActionListener(e -> {
                float step = 0.2f;
                if (hoverAlpha < target) hoverAlpha = Math.min(target, hoverAlpha + step);
                else if (hoverAlpha > target) hoverAlpha = Math.max(target, hoverAlpha - step);
                repaint();
                if (hoverAlpha == target) animator.stop();
            });
            animator.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int red = (int) (base.getRed() + (hoverColor.getRed() - base.getRed()) * hoverAlpha);
            int green = (int) (base.getGreen() + (hoverColor.getGreen() - base.getGreen()) * hoverAlpha);
            int blue = (int) (base.getBlue() + (hoverColor.getBlue() - base.getBlue()) * hoverAlpha);
            g2.setColor(new Color(red, green, blue));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}