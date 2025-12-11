package mygame.objects;

import javax.media.opengl.GL;

public class Enemy extends GameObject {

    public enum TypesOfEnemies {
        STRAIGHT, CHASER, SQUAD_V, SQUAD_ENTER_LEFT, SQUAD_ENTER_RIGHT, CIRCLE_PATH
    }

    private TypesOfEnemies type;
    private Player playerTarget;
    private float startX, startY;
    private int timeAlive = 0;

    // متغيرات الرسم
    private int textureIndex;          // الصورة الحالية
    private int originalTextureIndex;  // الصورة الأصلية (عشان نعرف نوعه)

    // --- متغيرات الصحة والموت ---
    public int health = 100;
    public int maxHealth = 100;

    public boolean isDying = false;       // هل هو بيموت؟
    public long dyingStartTime = 0;       // متى بدأ الموت
    public boolean readyToRemove = false; // هل خلص الأنيميشن وجاهز للحذف؟

    public Enemy(float x, float y, float size, TypesOfEnemies type, Player player, int textureIndex) {
        super(x, y, size, size);
        this.type = type;
        this.playerTarget = player;
        this.startX = x;
        this.startY = y;
        this.speed = 3.0f;

        this.textureIndex = textureIndex;
        this.originalTextureIndex = textureIndex; // حفظ النوع الأصلي

        this.health = 100;
        this.maxHealth = 100;
    }

    @Override
    public void update() {
        // ... (Keep existing update logic exactly as it is) ...
        // 1. لو العدو بيموت، شغل أنيميشن الموت وماتحركوش
        if (isDying) {
            updateDeathAnimation();
            return;
        }
        timeAlive++;
        switch (type) {
            case STRAIGHT: y -= speed; break;
            case SQUAD_V: y -= speed; break;
            case CHASER:
                y -= speed;
                if (playerTarget != null) {
                    if (x < playerTarget.getX()) x += 2.0f;
                    if (x > playerTarget.getX()) x -= 2.0f;
                }
                break;
            case SQUAD_ENTER_LEFT:
                x += 3.0f;
                y = startY - (timeAlive * 2.5f) + (float)(Math.sin(timeAlive * 0.02) * 30);
                break;
            case SQUAD_ENTER_RIGHT:
                x -= 3.0f;
                y = startY - (timeAlive * 2.5f) + (float)(Math.sin(timeAlive * 0.02) * 30);
                break;
            case CIRCLE_PATH:
                y -= 2.0f;
                x = startX + (float)(Math.sin(timeAlive * 0.02) * 80);
                break;
        }

        if (y < -100 || x < -200 || x > 1000) setAlive(false);
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    // We will handle rendering in GameManager to ensure the bar is drawn ON TOP
    @Override
    public void render(GL gl, int[] textures) {
        drawTexture(gl, textures[textureIndex], x, y, width, height);
    }

    private void updateDeathAnimation() {
        long timePassed = System.currentTimeMillis() - dyingStartTime;

        // المدة الكلية للأنيميشن (مثلاً 300 مللي ثانية)
        if (timePassed > 300) {
            readyToRemove = true; // جاهز للحذف من الـ GameManager
        } else {
            // تحديد الـ Index المبدئي لصور الموت بناءً على نوع العدو
            int deathStartIndex = 55; // افتراضي (Enemy 1)

            if (originalTextureIndex == 21) deathStartIndex = 55;      // Enemy 1
            else if (originalTextureIndex == 22) deathStartIndex = 57; // Enemy 2
            else if (originalTextureIndex == 20 || originalTextureIndex == 23) deathStartIndex = 59; // Enemy 3

            // التبديل بين الصورتين (كل 150 مللي ثانية)
            if (timePassed < 150) {
                textureIndex = deathStartIndex;     // الصورة الأولى
            } else {
                textureIndex = deathStartIndex + 1; // الصورة الثانية
            }
        }
    }

    // دالة لبدء الموت
    public void startDeath() {
        if (!isDying) {
            isDying = true;
            dyingStartTime = System.currentTimeMillis();
        }
    }
    // Getters
    public int getTextureIndex() { return textureIndex; }
    public boolean readyToFire() {
        if (isDying) return false; // لا يطلق النار وهو يموت
        return Math.random() < 0.003; }
    public TypesOfEnemies getType() { return type; }
}