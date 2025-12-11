package com.mygame.objects;


import javax.media.opengl.GL;

public class GoldCoin extends GameObject {

    private float rotateAngle = 0;

    public GoldCoin(float x, float y) {
        super(x, y, 20, 20);
        this.speed = 2.0f;
    }

    @Override
    public void update() {
        y -= speed;
        rotateAngle += 5.0f; // تدوير مستمر
    }

    @Override
    public void render(GL gl) {
        // TODO (Dev C): تحقيق شرط الـ 3D
        gl.glPushMatrix();
        gl.glTranslatef(x + width/2, y + height/2, 0); // انتقل للمركز
        gl.glRotatef(rotateAngle, 0, 1, 0); // دوران حول Y (يبدو كـ 3D)

        gl.glColor3f(1, 0.8f, 0); // ذهبي
        // ارسم مربع (سيظهر كأنه يدور في 3D)
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(-10, -10);
        gl.glVertex2f(10, -10);
        gl.glVertex2f(10, 10);
        gl.glVertex2f(-10, 10);
        gl.glEnd();

        gl.glPopMatrix();
    }
}