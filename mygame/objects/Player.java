package com.mygame.objects;

import javax.media.opengl.GL; // استخدام GL
import java.awt.event.KeyEvent;

public class Player extends GameObject {

    private int playerID;
    private final float SCREEN_WIDTH = 800;
    private final float SCREEN_HEIGHT = 600;

    public Player(float x, float y, int playerID) {
        super(x, y, 50, 50);
        this.playerID = playerID;
        this.speed = 8.0f;
    }

    public void handleInput(boolean[] keys) {
        if (playerID == 1) {
            if (keys[KeyEvent.VK_UP]) y += speed;
            if (keys[KeyEvent.VK_DOWN]) y -= speed;
            if (keys[KeyEvent.VK_LEFT]) x -= speed;
            if (keys[KeyEvent.VK_RIGHT]) x += speed;
        } else if (playerID == 2) {
            if (keys[KeyEvent.VK_W]) y += speed;
            if (keys[KeyEvent.VK_S]) y -= speed;
            if (keys[KeyEvent.VK_A]) x -= speed;
            if (keys[KeyEvent.VK_D]) x += speed;
        }
    }

    @Override
    public void update() {
        if (x < 0) x = 0;
        if (x > SCREEN_WIDTH - width) x = SCREEN_WIDTH - width;
        if (y < 0) y = 0;
        if (y > SCREEN_HEIGHT - height) y = SCREEN_HEIGHT - height;
    }

    @Override
    public void render(GL gl) { // استخدام GL
        if (playerID == 1) {
            gl.glColor3f(0.2f, 0.2f, 1.0f);
        } else {
            gl.glColor3f(0.2f, 1.0f, 0.2f);
        }

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        // مقدمة الطائرة
        gl.glColor3f(1, 0, 0);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(x, y + height);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x + width / 2, y + height + 15);
        gl.glEnd();
    }
}