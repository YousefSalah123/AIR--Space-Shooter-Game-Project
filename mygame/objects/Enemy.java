package com.mygame.objects;

import javax.media.opengl.GL;

public class Enemy extends GameObject {

    // الأنواع: 0=عادي، 1=موجي، 2=مطارد (Chaser)
    private int type;
    private float startX;
    private long lastShotTime;
    private float fireRate;
    private Player target; // مرجعية للاعب عشان نعرف نطارده

    // نعدل الـ Constructor ليستقبل اللاعب (target)
    public Enemy(float x, float y, float size, int type, Player target) {
        super(x, y, size, size);
        this.startX = x;
        this.type = type;
        this.target = target; // تخزين اللاعب كهدف

        // المطارد يكون سريعاً قليلاً
        this.speed = (type == 2) ? 4.0f : (2.0f + (float)Math.random() * 2.0f);

        this.fireRate = 1000 + (float)Math.random() * 2000;
        this.lastShotTime = System.currentTimeMillis();
    }

    @Override
    public void update() {
        // --- المنطق حسب النوع ---

        if (type == 2 && target != null && target.isAlive()) {
            // === منطق المطاردة (Chaser Logic) ===
            // 1. حساب المسافة بين العدو واللاعب
            float dx = target.getX() - x;
            float dy = target.getY() - y;

            // 2. حساب طول المتجه (Hypotenuse)
            float distance = (float)Math.sqrt(dx*dx + dy*dy);

            // 3. التحرك نحو اللاعب (Normalization)
            // (dx / distance) يعطينا الاتجاه (-1 إلى 1)
            if (distance > 0) {
                x += (dx / distance) * speed;
                y += (dy / distance) * speed;
            }
        }
        else if (type == 1) {
            // === منطق الموجة (Wavy) ===
            y -= speed;
            x = startX + 50 * (float)Math.sin(y * 0.05);
        }
        else {
            // === منطق عادي (Straight) ===
            y -= speed;
        }

        if (y < -50) isAlive = false;
    }

    public boolean readyToFire() {
        // المطارد لا يطلق النار (مشغول بالمطاردة) لتبسيط اللعبة
        if (type == 2) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime > fireRate) {
            lastShotTime = currentTime;
            return true;
        }
        return false;
    }

    @Override
    public void render(GL gl) {
        if (type == 0) gl.glColor3f(0.8f, 0.2f, 0.2f); // أحمر (عادي)
        else if (type == 1) gl.glColor3f(1.0f, 0.5f, 0.0f); // برتقالي (موجي)
        else gl.glColor3f(0.8f, 0.0f, 0.8f); // بنفسجي (مطارد) !!!

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        // (اختياري) رسم عينين للمطارد لبيان أنه "شرير"
        if (type == 2) {
            gl.glColor3f(1, 1, 1); // أبيض
            float eyeSize = width / 4;
            // عين يسرى
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2f(x + eyeSize, y + eyeSize);
            gl.glVertex2f(x + 2*eyeSize, y + eyeSize);
            gl.glVertex2f(x + 2*eyeSize, y + 2*eyeSize);
            gl.glVertex2f(x + eyeSize, y + 2*eyeSize);
            // عين يمنى
            gl.glVertex2f(x + 2.5f*eyeSize, y + eyeSize);
            gl.glVertex2f(x + 3.5f*eyeSize, y + eyeSize);
            gl.glVertex2f(x + 3.5f*eyeSize, y + 2*eyeSize);
            gl.glVertex2f(x + 2.5f*eyeSize, y + 2*eyeSize);
            gl.glEnd();
        }
    }
}