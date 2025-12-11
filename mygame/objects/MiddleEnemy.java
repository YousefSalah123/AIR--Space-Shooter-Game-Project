package mygame.objects;

import javax.media.opengl.GL;

public class MiddleEnemy {
    public float x, y;
    public float radius = 30; // نصف القطر (يستخدم للتصادم والحجم)
    public int health = 100;
    public int maxHealth = 100;

    public float speedX = 3.0f;
    private float targetY = 450;

    // --- متغيرات الموت ---
    public boolean isDying = false;
    public long dyingStartTime = 0;
    public boolean readyToRemove = false;
    private int currentTextureIndex = -1; // لتخزين الصورة الحالية

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

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
        // 1. منطق الموت
        if (isDying) {
            long timePassed = System.currentTimeMillis() - dyingStartTime;
            if (timePassed > 400) { // مدة أطول قليلاً للعدو المتوسط
                readyToRemove = true;
            } else {
                // Enemy 3 Death start index = 59
                if (timePassed < 200) currentTextureIndex = 59;
                else currentTextureIndex = 60;
            }
            return; // لا يتحرك
        }

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

        // تحديد الصورة: إذا كان يموت نستخدم currentTextureIndex، وإلا نستخدم الصورة العادية (20)
        int textureToDraw;
        if (isDying && currentTextureIndex != -1) {
            textureToDraw = (textures.length > currentTextureIndex) ? textures[currentTextureIndex] : textures[20];
        } else {
            textureToDraw = (textures.length > 20) ? textures[20] : textures[5];
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, textureToDraw);

        // التلوين
        if (!isDying) { // نلغي التلوين أثناء الموت لكي تظهر صورة الانفجار بألوانها
            if (level == 1) gl.glColor3f(1.0f, 0.9f, 0.5f);
            else if (level == 2) gl.glColor3f(0.5f, 1.0f, 1.0f);
            else gl.glColor3f(1.0f, 0.5f, 1.0f);
        } else {
            gl.glColor3f(1, 1, 1);
        }

        float drawSize = radius * 2;
        float drawX = x - radius;
        float drawY = y - radius;

        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(drawX, drawY + drawSize);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(drawX + drawSize, drawY + drawSize);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(drawX + drawSize, drawY);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(drawX, drawY);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
        gl.glColor3f(1, 1, 1);

        // رسم البار فقط إذا لم يكن يمت
        // if (!isDying) drawHealthBar(gl);
    }

    public void startDeath() {
        if (!isDying) {
            isDying = true;
            dyingStartTime = System.currentTimeMillis();
        }
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