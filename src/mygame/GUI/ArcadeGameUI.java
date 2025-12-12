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
import java.util.List;

public class ArcadeGameUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Font gameFont;
    private JPanel highScoresPanel;
    private GameCredits creditsPanel;
    private Game game;

    // Constructor: Initializes the main frame, sets up the CardLayout, loads the font,
    // and creates and adds all main menu panels.
    public ArcadeGameUI(Game game) {
        setTitle("Arcade Air Mission");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loadGameFont();

        // Initialize Panels
        JPanel mainMenu = createMainMenuPanel();
        JPanel singlePlayer = createSinglePlayerPanel();
        JPanel instructions = createInstructionsPanel();
        highScoresPanel = createHighScoresPanel();

        creditsPanel = new GameCredits(e -> cardLayout.show(mainPanel, "MainMenu"));

        // Add to CardLayout
        mainPanel.add(mainMenu, "MainMenu");
        mainPanel.add(singlePlayer, "SinglePlayer");
        mainPanel.add(instructions, "Instructions");
        mainPanel.add(highScoresPanel, "HighScores");
        mainPanel.add(creditsPanel, "Credits");

        add(mainPanel);

        this.game = game;
    }

    // Loads the custom game font from resources or defaults to Verdana if not found.
    private void loadGameFont() {
        try {
            InputStream fontStream = getClass().getResourceAsStream("Assets//background3.png");
            if (fontStream != null) {
                gameFont = new Font("Verdana", Font.BOLD, 22);
            } else {
                gameFont = new Font("Verdana", Font.BOLD, 22);
            }
        } catch (Exception e) {
            gameFont = new Font("Verdana", Font.BOLD, 22);
        }
    }

    // ==========================================
    // ⭐ Method to display a custom warning message (modified for centering)
    // ==========================================
    private void showCustomWarning(String message) {
        JDialog warningDialog = new JDialog(this, "WARNING", true);
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
                path.moveTo(c, 0);
                path.lineTo(w - c, 0);
                path.lineTo(w, c);
                path.lineTo(w, h - c);
                path.lineTo(w - c, h);
                path.lineTo(c, h);
                path.lineTo(0, h - c);
                path.lineTo(0, c);
                path.closePath();

                g2.setColor(new Color(40, 0, 0, 230)); // Dark red background
                g2.fill(path);

                g2.setColor(new Color(255, 50, 50)); // Neon red border
                g2.setStroke(new BasicStroke(3f));
                g2.draw(path);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Title
        JLabel iconLabel = new JLabel("ACCESS DENIED");
        iconLabel.setFont(gameFont.deriveFont(Font.BOLD, 22f));
        iconLabel.setForeground(new Color(255, 50, 50));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ⭐ Message (Centering set here)
        JLabel msgLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Monospaced", Font.BOLD, 18)); // Clearer font
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER); // Explicit horizontal centering
        msgLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        // Button (ACKNOWLEDGE to enable red color)
        JButton btnOk = createGameButton("OK");
        btnOk.setPreferredSize(new Dimension(200, 40));
        btnOk.setForeground(new Color(255, 100, 100));
        btnOk.addActionListener(e -> warningDialog.dispose());

        panel.add(Box.createVerticalGlue());
        panel.add(iconLabel);
        panel.add(msgLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnOk);
        panel.add(Box.createVerticalGlue());

        warningDialog.add(panel);
        warningDialog.setVisible(true);
    }

    // ==========================================
    // The rest of the code as is
    // ==========================================

    // Creates the main menu panel with buttons for game sections and custom background drawing.
    private JPanel createMainMenuPanel() {
        ImageIcon bgIcon = new ImageIcon(getClass().getResource("Assets//background3.png"));
        Image bgImage = bgIcon.getImage();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                g.setColor(new Color(0, 0, 20, 130));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel("GALACTIC AIR FORCE");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(0x00FFFF));
        title.setFont(gameFont.deriveFont(36f));
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));

        JButton btnSingle = createGameButton("PLAY");
        JButton btnScores = createGameButton("HIGH SCORES");
        JButton btnInstr = createGameButton("INSTRUCTIONS");
        JButton btnCredits = createGameButton("CREDITS");

        // ⭐⭐ New button for toggling sound mute/unmute ⭐⭐
        JButton btnMute = createGameButton("SOUND");

        JButton btnExit = createGameButton("EXIT");

        btnSingle.addActionListener(e -> cardLayout.show(mainPanel, "SinglePlayer"));
        btnScores.addActionListener(e -> {
            mainPanel.remove(highScoresPanel);
            highScoresPanel = createHighScoresPanel();
            mainPanel.add(highScoresPanel, "HighScores");
            cardLayout.show(mainPanel, "HighScores");
        });
        btnInstr.addActionListener(e -> cardLayout.show(mainPanel, "Instructions"));
        btnCredits.addActionListener(e -> {
            creditsPanel.resetAnimations();
            cardLayout.show(mainPanel, "Credits");
        });

        // ⭐⭐ Action for the new button (must be linked to actual sound logic) ⭐⭐
        btnMute.addActionListener(e -> {
            game.toggleSound();
            System.out.println("Sound toggle requested.");
        });

        btnExit.addActionListener(e -> System.exit(0));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.add(btnSingle);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnScores);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnMute);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnCredits);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnInstr);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnExit);

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonsPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private java.awt.event.ActionListener startGameAction;

    // Sets the custom action to be performed when the user clicks 'Start Mission'.
    public void setStartGameAction(java.awt.event.ActionListener action) {
        this.startGameAction = action;
    }

    // Creates the panel where the user enters their name before starting the game.
    private JPanel createSinglePlayerPanel() {
        ImageIcon bgIcon = new ImageIcon(getClass().getResource("Assets//backgroundEnterInfo2.png"));
        Image bgImage = bgIcon.getImage();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0, 0, 30, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel("ENTER YOUR NAME");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(new Color(0x00FFFF));
        label.setFont(gameFont.deriveFont(24f));

        FancyTextField nameField = new FancyTextField(15);
        nameField.setMaximumSize(new Dimension(220, 35));

        JButton start = createGameButton("START MISSION");
        JButton back = createGameButton("BACK");

        start.addActionListener(e -> {
            String playerName = nameField.getText().trim();

            if (playerName.isEmpty()) {
                // ⭐ Required text here
                showCustomWarning("PLEASE ENTER YOUR NAME");
                return;
            }

            Game.setPlayerName(playerName);

            if (startGameAction != null) {
                startGameAction.actionPerformed(e);
            } else {
                // Fallback for testing/unlinked scenario
                JPanel gamePanel = createGamePanel(false);
                mainPanel.add(gamePanel, "GamePanel");
                cardLayout.show(mainPanel, "GamePanel");
            }
        });

        back.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        panel.add(Box.createVerticalGlue());
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(start);
        panel.add(Box.createVerticalStrut(10));
        panel.add(back);
        panel.add(Box.createVerticalGlue());

        return panel;
    }


    // Creates the instructions panel, displaying an image and a back button.
    private JPanel createInstructionsPanel() {
        ImageIcon instIcon = new ImageIcon(getClass().getResource("Assets//instruction.png"));
        Image instImage = instIcon.getImage();

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (instImage != null) g.drawImage(instImage, 0, 0, getWidth(), getHeight(), this);
            }
        };

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JButton back = createGameButton("BACK");
        back.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        back.setPreferredSize(new Dimension(150, 40));

        bottomPanel.add(back, BorderLayout.WEST);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Creates the high scores panel, loading and displaying the top 5 scores from storage.
    private JPanel createHighScoresPanel() {
        ImageIcon bgIcon = new ImageIcon(getClass().getResource("Assets//backgroundEnterInfo.png"));
        Image bgImage = bgIcon.getImage();

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0, 0, 40, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JLabel title = new JLabel("TOP 5 PILOTS", SwingConstants.CENTER);
        title.setForeground(new Color(0x66FFFF));
        title.setFont(gameFont.deriveFont(Font.BOLD, 36f));
        title.setBorder(BorderFactory.createEmptyBorder(25, 0, 20, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        List<HighScoreManagment.ScoreEntry> scores = HighScoreManagment.loadScores();

        JPanel scoresPanel = new JPanel();
        scoresPanel.setLayout(new BoxLayout(scoresPanel, BoxLayout.Y_AXIS));
        scoresPanel.setOpaque(false);
        scoresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (scores.isEmpty()) {
            JLabel empty = new JLabel("NO RECORDS FOUND", SwingConstants.CENTER);
            empty.setForeground(new Color(200, 240, 255));
            empty.setFont(gameFont.deriveFont(Font.PLAIN, 22f));
            scoresPanel.add(empty);
        } else {
            int rank = 1;
            for (HighScoreManagment.ScoreEntry entry : scores) {
                String text = String.format("%d.  %-12s  ............  %d", rank, entry.name, entry.score);
                JLabel scoreLabel = new JLabel(text, SwingConstants.CENTER);
                scoreLabel.setForeground(new Color(200, 240, 255));
                scoreLabel.setFont(gameFont.deriveFont(Font.PLAIN, 22f));
                scoreLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                scoresPanel.add(scoreLabel);
                rank++;
                if (rank > HighScoreManagment.MAX_SCORES) break;
            }
        }

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(Box.createVerticalStrut(40));
        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(scoresPanel);
        centerPanel.add(Box.createVerticalGlue());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JButton back = createGameButton("BACK");
        back.setPreferredSize(new Dimension(150, 40));
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(back);
        bottomPanel.add(Box.createHorizontalGlue());

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Creates a placeholder game panel (currently draws a black screen with stars and a back button).
    private JPanel createGamePanel(boolean isMultiplayer) {
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new OverlayLayout(gamePanel));
        gamePanel.setBackground(Color.BLACK);

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(Color.WHITE);
                for (int i = 0; i < 100; i++) {
                    int x = (int) (Math.random() * getWidth());
                    int y = (int) (Math.random() * getHeight());
                    g.fillRect(x, y, 2, 2);
                }
            }
        };
        canvas.setOpaque(false);
        canvas.setPreferredSize(new Dimension(900, 600));
        gamePanel.add(canvas);

        JButton back = new JButton("BACK TO MENU");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.setMaximumSize(new Dimension(200, 40));
        back.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        gamePanel.add(back);

        return gamePanel;
    }

    // Creates a custom-styled JButton with a hexagonal/angled border and hover effects.
    private JButton createGameButton(String text) {
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

                if (getForeground().getRed() > 200 && getForeground().getBlue() < 150) {
                    g2.setColor(new Color(60, 0, 0, 180));
                    g2.fillPolygon(p);
                    g2.setColor(new Color(255, 50, 50));
                } else {
                    g2.setColor(new Color(0, 20, 40, 180));
                    g2.fillPolygon(p);
                    g2.setColor(new Color(0x00FFFF));
                }

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
        btn.setForeground(new Color(200, 240, 255));
        btn.setFont(gameFont);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 45));
        btn.setHorizontalAlignment(SwingConstants.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // ⭐ This condition is important so the color returns to red if it was the warning button
                if (text.equals("ACKNOWLEDGE")) {
                    btn.setForeground(new Color(255, 100, 100));
                } else {
                    btn.setForeground(new Color(200, 240, 255));
                }
            }
        });
        return btn;
    }

    // Custom class for a JTextField with a sci-fi/arcade visual style.
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
                @Override
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
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
}