package mygame.GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class GameCredits extends JPanel {

    private Font gameFont;
    private List<DevCard> devCards = new ArrayList<>();
    private final List<Point> starPositions = new ArrayList<>();

    private Timer animationTimer;

    // Adjusted dimensions to fit 800px screen resolution
    // Layout Logic: 5 cards * 135 width + 4 gaps * 15 = 735px (Fits within 800px limit)
    private final int cardWidth = 135;
    private final int cardHeight = 200; // Reduced height for better fit
    private final int gap = 15;

    public GameCredits(ActionListener backAction) {
        setLayout(null);

        loadFont();
        generateStars();

        JLabel title = new JLabel("GALACTIC MISSION SQUAD", SwingConstants.CENTER);
        title.setForeground(new Color(0x00FFFF));
        title.setFont(new Font("Verdana", Font.BOLD, 32));
        title.setBounds(0, 30, 800, 50); // Set width to 800px

        JLabel shadowTitle = new JLabel("GALACTIC MISSION SQUAD", SwingConstants.CENTER);
        shadowTitle.setForeground(new Color(0, 0, 0, 100));
        shadowTitle.setFont(new Font("Verdana", Font.BOLD, 32));
        shadowTitle.setBounds(2, 32, 800, 50);

        add(title);
        add(shadowTitle);
        setComponentZOrder(shadowTitle, 1);
        setComponentZOrder(title, 0);

        List<Developer> developers = new ArrayList<>();
        developers.add(new Developer("Ahmed Mostafa", "Game Structure", "Assets/avatars/avatar1.png", "https://github.com/AhmedMostafa203", "https://www.linkedin.com/in/hammadahmed289"));
        developers.add(new Developer("Hammad Ahmed", "UI/UX Designer", "Assets/avatars/avatar2.png", "https://github.com/hammadahmedx15-stack", "https://www.linkedin.com/in/hammadahmed289"));
        developers.add(new Developer("Youssef Salah", "Logic & Sounds", "Assets/avatars/avatar3.png", "https://github.com/YousefSalah123", "https://www.linkedin.com/in/yousef-salah-nage-a3583636b"));
        developers.add(new Developer("Amr Mahmoud", "Game Structure", "Assets/avatars/avatar4.png", "https://github.com/Amr-Mahmoud293", "https://www.linkedin.com/in/amr-29-elbhar"));
        developers.add(new Developer("Mostafa Eid", "Media & Assets", "Assets/avatars/avatar5.png", "https://github.com/MustafaEid926", "http://www.linkedin.com/in/mustafaeid412"));

        for (Developer dev : developers) {
            DevCard card = new DevCard(dev);
            add(card);
            devCards.add(card);
        }

        JButton back = createGameButton("RETURN TO BASE");
        // Center button horizontally relative to 800px screen
        back.setBounds((800 - 220) / 2, 500, 220, 50);
        back.addActionListener(backAction);
        add(back);
    }

    public void resetAnimations() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        int total = devCards.size();
        int currentWidth = 800; // Fixed width reference for consistent layout calculations
        int currentHeight = 600;

        int totalWidth = (total * cardWidth) + ((total - 1) * gap);
        int startX = (currentWidth - totalWidth) / 2;

        // Adjusted Y-position (150) to center cards vertically
        int baseY = 150;

        for (int i = 0; i < total; i++) {
            DevCard card = devCards.get(i);
            card.setOpacity(0f);
            card.finalY = baseY;
            int xPos = startX + (i * (cardWidth + gap));
            card.setBounds(xPos, baseY + 300, cardWidth, cardHeight);
        }

        repaint();
        runHorizontalAnimation();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, new Color(10, 10, 30), 0, getHeight(), new Color(0, 0, 5));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(255, 255, 255, 150));
        for (Point p : starPositions) {
            g2.fillRect(p.x, p.y, 2, 2);
        }

        g2.setColor(new Color(0, 255, 255, 50));
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    private void loadFont() { gameFont = new Font("Verdana", Font.BOLD, 18); }

    private void generateStars() {
        for (int i = 0; i < 150; i++) {
            int x = (int) (Math.random() * 800);
            int y = (int) (Math.random() * 600);
            starPositions.add(new Point(x, y));
        }
    }

    private void runHorizontalAnimation() {
        final int[] index = {0};
        animationTimer = new Timer(200, e -> {
            if (index[0] < devCards.size()) {
                animateCard(devCards.get(index[0]));
                index[0]++;
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        animationTimer.start();
    }

    private void animateCard(DevCard c) {
        Timer t = new Timer(15, null);
        t.addActionListener(e -> {
            c.setOpacity(Math.min(c.getOpacity() + 0.04f, 1f));
            Point p = c.getLocation();
            int speed = Math.max(1, (p.y - c.finalY) / 10);
            if (p.y > c.finalY) {
                c.setLocation(p.x, p.y - speed);
            } else {
                c.setLocation(p.x, c.finalY);
                ((Timer) e.getSource()).stop();
            }
            c.repaint();
        });
        t.start();
    }

    class DevCard extends JPanel {
        float opacity = 0f;
        int finalY;

        DevCard(Developer d) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(255, 255, 255, 10));

            // Resized avatar to 70px (from 90px) to fit the smaller card layout
            JLabel img = new JLabel(new ImageIcon(makeCircularImage(loadImage(d.imagePath), 70)));
            img.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel name = new JLabel(d.name, SwingConstants.CENTER);
            name.setForeground(Color.WHITE);
            // Reduced font size for better legibility
            name.setFont(gameFont.deriveFont(Font.BOLD, 13f));
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel role = new JLabel(d.role, SwingConstants.CENTER);
            role.setForeground(new Color(0x00FFFF));
            role.setFont(gameFont.deriveFont(Font.PLAIN, 10f));
            role.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel icons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            icons.setOpaque(false);
            icons.add(new MagicIcon("Assets/github.png", d.github));
            icons.add(new MagicIcon("Assets/linkedin.png", d.linkedin));

            add(Box.createVerticalStrut(15));
            add(img);
            add(Box.createVerticalStrut(10));
            add(name);
            add(Box.createVerticalStrut(5));
            add(role);
            add(Box.createVerticalGlue());
            add(icons);
            add(Box.createVerticalStrut(15));
        }

        void setOpacity(float f) { opacity = f; }
        float getOpacity() { return opacity; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(0x00FFFF));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    class MagicIcon extends JLabel {
        boolean scaling = false;
        double scale = 1.0;
        final String link;
        Image baseImage;

        MagicIcon(String iconPath, String link) {
            this.link = link;
            BufferedImage rawImg = loadImage(iconPath);
            if (rawImg != null) {
                // Resized social icons to 20x20px
                baseImage = rawImg.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(baseImage));
            } else {
                setText("LINK");
                setForeground(Color.CYAN);
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { startScaleAnimation(true); }
                public void mouseExited(MouseEvent e) { startScaleAnimation(false); }
                public void mouseClicked(MouseEvent e) { if (link != null && !link.isEmpty()) openLink(link); }
            });
        }

        private void startScaleAnimation(boolean grow) {
            new Timer(10, ev -> {
                if (grow) { if (scale < 1.3) scale += 0.05; else ((Timer) ev.getSource()).stop(); }
                else { if (scale > 1.0) scale -= 0.05; else ((Timer) ev.getSource()).stop(); }
                repaint();
            }).start();
        }

        private void openLink(String url) {
            try { Desktop.getDesktop().browse(new URI(url)); } catch (Exception ignored) {}
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (baseImage == null) { super.paintComponent(g); return; }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int imgW = (int) (20 * scale);
            int imgH = (int) (20 * scale);
            int x = (getWidth() - imgW) / 2;
            int y = (getHeight() - imgH) / 2;
            g2.drawImage(baseImage, x, y, imgW, imgH, null);
            g2.dispose();
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) url = getClass().getResource("/StartGame/" + path);
            if (url == null) url = getClass().getResource("/" + path);
            if (url != null) return ImageIO.read(url);
        } catch (Exception e) {}
        BufferedImage fallback = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics g = fallback.createGraphics();
        g.setColor(new Color(50, 50, 50)); g.fillOval(0, 0, 50, 50); g.dispose();
        return fallback;
    }

    private Image makeCircularImage(BufferedImage src, int size) {
        BufferedImage circ = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circ.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Double(0, 0, size, size));
        g2.drawImage(src, 0, 0, size, size, null);
        g2.dispose();
        return circ;
    }

    private JButton createGameButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Polygon p = new Polygon();
                p.addPoint(20, 0); p.addPoint(w - 20, 0); p.addPoint(w, h / 2);
                p.addPoint(w - 20, h); p.addPoint(20, h); p.addPoint(0, h / 2);
                if (getModel().isRollover()) g2.setColor(new Color(0, 50, 100, 200));
                else g2.setColor(new Color(0, 20, 40, 180));
                g2.fillPolygon(p);
                g2.setColor(new Color(0x00FFFF));
                g2.setStroke(new BasicStroke(2));
                g2.drawPolygon(p);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFocusPainted(false); btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setForeground(new Color(200, 240, 255));
        btn.setFont(gameFont.deriveFont(Font.BOLD, 18f));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setForeground(new Color(200, 240, 255)); }
        });
        return btn;
    }

    static class Developer {
        String name, role, imagePath, github, linkedin;
        Developer(String n, String r, String img, String g, String l) {
            this.name = n; this.role = r; this.imagePath = img; this.github = g; this.linkedin = l;
        }
    }
}