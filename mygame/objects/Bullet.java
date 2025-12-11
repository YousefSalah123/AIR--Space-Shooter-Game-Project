package com.mygame.objects;


import javax.media.opengl.GL;

public class Bullet extends GameObject {

    public Bullet(float x, float y) {
        super(x, y, 5, 10); // رصاصة صغيرة
        this.speed = 10.0f;
    }

    @Override
    public void update() {
        y += speed; // تتحرك لأعلى
        if (y > 600) isAlive = false; // تموت لو خرجت
    }

    @Override
    public void render(GL gl) {
        gl.glColor3f(1, 1, 0); // أصفر
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();
    }
}