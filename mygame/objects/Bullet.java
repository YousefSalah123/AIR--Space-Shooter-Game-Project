package com.mygame.objects;

import javax.media.opengl.GL;

public class Bullet extends GameObject {

    private final boolean isEnemyBullet;
    private float speedX = 0;
    private int textureIndex; // <-- لازم المتغير ده يكون موجود

    // الكونستركتور لازم يستقبل الـ textureIndex
    public Bullet(float x, float y, float speedX, float speedY, boolean isEnemyBullet, int textureIndex) {
        super(x, y,
                isEnemyBullet ? 15 : 30,  // العرض: لو عدو 15، لو لاعب 30 (أعرض)
                isEnemyBullet ? 25 : 50   // الطول: لو عدو 25، لو لاعب 50 (أطول)
        );

        this.speedX = speedX;
        this.speed = speedY;
        this.isEnemyBullet = isEnemyBullet;
        this.textureIndex = textureIndex;
    }


    @Override
    public void update() {
        y += speed;
        x += speedX;
        if (y > 700 || y < -50 || x < -50 || x > 850) {
            setAlive(false);
        }
    }

    @Override
    public void render(GL gl, int[] textures) {
        gl.glEnable(GL.GL_BLEND);
        // استخدام رقم الصورة المخزن
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[textureIndex]);

        gl.glPushMatrix(); // 1. حفظ الإحداثيات الحالية

        // 2. نقل نقطة الرسم لمنتصف الرصاصة (عشان لما نلفها، تلف حوالين نفسها مش تطير بعيد)
        // بنستخدم x و y الحالية + نصف العرض ونصف الطول
        gl.glTranslated(x + width / 2, y + height / 2, 0);

        // 3. لو الرصاصة دي "طلقة عدو"، لفها 180 درجة
        if (isEnemyBullet) {
            gl.glRotated(180, 0, 0, 1); // الدوران حول محور Z
        }

        // 4. رسم الرصاصة
        // ملحوظة مهمة: بما إننا عملنا Translate للمنتصف، يبقى لازم نرسم
        // بحيث يكون (0,0) هو السنتر. يعني نبدأ من السالب للموجب.
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2d(-width / 2, -height / 2);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2d(width / 2, -height / 2);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2d(width / 2, height / 2);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2d(-width / 2, height / 2);
        gl.glEnd();

        gl.glPopMatrix(); // 5. استرجاع الإحداثيات عشان باقي اللعبة تترسم صح
        gl.glDisable(GL.GL_BLEND);
    }

    public boolean isEnemyBullet() { return isEnemyBullet; }
}