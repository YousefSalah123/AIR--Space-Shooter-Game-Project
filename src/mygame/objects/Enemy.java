package mygame.objects;

import javax.media.opengl.GL;

// Represents a standard enemy ship in the game, handling different movement patterns,
// health, and a simple death animation.
public class Enemy extends GameObject {

    // Enum defining the different types of movement patterns for enemies.
    public enum TypesOfEnemies {
        STRAIGHT, CHASER, SQUAD_V, SQUAD_ENTER_LEFT, SQUAD_ENTER_RIGHT, CIRCLE_PATH
    }

    private TypesOfEnemies type;
    private Player playerTarget;
    private float startX, startY;
    private int timeAlive = 0;

    // Drawing variables
    private int textureIndex;          // Current texture index for rendering
    private int originalTextureIndex;  // Original texture index (to determine enemy type for death animation)

    // --- Health and Death variables ---
    public int health = 100;
    public int maxHealth = 100;

    public boolean isDying = false;       // Is the enemy dying?
    public long dyingStartTime = 0;       // When did the death sequence start?
    public boolean readyToRemove = false; // Is the animation finished and ready for cleanup?

    // Constructor: Initializes the enemy with position, size, type, player reference, and texture.
    public Enemy(float x, float y, float size, TypesOfEnemies type, Player player, int textureIndex) {
        super(x, y, size, size);
        this.type = type;
        this.playerTarget = player;
        this.startX = x;
        this.startY = y;
        this.speed = 3.0f;

        this.textureIndex = textureIndex;
        this.originalTextureIndex = textureIndex; // Save the original type index

        this.health = 100;
        this.maxHealth = 100;
    }

    // Updates the enemy's state, primarily handling movement based on its type
    // or triggering the death animation update if dying.
    @Override
    public void update() {
        // 1. If the enemy is dying, run the death animation and stop movement
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

    // Renders the enemy's current texture.
    // The health bar rendering is often handled externally (e.g., in GameManager)
    // to ensure it draws on top of all enemies.
    @Override
    public void render(GL gl, int[] textures) {
        drawTexture(gl, textures[textureIndex], x, y, width, height);
    }

    // Updates the texture index to create a simple frame-based death animation.
    private void updateDeathAnimation() {
        long timePassed = System.currentTimeMillis() - dyingStartTime;

        // Total animation duration (e.g., 300 milliseconds)
        if (timePassed > 300) {
            readyToRemove = true; // Ready for deletion from GameManager
        } else {
            // Determine the starting index for death textures based on the enemy's original type
            int deathStartIndex = 55; // Default (Enemy 1)

            if (originalTextureIndex == 21) deathStartIndex = 55;      // Enemy 1
            else if (originalTextureIndex == 22) deathStartIndex = 57; // Enemy 2
            else if (originalTextureIndex == 20 || originalTextureIndex == 23) deathStartIndex = 59; // Enemy 3

            // Alternate between two frames (every 150 milliseconds)
            if (timePassed < 150) {
                textureIndex = deathStartIndex;     // First frame
            } else {
                textureIndex = deathStartIndex + 1; // Second frame
            }
        }
    }

    // Initiates the death sequence by setting the dying flag and recording the start time.
    public void startDeath() {
        if (!isDying) {
            isDying = true;
            dyingStartTime = System.currentTimeMillis();
        }
    }

    // Determines if the enemy is ready to fire based on a small random chance,
    // provided it is not currently dying.
    public boolean readyToFire() {
        if (isDying) return false; // Cannot fire while dying
        return Math.random() < 0.003; }

    // Returns the movement type of the enemy.
    public TypesOfEnemies getType() { return type; }
}