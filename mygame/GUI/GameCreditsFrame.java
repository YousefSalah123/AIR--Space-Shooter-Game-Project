package mygame.GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class GameCreditsFrame extends JFrame {

    private Font gameFont;
    private JPanel mainPanel;
    private List<DevCard> devCards = new ArrayList<>();

    // ⭐ قائمة لتخزين النجوم لمنع الوميض
    private final List<Point> starPositions = new ArrayList<>();

    public GameCreditsFrame() {
        setTitle("Credits - Galactic Air Mission");
        setSize(950, 650); // زيادة طفيفة في الطول
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true); // إزالة شريط العنوان لشكل أكثر احترافية
        setShape(new RoundRectangle2D.Double(0, 0, 950, 650, 20, 20)); // زوايا دائرية

        loadFont();
        generateStars(); // توليد النجوم مرة واحدة

        // 🌌 الخلفية
        mainPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // تدرج لوني عميق
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 10, 30),
                        0, getHeight(), new Color(0, 0, 5));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // رسم النجوم الثابتة (بدون وميض)
                g2.setColor(new Color(255, 255, 255, 150));
                for (Point p : starPositions) {
                    g2.fillRect(p.x, p.y, 2, 2);
                }

                // رسم إطار نيون خفيف حول النافذة
                g2.setColor(new Color(0, 255, 255, 100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        add(mainPanel);

        // العنوان
        JLabel title = new JLabel("GALACTIC MISSION SQUAD", SwingConstants.CENTER);
        title.setForeground(new Color(0x00FFFF));
        title.setFont(new Font("Verdana", Font.BOLD, 32));
        title.setBounds(0, 30, getWidth(), 50);

        // تأثير ظل للعنوان
        JLabel shadowTitle = new JLabel("GALACTIC MISSION SQUAD", SwingConstants.CENTER);
        shadowTitle.setForeground(new Color(0, 0, 0, 100));
        shadowTitle.setFont(new Font("Verdana", Font.BOLD, 32));
        shadowTitle.setBounds(2, 32, getWidth(), 50);

        mainPanel.add(title);
        mainPanel.add(shadowTitle); // إضافة الظل أولاً (خلف النص)
        mainPanel.setComponentZOrder(shadowTitle, 1);
        mainPanel.setComponentZOrder(title, 0);

        // ===== بيانات الفريق =====
        List<Developer> developers = new ArrayList<>();
        developers.add(new Developer("Ahmed Mostafa", "Lead Developer", "Assets/avatars/avatar1.png", "https://github.com/mohamedahmed", "https://www.linkedin.com/in/mohamedahmed/"));
        developers.add(new Developer("Hammad Ahmed", "UI / UX Designer", "Assets/avatars/avatar2.png", "https://github.com/hammadahmedx15-stack", "https://www.linkedin.com/in/hammadahmed289"));
        developers.add(new Developer("Youssef Salah", "AI Systems Lead", "Assets/avatars/avatar3.png", "https://github.com/YousefSalah123", "https://www.linkedin.com/in/yousef-salah-nage-a3583636b"));
        developers.add(new Developer("Amr Mahmoud", "Sound Engineer", "Assets/avatars/avatar4.png", "https://github.com/Amr-Mahmoud293", "https://www.linkedin.com/in/amr-29-elbhar"));
        developers.add(new Developer("Mostafa Eid", "Quality Assurance", "Assets/avatars/avatar5.png", "https://github.com/monaadel", "http://www.linkedin.com/in/mustafaeid412"));

        // إعداد التخطيط
        int total = developers.size();
        int cardWidth = 160;
        int cardHeight = 220; // زيادة قليلة للارتفاع
        int gap = 20; // مسافة ثابتة بين الكروت

        // حساب العرض الكلي للكروت لتوسيطها بدقة
        int totalWidth = (total * cardWidth) + ((total - 1) * gap);
        int startX = (getWidth() - totalWidth) / 2;
        int baseY = (getHeight() - cardHeight) / 2;

        for (int i = 0; i < total; i++) {
            Developer dev = developers.get(i);
            DevCard card = new DevCard(dev);

            // الموقع المبدئي (تحت الشاشة للأنيميشن)
            int xPos = startX + (i * (cardWidth + gap));
            card.setBounds(xPos, baseY + 300, cardWidth, cardHeight);

            card.finalY = baseY;
            card.setOpacity(0f);

            mainPanel.add(card);
            devCards.add(card);
        }

        // زر العودة
        JButton back = createGameButton("RETURN TO BASE");
        back.setBounds((getWidth() - 220) / 2, 560, 220, 50);
        back.addActionListener(e -> dispose());
        mainPanel.add(back);

//        JLabel footer = new JLabel("H_A", SwingConstants.CENTER);
//        footer.setForeground(new Color(100, 120, 140));
//        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
//        footer.setBounds(0, 620, getWidth(), 20);
//        mainPanel.add(footer);

        // تشغيل الأنيميشن
        runHorizontalAnimation();
    }

    private void loadFont() {
        gameFont = new Font("Verdana", Font.BOLD, 18);
    }

    private void generateStars() {
        for (int i = 0; i < 150; i++) {
            int x = (int) (Math.random() * 950);
            int y = (int) (Math.random() * 650);
            starPositions.add(new Point(x, y));
        }
    }

    // ========= الأنيميشن =========
    private void runHorizontalAnimation() {
        final int[] index = {0};
        Timer starter = new Timer(200, e -> { // تسريع الفاصل الزمني قليلاً
            if (index[0] < devCards.size()) {
                animateCard(devCards.get(index[0]));
                index[0]++;
            } else ((Timer) e.getSource()).stop();
        });
        starter.start();
    }

    private void animateCard(DevCard c) {
        Timer t = new Timer(15, null); // 60 FPS تقريباً
        t.addActionListener(e -> {
            c.setOpacity(Math.min(c.getOpacity() + 0.04f, 1f)); // زيادة الشفافية
            Point p = c.getLocation();

            // معادلة حركة ناعمة (Easing)
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

    // ========= بطاقة مطور =========
    class DevCard extends JPanel {
        float opacity;
        int finalY;

        DevCard(Developer d) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            // خلفية زجاجية خفيفة للكارت
            setBackground(new Color(255, 255, 255, 10));

            // الصورة
            JLabel img = new JLabel(new ImageIcon(makeCircularImage(loadImage(d.imagePath), 90)));
            img.setAlignmentX(Component.CENTER_ALIGNMENT);

            // الاسم
            JLabel name = new JLabel(d.name, SwingConstants.CENTER);
            name.setForeground(Color.WHITE);
            name.setFont(gameFont.deriveFont(Font.BOLD, 15f));
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            // الدور الوظيفي
            JLabel role = new JLabel(d.role, SwingConstants.CENTER);
            role.setForeground(new Color(0x00FFFF)); // لون نيون
            role.setFont(gameFont.deriveFont(Font.PLAIN, 12f));
            role.setAlignmentX(Component.CENTER_ALIGNMENT);

            // الأيقونات
            JPanel icons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            icons.setOpaque(false);
            icons.add(new MagicIcon("Assets/github.png", d.github));
            icons.add(new MagicIcon("Assets/linkedin.png", d.linkedin));

            add(Box.createVerticalStrut(20));
            add(img);
            add(Box.createVerticalStrut(15));
            add(name);
            add(Box.createVerticalStrut(5));
            add(role);
            add(Box.createVerticalGlue());
            add(icons);
            add(Box.createVerticalStrut(20));
        }

        void setOpacity(float f) {
            opacity = f;
        }

        float getOpacity() {
            return opacity;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // تطبيق الشفافية الكلية
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            // رسم خلفية الكارت (مستطيل بزوايا دائرية وحدود)
            g2.setColor(new Color(255, 255, 255, 20)); // شفاف جداً
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

            g2.setColor(new Color(0x00FFFF)); // حدود سماوي
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // ========= أيقونة تفاعلية =========
    class MagicIcon extends JLabel {
        boolean scaling = false;
        double scale = 1.0;
        final String link;
        Image baseImage;

        MagicIcon(String iconPath, String link) {
            this.link = link;
            BufferedImage rawImg = loadImage(iconPath);
            if (rawImg != null) {
                baseImage = rawImg.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(baseImage));
            } else {
                setText("LINK"); // نص بديل في حال عدم وجود الصورة
                setForeground(Color.CYAN);
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    startScaleAnimation(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    startScaleAnimation(false);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (link != null && !link.isEmpty()) openLink(link);
                }
            });
        }

        private void startScaleAnimation(boolean grow) {
            new Timer(10, ev -> {
                if (grow) {
                    if (scale < 1.3) scale += 0.05;
                    else ((Timer) ev.getSource()).stop();
                } else {
                    if (scale > 1.0) scale -= 0.05;
                    else ((Timer) ev.getSource()).stop();
                }
                repaint();
            }).start();
        }

        private void openLink(String url) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ignored) {
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (baseImage == null) {
                super.paintComponent(g);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int imgW = (int) (24 * scale);
            int imgH = (int) (24 * scale);
            int x = (getWidth() - imgW) / 2;
            int y = (getHeight() - imgH) / 2;

            g2.drawImage(baseImage, x, y, imgW, imgH, null);
            g2.dispose();
        }
    }

    // ========= أدوات مساعدة =========
    private BufferedImage loadImage(String path) {
        try {
            // محاولة التحميل بمسارين مختلفين لضمان العمل
            java.net.URL url = getClass().getResource(path);
            if (url == null) url = getClass().getResource("/StartGame/" + path);
            if (url == null) url = getClass().getResource("/" + path);

            if (url != null) return ImageIO.read(url);
        } catch (Exception e) {
            // تجاهل الخطأ وإرجاع صورة افتراضية
        }
        // صورة بديلة رمادية في حال الفشل
        BufferedImage fallback = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics g = fallback.createGraphics();
        g.setColor(new Color(50, 50, 50));
        g.fillOval(0, 0, 50, 50);
        g.dispose();
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

    // ✅ الزر الهندسي (تم الإصلاح)
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

                // لون يتغير عند التحويم
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

        btn.setFocusPainted(false); // 👈 إزالة المستطيل المزعج
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);

        btn.setForeground(new Color(200, 240, 255));
        btn.setFont(gameFont.deriveFont(Font.BOLD, 18f));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

    // ========= كلاس البيانات (Data Class) =========
    static class Developer {
        String name, role, imagePath, github, linkedin;

        Developer(String n, String r, String img, String g, String l) {
            this.name = n;
            this.role = r;
            this.imagePath = img;
            this.github = g;
            this.linkedin = l;
        }
    }

    // ========= التشغيل الرئيسي =========
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // محاولة تفعيل التسريع الرسومي للنصوص لضمان النعومة
            System.setProperty("sun.java2d.opengl", "true");
            new GameCreditsFrame().setVisible(true);
        });
    }
}