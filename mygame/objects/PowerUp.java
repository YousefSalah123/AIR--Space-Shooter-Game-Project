package com.mygame.objects;

import javax.media.opengl.GL;

public class PowerUp extends GameObject {

    // 0 = Health (قلب), 1 = Rapid Fire (سرعة ضرب)
    private int type;

    public PowerUp(float x, float y, int type) {
        super(x, y, 30, 30); // حجم المساعدة
        this.type = type;
        this.speed = 3.0f;   // سرعة السقوط
    }

    @Override
    public void update() {
        y -= speed; // تسقط لأسفل
        if (y < -50) isAlive = false;
    }

    @Override
    public void render(GL gl) {
        if (type == 0) {
            gl.glColor3f(1.0f, 0.0f, 0.0f); // أحمر (قلب)
        } else {
            gl.glColor3f(0.0f, 1.0f, 1.0f); // سماوي (سرعة)
        }

        // رسم مربع المساعدة
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        // رسم رمز بسيط في المنتصف (اختياري لتمييز الشكل)
        gl.glColor3f(1, 1, 1); // أبيض
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x + 10, y + 10);
        gl.glVertex2f(x + 20, y + 10);
        gl.glVertex2f(x + 20, y + 20);
        gl.glVertex2f(x + 10, y + 20);
        gl.glEnd();
    }

    public int getType() {
        return type;
    }
}