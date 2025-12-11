package com.mygame.objects;

import javax.media.opengl.GL;

public class GoldCoin extends GameObject {
    private float rotateAngle = 0;

    public GoldCoin(float x, float y) {
        super(x, y, 30, 30);
        this.speed = 3.0f;
    }

    @Override
    public void update() {
        y -= speed; // تسقط لأسفل
        rotateAngle += 5.0f; // تدوير مستمر
        if (y < -50) isAlive = false;
    }

    @Override
    public void render(GL gl) {
        // --- 3D TRICK ---
        // نرسم شكلاً يدور حول المحور Y ليبدو ثلاثي الأبعاد
        gl.glPushMatrix();
        gl.glTranslatef(x + width/2, y + height/2, 0); // نذهب للمركز
        gl.glRotatef(rotateAngle, 0, 1, 0); // دوران 3D حول Y

        gl.glColor3f(1, 0.84f, 0); // لون ذهبي
        // رسم "عملة" (مربع يدور)
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-10, -10);
        gl.glVertex2f(10, -10);
        gl.glVertex2f(10, 10);
        gl.glVertex2f(-10, 10);
        gl.glEnd();

        gl.glPopMatrix();
    }
}