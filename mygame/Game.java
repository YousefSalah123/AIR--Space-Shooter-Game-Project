package com.mygame;

import com.mygame.engine.GameListener;
import com.sun.opengl.util.FPSAnimator;

import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;

public class Game extends JFrame {

    private GLCanvas glCanvas;
    private GameListener listener = new GameListener();
    private FPSAnimator animator;

    public static void main(String[] args) {
        // تشغيل اللعبة في الـ Event Dispatch Thread لضمان استقرار الواجهة
        SwingUtilities.invokeLater(() -> new Game());
    }

    public Game() {
        super("Airplane Shooter 2D");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false); // مهم: منع تغيير الحجم للحفاظ على أبعاد الصور
        setLayout(new BorderLayout());

        // 1. إعداد الـ Canvas
        glCanvas = new GLCanvas();

        // 2. ربط الـ Listener
        glCanvas.addGLEventListener(listener);
        glCanvas.addKeyListener(listener);

        // 3. إعدادات التركيز (Focus) للكيبورد
        glCanvas.setFocusable(true);
        glCanvas.requestFocusInWindow();

        add(glCanvas, BorderLayout.CENTER);

        setVisible(true);

        // 4. تشغيل الـ Animator
        animator = new FPSAnimator(glCanvas, 60);
        animator.start();
    }
}