package com.mygame.objects;

import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;

public class Boss extends GameObject {

    private int health, maxHealth, level;
    private float moveSpeed = 2.0f;
    private int direction = 1;

    // منطق الليزر
    public boolean isFiringLaser = false;
    private long lastLaserTime = 0;
    private long lastShotTime = 0;

    // --- متغيرات الموت والأنيميشن ---
    public boolean isDying = false;
    public boolean animationFinished = false;
    private int dieFrameCounter = 0;
    private int dieFrameDelay = 10; // سرعة تبديل الصور (كل 10 فريمات صورة)
    private int currentTextureOffset = 0; // للمساعدة في التنقل بين صور الانفجار

    public Boss(float x, float y, int level) {
        super(x, y, 50, 50);
        this.level = level;

        // إعدادات الصحة والأبعاد حسب المستوى
        if (level == 1) {
            maxHealth = 200; width = 80; height = 80; moveSpeed = 2.0f;
        } else if (level == 2) {
            maxHealth = 600; width = 110; height = 110; moveSpeed = 3.0f;
        } else {
            maxHealth = 1200; width = 150; height = 150; moveSpeed = 4.0f;
        }
        this.health = maxHealth;
    }

    @Override
    public void update() {
        // إذا كان الزعيم يحتضر، توقف عن الحركة والمنطق
        if (isDying) return;

        if (!isFiringLaser) {
            float currentSpeed = (health < maxHealth / 2) ? moveSpeed * 1.5f : moveSpeed;
            x += currentSpeed * direction;
            if (x > (800 - width) || x < 0) direction *= -1;

            float shakeAmount = (level == 3) ? 40 : 20;
            y = (600 - height - 50) + (float) Math.sin(System.currentTimeMillis() / 400.0) * shakeAmount;
        } else {
            // اهتزاز بسيط أثناء إطلاق الليزر
            x += (Math.random() * 6) - 3;
        }

        if (level == 3 && health < (maxHealth * 0.20)) manageLaserLogic();
        else isFiringLaser = false;
    }

    private void manageLaserLogic() {
        long currentTime = System.currentTimeMillis();
        int laserDuration = 100 + (level * 250);
        int cooldown = 2000 - (level * 100);
        if (isFiringLaser) {
            if (currentTime - lastLaserTime > laserDuration) { isFiringLaser = false; lastLaserTime = currentTime; }
        } else {
            if (currentTime - lastLaserTime > cooldown) { isFiringLaser = true; lastLaserTime = currentTime; }
        }
    }

    public void shootLogic(ArrayList<Bullet> bullets) {
        // لا يطلق النار إذا كان يطلق الليزر أو إذا كان يموت
        if (isFiringLaser || isDying) return;

        long currentTime = System.currentTimeMillis();
        int fireRate = 1000 - (level * 100);

        if (currentTime - lastShotTime > fireRate) {
            bullets.add(new Bullet(x + width / 2, y, 0, -10, true));
            if (level >= 2) {
                bullets.add(new Bullet(x, y, -3, -8, true));
                bullets.add(new Bullet(x + width, y, 3, -8, true));
            }
            lastShotTime = currentTime;
        }
    }

    @Override
    public void render(GL gl, int[] textures) {
        // رسم الليزر فقط إذا لم يكن يموت
        if (isFiringLaser && level == 3 && !isDying) drawBossLaser(gl);

        // --- تحديد الصورة المناسبة للرسم ---
        int textureIndex;

        if (!isDying) {
            // الحالة الطبيعية
            // Level 1 uses index 7, Level 2+ uses index 12
            textureIndex = (level == 1) ? textures[7] : textures[12];

            // تغيير اللون للأحمر عند الغضب
            if (health < maxHealth * 0.30) gl.glColor3f(1.0f, 0.5f, 0.5f);
            else gl.glColor3f(1, 1, 1);

        } else {
            // حالة الموت (Animation)
            gl.glColor3f(1, 1, 1); // إعادة اللون للأبيض
            dieFrameCounter++;

            if (dieFrameCounter > dieFrameDelay) {
                dieFrameCounter = 0;
                currentTextureOffset++; // ننتقل للصورة التالية
            }

            // حساب الـ Index بناءً على المصفوفة في GameListener
            if (level == 1) {
                // Boss 1 dying frames: 8, 9, 10, 11 (4 frames)
                if (currentTextureOffset > 3) { // 0,1,2,3
                    animationFinished = true;
                    currentTextureOffset = 3; // نثبت على آخر صورة
                }
                textureIndex = textures[8 + currentTextureOffset];
            } else {
                // Boss 2 dying frames: 13, 14, 15, 16, 17, 18 (6 frames)
                // نستخدم نفس صور Boss 2 للمستوى 3 أيضاً
                if (currentTextureOffset > 5) { // 0..5
                    animationFinished = true;
                    currentTextureOffset = 5;
                }
                textureIndex = textures[13 + currentTextureOffset];
            }
        }

        // رسم الزعيم
        if (!animationFinished || isDying) {
            drawTexture(gl, textureIndex, x, y, width, height);
        }

        // إعادة ضبط اللون
        gl.glColor3f(1, 1, 1);

        // رسم شريط الصحة فقط إذا لم يكن يموت
        if (!isDying) drawHealthBar(gl);
    }

    private void drawBossLaser(GL gl) {
        gl.glEnable(GL.GL_BLEND);
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.8f);
        Rectangle rect = getLaserBounds();
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(rect.x, rect.y);
        gl.glVertex2f(rect.x + rect.width, rect.y);
        gl.glVertex2f(rect.x + rect.width, 0); // يمتد لأسفل الشاشة
        gl.glVertex2f(rect.x, 0);
        gl.glEnd();
        gl.glDisable(GL.GL_BLEND);
        gl.glColor3f(1, 1, 1);
    }

    private void drawHealthBar(GL gl) {
        float barWidth = width;
        float barHeight = 10;
        float barX = x;
        float barY = y + height + 10;

        // الخلفية
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY); gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight); gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // الصحة
        float hpPercent = (float) health / maxHealth;
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY); gl.glVertex2f(barX + (barWidth * hpPercent), barY);
        gl.glVertex2f(barX + (barWidth * hpPercent), barY + barHeight); gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();
    }

    public Rectangle getLaserBounds() {
        return (!isFiringLaser) ? new Rectangle(0,0,0,0) : new Rectangle((int)(x+width/2-30), 0, 60, (int)y);
    }

    public void takeDamage() {
        if (isDying) return; // لا يتضرر وهو يموت

        health -= 7;
        if (health <= 0) {
            isDying = true; // تفعيل الأنيميشن
            health = 0;
            // لا نضع setAlive(false) هنا، ننتظر انتهاء الأنيميشن
        }
    }
}