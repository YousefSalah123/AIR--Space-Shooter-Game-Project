package com.mygame.objects;

import java.awt.Rectangle;
import java.util.ArrayList;
import javax.media.opengl.GL;

public class Boss extends GameObject {

    private int health;
    private int maxHealth;
    private int level;

    // إعدادات الحركة
    private float moveSpeed = 2.0f;
    private int direction = 1;

    // إعدادات الليزر القاتل
    public boolean isFiringLaser = false;
    private long lastLaserTime = 0;
    private boolean isLaserCoolingDown = false;

    // إعدادات الرصاص
    private long lastShotTime = 0;

    public Boss(float x, float y, int level) {
        super(x, y, 50, 50);
        this.level = level;

        // --- ضبط الصعوبة والحجم بناءً على المستوى ---
        if (level == 1) {
            this.maxHealth = 200;
            this.width = 80;
            this.height = 80;
            this.moveSpeed = 2.0f;
        }
        else if (level == 2) {
            this.maxHealth = 600;
            this.width = 110;
            this.height = 110;
            this.moveSpeed = 3.0f;
        }
        else {
            this.maxHealth = 1200;
            this.width = 150;
            this.height = 150;
            this.moveSpeed = 4.0f;
        }

        this.health = maxHealth;
    }

    @Override
    public void update() {
        // --- 1. الحركة ---
        if (!isFiringLaser) {
            // لو دمه نزل للنص، سرعته بتزيد (Enrage)
            float currentSpeed = (health < maxHealth / 2) ? moveSpeed * 1.5f : moveSpeed;
            x += currentSpeed * direction;

            if (x > (800 - width) || x < 0) direction *= -1;

            // حركة اهتزازية
            float shakeAmount = (level == 3) ? 40 : 20;
            y = (600 - height - 50) + (float) Math.sin(System.currentTimeMillis() / 400.0) * shakeAmount;
        } else {
            // اهتزاز أثناء ضرب الليزر
            x += (Math.random() * 6) - 3;
        }

        // --- 2. تفعيل الليزر (للزعيم رقم 3 فقط) ---
        // التعديل هنا: ضيفنا شرط (level == 3)
        if (level == 3 && health < (maxHealth * 0.20)) {
            manageLaserLogic();
        } else {
            isFiringLaser = false;
        }
    }

    private void manageLaserLogic() {
        long currentTime = System.currentTimeMillis();
        int laserDuration = 100 + (level * 250);
        int cooldown = 2000 - (level * 100);

        if (isFiringLaser) {
            if (currentTime - lastLaserTime > laserDuration) {
                isFiringLaser = false;
                isLaserCoolingDown = true;
                lastLaserTime = currentTime;
            }
        } else {
            if (currentTime - lastLaserTime > cooldown) {
                isFiringLaser = true;
                isLaserCoolingDown = false;
                lastLaserTime = currentTime;
                System.out.println("WARNING: BOSS " + level + " LASER!");
            }
        }
    }

    public void shootLogic(ArrayList<Bullet> bullets) {
        if (isFiringLaser) return;

        long currentTime = System.currentTimeMillis();
        int fireRate = 1000 - (level * 100);

        if (currentTime - lastShotTime > fireRate) {
            // 1. طلقة المنتصف
            bullets.add(new Bullet(x + width/2, y, 0, -10, true));

            // 2. طلقات جانبية
            if (level >= 2) {
                bullets.add(new Bullet(x, y, -3, -8, true));
                bullets.add(new Bullet(x + width, y, 3, -8, true));
            }

            lastShotTime = currentTime;
        }
    }

    @Override
    public void render(GL gl) {
        // --- رسم الليزر (للزعيم 3 فقط) ---
        if (isFiringLaser && level == 3) {
            drawBossLaser(gl);
        }

        // --- الألوان حسب حالة الدم (للجميع) ---
        if (health < maxHealth * 0.30) gl.glColor3f(1.0f, 0.0f, 0.0f); // أحمر
        else if (health < maxHealth * 0.50) gl.glColor3f(1.0f, 0.5f, 0.0f); // برتقالي
        else {
            if (level == 1) gl.glColor3f(0.0f, 0.8f, 0.0f);      // أخضر
            else if (level == 2) gl.glColor3f(0.0f, 0.0f, 1.0f); // أزرق
            else gl.glColor3f(0.6f, 0.0f, 0.8f);                 // بنفسجي
        }

        // رسم الجسم
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        // رسم العيون
        drawEyes(gl);

        drawHealthBar(gl);
    }

