package com.mygame.objects;

import javax.media.opengl.GL;

public class Bullet extends GameObject {

    private final boolean isEnemyBullet;
    private float speedX = 0;
    private int textureIndex;

    public Bullet(float x, float y, float speedX, float speedY, boolean isEnemyBullet, int textureIndex) {
        super(x, y,
                isEnemyBullet ? 15 : 30,  // العرض
                isEnemyBullet ? 25 : 50   // الطول
        );

        this.speedX = speedX;
        this.speed = speedY; // هنا speed تعبر عن speedY
        this.isEnemyBullet = isEnemyBullet;
        this.textureIndex = textureIndex;
    }

    @Override
    public void update() {
        y += speed;
        x += speedX;

        // التحقق من الخروج عن حدود الشاشة (مع هامش صغير)
        if (y > 700 || y < -50 || x < -50 || x > 850) {
            setAlive(false);
        }
    }

    @Override
    public void render(GL gl, int[] textures) {
        // حماية: لا نرسم إذا كانت الطلقة ميتة
        if (!isAlive) return;

        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[textureIndex]);

        gl.glPushMatrix();

        // 1. نقل نقطة الرسم لمنتصف الطلقة (Pivot Point)
        // هذا ضروري جداً لكي تدور الطلقة حول مركزها
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        gl.glTranslated(centerX, centerY, 0);

        // 2. حساب زاوية الدوران ديناميكياً
        // Math.atan2(y, x) تحسب الزاوية بالراديان بناءً على متجه الحركة
        // نستخدم speed (التي تمثل Y) و speedX
        double angleRad = Math.atan2(speed, speedX);
        double angleDeg = Math.toDegrees(angleRad);

        // 3. تطبيق الدوران
        // نطرح 90 درجة لأن الزاوية 0 في الرياضيات تشير لليمين،
        // بينما صور الطلقات عادة ما تكون مرسومة وهي تشير للأعلى.
        // (إذا كانت صورك مقلوبة، جرب +90 بدلاً من -90)
        gl.glRotated(angleDeg - 90, 0, 0, 1);

        // 4. رسم الطلقة
        // بما أننا قمنا بـ Translate للمنتصف، نرسم المربع حول نقطة (0,0) الجديدة
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2d(-width / 2, -height / 2);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2d(width / 2, -height / 2);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2d(width / 2, height / 2);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2d(-width / 2, height / 2);
        gl.glEnd();

        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    public boolean isEnemyBullet() { return isEnemyBullet; }
}