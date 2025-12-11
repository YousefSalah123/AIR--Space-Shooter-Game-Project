package com.mygame.objects;

import javax.media.opengl.GL;

public class Bullet extends GameObject {

    private boolean isEnemyBullet;
    private float speedX = 0; // السرعة الأفقية (مهمة للطلقات المائلة)

    // --- Constructor 1: للضرب العادي المستقيم ---
    public Bullet(float x, float y, boolean isEnemyBullet) {
        super(x, y, 8, 20); // الحجم الافتراضي للرصاصة
        this.isEnemyBullet = isEnemyBullet;

        // تحديد السرعة الرأسية: سالب للأعداء (ينزل لتحت)، موجب للاعب (يطلع لفوق)
        this.speed = isEnemyBullet ? -7.0f : 15.0f;
        this.speedX = 0;
    }

    // --- Constructor 2: للضرب المتقدم (Spread, Boss, Laser) ---
    public Bullet(float x, float y, float speedX, float speedY, boolean isEnemyBullet) {
        super(x, y, 10, 20);
        this.isEnemyBullet = isEnemyBullet;
        this.speed = speedY; // هنا speed من الكلاس الأب بتمثل السرعة الرأسية
        this.speedX = speedX;

        // تعديل شكل الليزر (لو السرعة عالية جداً)
        if (Math.abs(speedY) > 30) {
            this.width = 12;  // أرفع
            this.height = 60; // أطول بكتير
        }
    }

    @Override
    public void update() {
        y += speed;   // الحركة الرأسية
        x += speedX;  // الحركة الأفقية

        // قتل الرصاصة فور خروجها من الشاشة (مع هامش صغير)
        if (y > 700 || y < -50 || x < -50 || x > 850) {
            setAlive(false);
        }
    }

    @Override
    public void render(GL gl) {
        // --- تحديد اللون ---
        if (isEnemyBullet) {
            gl.glColor3f(1.0f, 0.2f, 0.2f); // أحمر فاتح (للأعداء)
        } else {
            // لو دي رصاصة ليزر (بنعرفها من السرعة)
            if (Math.abs(speed) > 30) {
                gl.glColor3f(0.0f, 1.0f, 1.0f); // سماوي ساطع (Cyan)
            } else {
                gl.glColor3f(1.0f, 1.0f, 0.0f); // أصفر (للرصاص العادي)
            }
        }

        // --- رسم الرصاصة ---
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        // (إضافة جمالية) قلب أبيض لليزر عشان يبان مضيء
        if (!isEnemyBullet && Math.abs(speed) > 30) {
            gl.glColor3f(1.0f, 1.0f, 1.0f); // أبيض
            gl.glBegin(GL.GL_LINES); // خط رفيع في النص
            gl.glVertex2f(x + width/2, y);
            gl.glVertex2f(x + width/2, y + height);
            gl.glEnd();
        }
    }

    // --- Getters (مهمة للـ GameManager) ---
    public boolean isEnemyBullet() {
        return isEnemyBullet;
    }

    // الدالة دي بنستخدمها عشان نعرف دي رصاصة ليزر ولا لأ في التصادمات
    public float getSpeedY() {
        return speed;
    }
}