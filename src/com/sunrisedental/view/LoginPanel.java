package com.sunrisedental.view;

import com.sunrisedental.controller.AuthController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {

    private static final Color BG_TOP        = new Color(0x5B6CF2);
    private static final Color BG_BOTTOM     = new Color(0xEFF3FB);
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color ACCENT        = new Color(0x5B6CF2);
    private static final Color ACCENT_DARK   = new Color(0x4A58D6);
    private static final Color TEXT_PRIMARY  = new Color(0x1A1F36);
    private static final Color TEXT_SECOND   = new Color(0x8A93A6);
    private static final Color FIELD_BORDER  = new Color(0xDCE1EA);
    private static final Color FIELD_FOCUS   = ACCENT;
    private static final Color ERROR_RED     = new Color(0xE5484D);

    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_FIELD    = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD, 14);

    private final AuthController authController;
    private final MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authController = new AuthController();

        setLayout(new GridBagLayout());
        setOpaque(true);

        // ---- Card ----------------------------------------------------
        RoundedShadowPanel card = new RoundedShadowPanel(22);
        card.setLayout(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(42, 44, 38, 44));
        card.setPreferredSize(new Dimension(400, 420));

        GridBagConstraints cc = new GridBagConstraints();
        cc.gridx = 0;
        cc.gridy = 0;
        cc.fill = GridBagConstraints.HORIZONTAL;
        cc.weightx = 1;

        // Logo badge
        JLabel logo = new JLabel("SD", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), ACCENT_DARK);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setPreferredSize(new Dimension(56, 56));
        logo.setMaximumSize(new Dimension(56, 56));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel logoWrap = new JPanel();
        logoWrap.setOpaque(false);
        logoWrap.add(logo);
        cc.insets = new Insets(0, 0, 18, 0);
        card.add(logoWrap, cc);

        // Title + subtitle
        JLabel titleLabel = new JLabel("Welcome back", SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cc.gridy++;
        cc.insets = new Insets(0, 0, 4, 0);
        card.add(titleLabel, cc);

        JLabel subtitleLabel = new JLabel("Sign in to Sunrise Dental Clinic System", SwingConstants.CENTER);
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(TEXT_SECOND);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cc.gridy++;
        cc.insets = new Insets(0, 0, 26, 0);
        card.add(subtitleLabel, cc);

        // Username field
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(FONT_LABEL);
        userLabel.setForeground(TEXT_SECOND);
        cc.gridy++;
        cc.insets = new Insets(0, 2, 6, 0);
        card.add(userLabel, cc);

        RoundedTextField usernameField = new RoundedTextField();
        cc.gridy++;
        cc.insets = new Insets(0, 0, 16, 0);
        card.add(usernameField, cc);

        // Password field
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(FONT_LABEL);
        passLabel.setForeground(TEXT_SECOND);
        cc.gridy++;
        cc.insets = new Insets(0, 2, 6, 0);
        card.add(passLabel, cc);

        RoundedPasswordField passwordField = new RoundedPasswordField();
        cc.gridy++;
        cc.insets = new Insets(0, 0, 8, 0);
        card.add(passwordField, cc);

        // Inline error/status label (hidden until a failed attempt)
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ERROR_RED);
        cc.gridy++;
        cc.insets = new Insets(0, 2, 14, 0);
        card.add(statusLabel, cc);

        // Login button
        RoundedButton loginBtn = new RoundedButton("Sign In", ACCENT, ACCENT_DARK);
        loginBtn.setFont(FONT_BUTTON);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(10, 46));
        cc.gridy++;
        cc.insets = new Insets(4, 0, 0, 0);
        card.add(loginBtn, cc);

        // Footer note
        JLabel footer = new JLabel("Sunrise Dental Clinic  ·  Staff & Patient Portal", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(TEXT_SECOND);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        cc.gridy++;
        cc.insets = new Insets(22, 0, 0, 0);
        card.add(footer, cc);

        add(card, new GridBagConstraints());

        // --- Behavior: identical to the original implementation ---
        Runnable attemptLogin = () -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            AuthController.LoginResult result = authController.authenticate(username, password);
            if (result == AuthController.LoginResult.SUCCESS) {
                mainFrame.loadApplication();
            } else {
                statusLabel.setText(result == AuthController.LoginResult.ACCOUNT_DEACTIVATED
                        ? "Account deactivated."
                        : "Invalid username or password.");
                shake(card);
            }
        };

        loginBtn.addActionListener(e -> attemptLogin.run());
        // Convenience: pressing Enter in either field submits the form
        ActionListener enterHandler = e -> attemptLogin.run();
        usernameField.addActionListener(enterHandler);
        passwordField.addActionListener(enterHandler);
    }

    /** Diagonal gradient backdrop, painted directly on this panel (behind the centered card). */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, BG_TOP, getWidth(), getHeight(), BG_BOTTOM);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    /** Small non-destructive "shake" animation used only for invalid-login feedback. */
    private void shake(JComponent component) {
        Point original = component.getLocation();
        Timer timer = new Timer(16, null);
        int[] frame = {0};
        int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        timer.addActionListener(e -> {
            if (frame[0] >= offsets.length) {
                timer.stop();
                component.setLocation(original);
                return;
            }
            component.setLocation(original.x + offsets[frame[0]], original.y);
            frame[0]++;
        });
        timer.start();
    }

    /** White rounded card with a soft drop shadow, drawn with translucent layered rects (cheap, no images needed). */
    private static final class RoundedShadowPanel extends JPanel {
        private final int radius;

        RoundedShadowPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowLayers = 8;
            for (int i = shadowLayers; i > 0; i--) {
                float alpha = 0.02f * (shadowLayers - i + 1);
                g2.setColor(new Color(0.05f, 0.08f, 0.2f, Math.min(alpha, 0.16f)));
                g2.fillRoundRect(i, i + 4, getWidth() - i * 2, getHeight() - i * 2, radius, radius);
            }
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded, focus-aware single-line text field. */
    private static class RoundedTextField extends JTextField {
        private Color borderColor = FIELD_BORDER;

        RoundedTextField() {
            super(16);
            setOpaque(false);
            setFont(FONT_FIELD);
            setForeground(TEXT_PRIMARY);
            setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
            setPreferredSize(new Dimension(10, 44));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    borderColor = FIELD_FOCUS;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    borderColor = FIELD_BORDER;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xFAFBFD));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderColor == FIELD_FOCUS ? 1.8f : 1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
        }
    }

    /** Rounded, focus-aware password field (mirrors RoundedTextField). */
    private static class RoundedPasswordField extends JPasswordField {
        private Color borderColor = FIELD_BORDER;

        RoundedPasswordField() {
            super(16);
            setOpaque(false);
            setFont(FONT_FIELD);
            setForeground(TEXT_PRIMARY);
            setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
            setPreferredSize(new Dimension(10, 44));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    borderColor = FIELD_FOCUS;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    borderColor = FIELD_BORDER;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xFAFBFD));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderColor == FIELD_FOCUS ? 1.8f : 1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
        }
    }

    /**
     * Rounded button with a smooth (Timer-driven) hover/press color transition.
     * The timer always stops itself once the target alpha is reached, so it
     * never runs in the background or leaks after the button is discarded.
     */
    static class RoundedButton extends JButton {
        private final Color base;
        private final Color hoverColor;
        private float hoverAlpha = 0f;   // 0 = base color, 1 = fully hovered
        private boolean pressed = false;
        private Timer animator;

        RoundedButton(String text, Color base, Color hoverColor) {
            super(text);
            this.base = base;
            this.hoverColor = hoverColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animateTo(1f);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    animateTo(0f);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        private void animateTo(float target) {
            if (animator != null && animator.isRunning()) animator.stop();
            animator = new Timer(12, null);
            animator.addActionListener(e -> {
                float step = 0.18f;
                if (hoverAlpha < target) {
                    hoverAlpha = Math.min(target, hoverAlpha + step);
                } else if (hoverAlpha > target) {
                    hoverAlpha = Math.max(target, hoverAlpha - step);
                }
                repaint();
                if (hoverAlpha == target) animator.stop();
            });
            animator.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = blend(base, hoverColor, hoverAlpha);
            if (pressed) fill = fill.darker();

            int h = getHeight();
            int shrink = pressed ? 1 : 0;
            g2.setColor(fill);
            g2.fillRoundRect(0, shrink, getWidth(), h - shrink, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }

        private static Color blend(Color c1, Color c2, float ratio) {
            float r = Math.max(0f, Math.min(1f, ratio));
            int red = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * r);
            int green = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * r);
            int blue = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * r);
            return new Color(red, green, blue);
        }
    }
}