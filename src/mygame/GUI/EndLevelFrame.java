package mygame.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// A custom JFrame dialog displayed at the end of a level (Victory or Game Over),
// featuring a space-themed animated background and custom buttons.
public class EndLevelFrame extends JFrame {

    // --- Colors and Constants ---
    private final Color SPACE_DARK = new Color(5, 5, 15);
    private final Color NEBULA_PURPLE = new Color(75, 0, 130, 50);
    private final Color NEBULA_BLUE = new Color(0, 50, 150, 40);
    private final Color HUD_CYAN = new Color(0, 255, 255);
    private final Color HUD_RED = new Color(255, 50, 50);
    private final Font BUTTON_FONT = new Font("Verdana", Font.BOLD, 18); // Same default button font

    // --- Animation Variables ---
    private final List<Star> stars = new ArrayList<>();
    private ShootingStar shootingStar;
    private final Timer animationTimer;
    private final boolean isVictory;

    // Constructor: Sets up the frame appearance, initializes the space background,
    // and creates the score/button layout based on the 'victory' status.
    public EndLevelFrame(boolean victory, int score, ActionListener onRetry, ActionListener onNext) {
        this.isVictory = victory;
        Color themeColor = victory ? HUD_CYAN : HUD_RED;

        setTitle("Mission Status");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setUndecorated(true); // Remove traditional title bar
        setBackground(new Color(0, 0, 0, 0)); // Transparent background for custom shape
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 1. Initialize Stars (Space Background)
        for (int i = 0; i < 150; i++) {
            stars.add(new Star(getWidth(), getHeight()));
        }
        shootingStar = new ShootingStar(getWidth(), getHeight());

        // 2. Main Panel (Drawing)
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Draw the beveled window shape (Sci-Fi Shape)
                Path2D path = new Path2D.Double();
                int corner = 30;
                path.moveTo(corner, 0);
                path.lineTo(w - corner, 0);
                path.lineTo(w, corner);
                path.lineTo(w, h - corner);
                path.lineTo(w - corner, h);
                path.lineTo(corner, h);
                path.lineTo(0, h - corner);
                path.lineTo(0, corner);
                path.closePath();

                g2.setClip(path); // Clip drawing within the shape

                // Space Background
                drawSpaceBackground(g2, w, h);
                drawStarsAndNebula(g2);
                drawShootingStar(g2);
                drawTechGrid(g2, w, h);

                // Outer Glowing Border
                g2.setStroke(new BasicStroke(3));
                g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 200));
                g2.draw(path);
            }
        };
        mainPanel.setLayout(new GridBagLayout()); // For content centering

        // 3. Content (Text and Buttons)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel(victory ? "MISSION ACCOMPLISHED" : "MISSION FAILED");
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 36));
        titleLabel.setForeground(themeColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Score
        JLabel scoreLabel = new JLabel("SCORE: " + score);
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

        // --- Use Required Buttons ---
        JButton retryBtn = createGameButton("RETRY MISSION");
        JButton nextBtn = createGameButton(victory ? "BACK TO MENU" : "MAIN MENU");

        retryBtn.addActionListener(onRetry);
        nextBtn.addActionListener(onNext);

        // Add components
        contentPanel.add(titleLabel);
        contentPanel.add(scoreLabel);
        contentPanel.add(retryBtn);
        contentPanel.add(Box.createVerticalStrut(20)); // Space between buttons
        contentPanel.add(nextBtn);

        mainPanel.add(contentPanel);
        add(mainPanel);

        // 4. Start Animation
        animationTimer = new Timer(16, e -> {
            updateSpace();
            mainPanel.repaint();
        });
        animationTimer.start();
    }

    /**
     * 🔷 Same button function as in ArcadeGameUI code
     * Creates a custom-styled JButton with a hexagonal/angled border and hover effects.
     */
    private JButton createGameButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Draw the hexagonal shape (Polygon)
                Polygon p = new Polygon();
                p.addPoint(20, 0);
                p.addPoint(w - 20, 0);
                p.addPoint(w, h / 2);
                p.addPoint(w - 20, h);
                p.addPoint(20, h);
                p.addPoint(0, h / 2);

                // Fill (Dark transparent blue color)
                g2.setColor(new Color(0, 20, 40, 180));
                g2.fillPolygon(p);

                // Border (Neon cyan color)
                g2.setColor(new Color(0x00FFFF));
                g2.setStroke(new BasicStroke(2));
                g2.drawPolygon(p);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        // Set button properties
        btn.setFocusPainted(false); // 👈 This line removes the rectangle around the text
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);

        btn.setForeground(new Color(200, 240, 255));
        btn.setFont(new Font("Verdana", Font.BOLD, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(280, 50));
        btn.setPreferredSize(new Dimension(280, 50));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Color.WHITE);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(new Color(200, 240, 255));
                btn.repaint();
            }
        });

        return btn;
    }

    // ==========================================
    //       Space Drawing Logic (Space Engine)
    // ==========================================

    // Updates the position of stars and the shooting star for animation.
    private void updateSpace() {
        for (Star s : stars) {
            s.y += s.speed;
            if (s.y > getHeight()) {
                s.y = -5;
                s.x = Math.random() * getWidth();
            }
        }
        shootingStar.update(getWidth(), getHeight());
    }

    // Draws the dark, gradient background simulating outer space.
    private void drawSpaceBackground(Graphics2D g2, int w, int h) {
        GradientPaint gp = new GradientPaint(w / 2, 0, new Color(10, 10, 30), w / 2, h, Color.BLACK);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
    }

    // Draws the star field and abstract nebula shapes.
    private void drawStarsAndNebula(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        g2.setColor(NEBULA_PURPLE);
        g2.fillOval(-100, -100, w / 2 + 200, h / 2 + 200);
        g2.setColor(NEBULA_BLUE);
        g2.fillOval(w / 2, h / 2, w / 2 + 100, h / 2 + 100);

        for (Star s : stars) {
            int alpha = (int) (s.z * 255);
            if (alpha > 255) alpha = 255;
            g2.setColor(new Color(255, 255, 255, alpha));
            double size = s.z * 2.5;
            g2.fillOval((int) s.x, (int) s.y, (int) size, (int) size);
        }
    }

    // Draws the shooting star if it is currently active.
    private void drawShootingStar(Graphics2D g2) {
        if (!shootingStar.active) return;
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine((int) shootingStar.x, (int) shootingStar.y,
                (int) (shootingStar.x - shootingStar.speedX * 5),
                (int) (shootingStar.y - shootingStar.speedY * 5));
    }

    // Draws a faint, grid overlay for a "tech display" aesthetic.
    private void drawTechGrid(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(255, 255, 255, 10));
        g2.setStroke(new BasicStroke(1));
        for (int x = 0; x < w; x += 50) g2.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 50) g2.drawLine(0, y, w, y);
    }

    // Helper classes
    private static class Star {
        double x, y, speed, z;

        Star(int w, int h) {
            x = Math.random() * w;
            y = Math.random() * h;
            z = Math.random();
            speed = 0.5 + (z * 3.0);
        }
    }

    private static class ShootingStar {
        double x, y, speedX, speedY;
        boolean active = false;
        Random rand = new Random();

        ShootingStar(int w, int h) {
            reset(w, h);
        }

        void update(int w, int h) {
            if (active) {
                x += speedX;
                y += speedY;
                if (x < 0 || y > h || x > w) active = false;
            } else if (rand.nextInt(100) < 2) {
                reset(w, h);
                active = true;
            }
        }

        void reset(int w, int h) {
            x = rand.nextInt(w);
            y = 0;
            speedX = -5 - rand.nextDouble() * 5;
            speedY = 5 + rand.nextDouble() * 5;
        }
    }

}