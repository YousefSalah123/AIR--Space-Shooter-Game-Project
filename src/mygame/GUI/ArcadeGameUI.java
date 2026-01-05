package mygame.GUI;

import mygame.Game;
import mygame.engine.HighScoreManagment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArcadeGameUI extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Font gameFont;
    private JPanel highScoresPanel;
    private GameCredits creditsPanel;
    private Game game;
    private boolean isMultiplayerMode = false;

    // --- Animated Background Variables ---
    private final Color NEBULA_PURPLE = new Color(75, 0, 130, 50);
    private final Color NEBULA_BLUE = new Color(0, 50, 150, 40);
    private final List<Star> stars = new ArrayList<>();
    private ShootingStar shootingStar;
    private Timer animationTimer;

    // Name Entry Components
    private JLabel nameLabel1;
    private FancyTextField nameField1;
    private JLabel nameLabel2;
    private FancyTextField nameField2;

    public ArcadeGameUI(Game game) {
        this.game = game;

        // Panel Settings
        setLayout(new BorderLayout());
        setBounds(0, 0, 800, 600);
        setOpaque(true);

        // Initialize Stars
        for (int i = 0; i < 150; i++) stars.add(new Star(800, 600));
        shootingStar = new ShootingStar(800, 600);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);

        loadGameFont();

        // Initialize Menus
        JPanel mainMenu = createMainMenuPanel();
        JPanel nameEntry = createNameEntryPanel();
        JPanel instructions = createInstructionsPanel();
        highScoresPanel = createHighScoresPanel();
        creditsPanel = new GameCredits(e -> cardLayout.show(mainPanel, "MainMenu"));

        mainPanel.add(mainMenu, "MainMenu");
        mainPanel.add(nameEntry, "NameEntry");
        mainPanel.add(instructions, "Instructions");
        mainPanel.add(highScoresPanel, "HighScores");
        mainPanel.add(creditsPanel, "Credits");

        add(mainPanel, BorderLayout.CENTER);

        // Start Animation Loop
        animationTimer = new Timer(16, e -> {
            updateSpace();
            repaint();
        });
        animationTimer.start();
    }

    // --- Background Rendering ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Space Gradient
        GradientPaint gp = new GradientPaint(w / 2, 0, new Color(10, 10, 30), w / 2, h, Color.BLACK);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // Draw Stars and Nebulas
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

        // Draw Shooting Stars
        if (shootingStar.active) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine((int) shootingStar.x, (int) shootingStar.y,
                    (int) (shootingStar.x - shootingStar.speedX * 5),
                    (int) (shootingStar.y - shootingStar.speedY * 5));
        }

        // Draw Subtle Tech Grid
        g2.setColor(new Color(255, 255, 255, 10));
        g2.setStroke(new BasicStroke(1));
        for (int x = 0; x < w; x += 50) g2.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 50) g2.drawLine(0, y, w, y);
    }

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

    private void loadGameFont() {
        try {
            InputStream fontStream = getClass().getResourceAsStream("Assets//background3.png");
            gameFont = new Font("Verdana", Font.BOLD, 22);
        } catch (Exception e) {
            gameFont = new Font("Verdana", Font.BOLD, 22);
        }
    }

    // --- Helper Method: Draw Attractive Neon Title ---
    private void drawNeonTitle(Graphics2D g2, String text, int yPosition) {
        // 1. Prepare Large Font
        Font titleFont = gameFont.deriveFont(Font.BOLD, 55f);
        g2.setFont(titleFont);

        // 2. Convert Text to Shape for Sharp Borders
        java.awt.font.FontRenderContext frc = g2.getFontRenderContext();
        java.awt.font.TextLayout textLayout = new java.awt.font.TextLayout(text, titleFont, frc);

        // Center Text
        double textWidth = textLayout.getBounds().getWidth();
        double x = (getWidth() - textWidth) / 2;

        // Translate Graphics Context
        g2.translate(x, yPosition);

        // Get Text Outline
        Shape outline = textLayout.getOutline(null);

        // --- Layer 1: Deep Drop Shadow ---
        // Adds 3D depth
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(0, 0, 0, 180));
        g2.translate(4, 4); // Shadow Offset
        g2.draw(outline);
        g2.fill(outline);
        g2.translate(-4, -4); // Reset Offset

        // --- Layer 2: Outer Neon Glow ---
        // Wide stroke for glow effect
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(0, 255, 255, 100)); // Cyan Glow
        g2.draw(outline);

        // --- Layer 3: Gradient Fill ---
        // Metallic Gradient (Snow White to Deep Blue)
        GradientPaint fillPaint = new GradientPaint(
                0, (float) -textLayout.getBounds().getHeight(), new Color(220, 255, 255), // Snow White
                0, 10, new Color(0, 100, 200) // Deep Blue
        );
        g2.setPaint(fillPaint);
        g2.fill(outline);

        // --- Layer 4: Crisp Border ---
        // Thin white border for definition
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(Color.WHITE);
        g2.draw(outline);

        // Reset Translation
        g2.translate(-x, -yPosition);
    }

    // --- Internal Panels ---

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw title in the top third
                drawNeonTitle((Graphics2D) g, "GALACTIC STRIKE", 110);
            }
        };
        panel.setLayout(new GridBagLayout()); // Use GridBag for optimal centering
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 0, 10, 0); // Vertical spacing

        // --- 1. Title Spacer ---
        // (Since title is drawn via paintComponent, not a Swing component)
        gbc.gridy = 0;
        panel.add(Box.createVerticalStrut(140), gbc);

        // --- 2. Main Game Buttons (Large & Distinct) ---
        // Single Player Button (Cyan)
        JButton btnSingle = createGameButton("SINGLE PLAYER", 320, 55, new Color(0, 255, 255));
        // Multiplayer Button (Orange/Gold for distinction)
        JButton btnMulti = createGameButton("MULTIPLAYER", 320, 55, new Color(255, 165, 0));

        btnSingle.addActionListener(e -> { isMultiplayerMode = false; updateNameEntryUI(); cardLayout.show(mainPanel, "NameEntry"); });
        btnMulti.addActionListener(e -> { isMultiplayerMode = true; updateNameEntryUI(); cardLayout.show(mainPanel, "NameEntry"); });

        gbc.gridy = 1; panel.add(btnSingle, gbc);
        gbc.gridy = 2; panel.add(btnMulti, gbc);

        // --- 3. Sub-button Grid (Small) ---
        // High Scores, Instructions, Sound, Credits
        JPanel subGrid = new JPanel(new GridLayout(2, 2, 15, 15)); // 2 rows, 2 cols, 15px gap
        subGrid.setOpaque(false);

        // Unified Utility Color (Neon Green)
        Color utilityColor = new Color(100, 255, 150);
        int subW = 160;
        int subH = 35; // Small height

        JButton btnScores = createGameButton("SCORES", subW, subH, utilityColor);
        JButton btnInstr = createGameButton("HELP", subW, subH, utilityColor);
        JButton btnSound = createGameButton("SOUND", subW, subH, utilityColor);
        JButton btnCredits = createGameButton("CREDITS", subW, subH, utilityColor);

        btnScores.addActionListener(e -> {
            mainPanel.remove(highScoresPanel); highScoresPanel = createHighScoresPanel();
            mainPanel.add(highScoresPanel, "HighScores"); cardLayout.show(mainPanel, "HighScores");
        });
        btnInstr.addActionListener(e -> cardLayout.show(mainPanel, "Instructions"));
        btnSound.addActionListener(e -> { if (game != null) game.toggleSound(); });
        btnCredits.addActionListener(e -> { creditsPanel.resetAnimations(); cardLayout.show(mainPanel, "Credits"); });

        subGrid.add(btnScores);
        subGrid.add(btnInstr);
        subGrid.add(btnSound);
        subGrid.add(btnCredits);

        gbc.gridy = 3;
        gbc.insets = new Insets(25, 0, 15, 0); // Large gap separating game buttons from utilities
        panel.add(subGrid, gbc);

        // --- 4. Exit Button (Small Red at Bottom) ---
        JButton btnExit = createGameButton("EXIT GAME", 140, 35, new Color(255, 50, 80));
        btnExit.addActionListener(e -> System.exit(0));

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 20, 0);
        panel.add(btnExit, gbc);

        return panel;
    }

    private void updateNameEntryUI() {
        nameField1.setText("");
        nameField2.setText("");
        if (isMultiplayerMode) {
            nameLabel1.setText("PLAYER 1 NAME"); nameLabel2.setVisible(true); nameField2.setVisible(true);
        } else {
            nameLabel1.setText("ENTER YOUR NAME"); nameLabel2.setVisible(false); nameField2.setVisible(false);
        }
    }

    private JPanel createNameEntryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(Box.createVerticalStrut(100)); // Top spacer

        nameLabel1 = new JLabel("PLAYER 1 NAME");
        nameLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel1.setForeground(new Color(0x00FFFF));
        nameLabel1.setFont(gameFont.deriveFont(24f));

        nameField1 = new FancyTextField(15);
        nameField1.setMaximumSize(new Dimension(220, 35));

        nameLabel2 = new JLabel("PLAYER 2 NAME");
        nameLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel2.setForeground(new Color(255, 50, 50));
        nameLabel2.setFont(gameFont.deriveFont(24f));

        nameField2 = new FancyTextField(15);
        nameField2.setMaximumSize(new Dimension(220, 35));

        JButton start = createGameButton("START MISSION");
        JButton back = createGameButton("BACK");

        start.addActionListener(e -> {
            String p1Name = nameField1.getText().trim(); String p2Name = nameField2.getText().trim();
            if (p1Name.isEmpty()) { showCustomWarning("ENTER PLAYER 1 NAME"); return; }
            if (isMultiplayerMode && p2Name.isEmpty()) { showCustomWarning("ENTER PLAYER 2 NAME"); return; }

            Game.setPlayerName(p1Name);
            if (isMultiplayerMode) Game.setPlayer2Name(p2Name); else Game.setPlayer2Name("AI-P2");
            if (game != null) game.startActualGame(isMultiplayerMode);
        });
        back.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        panel.add(nameLabel1); panel.add(Box.createVerticalStrut(10)); panel.add(nameField1);
        panel.add(Box.createVerticalStrut(20));
        panel.add(nameLabel2); panel.add(Box.createVerticalStrut(10)); panel.add(nameField2);
        panel.add(Box.createVerticalStrut(40));
        panel.add(start); panel.add(Box.createVerticalStrut(15)); panel.add(back);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 40, 200));
                g.fillRect(0, 0, getWidth(), getHeight());

                ImageIcon instIcon = new ImageIcon(getClass().getResource("Assets//instruction.png"));
                Image instImage = instIcon.getImage();

                if (instImage != null) {
                    // Optimized scaling logic to fill screen
                    int panelW = getWidth();
                    int panelH = getHeight();

                    // Leave space for bottom button (~80px)
                    int maxW = panelW ; // Simple margins
                    int maxH = panelH - 120; // Bottom button space

                    int imgW = instImage.getWidth(this);
                    int imgH = instImage.getHeight(this);

                    // Calculate ratio to maintain Aspect Ratio
                    double widthRatio = (double) maxW / imgW;
                    double heightRatio = (double) maxH / imgH;
                    double ratio = Math.min(widthRatio, heightRatio); // Use min to ensure full visibility

                    int newW = (int) (imgW * ratio);
                    int newH = (int) (imgH * ratio);

                    // Center Image
                    int x = (panelW - newW) / 2;
                    int y = (maxH - newH) / 2 + 20; // Position in center of available space

                    g.drawImage(instImage, x, y, newW, newH, this);
                }
            }
        };
        panel.setOpaque(false);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);

        JButton back = createGameButton("BACK");
        back.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        back.setPreferredSize(new Dimension(200, 45));

        bottomPanel.add(back);
        // Add large bottom margin to lift button (avoid clipping)
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 60, 0));

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHighScoresPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 40, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
                // Use new Title Method
                drawNeonTitle((Graphics2D) g, "HALL OF FAME", 60);
            }
        };
        panel.setOpaque(false);

        // Main List Container
        JPanel listsContainer = new JPanel(new GridLayout(1, 2, 40, 0)); // 40px gap
        listsContainer.setOpaque(false);
        // Margins: Top (under title), Left, Bottom, Right
        listsContainer.setBorder(BorderFactory.createEmptyBorder(80, 50, 20, 50));

        JPanel singlePanel = createScoreListPanel("SINGLE PLAYER", false);
        JPanel multiPanel = createScoreListPanel("MULTIPLAYER", true);

        listsContainer.add(singlePanel);
        listsContainer.add(multiPanel);

        // Bottom Button Area
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);

        JButton back = createGameButton("BACK");
        back.setPreferredSize(new Dimension(200, 45));
        back.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        bottomPanel.add(back);
        // Safety margin (60px) to lift button
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 60, 0));

        panel.add(listsContainer, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createScoreListPanel(String titleText, boolean isMulti) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        JLabel header = new JLabel(titleText, SwingConstants.CENTER);
        header.setForeground(isMulti ? new Color(255, 100, 100) : new Color(100, 255, 100));
        header.setFont(gameFont.deriveFont(Font.BOLD, 22f));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        container.add(header);

        List<HighScoreManagment.ScoreEntry> scores = HighScoreManagment.loadScores(isMulti);

        if (scores.isEmpty()) {
            JLabel empty = new JLabel("- NO DATA -", SwingConstants.CENTER);
            empty.setForeground(Color.GRAY);
            empty.setFont(gameFont.deriveFont(Font.PLAIN, 18f));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(empty);
        } else {
            int rank = 1;
            for (HighScoreManagment.ScoreEntry entry : scores) {
                String text = String.format("%d. %s : %d", rank, entry.name, entry.score);
                JLabel scoreLabel = new JLabel(text, SwingConstants.CENTER);
                scoreLabel.setForeground(Color.WHITE);
                scoreLabel.setFont(gameFont.deriveFont(Font.PLAIN, 16f));
                scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                scoreLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                container.add(scoreLabel);
                rank++;
            }
        }

        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        return container;
    }

    private void showCustomWarning(String message) {
        JDialog warningDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "WARNING", true);
        warningDialog.setUndecorated(true);
        warningDialog.setBackground(new Color(0, 0, 0, 0));
        warningDialog.setSize(400, 250);
        warningDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Path2D path = new Path2D.Double();
                int c = 20;
                path.moveTo(c, 0); path.lineTo(w - c, 0); path.lineTo(w, c); path.lineTo(w, h - c);
                path.lineTo(w - c, h); path.lineTo(c, h); path.lineTo(0, h - c); path.lineTo(0, c);
                path.closePath();
                g2.setColor(new Color(40, 0, 0, 230)); g2.fill(path);
                g2.setColor(new Color(255, 50, 50)); g2.setStroke(new BasicStroke(3f)); g2.draw(path);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel("ACCESS DENIED");
        iconLabel.setFont(gameFont.deriveFont(Font.BOLD, 22f));
        iconLabel.setForeground(new Color(255, 50, 50));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        msgLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JButton btnOk = createGameButton("OK");
        btnOk.setPreferredSize(new Dimension(200, 40));
        btnOk.setForeground(new Color(255, 100, 100));
        btnOk.addActionListener(e -> warningDialog.dispose());

        panel.add(Box.createVerticalGlue()); panel.add(iconLabel); panel.add(msgLabel);
        panel.add(Box.createVerticalStrut(10)); panel.add(btnOk); panel.add(Box.createVerticalGlue());

        warningDialog.add(panel); warningDialog.setVisible(true);
    }

    // Updated method to create buttons with variable sizes and colors
    private JButton createGameButton(String text, int width, int height, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Chamfered Sci-Fi Shape
                Polygon p = new Polygon();
                int cut = h / 3; // Bevel angle depends on height
                p.addPoint(cut, 0);
                p.addPoint(w - cut, 0);
                p.addPoint(w, h / 2);
                p.addPoint(w - cut, h);
                p.addPoint(cut, h);
                p.addPoint(0, h / 2);

                // Hover Effect
                if (getModel().isRollover()) {
                    g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 100)); // High transparency on hover
                    g2.fillPolygon(p);
                    g2.setColor(Color.WHITE); // Borders turn white
                } else {
                    g2.setColor(new Color(0, 10, 30, 200)); // Fixed dark background
                    g2.fillPolygon(p);
                    g2.setColor(baseColor); // Base border color
                }

                g2.setStroke(new BasicStroke(2f));
                g2.drawPolygon(p);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);

        // Text Color
        btn.setForeground(new Color(220, 240, 255));

        // Font size depends on button height
        float fontSize = height > 40 ? 20f : 14f;
        btn.setFont(gameFont.deriveFont(Font.BOLD, fontSize));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Dimensions
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));

        // Mouse Listeners for text color change
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setForeground(new Color(220, 240, 255)); }
        });

        return btn;
    }

    // Helper method to maintain legacy compatibility if default button is needed
    private JButton createGameButton(String text) {
        return createGameButton(text, 250, 45, new Color(0x00FFFF));
    }

    class FancyTextField extends JTextField {
        private boolean focused = false;

        public FancyTextField(int columns) {
            super(columns);
            setOpaque(false);
            setForeground(new Color(0x00FFFF));
            setCaretColor(new Color(0x00FFFF));
            setFont(gameFont.deriveFont(20f));
            setHorizontalAlignment(JTextField.CENTER);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { focused = true; repaint(); }
                public void focusLost(FocusEvent e) { focused = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int arc = 25;

            Color baseColor = new Color(0, 30, 60, 120);
            g2.setColor(baseColor);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setColor(focused ? new Color(0x00FFFF) : new Color(0x007777));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class Star {
        double x, y, speed, z;
        Star(int w, int h) {
            x = Math.random() * w; y = Math.random() * h;
            z = Math.random(); speed = 0.5 + (z * 3.0);
        }
    }

    private static class ShootingStar {
        double x, y, speedX, speedY; boolean active = false; Random rand = new Random();
        ShootingStar(int w, int h) { reset(w, h); }
        void update(int w, int h) {
            if (active) {
                x += speedX; y += speedY;
                if (x < 0 || y > h || x > w) active = false;
            } else if (rand.nextInt(100) < 2) { reset(w, h); active = true; }
        }
        void reset(int w, int h) {
            x = rand.nextInt(w); y = 0;
            speedX = -5 - rand.nextDouble() * 5; speedY = 5 + rand.nextDouble() * 5;
        }
    }
}