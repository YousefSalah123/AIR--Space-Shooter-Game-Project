package mygame2.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicReference;

public class TestPauseMenu {

    public static void main(String[] args) {
        // تشغيل الواجهة في الـ Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {

            // استخدام AtomicReference عشان نقدر نستخدم المتغير frame جوه الـ Lambdas
            AtomicReference<PauseMenuFrame> frameRef = new AtomicReference<>();

            System.out.println("🚀 Starting Pause Menu Test...");

            // إنشاء النافذة
            PauseMenuFrame frame = new PauseMenuFrame(
                    // 1. زر Resume
                    e -> {
                        System.out.println("▶️ Button Pressed: RESUME");
                        // إغلاق النافذة عند الضغط
                        if (frameRef.get() != null) {
                            frameRef.get().dispose();
                            System.out.println("✅ Test Finished (Window Closed)");
                            System.exit(0);
                        }
                    },

                    // 2. زر Restart
                    e -> System.out.println("🔄 Button Pressed: RESTART LEVEL"),

                    // 3. زر Menu
                    e -> System.out.println("🏠 Button Pressed: BACK TO MENU"),

                    // 4. تغيير الصوت (Volume Slider)
                    e -> {
                        // الحدث ده بيتبعت لما تحرك السلايدر
                        String command = ((ActionEvent) e).getActionCommand(); // بيرجع "VOLUME:50" مثلاً
                        System.out.println("🔊 Sound Event: " + command);
                    }
            );

            // ربط المرجع بالنافذة الحقيقية
            frameRef.set(frame);

            // عرض النافذة
            frame.setVisible(true);

            System.out.println("✨ Window is visible. Hover over buttons to test flicker.");
            System.out.println("ℹ️ Note: If you have 'resources/music.wav', it will play.");
        });
    }
}