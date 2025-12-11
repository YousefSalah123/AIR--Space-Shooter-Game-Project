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
    private int dieFrameDelay = 10;
    private int currentTextureOffset = 0;

    public Boss(float x, float y, int level) {
        super(x, y, 50, 50);
        this.level = level;

        // إعدادات الصحة والأبعاد حسب المستوى
        if (level == 1) {
            maxHealth = 200; width = 120; height = 120; moveSpeed = 2.0f;
        } else if (level == 2) {
            maxHealth = 600; width = 160; height = 160; moveSpeed = 3.0f;
        } else {
            maxHealth = 1200; width = 200; height = 200; moveSpeed = 4.0f;
        }
        this.health = maxHealth;
    }

    @Override
    public void update() {
        if (isDying) return;

        if (!isFiringLaser) {
            float currentSpeed = (health < maxHealth / 2) ? moveSpeed * 1.5f : moveSpeed;
            x += currentSpeed * direction;
            if (x > (800 - width) || x < 0) direction *= -1;

            float shakeAmount = (level == 3) ? 40 : 20;
            y = (600 - height - 50) + (float) Math.sin(System.currentTimeMillis() / 400.0) * shakeAmount;
        } else {
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
        if (isFiringLaser && level == 3 && !isDying) drawBossLaser(gl);

        int textureIndex;

        // ============================================================
        // 1. تحديد نطاق صور كل بوس (البداية والنهاية) بناءً على GameListener
        // ============================================================

        int myBossStartIndex;
        int myBossEndIndex;

        if (level == 1) {
            // Boss 1: من 7 إلى 11
            myBossStartIndex = 7;
            myBossEndIndex   = 11;
        } else if (level == 2) {
            // Boss 2: من 12 إلى 18 (شامل صور Boss2.5)
            myBossStartIndex = 12;
            myBossEndIndex   = 18;
        } else {
            // Boss 3 (مؤقتاً نعامله زي ليفل 2 لو ملوش صور خاصة)
            myBossStartIndex = 12;
            myBossEndIndex   = 18;
        }

        // ============================================================

        if (!isDying) {
            // --- حالة الحياة (تغيير الشكل حسب الدمج) ---

            float hpPercent = (float) health / maxHealth;
            int offset = 0;

            // نحسب عدد الصور المتاحة لهذا البوس عشان نقسم الصحة عليهم
            int totalImages = myBossEndIndex - myBossStartIndex + 1;

            // معادلة ذكية: تحويل نسبة الصحة لرقم الصورة المناسب
            // (1.0 - hpPercent) عشان كل ما الصحة تقل، الاندكس يزيد
            // بنضرب في (totalImages - 1) عشان أخر صورة تكون للموت

            // لكن للتبسيط وللحفاظ على اللوجيك القديم بتاعك (5 مراحل):
            if (hpPercent > 0.80) offset = 0;
            else if (hpPercent > 0.60) offset = 1;
            else if (hpPercent > 0.40) offset = 2;
            else if (hpPercent > 0.20) offset = 3;
            else offset = (totalImages > 4) ? 4 : totalImages - 1; // نتأكد إننا منخرجش بره الحدود

            // نضمن إننا مخرجناش عن حدود صور البوس
            if (myBossStartIndex + offset > myBossEndIndex) {
                textureIndex = textures[myBossEndIndex];
            } else {
                textureIndex = textures[myBossStartIndex + offset];
            }

            gl.glColor3f(1, 1, 1);

        } else {
            // --- حالة الموت (تشغيل كل صور البوس ورا بعض) ---

            // سرعة العرض (كل ما الرقم يقل، الجري يكون أسرع)
            // ممكن تخليها 5 عشان يلحق يعرضهم كلهم بسرعة
            int deathSpeed = 5;

            dieFrameCounter++;
            if (dieFrameCounter > deathSpeed) {
                dieFrameCounter = 0;
                currentTextureOffset++;
            }

            // حساب الصورة الحالية في الانيميشن
            int currentAnimIndex = myBossStartIndex + currentTextureOffset;

            // لو وصلنا لآخر صورة للبوس، نوقف الأنيميشن ونخفيه
            if (currentAnimIndex > myBossEndIndex) {
                animationFinished = true;
                // عشان ميعملش Crash نثبته على آخر صورة لحد ما يختفي
                textureIndex = textures[myBossEndIndex];
            } else {
                textureIndex = textures[currentAnimIndex];
            }

            // إضافة لون أحمر خفيف أثناء الانهيار (اختياري)
            gl.glColor3f(1.0f, 0.8f, 0.8f);
        }

        // الرسم النهائي
        if (!animationFinished || isDying) {
            // شرط إضافي: لو الانيميشن خلص (animationFinished = true) ميرسمش حاجة خالص
            if (!animationFinished) {
                drawTexture(gl, textureIndex, x, y, width, height);
            }
        }

        // رسم البار فقط وهو عايش
        gl.glColor3f(1, 1, 1); // إعادة اللون
        if (!isDying) drawHealthBar(gl);
    }

    // --- التعديل الأساسي هنا ---
    private void drawHealthBar(GL gl) {
        // 1. مهم جداً: إيقاف التكستشر عشان الألوان تظهر صح ومش سوداء
        gl.glDisable(GL.GL_TEXTURE_2D);

        float barWidth = width;
        float barHeight = 10;
        float barX = x;
        float barY = y + height + 10; // فوق البوس

        // 2. رسم الخلفية الحمراء (Full Width)
        gl.glColor3f(1.0f, 0.0f, 0.0f); // أحمر
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // 3. رسم الجزء الأخضر (بناءً على النسبة)
        float hpPercent = (float) health / maxHealth;
        float currentGreenWidth = barWidth * hpPercent;

        gl.glColor3f(0.0f, 1.0f, 0.0f); // أخضر
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + currentGreenWidth, barY);
        gl.glVertex2f(barX + currentGreenWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // 4. (اختياري) إطار أبيض عشان يحدد البار
        gl.glColor3f(1.0f, 1.0f, 1.0f); // أبيض
        gl.glLineWidth(1.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // 5. إعادة تفعيل التكستشر لباقي اللعبة
        gl.glEnable(GL.GL_TEXTURE_2D);

        // إعادة اللون للأبيض عشان الرسومات اللي بعد كده متتلونش
        gl.glColor3f(1, 1, 1);
    }

    private void drawBossLaser(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); // تأكد من تفعيل دالة الدمج للشفافية

        Rectangle rect = getLaserBounds();

        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(rect.x - 10, rect.y);
        gl.glVertex2f(rect.x + rect.width + 10, rect.y);
        gl.glVertex2f(rect.x + rect.width + 10, 0);
        gl.glVertex2f(rect.x - 10, 0);
        gl.glEnd();

        gl.glColor4f(1.0f, 1.0f, 0.0f, 0.8f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(rect.x + 10, rect.y);
        gl.glVertex2f(rect.x + rect.width - 10, rect.y);
        gl.glVertex2f(rect.x + rect.width - 10, 0);
        gl.glVertex2f(rect.x + 10, 0);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_BLEND);
        gl.glColor3f(1, 1, 1);
    }

    public Rectangle getLaserBounds() {
        if (!isFiringLaser) {
            return new Rectangle(0, 0, 0, 0);
        }
        int laserWidth = 60;
        int startX = (int)(x + width / 2 - laserWidth / 2);
        return new Rectangle(startX, 0, laserWidth, (int)y);
    }

    public void takeDamage() {
        if (isDying) return;

        health -= 7;
        if (health <= 0) {
            isDying = true;
            health = 0;
        }
    }
}