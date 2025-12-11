package com.mygame.engine;

import javax.media.opengl.*;
import java.awt.event.*;

public class GameListener implements GLEventListener, KeyListener {

    GameManager manager = new GameManager();
    boolean[] keys = new boolean[256]; // لتخزين حالة الأزرار (حركة ناعمة)

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClearColor(0, 0, 0, 1); // خلفية سوداء

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, 800, 0, 600, -1, 1); // إحداثيات الشاشة
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        // 1. معالجة الإدخال (Dev B)
        manager.player1.handleInput(keys);

        // 2. تحديث المنطق (Dev A)
        manager.update();

        // 3. الرسم (الجميع)
        manager.render(gl);
    }

    // --- KeyListener ---
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if(code >= 0 && code < keys.length) {
            keys[code] = true;
        }

        // Immediate movement when arrow keys or WASD are pressed
        if(code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A
           || code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D
           || code == KeyEvent.VK_UP || code == KeyEvent.VK_W
           || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {

            // Call handleInput now so movement is immediate (smooth movement still handled each frame)
            manager.player1.handleInput(keys);
        }

        // إطلاق النار (ضغطة واحدة)
        if(code == KeyEvent.VK_SPACE) {
            manager.playerShoot();
        }
        e.getComponent().repaint();

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() >= 0 && e.getKeyCode() < keys.length) keys[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) { }
    @Override
    public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) { }

    @Override
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {

    }
}