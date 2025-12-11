package mygame.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PauseMenuFrame extends JFrame {

    // --- الألوان ---
    private final Color SPACE_DARK = new Color(5, 5, 15);
    private final Color NEBULA_PURPLE = new Color(75, 0, 130, 50);
    private final Color NEBULA_BLUE = new Color(0, 50, 150, 40);
    private final Color HUD_ORANGE = new Color(255, 165, 0);
    private final Color HUD_CYAN = new Color(0, 255, 255);
    private final Color HUD_GREEN = new Color(50, 255, 50);
    private final Color HUD_RED = new Color(255, 50, 50);
    private final Color HUD_YELLOW = new Color(255, 215, 0);

    private final List<Star> stars = new ArrayList<>();
    private ShootingStar shootingStar;
    private final Timer animationTimer;

    public static boolean isMuted = false;

    public PauseMenuFrame(ActionListener onResume, ActionListener onRestart, ActionListener onMenu, ActionListener onToggleSound) {
        setTitle("SYSTEM PAUSED");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        // 1. النجوم
        for (int i = 0; i < 150; i++) stars.add(new Star(getWidth(), getHeight()));
        shootingStar = new ShootingStar(getWidth(), getHeight());

        // 2. اللوحة الرئيسية
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // شكل النافذة
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

                g2.setClip(path);

                // الخلفية الفضائية
                GradientPaint gp = new GradientPaint(w / 2, 0, SPACE_DARK, w / 2, h, Color.BLACK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                drawStarsAndNebula(g2);
                drawShootingStar(g2);
                drawTechGrid(g2, w, h);

                // الإطار الخارجي المتوهج
                g2.setStroke(new BasicStroke(3));
                g2.setColor(new Color(HUD_ORANGE.getRed(), HUD_ORANGE.getGreen(), HUD_ORANGE.getBlue(), 200));
                g2.draw(path);

                g2.dispose();
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        // 3. المحتوى (النصوص والأزرار)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("PAUSED");
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 36));
        titleLabel.setForeground(HUD_ORANGE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JButton resumeBtn = createGameButton("RESUME MISSION", HUD_CYAN);
        JButton restartBtn = createGameButton("RESTART LEVEL", HUD_GREEN);
        JButton menuBtn = createGameButton("BACK TO MENU", HUD_RED);
        JButton muteBtn = createGameButton(isMuted ? "SOUND: OFF" : "SOUND: ON", HUD_YELLOW);

        resumeBtn.addActionListener(onResume);
        restartBtn.addActionListener(onRestart);
        menuBtn.addActionListener(onMenu);
        muteBtn.addActionListener(e -> {
            isMuted = !isMuted;
            muteBtn.setText(isMuted ? "SOUND: OFF" : "SOUND: ON");
            onToggleSound.actionPerformed(e);
        });

        contentPanel.add(titleLabel);
        contentPanel.add(resumeBtn);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(restartBtn);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(muteBtn);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(menuBtn);

        mainPanel.add(contentPanel);
        add(mainPanel);

        // 4. الأنيميشن
        animationTimer = new Timer(16, e -> {
            updateSpace();
            mainPanel.repaint();
        });
        animationTimer.start();

        setupEscKey(onResume);
    }

    private void setupEscKey(ActionListener onResume) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Resume");
        getRootPane().getActionMap().put("Resume", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                onResume.actionPerformed(e);
            }
        });
    }

    private JButton createGameButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                Polygon p = new Polygon();
                p.addPoint(20, 0);
                p.addPoint(w - 20, 0);
                p.addPoint(w, h / 2);
                p.addPoint(w - 20, h);
                p.addPoint(20, h);
                p.addPoint(0, h / 2);

                g2.setColor(new Color(0, 20, 40, 180));
                g2.fillPolygon(p);

                g2.setColor(baseColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawPolygon(p);
                super.paintComponent(g);
                g2.dispose();
            }
        };

        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(baseColor);
        btn.setFont(new Font("Verdana", Font.BOLD, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(280, 50));
        btn.setPreferredSize(new Dimension(280, 50));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Color.WHITE);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(baseColor);
                btn.repaint();
            }
        });
        return btn;
    }

    // ==========================================
    // Space Logic
    // ==========================================
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

    private void drawStarsAndNebula(Graphics2D g2) {
        g2.setColor(NEBULA_PURPLE);
        g2.fillOval(-100, -100, getWidth() / 2 + 200, getHeight() / 2 + 200);
        g2.setColor(NEBULA_BLUE);
        g2.fillOval(getWidth() / 2, getHeight() / 2, getWidth() / 2 + 100, getHeight() / 2 + 100);
        for (Star s : stars) {
            int alpha = (int) (s.z * 255);
            if (alpha > 255) alpha = 255;
            g2.setColor(new Color(255, 255, 255, alpha));
            double size = s.z * 2.5;
            g2.fillOval((int) s.x, (int) s.y, (int) size, (int) size);
        }
    }

    private void drawShootingStar(Graphics2D g2) {
        if (!shootingStar.active) return;
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine((int) shootingStar.x, (int) shootingStar.y,
                (int) (shootingStar.x - shootingStar.speedX * 5),
                (int) (shootingStar.y - shootingStar.speedY * 5));
    }

    private void drawTechGrid(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(255, 165, 0, 10));
        g2.setStroke(new BasicStroke(1));
        for (int x = 0; x < w; x += 50) g2.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 50) g2.drawLine(0, y, w, y);
    }

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