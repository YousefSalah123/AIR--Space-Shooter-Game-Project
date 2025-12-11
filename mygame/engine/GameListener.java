package com.mygame.engine;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import java.awt.event.*;

public class GameListener implements GLEventListener, KeyListener {

    GameManager manager = new GameManager();
    boolean[] keys = new boolean[256];

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClearColor(0, 0, 0, 1); // خلفية سوداء

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, 800, 0, 600, -1, 1);
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        // 1. نقل حالة الأزرار للاعب (للحركة السلسة)
        manager.player.handleInput(keys);

        // 2. تحديث منطق اللعبة
        manager.update();

        // 3. رسم اللعبة
        manager.render(gl);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() < 256) keys[e.getKeyCode()] = true;

        // (تم حذف كود زر المسافة SPACE)
        // الضرب الآن أوتوماتيكي بالكامل
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() < 256) keys[e.getKeyCode()] = false;

        // (تم حذف كود زر المسافة SPACE)
    }

    @Override
    public void keyTyped(KeyEvent e) { }
    @Override
    public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) { }
    @Override
    public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) { }
}