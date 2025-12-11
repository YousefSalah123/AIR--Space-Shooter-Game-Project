package mygame.GUI;

import mygame.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * لوحة زر الإيقاف المؤقت/مفتاح ESC بتصميم شفاف وحدود ملونة.
 */
public class PauseButtonPanel extends JPanel {

    private final Game game;
    private final JLabel pauseLabel;

    // ⭐ الألوان الثابتة الجديدة:
    private static final Color TEXT_COLOR = new Color(0x32FFC8); // ⭐ أرجواني ساطع / فوشيا// أخضر سماوي ساطع (Neon Green/Cyan)
    private static final Color BORDER_COLOR = new Color(0x32FFC8); // نفس لون النص للحدود
    private static final Color HOVER_COLOR = new Color(0xFF44AA); // ⭐ أصفر ساطع عند التمرير

    public PauseButtonPanel(Game game) {
        this.game = game;

        // --- 1. إعداد اللوحة الأساسية (الشفافية التامة) ---
        setOpaque(false);
        setLayout(new BorderLayout());

        // --- 2. إعداد الـ JLabel لنص "ESC" ---
        pauseLabel = new JLabel("ESC", SwingConstants.CENTER);
        pauseLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        pauseLabel.setForeground(TEXT_COLOR);
        pauseLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ⭐ إضافة حد (Border) لتقليد شكل الزر بدون خلفية مملوءة
        // نستخدم حافة مركبة: حشو فارغ + حافة خطية ملونة
        pauseLabel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 2), // الحد الخارجي الملون
                        BorderFactory.createEmptyBorder(6, 12, 6, 12)    // الحشو الداخلي
                )
        );

        // --- 3. إضافة مستمع الماوس (MouseListener) لتفعيل الإيقاف المؤقت ---
        pauseLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (game != null) {
                    game.togglePause();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // ⭐ تأثير لون النص عند التمرير (الأصفر الساطع)
                pauseLabel.setForeground(HOVER_COLOR);

                // ⭐ تغيير لون الحد عند التمرير
                pauseLabel.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(HOVER_COLOR, 2),
                                BorderFactory.createEmptyBorder(6, 12, 6, 12)
                        )
                );
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // ⭐ إرجاع اللون الأصلي (الأرجواني)
                pauseLabel.setForeground(TEXT_COLOR);

                // ⭐ إرجاع لون الحد الأصلي
                pauseLabel.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                                BorderFactory.createEmptyBorder(6, 12, 6, 12)
                        )
                );
            }
        });

        this.add(pauseLabel, BorderLayout.CENTER);

        this.setPreferredSize(new Dimension(85, 35));
    }

    // تأكيد الشفافية
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}