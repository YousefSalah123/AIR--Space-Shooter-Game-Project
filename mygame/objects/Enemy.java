package com.mygame.objects;


import javax.media.opengl.GL;

public class Enemy extends GameObject {

    private int typeOfMove; // 0: Straight, 1: Wavy, 2: Chaser
    private float speed;
    private int type;

    public Enemy(float x, float y, int type) {
        super(x, y, 40, 40);
        this.type = type;
        this.speed = 3.0f;
    }

    @Override
    public void update() {
        // TODO (Dev C): حرك العدو لأسفل
        // إذا type == 1 استخدم Math.sin للحركة الموجية
        y -= speed;

        if (y < -50) isAlive = false; // مات إذا خرج من الشاشة
    }

    @Override
    public void render(GL gl) {
        // TODO (Dev C): ارسم العدو (مربع أحمر مؤقتاً)
        gl.glColor3f(1, 0, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();
    }
}