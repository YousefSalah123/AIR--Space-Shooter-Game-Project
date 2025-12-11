package com.mygame.objects;

import javax.media.opengl.GL;

public class MiddleEnemy {
    public float x, y;
    public float radius = 30; // نصف القطر (يستخدم للتصادم والحجم)
    public int health = 100;
    public int maxHealth = 100;

    public float speedX = 3.0f;
    private float targetY = 450;

    public long lastShotTime = 0;
    public long shotDelay = 1000;
    public int type; // 1: مروحة، 2: تتبع
    public int level;

    public MiddleEnemy(float startX, float startY, int type, int level) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        this.level = level;

        // تعديل الحجم بناءً على المستوى
        if (level == 3) {
            this.radius = 45; // حجم أكبر للمستوى الثالث
        } else {
            this.radius = 30; // الحجم الطبيعي للمستوى الأول والثاني
        }

        this.maxHealth = health;
    }

    public void update(int screenWidth) {
        if (y > targetY) {
            y -= 2.0f; // النزول عند البداية
        } else {
            x += speedX; // الحركة يمين ويسار
            if (x > screenWidth - (radius + 20) || x < (radius + 20)) {
                speedX = -speedX;
            }
        }
    }

    // تم التعديل: تستقبل مصفوفة الصور
    public void render(GL gl, int[] textures) {

        gl.glEnable(GL.GL_BLEND);
        // نستخدم الصورة رقم 7 (تأكد من وجود صورة باسم 7.png أو عدل الرقم حسب الـ GameListener)
        // إذا لم تكن قد أضفت 7.png، يمكنك استخدام textures[2] (صورة العدو العادي) مؤقتاً
        int textureIndex = (textures.length > 7) ? textures[20] : textures[5];
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureIndex);

        // --- تغيير لون التظليل (Tint) حسب المستوى ---
        if (level == 1) gl.glColor3f(1.0f, 0.9f, 0.5f);      // أصفر فاتح
        else if (level == 2) gl.glColor3f(0.5f, 1.0f, 1.0f); // سماوي فاتح
        else gl.glColor3f(1.0f, 0.5f, 1.0f);                 // بنفسجي فاتح

        // حساب أبعاد المربع بناءً على نصف القطر
        float drawSize = radius * 2;
        float drawX = x - radius; // التحويل من المركز إلى الركن الأيسر
        float drawY = y - radius; // التحويل من المركز إلى الركن السفلي

        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);

        // رسم الصورة
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(drawX, drawY + drawSize);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(drawX + drawSize, drawY + drawSize);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(drawX + drawSize, drawY);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(drawX, drawY);

        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);

        // إعادة اللون للأبيض لعدم التأثير على العناصر التالية
        gl.glColor3f(1, 1, 1);

        drawHealthBar(gl);
    }

    private void drawHealthBar(GL gl) {
        float barWidth = radius * 2;
        float barHeight = 6;
        float barX = x - radius; // بداية البار من اليسار
        float barY = y + radius + 10; // فوق العدو

        // الخلفية (أحمر غامق)
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // الصحة (أخضر فاقع)
        float currentWidth = (float)health / maxHealth * barWidth;
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + currentWidth, barY);
        gl.glVertex2f(barX + currentWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // إعادة اللون للأبيض
        gl.glColor3f(1,1,1);
    }
}