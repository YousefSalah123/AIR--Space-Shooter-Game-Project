package mygame.objects;

import javax.media.opengl.GL;
import java.awt.*;

// Represents a mid-level enemy (Mini-Boss) in the game, characterized by
// circular/side-to-side movement and health management.
public class MiddleEnemy {
    public float x, y;
    public float radius = 30; // Radius (used for collision and size)
    public int health = 100;
    public int maxHealth = 100;

    public float speedX = 3.0f;
    private float targetY = 450;

    // --- Death variables ---
    public boolean isDying = false;
    public long dyingStartTime = 0;
    public boolean readyToRemove = false;
    private int currentTextureIndex = -1; // To store the current texture index

    // Returns the X-coordinate.
    public float getX() {
        return x;
    }

    // Sets the X-coordinate.
    public void setX(float x) {
        this.x = x;
    }

    // Returns the Y-coordinate.
    public float getY() {
        return y;
    }

    // Sets the Y-coordinate.
    public void setY(float y) {
        this.y = y;
    }

    // Returns the current health.
    public int getHealth() {
        return health;
    }

    // Sets the current health.
    public void setHealth(int health) {
        this.health = health;
    }

    public long lastShotTime = 0;
    public long shotDelay = 1000;
    public int type; // 1: Fan pattern, 2: Chaser
    public int level;

    // Constructor: Initializes the enemy with starting position, type, and level,
    // adjusting its size based on the level.
    public MiddleEnemy(float startX, float startY, int type, int level) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        this.level = level;

        // Adjust size based on level
        if (level == 3) {
            this.radius = 45; // Larger size for Level 3
        } else {
            this.radius = 30; // Normal size for Level 1 and 2
        }

        this.maxHealth = health;
    }

    // Updates the enemy's movement logic (initial descent, then side-to-side)
    // and handles the death animation sequence.
    public void update(int screenWidth) {
        // 1. Death logic
        if (isDying) {
            long timePassed = System.currentTimeMillis() - dyingStartTime;
            if (timePassed > 400) { // Slightly longer duration for the middle enemy
                readyToRemove = true;
            } else {
                // Enemy 3 Death start index = 59
                if (timePassed < 200) currentTextureIndex = 59;
                else currentTextureIndex = 60;
            }
            return; // Stop movement
        }

        if (y > targetY) {
            y -= 2.0f; // Initial descent
        } else {
            x += speedX; // Horizontal movement
            if (x > screenWidth - (radius + 20) || x < (radius + 20)) {
                speedX = -speedX;
            }
        }
    }

    // Renders the MiddleEnemy, selecting the appropriate texture for alive or dying state,
    // and applies level-specific color tints when alive.
    public void render(GL gl, int[] textures) {
        gl.glEnable(GL.GL_BLEND);

        // Determine texture: if dying, use currentTextureIndex, otherwise use the default texture (20)
        int textureToDraw;
        if (isDying && currentTextureIndex != -1) {
            textureToDraw = (textures.length > currentTextureIndex) ? textures[currentTextureIndex] : textures[20];
        } else {
            textureToDraw = (textures.length > 20) ? textures[20] : textures[5];
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, textureToDraw);

        // Coloring
        if (!isDying) { // Disable coloring during death to show the explosion image colors
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

        // Draw Health Bar (if not dying)
        // if (!isDying) drawHealthBar(gl);
    }

    // Initiates the death sequence by setting the flag and recording the start time.
    public void startDeath() {
        if (!isDying) {
            isDying = true;
            dyingStartTime = System.currentTimeMillis();
        }
    }
}