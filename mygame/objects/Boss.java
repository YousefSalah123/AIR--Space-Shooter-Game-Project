package com.mygame.objects;

import javax.media.opengl.GL;
import java.util.ArrayList;

public class Boss extends Enemy {

    private int hp;
    private int maxHp;
    private int level; // 1, 2, or 3
    private boolean entered = false;
    private float moveDirection = 1;

    public Boss(float x, float y, int level) {
        super(x, y, 0, 0,null); // الحجم سنحدده بالأسفل
        this.level = level;

        // إعدادات الزعيم حسب المستوى
        switch (level) {
            case 1: // الزعيم الضعيف
                this.width = 80; this.height = 60;
                this.maxHp = 20;
                this.speed = 2.0f;
                break;
            case 2: // الزعيم المتوسط
                this.width = 100; this.height = 80;
                this.maxHp = 40;
                this.speed = 4.0f; // أسرع
                break;
            case 3: // الزعيم النهائي (الدبابة)
                this.width = 150; this.height = 100;
                this.maxHp = 80;
                this.speed = 1.5f; // بطيء لكن قوي
                break;
        }
        this.hp = maxHp;
    }

    @Override
    public void update() {
        // 1. مرحلة الدخول (ثابتة للكل)
        if (!entered) {
            y -= 2.0f; // ينزل ببطء
            if (y <= 450) entered = true;
        }
        // 2. القتال
        else {
            x += speed * moveDirection;

            // الارتداد من الحواف
            if (x > 700 || x < 0) {
                moveDirection *= -1;
            }
        }
    }

    // دالة الإطلاق الذكية (تختلف حسب المستوى)
    public void shootLogic(ArrayList<Bullet> bullets) {
        if (!entered) return; // لا يضرب وهو لسه داخل

        double chance = Math.random();

        if (level == 1) {
            // المستوى 1: طلقة واحدة مستقيمة (بطيء)
            if (chance < 0.02) {
                bullets.add(new Bullet(x + width/2, y, 0, -7, true));
            }
        }
        else if (level == 2) {
            // المستوى 2: 3 طلقات (يمين، نص، شمال) (أسرع)
            if (chance < 0.03) {
                bullets.add(new Bullet(x + width/2, y, 0, -8, true));  // نص
                bullets.add(new Bullet(x + width/2, y, 3, -7, true));  // يمين
                bullets.add(new Bullet(x + width/2, y, -3, -7, true)); // شمال
            }
        }
        else if (level == 3) {
            // المستوى 3: رشاش (Machine Gun)
            if (chance < 0.10) { // سريع جداً
                // طلقة عشوائية الاتجاه قليلاً
                float randX = (float)(Math.random() * 6) - 3; // من -3 لـ 3
                bullets.add(new Bullet(x + width/2, y, randX, -9, true));
            }
        }
    }

    @Override
    public void render(GL gl) {
        // تغيير لون الزعيم حسب المستوى
        if (level == 1) gl.glColor3f(0.6f, 0.6f, 0.6f); // رمادي
        else if (level == 2) gl.glColor3f(0.2f, 0.2f, 0.8f); // أزرق غامق
        else gl.glColor3f(0.8f, 0.0f, 0.0f); // أحمر مرعب

        // رسم الجسم
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        // رسم شريط الصحة (كما هو من الكود السابق)
        drawHealthBar(gl);
    }

    private void drawHealthBar(GL gl) {
        float barWidth = (float)hp / maxHp * width;

        // إطار أحمر
        gl.glColor3f(1, 0, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y + height + 10);
        gl.glVertex2f(x + width, y + height + 10);
        gl.glVertex2f(x + width, y + height + 20);
        gl.glVertex2f(x, y + height + 20);
        gl.glEnd();

        // صحة خضراء
        gl.glColor3f(0, 1, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y + height + 10);
        gl.glVertex2f(x + barWidth, y + height + 10);
        gl.glVertex2f(x + barWidth, y + height + 20);
        gl.glVertex2f(x, y + height + 20);
        gl.glEnd();
    }

    public void takeDamage() {
        hp--;
        if (hp <= 0) isAlive = false;
    }
}