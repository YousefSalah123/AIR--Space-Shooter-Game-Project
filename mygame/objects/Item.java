package com.mygame.objects;

import javax.media.opengl.GL;

public class Item extends GameObject {

    public enum ItemType {HEALTH, RAPID_FIRE, GOLD_COIN}

    private final ItemType type;

    public Item(float x, float y, ItemType type) {
        super(x, y, 70, 40); // حجم العنصر
        this.type = type;
        this.speed = 3.0f;
    }

    public ItemType getType() {
        return type;
    }

    @Override
    public void update() {
        y -= speed;
        // يختفي عند الخروج من الشاشة
        if (y < -50) isAlive = false;
    }

    @Override
    public void render(GL gl, int[] textures) {
        int textureIndex;

        // اختيار الصورة بناءً على نوع العنصر
        switch (type) {
            case HEALTH:
                // الاندكس 19 هو heart.png حسب GameListener
                textureIndex = textures[19];
                break;

            case GOLD_COIN:
                // الاندكس 24 هو coin.png حسب GameListener
                textureIndex = textures[24];
                break;

            case RAPID_FIRE:
                // بما أنه لا توجد صورة محددة للسرعة في القائمة حالياً
                // سنستخدم صورة العملة مؤقتاً أو يمكنك إضافة صورة "lightning.png" لاحقاً
                textureIndex = textures[24];
                break;

            default:
                textureIndex = textures[24];
                break;
        }

        // رسم العنصر باستخدام دالة الرسم الموجودة في GameObject
        // يمكنك تغيير اللون قليلاً لتمييز الـ Rapid Fire إذا كان يستخدم نفس صورة العملة
        if (type == ItemType.RAPID_FIRE) {
            gl.glColor3f(0.5f, 0.5f, 1.0f); // ميل للأزرق
        } else {
            gl.glColor3f(1, 1, 1); // لون طبيعي
        }

        drawTexture(gl, textureIndex, x, y, width, height);

        // إعادة اللون للأبيض
        gl.glColor3f(1, 1, 1);
    }
}