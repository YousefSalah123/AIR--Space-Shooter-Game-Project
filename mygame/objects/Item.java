package mygame.objects;

import javax.media.opengl.GL;

public class Item extends GameObject {

    public enum ItemType {HEALTH, RAPID_FIRE, GOLD_COIN}

    private final ItemType type;

    public Item(float x, float y, ItemType type) {
        // عدلت الحجم ليكون مربعاً (50x50) عشان الصورة متتمطش
        super(x, y, 90, 50);
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
                // الاندكس 19 هو heart.png
                textureIndex = textures[19];
                break;

            case GOLD_COIN:
                // الاندكس 24 هو coin.png
                textureIndex = textures[24];
                break;

            case RAPID_FIRE:
                // --- التعديل هنا ---
                // الاندكس 40 هو PowerUp.png (آخر صورة ضفناها في GameListener)
                textureIndex = textures[40];
                break;

            default:
                textureIndex = textures[24];
                break;
        }

        // رسم العنصر باللون الأبيض (عشان يظهر بألوان الصورة الأصلية)
        gl.glColor3f(1, 1, 1);
        drawTexture(gl, textureIndex, x, y, width, height);
    }
}