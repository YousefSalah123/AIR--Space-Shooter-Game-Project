package com.mygame.objects;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends GameObject {

    public final float SCREEN_WIDTH = 800;
    public final float SCREEN_HEIGHT = 600;
    public static final int MAX_HEALTH = 15;

    // --- حالة الانتقال ---
    public boolean isFlyingOff = false;

    // --- حالة الموت (الأنيميشن) ---
    public boolean isDying = false; // هل اللاعب في حالة احتضار؟
    public boolean animationFinished = false; // هل انتهى الأنيميشن؟
    private int dieFrameCounter = 0; // عداد لتبطيء سرعة الأنيميشن
    private int dieFrameDelay = 10;  // سرعة التبديل (كل 10 فريمات صورة)
    private int currentTextureIndex = 1; // نبدأ بصورة البطل العادية (Hero.png)

    // --- القدرات ---
    public boolean specialAttackAvailable = true;
    public boolean isSpecialAttackActive = false;
    private long specialAttackEndTime = 0;
    public boolean specialAttackUsedOnEnemies = false;
    public int specialAmmo = 1;

    private int health;
    public int weaponLevel = 1;

    // --- الدرع والليزر ---
    public boolean isShieldActive = false;
    public boolean isShieldAvailable = true;
    private long shieldEndTime = 0;

    public boolean isLaserBeamActive = false;
    public boolean isLaserAvailable = true;
    private long laserEndTime = 0;

    public Player(float x, float y) {
        super(x, y, 80, 80);
        this.speed = 8.0f;
        this.health = MAX_HEALTH;
    }

    @Override
    public void update() {
        // إذا كان اللاعب يموت، لا تقم بتحديث الحركة أو الحدود
        if (isDying) return;

        // 1. منطق الانتقال للمستوى التالي (Fly Off)
        if (isFlyingOff) {
            float targetX = (SCREEN_WIDTH / 2) - (width / 2);
            x += (targetX - x) * 0.05f; // تحرك ناعم للوسط
            y += 7.0f; // صعود للأعلى
            return;
        }

        // 2. حدود الشاشة العادية
        if (x < 0) x = 0;
        if (x > SCREEN_WIDTH - width) x = SCREEN_WIDTH - width;
        if (y < 10) y = 10;
        if (y > SCREEN_HEIGHT - height) y = SCREEN_HEIGHT - height;

        // 3. التوقيتات
        long now = System.currentTimeMillis();
        if (isShieldActive && now > shieldEndTime) {
            isShieldActive = false;
        }
        if (isLaserBeamActive && now > laserEndTime) {
            isLaserBeamActive = false;
        }
        if (isSpecialAttackActive && now > specialAttackEndTime) {
            isSpecialAttackActive = false;
        }
    }

    // --- التحكم في الانتقال ---
    public void triggerFlyOff() {
        isFlyingOff = true;
        isShieldActive = false;
        isLaserBeamActive = false;
    }

    public void resetPosition() {
        isFlyingOff = false;
        isDying = false;
        animationFinished = false;
        currentTextureIndex = 1; // العودة للصورة الطبيعية
        this.x = 375;
        this.y = 50;
    }

    // --- المدخلات ---
    public void handleInput(boolean[] keys) {
        if (isFlyingOff || isDying) return; // منع الحركة أثناء الموت

        if (keys[KeyEvent.VK_UP]) y += speed;
        if (keys[KeyEvent.VK_DOWN]) y -= speed;
        if (keys[KeyEvent.VK_LEFT]) x -= speed;
        if (keys[KeyEvent.VK_RIGHT]) x += speed;
    }

    // --- القدرات ---
    public void activateShieldManual() {
        if (isShieldAvailable && !isShieldActive && !isDying) {
            isShieldActive = true;
            isShieldAvailable = false;
            shieldEndTime = System.currentTimeMillis() + 7000;
        }
    }

    public void addShieldInventory() { isShieldAvailable = true; }

    public void activateLaserBeam() {
        if (isLaserAvailable && !isLaserBeamActive && !isDying) {
            isLaserBeamActive = true;
            isLaserAvailable = false;
            laserEndTime = System.currentTimeMillis() + 3500;
        }
    }

    public void refillLaser() { isLaserAvailable = true; }

    public void upgradeWeapon() { if (weaponLevel < 3) weaponLevel++; }

    public void addSpecialAmmo(int amount) {
        specialAmmo += amount;
        if (specialAmmo > 0) specialAttackAvailable = true;
    }

    public void activateSpecialAttack() {
        if (specialAmmo > 0 && !isSpecialAttackActive && !isDying) {
            specialAmmo--;
            if (specialAmmo <= 0) specialAttackAvailable = false;

            isSpecialAttackActive = true;
            specialAttackEndTime = System.currentTimeMillis() + 2000;
            specialAttackUsedOnEnemies = false;
        }
    }

    // --- الرسم (Render) ---
    @Override
    public void render(GL gl, int[] textures) {
        // 1. رسم الليزر لو مفعل
        if (isLaserBeamActive && !isFlyingOff && !isDying) drawLaserBeam(gl);

        int textureToDraw;

        // 2. منطق اختيار الصورة
        if (!isDying) {
            // ============================================================
            // التعديل الجديد: تغيير الصورة بناءً على نسبة الصحة الحالية
            // ============================================================
            float healthPercent = (float) health / (float) MAX_HEALTH;

            if (healthPercent > 0.75f) {
                textureToDraw = textures[1]; // صحة ممتازة (Hero.png)
            } else if (healthPercent > 0.50f) {
                textureToDraw = textures[2]; // خدوش بسيطة (Hero2.png)
            } else if (healthPercent > 0.25f) {
                textureToDraw = textures[3]; // متضرر (Hero3.png)
            } else {
                textureToDraw = textures[4]; // حالة حرجة (Hero4.png)
            }

            // عشان لما أنيميشن الموت يبدأ، يبدأ من آخر صورة كان عليها (اختياري)
            // currentTextureIndex = (healthPercent > 0.75f) ? 1 : ...;
        } else {
            // ============================================================
            // حالة الموت: تدرج الصور (الأنيميشن)
            // ============================================================
            dieFrameCounter++;
            if (dieFrameCounter > dieFrameDelay) {
                dieFrameCounter = 0;

                // التأكد من بداية الأنيميشن بشكل صحيح
                if (currentTextureIndex < 2) currentTextureIndex = 2;

                if (currentTextureIndex < 4) {
                    currentTextureIndex++; // انتقل للصورة التالية
                } else {
                    animationFinished = true; // انتهى العرض واختفى اللاعب
                }
            }

            // حماية عشان مياخدش اندكس غلط
            if (currentTextureIndex < textures.length) {
                textureToDraw = textures[currentTextureIndex];
            } else {
                textureToDraw = textures[4];
            }
        }

        // 3. رسم اللاعب (فقط إذا لم ينته أنيميشن الموت)
        if (!animationFinished) {
            drawTexture(gl, textureToDraw, x, y, width, height);
        }

        // 4. رسم شعلة المحرك (تظهر فقط وهو حي)
        if (isFlyingOff && !isDying) {
            gl.glEnable(GL.GL_BLEND);
            gl.glColor4f(1.0f, 0.5f, 0.0f, 0.8f);
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glVertex2f(x + width/2 - 10, y);
            gl.glVertex2f(x + width/2 + 10, y);
            gl.glVertex2f(x + width/2, y - 40);
            gl.glEnd();
            gl.glDisable(GL.GL_BLEND);
            gl.glColor3f(1,1,1);
        }

        // 5. رسم البار والدرع (يظهروا فقط وهو حي ومش بيطير للنهاية)
        if (!isFlyingOff && !isDying) {
            drawHealthBar(gl);
            if (isShieldActive) drawShield(gl);
        }
    }

    protected void drawTexture(GL gl, int textureId, float x, float y, float w, float h) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glColor3f(1, 1, 1);

        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(x, y + h);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(x + w, y + h);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(x + w, y);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(x, y);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
    }

    // In Player.java
    private void drawHealthBar(GL gl) {
        // 1. إيقاف التكستشر للرسم الهندسي
        gl.glDisable(GL.GL_TEXTURE_2D);

        // 2. إعدادات الحجم (نفس ستايل الأعداء: صغير ورفيع)
        float barTotalWidth = 40f; // عرض ثابت وصغير
        float barHeight = 4f;      // سمك رفيع

        // 3. التمركز (تحت الطيارة بـ 10 بيكسل)
        // بنحسب نقطة البداية عشان يكون في نص عرض اللاعب بالظبط
        float barStartX = x + (width - barTotalWidth) / 2;
        float barStartY = y - 10;

        // 4. حساب النسبة
        float healthPercent = Math.max(0f, Math.min((float) health / MAX_HEALTH, 1f));

        // 5. رسم الخلفية (أحمر غامق)
        gl.glColor3f(0.6f, 0.0f, 0.0f);
        gl.glRectf(barStartX, barStartY, barStartX + barTotalWidth, barStartY + barHeight);

        // 6. رسم الصحة الحالية (أخضر ساطع)
        if (healthPercent > 0) {
            gl.glColor3f(0.0f, 1.0f, 0.0f);
            gl.glRectf(barStartX, barStartY, barStartX + (barTotalWidth * healthPercent), barStartY + barHeight);
        }

        // 7. استعادة الألوان والتكستشر
        gl.glColor3f(1, 1, 1);
        gl.glEnable(GL.GL_TEXTURE_2D);
    }
    private void drawShield(GL gl) {
        gl.glEnable(GL.GL_BLEND); gl.glColor4f(0.0f, 1.0f, 1.0f, 0.4f);
        float cx = x + width/2, cy = y + height/2; float radius = 45;
        gl.glBegin(GL.GL_POLYGON); for (int i = 0; i < 360; i += 20) { double angle = Math.toRadians(i); gl.glVertex2d(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius); } gl.glEnd();
        gl.glDisable(GL.GL_BLEND); gl.glColor3f(1,1,1);
    }
    private void drawLaserBeam(GL gl) {
        // 1. إيقاف نظام الصور مؤقتاً للرسم الهندسي
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);

        float cx = x + width / 2; // منتصف اللاعب
        float yStart = y + height; // بداية الليزر من مقدمة الطائرة
        float yEnd = 600;          // نهاية الشاشة

        // --- الطبقة الخارجية (التوهج - Glow) ---
        // لون سماوي (Cyan) مع شفافية 0.4
        gl.glColor4f(0.0f, 1.0f, 1.0f, 0.4f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(cx - 15, yStart);
        gl.glVertex2f(cx + 15, yStart);
        gl.glVertex2f(cx + 15, yEnd);
        gl.glVertex2f(cx - 15, yEnd);
        gl.glEnd();

        // --- الطبقة الداخلية (القلب - Core) ---
        // لون أبيض ساطع (White) ورفيع
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.9f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(cx - 4, yStart); // أرفع (4 بكسل)
        gl.glVertex2f(cx + 4, yStart);
        gl.glVertex2f(cx + 4, yEnd);
        gl.glVertex2f(cx - 4, yEnd);
        gl.glEnd();

        // 2. إعادة تفعيل نظام الصور واللون الطبيعي لباقي اللعبة
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_BLEND);
        gl.glColor3f(1, 1, 1);
    }
    public Rectangle getLaserBounds() { return new Rectangle((int) (x + width / 2 - 10), (int) (y + height), 20, 600); }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
}