    private void drawEyes(GL gl) {
        gl.glColor3f(0, 0, 0); // أسود

        float eyeSize = width * 0.2f;
        float eyeY = y + (height * 0.4f);

        // عين يسرى
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x + (width*0.2f), eyeY);
        gl.glVertex2f(x + (width*0.2f) + eyeSize, eyeY);
        gl.glVertex2f(x + (width*0.2f) + eyeSize, eyeY + eyeSize);
        gl.glVertex2f(x + (width*0.2f), eyeY + eyeSize);

        // عين يمنى
        gl.glVertex2f(x + width - (width*0.2f) - eyeSize, eyeY);
        gl.glVertex2f(x + width - (width*0.2f), eyeY);
        gl.glVertex2f(x + width - (width*0.2f), eyeY + eyeSize);
        gl.glVertex2f(x + width - (width*0.2f) - eyeSize, eyeY + eyeSize);
        gl.glEnd();

        // --- الحدقة الحمراء ---
        // تظهر للجميع لو الدم أقل من 30% (غضب) أو لو بيضرب ليزر
        boolean isAngry = (health < maxHealth * 0.30) || isFiringLaser;

        if (isAngry) {
            gl.glColor3f(1, 0, 0);
            float pupilSize = eyeSize / 2;
            gl.glBegin(GL.GL_QUADS);
            // يسار
            gl.glVertex2f(x + (width*0.2f) + pupilSize/2, eyeY + pupilSize/2);
            gl.glVertex2f(x + (width*0.2f) + pupilSize*1.5f, eyeY + pupilSize/2);
            gl.glVertex2f(x + (width*0.2f) + pupilSize*1.5f, eyeY + pupilSize*1.5f);
            gl.glVertex2f(x + (width*0.2f) + pupilSize/2, eyeY + pupilSize*1.5f);
            // يمين
            gl.glVertex2f(x + width - (width*0.2f) - pupilSize*1.5f, eyeY + pupilSize/2);
            gl.glVertex2f(x + width - (width*0.2f) - pupilSize/2, eyeY + pupilSize/2);
            gl.glVertex2f(x + width - (width*0.2f) - pupilSize/2, eyeY + pupilSize*1.5f);
            gl.glVertex2f(x + width - (width*0.2f) - pupilSize*1.5f, eyeY + pupilSize*1.5f);
            gl.glEnd();
        }
    }

    private void drawBossLaser(GL gl) {
        float laserWidth = 20 + (level * 2);

        gl.glEnable(GL.GL_BLEND);
        // القلب الأبيض
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.9f);
        gl.glBegin(GL.GL_QUADS);
        float laserX = x + width/2;
        gl.glVertex2f(laserX - (laserWidth/3), y);
        gl.glVertex2f(laserX + (laserWidth/3), y);
        gl.glVertex2f(laserX + (laserWidth/3), 0);
        gl.glVertex2f(laserX - (laserWidth/3), 0);
        gl.glEnd();

        // التوهج
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.6f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(laserX - laserWidth, y);
        gl.glVertex2f(laserX + laserWidth, y);
        gl.glVertex2f(laserX + laserWidth, 0);
        gl.glVertex2f(laserX - laserWidth, 0);
        gl.glEnd();
        gl.glDisable(GL.GL_BLEND);
    }

    private void drawHealthBar(GL gl) {
        float barWidth = width * 1.2f;
        float barX = x - (width * 0.1f);
        float healthPercent = (float) health / maxHealth;

        gl.glColor3f(0.5f, 0.5f, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, y + height + 10);
        gl.glVertex2f(barX + barWidth, y + height + 10);
        gl.glVertex2f(barX + barWidth, y + height + 15);
        gl.glVertex2f(barX, y + height + 15);
        gl.glEnd();

        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, y + height + 10);
        gl.glVertex2f(barX + (barWidth * healthPercent), y + height + 10);
        gl.glVertex2f(barX + (barWidth * healthPercent), y + height + 15);
        gl.glVertex2f(barX, y + height + 15);
        gl.glEnd();
    }

    public Rectangle getLaserBounds() {
        if (!isFiringLaser) return new Rectangle(0,0,0,0);
        float laserWidth = 20 + (level * 10);
        return new Rectangle((int)(x + width/2 - laserWidth), 0, (int)(laserWidth * 2), (int)y);
    }

    public void takeDamage() {
        int damageAmount = 7; // قيمة ثابتة لكل المستويات كما طلبت
        health -= damageAmount;
        if (health <= 0) {
            setAlive(false);
        }
    }

    public int getHealth() { return health; }
}