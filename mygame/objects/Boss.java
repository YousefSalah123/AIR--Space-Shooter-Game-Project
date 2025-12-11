package mygame.objects;

import mygame.engine.SoundManager;
import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;

public class Boss extends GameObject {

    public int health;
    private int maxHealth;
    private int level;
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

    public void shootLogic(ArrayList<Bullet> bullets, SoundManager soundManager) {
        if (isFiringLaser || isDying) return;

        long currentTime = System.currentTimeMillis();
        // سرعة الضرب تزيد مع المستويات
        int fireRate = 1200 - (level * 200);

        if (currentTime - lastShotTime > fireRate) {

            // تحديد ارتفاع خروج الطلقة (فوهة المدفع)
            float spawnY = y + (height * 0.1f);

            // --- Level 1: طلقتين (يمين ويسار) ---
            if (level == 1) {
                float spawnX_Left = x + (width * 0.2f);
                float spawnX_Right = x + (width * 0.8f);

                bullets.add(new Bullet(spawnX_Left, spawnY, 0, -8, true, 6));
                bullets.add(new Bullet(spawnX_Right, spawnY, 0, -8, true, 6));
            }
            // --- Level 2: 4 طلقات (موزعة بالتساوي) ---
            else if (level == 2) {
                // نوزع 4 طلقات على عرض البوس (20%, 40%, 60%, 80%)
                bullets.add(new Bullet(x + (width * 0.2f), spawnY, 0, -9, true, 6));
                bullets.add(new Bullet(x + (width * 0.4f), spawnY, 0, -9, true, 6));
                bullets.add(new Bullet(x + (width * 0.6f), spawnY, 0, -9, true, 6));
                bullets.add(new Bullet(x + (width * 0.8f), spawnY, 0, -9, true, 6));
            }
            // --- Level 3: 5 طلقات (مروحة Fan) ---
            else {
                float centerX = x + width / 2;

                // الوسط
                bullets.add(new Bullet(centerX, spawnY, 0, -12, true, 6));

                // زاوية داخلية
                bullets.add(new Bullet(x + (width * 0.3f), spawnY, -2, -10, true, 6));
                bullets.add(new Bullet(x + (width * 0.7f), spawnY, 2, -10, true, 6));

                // زاوية خارجية
                bullets.add(new Bullet(x + (width * 0.1f), spawnY, -4, -9, true, 6));
                bullets.add(new Bullet(x + (width * 0.9f), spawnY, 4, -9, true, 6));
            }

            if (soundManager != null) {
                soundManager.playSound("boss_laser");
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
        } else if (level == 2){
            myBossStartIndex = 12;
            myBossEndIndex   = 18;
        }
        else {
            // المستوى 3 (حسب الكود الخاص بك)
            myBossStartIndex = 61;
            myBossEndIndex   = 67;
        }

        int textureIndex;

        // ============================================================
        // 3. منطق اختيار الصورة (حي vs ميت)
        // ============================================================
        if (!isDying) {
            // --- حالة الحياة ---
            // يظل البوس على صورته الأولى (السليمة) ولا يتغير شكله مع نقص الصحة
            textureIndex = textures[myBossStartIndex];
            gl.glColor3f(1, 1, 1);

        } else {
            // --- حالة الموت (Animation) ---
            int deathSpeed = 5;

            dieFrameCounter++;
            if (dieFrameCounter > deathSpeed) {
                dieFrameCounter = 0;
                currentTextureOffset++;
            }

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

        if (!animationFinished) {
            drawTexture(gl, textureIndex, x, y, width, height);
        }

        // رسم شريط الصحة (فقط إذا كان حياً)
        gl.glColor3f(1, 1, 1);
        if (!isDying) {
            drawHealthBar(gl);
        }
    }

    private void drawHealthBar(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);

        float barWidth = width-5;
        float barHeight = 13;
        float barX = x;
        float barY = y + height-27; // فوق البوس

        // Background (Red)
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Health (Green)
        float hpPercent = (float) health / maxHealth;
        float currentGreenWidth = barWidth * hpPercent;

        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + currentGreenWidth, barY);
        gl.glVertex2f(barX + currentGreenWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // White Border
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(1.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1, 1, 1);
    }

    private void drawBossLaser(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        Rectangle rect = getLaserBounds();
        float topY = rect.y + rect.height;
        float bottomY = 0;

        // Glow
        gl.glColor4f(1.0f, 0.6f, 0.6f, 0.6f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(rect.x - 10, topY);
        gl.glVertex2f(rect.x + rect.width + 10, topY);
        gl.glVertex2f(rect.x + rect.width + 10, bottomY);
        gl.glVertex2f(rect.x - 10, bottomY);
        gl.glEnd();

        // Core
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.9f);
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