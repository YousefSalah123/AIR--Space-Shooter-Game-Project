package com.mygame.objects;

import javax.media.opengl.GL;

public class Bullet extends GameObject {

    private boolean isEnemyBullet;
    private float speedX = 0; // سرعة أفقية (الجديد)

    // Constructor القديم (للضرب المستقيم)
    public Bullet(float x, float y, boolean isEnemyBullet) {
        super(x, y, 5, 15);
        this.isEnemyBullet = isEnemyBullet;
        this.speed = isEnemyBullet ? -7.0f : 15.0f;
    }

    // Constructor جديد (للضرب المائل - للزعيم)
    public Bullet(float x, float y, float speedX, float speedY, boolean isEnemyBullet) {
        super(x, y, 8, 20); // طلقة أكبر قليلاً
        this.isEnemyBullet = isEnemyBullet;
        this.speed = speedY;
        this.speedX = speedX;
    }

    @Override
    public void update() {
        y += speed;   // حركة رأسية
        x += speedX;  // حركة أفقية (للطلقات المائلة)

        if (y > 600 || y < -50 || x < 0 || x > 800) {
            setAlive(false);
        }
    }

    @Override
    public void render(GL gl) {
        if (isEnemyBullet) gl.glColor3f(1, 0, 0); // أحمر
        else gl.glColor3f(1, 1, 0); // أصفر

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();
    }

    public boolean isEnemyBullet() { return isEnemyBullet; }
}