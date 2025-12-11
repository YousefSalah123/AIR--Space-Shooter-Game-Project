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
    // إحداثيات المدافع (Offsets) بالنسبة لجسم البوس
    // إحداثيات المدافع (Offsets) معدلة لتناسب حجم البوس (120 - 160 - 200)

    // المدفع الأيسر (حوالي ربع العرض)
    private float gun1_OffsetX = 15;

    // ارتفاع الفوهة (قريب من الصفر لأن البوس باصص لتحت، فالطلقة تخرج من أسفل جسمه)
    private float gun1_OffsetY = 10;

    // المدفع الأيمن (حوالي ثلاثة أرباع العرض)
    private float gun2_OffsetX = 80;

    // نفس ارتفاع المدفع الأول
    private float gun2_OffsetY = 10;

    public Boss(float x, float y, int level) {
        super(x, y, 50, 50);
        this.level = level;

        // إعدادات الصحة والأبعاد حسب المستوى
        if (level == 1) {
            maxHealth = 200; width = 150; height = 150; moveSpeed = 2.0f;
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
        // سرعة الضرب تعتمد على الليفل
        int fireRate = 1200 - (level * 150);

        if (currentTime - lastShotTime > fireRate) {

            // --- هنا السحر: إخراج الطلقة من فوهة المدفع المرسومة ---

            // حساب الموقع الحقيقي للفوهة في الشاشة
            // BossX + OffsetX
            float spawnX_Left = x + gun1_OffsetX;
            float spawnY      = y + gun1_OffsetY;

            float spawnX_Right = x + gun2_OffsetX;

            // إطلاق طلقتين من المدافع
            // المعامل الأخير (textureIndex) هو صورة طلقة العدو (مثلاً 6)
            bullets.add(new Bullet(spawnX_Left, spawnY, 0, -8, true, 6));
            bullets.add(new Bullet(spawnX_Right, spawnY, 0, -8, true, 6));

            // لو الليفل عالي (2 أو 3)، ممكن نضيف مدفع تالت في النص
            if (level >= 2) {
                bullets.add(new Bullet(x + width / 2 - 7, y + height, 0, -10, true, 6));
            }

            lastShotTime = currentTime;
        }
    }
    @Override
    public void render(GL gl, int[] textures) {
        // 1. رسم الليزر (فقط في المستوى 3 وأثناء الحياة)
        if (isFiringLaser && level == 3 && !isDying) {
            drawBossLaser(gl);
        }

        // ============================================================
        // 2. تحديد نطاق صور البوس بناءً على المستوى
        // ============================================================
        int myBossStartIndex;
        int myBossEndIndex;

        if (level == 1) {
            myBossStartIndex = 7;
            myBossEndIndex   = 11;
        } else {
            // المستويات 2 و 3 (يستخدمون نفس الاندكس حسب الكود السابق)
            myBossStartIndex = 12;
            myBossEndIndex   = 18;
        }

        int textureIndex;

        // ============================================================
        // 3. منطق اختيار الصورة (حي vs ميت)
        // ============================================================
        if (!isDying) {
            // --- حالة الحياة ---
            // يظل البوس على صورته الأولى (السليمة) ولا يتغير شكله مع نقص الصحة
            textureIndex = textures[myBossStartIndex];

            // لون طبيعي
            gl.glColor3f(1, 1, 1);

        } else {
            // --- حالة الموت (Animation) ---
            // هنا يتم تشغيل جميع الصور (من السليم للمدمر) كتسلسل انفجار
            int deathSpeed = 5; // سرعة الأنيميشن (كل ما الرقم قل، بقى أسرع)

            dieFrameCounter++;
            if (dieFrameCounter > deathSpeed) {
                dieFrameCounter = 0;
                currentTextureOffset++;
            }

            // حساب الصورة الحالية في شريط الانفجار
            int currentAnimIndex = myBossStartIndex + currentTextureOffset;

            // التحقق من انتهاء الصور
            if (currentAnimIndex > myBossEndIndex) {
                animationFinished = true;
                textureIndex = textures[myBossEndIndex]; // الثبات على آخر صورة
            } else {
                textureIndex = textures[currentAnimIndex];
            }

            // تأثير لوني (أحمر خفيف) أثناء الانفجار
            gl.glColor3f(1.0f, 0.7f, 0.7f);
        }

        // ============================================================
        // 4. الرسم النهائي
        // ============================================================

        // رسم جسم البوس (إذا لم ينتهِ الانفجار)
        if (!animationFinished) {
            drawTexture(gl, textureIndex, x, y, width, height);
        }

        // رسم شريط الصحة (فقط إذا كان حياً)
        // نعيد اللون للأبيض لضمان أن البار يظهر بألوانه الصحيحة
        gl.glColor3f(1, 1, 1);
        if (!isDying) {
            drawHealthBar(gl);
        }
    }    // --- التعديل الأساسي هنا ---
    private void drawHealthBar(GL gl) {
        // 1. مهم جداً: إيقاف التكستشر عشان الألوان تظهر صح ومش سوداء
        gl.glDisable(GL.GL_TEXTURE_2D);

        float barWidth = width-5;
        float barHeight = 13;
        float barX = x;
        float barY = y + height-27; // فوق البوس

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
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        Rectangle rect = getLaserBounds();
        float topY = rect.y + rect.height; // أعلى الليزر (البوس)
        float bottomY = 0;                 // أسفل الشاشة

        // --- الليزر الخارجي (Glow) ---
        gl.glColor4f(1.0f, 0.6f, 0.6f, 0.6f); // أحمر فاتح شفاف
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(rect.x - 10, topY);
        gl.glVertex2f(rect.x + rect.width + 10, topY);
        gl.glVertex2f(rect.x + rect.width + 10, bottomY);
        gl.glVertex2f(rect.x - 10, bottomY);
        gl.glEnd();

        // --- الليزر الداخلي (Core) ---
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.9f); // أحمر قوي
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(rect.x + 3, topY);
        gl.glVertex2f(rect.x + rect.width - 3, topY);
        gl.glVertex2f(rect.x + rect.width - 3, bottomY);
        gl.glVertex2f(rect.x + 3, bottomY);
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

        health -= 3;
        if (health <= 0) {
            isDying = true;
            health = 0;
        }
    }
}