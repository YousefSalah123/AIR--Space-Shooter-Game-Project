package com.mygame.objects;

import javax.media.opengl.GL;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

public class Player extends GameObject {

    private final float SCREEN_WIDTH = 800;
    private final float SCREEN_HEIGHT = 600;

    // --- الأسلحة ---
    public int weaponLevel = 1;

    // --- نظام الدرع اليدوي (تعديل) ---
    public boolean isShieldActive = false;
    public boolean isShieldAvailable = false; // هل اللاعب يملك درعاً في المخزون؟
    private long shieldEndTime = 0;

    // --- نظام الليزر ---
    public boolean isLaserBeamActive = false;
    public boolean isLaserAvailable = true;
    private long laserEndTime = 0;

    public Player(float x, float y) {
        super(x, y, 50, 50);
        this.speed = 8.0f;
    }

    @Override
    public void update() {
        if (x < 0) x = 0;
        if (x > SCREEN_WIDTH - width) x = SCREEN_WIDTH - width;
        if (y < 0) y = 0;
        if (y > SCREEN_HEIGHT - height) y = SCREEN_HEIGHT - height;

        // إدارة وقت الدرع
        if (isShieldActive && System.currentTimeMillis() > shieldEndTime) {
            isShieldActive = false;
            System.out.println("Shield Deactivated!");
        }

        // إدارة وقت الليزر
        if (isLaserBeamActive && System.currentTimeMillis() > laserEndTime) {
            isLaserBeamActive = false;
            System.out.println("Laser Beam Ended!");
        }
    }

    // --- دوال التحكم الجديدة ---

    // 1. تفعيل الدرع (يدوياً)
    public void activateShieldManual() {
        if (isShieldAvailable && !isShieldActive) {
            isShieldActive = true;
            isShieldAvailable = false; // استهلاك الدرع
            shieldEndTime = System.currentTimeMillis() + 7000; // مدة 7 ثواني
            System.out.println("Shield Activated Manually!");
        } else {
            System.out.println("No Shield Available!");
        }
    }

    // إضافة درع للمخزون (مكافأة)
    public void addShieldInventory() {
        isShieldAvailable = true;
        System.out.println("Shield Added to Inventory (Press X to use)");
    }

    public void activateLaserBeam() {
        if (isLaserAvailable && !isLaserBeamActive) {
            isLaserBeamActive = true;
            isLaserAvailable = false;
            laserEndTime = System.currentTimeMillis() + 3500;
        }
    }

    public void refillLaser() { isLaserAvailable = true; }
    public void upgradeWeapon() { if (weaponLevel < 3) weaponLevel++; }

    // --- الرسم (تعديل سمك الليزر) ---
    @Override
    public void render(GL gl) {
        if (isLaserBeamActive) drawLaserBeam(gl);

        // رسم الطائرة
        gl.glColor3f(0.1f, 0.1f, 0.8f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(x + width/2, y + height);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glEnd();

        // رسم علامة وجود درع في المخزون (دائرة صغيرة زرقاء فوق الطيارة)
        if (isShieldAvailable && !isShieldActive) {
            gl.glColor3f(0, 1, 1);
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex2f(x + width/2 - 5, y - 15);
            gl.glVertex2f(x + width/2 + 5, y - 15);
            gl.glVertex2f(x + width/2 + 5, y - 5);
            gl.glVertex2f(x + width/2 - 5, y - 5);
            gl.glEnd();
        }

        // رسم الدرع النشط
        if (isShieldActive) {
            gl.glColor3f(0.0f, 1.0f, 1.0f);
            gl.glBegin(GL.GL_LINE_LOOP);
            float cx = x + width/2, cy = y + height/2;
            for (int i = 0; i < 360; i += 20) {
                double angle = Math.toRadians(i);
                gl.glVertex2d(cx + Math.cos(angle) * 40, cy + Math.sin(angle) * 40);
            }
            gl.glEnd();
        }
    }

    private void drawLaserBeam(GL gl) {
        gl.glEnable(GL.GL_BLEND);

        // القلب الأبيض (رفيع جداً - 6 بيكسل)
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.9f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x + width/2 - 3, y + height);
        gl.glVertex2f(x + width/2 + 3, y + height);
        gl.glVertex2f(x + width/2 + 3, 600);
        gl.glVertex2f(x + width/2 - 3, 600);
        gl.glEnd();

        // التوهج (أرفع من الأول - 20 بيكسل)
        gl.glColor4f(0.0f, 1.0f, 1.0f, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x + width/2 - 10, y + height);
        gl.glVertex2f(x + width/2 + 10, y + height);
        gl.glVertex2f(x + width/2 + 10, 600);
        gl.glVertex2f(x + width/2 - 10, 600);
        gl.glEnd();

        gl.glDisable(GL.GL_BLEND);
    }

    public Rectangle getLaserBounds() {
        // تعديل التصادم ليتناسب مع العرض الجديد
        return new Rectangle((int)(x + width/2 - 10), (int)(y + height), 20, 600);
    }

    public void handleInput(boolean[] keys) {
        if (keys[KeyEvent.VK_UP]) y += speed;
        if (keys[KeyEvent.VK_DOWN]) y -= speed;
        if (keys[KeyEvent.VK_LEFT]) x -= speed;
        if (keys[KeyEvent.VK_RIGHT]) x += speed;
    }
}