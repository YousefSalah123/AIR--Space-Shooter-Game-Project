package com.mygame.objects;

import javax.media.opengl.GL;
import java.awt.Rectangle;

public class Enemy extends GameObject {

    // 1: Normal (مستقيم), 2: Chaser (ملاحق), 3: Snake/Wave (موجة)
    private int type;
    private Player playerTarget;

    // متغيرات الحركة الموجية (للنوع 3)
    private float startX; // نقطة الارتكاز الأفقية
    private float angle = 0; // زاوية الموجة

    public Enemy(float x, float y, float size, int type, Player player) {
        super(x, y, size, size);
        this.type = type;
        this.playerTarget = player;
        this.startX = x; // حفظ مكان البداية

        // ضبط السرعة حسب النوع
        if (type == 2) this.speed = 4.0f;      // الملاحق سريع
        else if (type == 3) this.speed = 3.0f; // الموجة متوسطة
        else this.speed = 2.0f;                // العادي بطيء
    }

    @Override
    public void update() {
        // --- منطق الحركة ---

        if (type == 1) {
            // النوع 1: نزول مستقيم عادي
            y -= speed;
        }
        else if (type == 2) {
            // النوع 2: مطاردة اللاعب
            y -= speed;
            if (playerTarget != null) {
                if (x < playerTarget.getX()) x += 1.5f;
                if (x > playerTarget.getX()) x -= 1.5f;
            }
        }
        else if (type == 3) {
            // النوع 3: حركة الثعبان (Sine Wave) 🐍
            y -= speed; // ينزل لتحت
            angle += 0.05f; // سرعة التمايل

            // المعادلة: المركز + (سعة الموجة * جا الزاوية)
            // 80 هو عرض الموجة (Amplitude)
            x = startX + (float) (Math.sin(angle) * 80);
        }

        // الموت عند الخروج من الشاشة
        if (y < -50) setAlive(false);
    }

    @Override
    public void render(GL gl) {
        // تمييز الألوان عشان نعرف الفرق
        if (type == 1) gl.glColor3f(1.0f, 0.0f, 0.0f);      // أحمر (عادي)
        else if (type == 2) gl.glColor3f(1.0f, 0.5f, 0.0f); // برتقالي (ملاحق)
        else if (type == 3) gl.glColor3f(1.0f, 0.0f, 1.0f); // بنفسجي (موجة)

        // رسم جسم العدو
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        // رسم تفاصيل (عيون)
        gl.glColor3f(0, 0, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x + 10, y + 10);
        gl.glVertex2f(x + 15, y + 10);
        gl.glVertex2f(x + 15, y + 20);
        gl.glVertex2f(x + 10, y + 20);

        gl.glVertex2f(x + width - 15, y + 10);
        gl.glVertex2f(x + width - 10, y + 10);
        gl.glVertex2f(x + width - 10, y + 20);
        gl.glVertex2f(x + width - 15, y + 20);
        gl.glEnd();
    }

    public boolean readyToFire() {
        return Math.random() < 0.005;
    }
}