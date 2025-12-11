package mygame2.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.Objects;

public class ArcadeGameUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Font gameFont;

    public ArcadeGameUI() {
        setTitle("Arcade Air Mission");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loadGameFont();

        // إنشاء الشاشات المختلفة
        JPanel mainMenu = createMainMenuPanel();
        JPanel singlePlayer = createSinglePlayerPanel();
        JPanel multiPlayer = createMultiPlayerPanel();
        JPanel instructions = createInstructionsPanel();
        JPanel highScores = createHighScoresPanel();

        // إضافتها إلى الـ mainPanel
        mainPanel.add(mainMenu, "MainMenu");
        mainPanel.add(singlePlayer, "SinglePlayer");
        mainPanel.add(multiPlayer, "MultiPlayer");
        mainPanel.add(instructions, "Instructions");
        mainPanel.add(highScores, "HighScores");

        add(mainPanel);
    }

    /**
     * تحميل الخط المخصص
     */
    private void loadGameFont() {
        try {
            InputStream fontStream = getClass().getResourceAsStream("Assets/Front.png");
            // ملاحظة: هذا المسار يبدو وكأنه لصورة وليس لملف خط، إذا كان لديك ملف .ttf ضعه هنا
            if (fontStream != null) {
                // gameFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(22f);
                gameFont = new Font("Verdana", Font.BOLD, 22); // fallback
            } else {
                gameFont = new Font("Verdana", Font.BOLD, 22);
            }
        } catch (Exception e) {
            gameFont = new Font("Verdana", Font.BOLD, 22);
        }
    }

    /**
     * ----------- القـــــــــائمة الرئيسية -----------
     */
    private JPanel createMainMenuPanel() {
        // التصحيح: استخدام مسار نسبي، واستخدام try-catch لتجنب توقف البرنامج إذا لم توجد الصورة
        ImageIcon bgIcon = null;
        try {
            // لاحظ المسار يبدأ بـ / ثم اسم المجلد
            java.net.URL imgURL = getClass().getResource("Assets/Front.png");
            if (imgURL != null) {
                bgIcon = new ImageIcon(imgURL);
            } else {
                // محاولة بديلة في حال كان المجلد خارج الـ src
                bgIcon = new ImageIcon("Assets/Front.png");
            }
        } catch (Exception e) {
            System.err.println("Error loading Front.png: " + e.getMessage());
        }

        Image bgImage = (bgIcon != null) ? bgIcon.getImage() : null;

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


        JButton btnSingle = createGameButton("SINGLE PLAYER");
        JButton btnMulti = createGameButton("MULTIPLAYER");
        JButton btnScores = createGameButton("HIGH SCORES");
        JButton btnInstr = createGameButton("INSTRUCTIONS");
        JButton btnCredits = createGameButton("CREDITS"); // الزر الجديد
        JButton btnExit = createGameButton("EXIT");

        btnSingle.addActionListener(e -> cardLayout.show(mainPanel, "SinglePlayer"));
        btnMulti.addActionListener(e -> cardLayout.show(mainPanel, "MultiPlayer"));
        btnScores.addActionListener(e -> cardLayout.show(mainPanel, "HighScores"));
        btnInstr.addActionListener(e -> cardLayout.show(mainPanel, "Instructions"));
        btnCredits.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new GameCreditsFrame().setVisible(true));
        });

        btnExit.addActionListener(e -> System.exit(0));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.add(btnSingle);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnMulti);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnScores);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnInstr);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnCredits); // هنا أضف الزر الجديد
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(btnExit);

        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonsPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // أضف هذا المتغير داخل كلاس ArcadeGameUI
    private java.awt.event.ActionListener startGameAction;

    public void setStartGameAction(java.awt.event.ActionListener action) {
        this.startGameAction = action;
    }


    /**
     * ----------- شاشة اللاعب الواحد -----------
     */
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
            if (startGameAction != null) {
                startGameAction.actionPerformed(e);
            } else {
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

    /**
     * ----------- شاشة اللعب الجماعي -----------
     */
    private JPanel createMultiPlayerPanel() {
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

        JLabel p1 = new JLabel("PLAYER 1 NAME:");
        JLabel p2 = new JLabel("PLAYER 2 NAME:");
        p1.setForeground(Color.CYAN);
        p2.setForeground(Color.CYAN);
        p1.setFont(gameFont);
        p2.setFont(gameFont);

        FancyTextField tf1 = new FancyTextField(15);
        FancyTextField tf2 = new FancyTextField(15);
        tf1.setMaximumSize(new Dimension(220, 35));
        tf2.setMaximumSize(new Dimension(220, 35));

        JLabel keys = new JLabel("P1 → ARROWS   |   P2 → W A S D");
        keys.setForeground(Color.YELLOW);
        keys.setAlignmentX(Component.CENTER_ALIGNMENT);
        keys.setFont(new Font("Monospaced", Font.BOLD, 16));

        JButton start = createGameButton("START CO-OP");
        JButton back = createGameButton("BACK");

        start.addActionListener(e -> {
            JPanel gamePanel = createGamePanel(true);
            mainPanel.add(gamePanel, "GamePanel");
            cardLayout.show(mainPanel, "GamePanel");
        });
        back.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));

        panel.add(Box.createVerticalGlue());
        JPanel player1Panel = new JPanel();
        player1Panel.setLayout(new BoxLayout(player1Panel, BoxLayout.Y_AXIS));
        player1Panel.setOpaque(false);
        player1Panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p1.setAlignmentX(Component.CENTER_ALIGNMENT);
        tf1.setAlignmentX(Component.CENTER_ALIGNMENT);
        player1Panel.add(p1);
        player1Panel.add(Box.createVerticalStrut(5));
        player1Panel.add(tf1);

        JPanel player2Panel = new JPanel();
        player2Panel.setLayout(new BoxLayout(player2Panel, BoxLayout.Y_AXIS));
        player2Panel.setOpaque(false);
        player2Panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p2.setAlignmentX(Component.CENTER_ALIGNMENT);
        tf2.setAlignmentX(Component.CENTER_ALIGNMENT);
        player2Panel.add(p2);
        player2Panel.add(Box.createVerticalStrut(5));
        player2Panel.add(tf2);

        panel.add(player1Panel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(player2Panel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(keys);
        panel.add(Box.createVerticalStrut(20));
        panel.add(start);
        panel.add(Box.createVerticalStrut(10));
        panel.add(back);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * ----------- شاشة التعليمات -----------
     */
    private JPanel createInstructionsPanel() {
        ImageIcon instIcon = new ImageIcon(getClass().getResource("Assets//instruction3.png"));
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

        JLabel info = new JLabel("USE ARROWS TO MOVE - SPACE TO FIRE - ESC TO EXIT");
        info.setForeground(Color.CYAN);
        info.setFont(new Font("Monospaced", Font.BOLD, 18));
        info.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(info, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * ----------- شاشة أعلى النتائج (High Scores) -----------
     */
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

        String[] topPlayers = {
                "1.  NovaBlaster ............  9500",
                "2.  StarFalcon .............  8600",
                "3.  AstroRider .............  8020",
                "4.  SkyEagle ...............  7450",
                "5.  CosmicX ................  7200"
        };

        JPanel scoresPanel = new JPanel();
        scoresPanel.setLayout(new BoxLayout(scoresPanel, BoxLayout.Y_AXIS));
        scoresPanel.setOpaque(false);
        scoresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (String player : topPlayers) {
            JLabel scoreLabel = new JLabel(player, SwingConstants.CENTER);
            scoreLabel.setForeground(new Color(200, 240, 255));
            scoreLabel.setFont(gameFont.deriveFont(Font.PLAIN, 22f));
            scoreLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            scoresPanel.add(scoreLabel);
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

    /**
     * ----------- شاشة اللعب أثناء الطيران (Game Panel) -----------
     */
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

        // تنبيه: كلاس GameHUD غير موجود في الكود المرسل، لذا تم تعليقه لتجنب الأخطاء
        // GameHUD hud = new GameHUD(isMultiplayer);
        // hud.setPreferredSize(new Dimension(900, 600));
        // gamePanel.add(hud);

        JButton back = new JButton("BACK TO MENU");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.setMaximumSize(new Dimension(200, 40));
        back.addActionListener(e -> cardLayout.show(mainPanel, "MainMenu"));
        gamePanel.add(back); // إضافة الزر مباشرة للبانل بدلاً من HUD

        return gamePanel;
    }

    /**
     * ----------- الزر المخصص (تم التعديل عليه) -----------
     */
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

                g2.setColor(new Color(0, 20, 40, 180));
                g2.fillPolygon(p);
                g2.setColor(new Color(0x00FFFF));
                g2.setStroke(new BasicStroke(2));
                g2.drawPolygon(p);

                super.paintComponent(g);
                g2.dispose();
            }
        };
        // ✅ هذا هو السطر الذي يزيل المستطيل حول الكلمة
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
                btn.setForeground(new Color(200, 240, 255));
            }
        });
        return btn;
    }

    /**
     * زر ناعم نيون أزرق
     */
    private JButton createNameScreenButton(String text) {
        final boolean[] hover = {false};
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), arc = 20;

                GradientPaint gp = new GradientPaint(
                        0, 0,
                        hover[0] ? new Color(0x33CCFF) : new Color(0x003366),
                        w, h,
                        hover[0] ? new Color(0x99FFFF) : new Color(0x005599)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                g2.setColor(hover[0] ? new Color(0x99FFFF) : new Color(0x66CCFF));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, arc, arc);

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(getText());
                int th = fm.getAscent();
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), (w - tw) / 2, (h + th / 2) - 2);

                g2.dispose();
            }
        };

        btn.setFocusPainted(false); // موجود بالفعل هنا
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(gameFont.deriveFont(Font.BOLD, 18f));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover[0] = true;
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover[0] = false;
                btn.repaint();
            }
        });

        return btn;
    }

    // ======= خانة إدخال فخمة =======
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
            int arc = 25; // درجة انحناء الزوايا

            // رسم خلفية الخانة (شفافة وغامقة)
            Color baseColor = new Color(0, 30, 60, 120);
            g2.setColor(baseColor);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // رسم الإطار (يضيء عند التركيز)
            g2.setColor(focused ? new Color(0x00FFFF) : new Color(0x007777));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.dispose();

            // استدعاء السوبر لرسم النص والمؤشر (Caret)
            super.paintComponent(g);
        }
    } // ---- نهاية كلاس FancyTextField ----

    // ==========================================
    //           دالة التشغيل الرئيسية
    // ==========================================
    public static void main(String[] args) {
        new ArcadeGameUI();
    }
} // ---- نهاية كلاس ArcadeGameUI ----