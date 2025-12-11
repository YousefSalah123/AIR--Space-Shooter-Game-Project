package com.mygame;
import com.mygame.GUI.*;
import com.mygame.engine.GameListener;
import com.mygame.engine.GameManager;
import com.sun.opengl.util.FPSAnimator;

import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;

public class Game extends JFrame {

    private GLCanvas glCanvas;
    private GameListener listener;
    private FPSAnimator animator;
    private ArcadeGameUI mainMenu;
    private GameManager manager;

    public static void main(String[] args) {
        // تشغيل اللعبة في الـ Event Dispatch Thread لضمان استقرار الواجهة
        SwingUtilities.invokeLater(() -> new Game());
    }

    public Game() {
        super("Airplane Shooter 2D");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // 1. أولاً: تهيئة المنطق والـ Listener (لتجنب NullPointer)
        manager = new GameManager(this);
        listener = new GameListener(manager);

        // 2. ثانياً: إعداد الـ Canvas وربط الـ Listener
        glCanvas = new GLCanvas();
        glCanvas.addGLEventListener(listener);
        glCanvas.addKeyListener(listener);

        // إعدادات التركيز لضمان عمل الكيبورد
        glCanvas.setFocusable(true);
        glCanvas.requestFocusInWindow();

        // إضافة الـ Canvas للنافذة
        add(glCanvas, BorderLayout.CENTER);

        // 3. ثالثاً: إعداد الـ Animator (لكن لا نبدأه الآن)
        animator = new FPSAnimator(glCanvas, 60);


        // 4. إظهار القائمة الرئيسية فور التشغيل
        showMainMenu();
    }

    public void showMainMenu() {
        // إخفاء نافذة اللعبة (الـ Canvas)
        this.setVisible(false);

        // إيقاف اللعبة مؤقتاً إذا كانت تعمل في الخلفية
        if (animator.isAnimating()) {
            animator.stop();
        }

        mainMenu = new ArcadeGameUI();

        // ربط زر "Start Game" في القائمة ببدء اللعبة الفعلية
        mainMenu.setStartGameAction(e -> startActualGame());

        mainMenu.setVisible(true);
    }

    public void startActualGame() {
        // إخفاء القائمة الرئيسية
        mainMenu.setVisible(false);

        // إظهار نافذة اللعبة
        this.setVisible(true);

        // إعادة التركيز للكيبورد
        glCanvas.requestFocusInWindow();

        // إعادة تهيئة بيانات اللعبة (Scores, Health, etc.)
        manager.resetGame();

        // بدء حلقة الرسم (Game Loop)
        if (!animator.isAnimating()) {
            animator.start();
        }
    }

    // يتم استدعاء هذه الدالة من GameManager عند انتهاء اللعبة
    public void handleGameOver(boolean victory, int score) {
        // إيقاف الرسم
        animator.stop();

        // استخدام مصفوفة لتمرير المتغير للـ Lambda (لحل مشكلة المتغيرات النهائية)
        final EndLevelFrame[] holder = new EndLevelFrame[1];

        holder[0] = new EndLevelFrame(
                victory,
                score,
                e -> {                      // زر Retry
                    holder[0].dispose();    // إغلاق نافذة النهاية
                    startActualGame();      // بدء اللعبة من جديد
                },
                e -> {                      // زر Main Menu
                    holder[0].dispose();    // إغلاق نافذة النهاية
                    this.setVisible(false); // إخفاء نافذة اللعبة
                    showMainMenu();         // العودة للقائمة
                }
        );

        holder[0].setVisible(true);
    }
}