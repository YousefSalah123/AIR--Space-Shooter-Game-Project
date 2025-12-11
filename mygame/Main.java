package com.mygame;

import com.mygame.engine.GameListener;
import com.sun.opengl.util.FPSAnimator;

import javax.media.opengl.GLCanvas;
import javax.swing.JFrame;
import java.awt.BorderLayout;

public class Main extends JFrame {

    private GLCanvas glCanvas;
    private GameListener listener = new GameListener();
    private FPSAnimator animator;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        super("Airplane Shooter 2D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // نفس مقاسات الـ glOrtho
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. إنشاء الكانفاس
        glCanvas = new GLCanvas();

        // 2. ربط الـ Listener (الرسم + الكيبورد)
        glCanvas.addGLEventListener(listener);
        glCanvas.addKeyListener(listener); // مهم جداً عشان التحكم يشتغل!

        // 3. التركيز (Focus) عشان الكيبورد يشتغل
        glCanvas.setFocusable(true);
        glCanvas.requestFocusInWindow();

        add(glCanvas, BorderLayout.CENTER);
        setVisible(true);

        // 4. تشغيل الأنيميشن (60 فريم في الثانية)
        animator = new FPSAnimator(glCanvas, 60);
        animator.start();
    }
}