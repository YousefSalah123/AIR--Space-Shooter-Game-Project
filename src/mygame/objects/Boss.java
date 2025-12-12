package mygame.objects;

import mygame.engine.SoundManager;
import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;

// Represents the main Boss enemy in the game, handling movement,
// health, complex shooting patterns, laser attacks, and death animation.
public class Boss extends GameObject {

    public int health;
    private int maxHealth;
    private int level;
    private float moveSpeed = 2.0f;
    private int direction = 1;

    // Laser logic
    public boolean isFiringLaser = false;
    private long lastLaserTime = 0;
    private long lastShotTime = 0;

    // --- Death and Animation variables ---
    public boolean isDying = false;
    public boolean animationFinished = false;
    private int dieFrameCounter = 0;
    private int currentTextureOffset = 0;

    // Constructor: Initializes the Boss with position and sets up health, size,
    // and speed based on the specified level.
    public Boss(float x, float y, int level) {
        super(x, y, 50, 50);
        this.level = level;

        // Health and dimensions setup based on level
        if (level == 1) {
            maxHealth = 200; width = 150; height = 150; moveSpeed = 2.0f;
        } else if (level == 2) {
            maxHealth = 600; width = 160; height = 160; moveSpeed = 3.0f;
        } else {
            maxHealth = 1200; width = 200; height = 200; moveSpeed = 4.0f;
        }
        this.health = maxHealth;
    }

    // Updates the boss's position, movement pattern (shaking), and laser logic.
    @Override
    public void update() {
        if (isDying) return;

        if (!isFiringLaser) {
            // Horizontal movement speed adjustment based on current health
            float currentSpeed = (health < maxHealth / 2) ? moveSpeed * 1.5f : moveSpeed;
            x += currentSpeed * direction;
            if (x > (800 - width) || x < 0) direction *= -1;

            // Vertical shaking/floating movement
            float shakeAmount = (level == 3) ? 40 : 20;
            y = (600 - height - 50) + (float) Math.sin(System.currentTimeMillis() / 400.0) * shakeAmount;
        } else {
            // Slight jitter during laser firing
            x += (Math.random() * 6) - 3;
        }

        // Only manage laser logic for Level 3 and below 20% health
        if (level == 3 && health < (maxHealth * 0.20)) manageLaserLogic();
        else isFiringLaser = false;
    }

    // Manages the timing for the boss's continuous laser attack (duration and cooldown).
    private void manageLaserLogic() {
        long currentTime = System.currentTimeMillis();
        int laserDuration = 100 + (level * 250);
        int cooldown = 2000 - (level * 100);
        if (isFiringLaser) {
            if (currentTime - lastLaserTime > laserDuration) { isFiringLaser = false; lastLaserTime = currentTime; }
        } else {
            if (currentTime - lastLaserTime > cooldown) { isFiringLaser = true; lastLaserTime = currentTime; }
        }
    }

    // Handles the boss's regular bullet firing logic, varying the pattern by level.
    public void shootLogic(ArrayList<Bullet> bullets, SoundManager soundManager) {
        if (isFiringLaser || isDying) return;

        long currentTime = System.currentTimeMillis();
        // Fire rate increases with levels
        int fireRate = 1200 - (level * 200);

        if (currentTime - lastShotTime > fireRate) {

            // Determine the bullet spawn height (cannon muzzle)
            float spawnY = y + (height * 0.1f);

            // --- Level 1: Two shots (Left and Right) ---
            if (level == 1) {
                float spawnX_Left = x + (width * 0.2f);
                float spawnX_Right = x + (width * 0.8f);

                bullets.add(new Bullet(spawnX_Left, spawnY, 0, -8, true, 6));
                bullets.add(new Bullet(spawnX_Right, spawnY, 0, -8, true, 6));
            }
            // --- Level 2: Four shots (Evenly spaced) ---
            else if (level == 2) {
                // Distribute 4 shots across the boss width (20%, 40%, 60%, 80%)
                bullets.add(new Bullet(x + (width * 0.2f), spawnY, 0, -9, true, 6));
                bullets.add(new Bullet(x + (width * 0.4f), spawnY, 0, -9, true, 6));
                bullets.add(new Bullet(x + (width * 0.6f), spawnY, 0, -9, true, 6));
                bullets.add(new Bullet(x + (width * 0.8f), spawnY, 0, -9, true, 6));
            }
            // --- Level 3: Five shots (Fan pattern) ---
            else {
                float centerX = x + width / 2;

                // Center
                bullets.add(new Bullet(centerX, spawnY, 0, -12, true, 6));

                // Inner angle
                bullets.add(new Bullet(x + (width * 0.3f), spawnY, -2, -10, true, 6));
                bullets.add(new Bullet(x + (width * 0.7f), spawnY, 2, -10, true, 6));

                // Outer angle
                bullets.add(new Bullet(x + (width * 0.1f), spawnY, -4, -9, true, 6));
                bullets.add(new Bullet(x + (width * 0.9f), spawnY, 4, -9, true, 6));
            }

            if (soundManager != null) {
                soundManager.playSound("boss_laser");
            }

            lastShotTime = currentTime;
        }
    }

    // Renders the Boss, including the laser, the boss texture (alive or dying animation), and the health bar.
    @Override
    public void render(GL gl, int[] textures) {
        // 1. Draw Laser (only Level 3 and while alive)
        if (isFiringLaser && level == 3 && !isDying) {
            drawBossLaser(gl);
        }

        // ============================================================
        // 2. Define Boss texture range based on level
        // ============================================================
        int myBossStartIndex;
        int myBossEndIndex;

        if (level == 1) {
            myBossStartIndex = 7;
            myBossEndIndex   = 11;
        } else if (level == 2){
            myBossStartIndex = 12;
            myBossEndIndex   = 18;
        }
        else {
            // Level 3 (based on your code)
            myBossStartIndex = 61;
            myBossEndIndex   = 66;
        }

        int textureIndex;

        // ============================================================
        // 3. Texture Selection Logic (Alive vs. Dead)
        // ============================================================
        if (!isDying) {
            // --- Alive State ---
            // Boss stays on its first (undamaged) image
            textureIndex = textures[myBossStartIndex];
            gl.glColor3f(1, 1, 1);

        } else {
            // --- Dying State (Animation) ---
            int deathSpeed = 5;

            dieFrameCounter++;
            if (dieFrameCounter > deathSpeed) {
                dieFrameCounter = 0;
                currentTextureOffset++;
            }

            int currentAnimIndex = myBossStartIndex + currentTextureOffset;

            // Check for animation end
            if (currentAnimIndex > myBossEndIndex) {
                animationFinished = true;
                textureIndex = textures[myBossEndIndex]; // Stick to the last frame
            } else {
                textureIndex = textures[currentAnimIndex];
            }

            // Color effect (slight red) during explosion
            gl.glColor3f(1.0f, 0.7f, 0.7f);
        }

        // ============================================================
        // 4. Final Drawing
        // ============================================================

        if (!animationFinished) {
            drawTexture(gl, textureIndex, x, y, width, height);
        }

        // Draw Health Bar (only if alive)
        gl.glColor3f(1, 1, 1);
        if (!isDying) {
            drawHealthBar(gl);
        }
    }

    // Draws the boss's health bar (red background, green health fill, white border) above the boss.
    private void drawHealthBar(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);

        float barWidth = width-5;
        float barHeight = 13;
        float barX = x;
        float barY = y + height-27; // Above the boss

        // Background (Red)
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Health (Green)
        float hpPercent = (float) health / maxHealth;
        float currentGreenWidth = barWidth * hpPercent;

        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + currentGreenWidth, barY);
        gl.glVertex2f(barX + currentGreenWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // White Border
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(1.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1, 1, 1);
    }

    // Draws the continuous red laser beam from the boss to the bottom of the screen.
    private void drawBossLaser(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        Rectangle rect = getLaserBounds();
        float topY = rect.y + rect.height;
        float bottomY = 0;

        // Glow
        gl.glColor4f(1.0f, 0.6f, 0.6f, 0.6f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(rect.x - 10, topY);
        gl.glVertex2f(rect.x + rect.width + 10, topY);
        gl.glVertex2f(rect.x + rect.width + 10, bottomY);
        gl.glVertex2f(rect.x - 10, bottomY);
        gl.glEnd();

        // Core
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.9f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(rect.x + 3, topY);
        gl.glVertex2f(rect.x + rect.width - 3, topY);
        gl.glVertex2f(rect.x + rect.width - 3, bottomY);
        gl.glVertex2f(rect.x + 3, bottomY);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_BLEND);
        gl.glColor3f(1, 1, 1);
    }

    // Calculates and returns the bounding box for the active laser beam.
    public Rectangle getLaserBounds() {
        if (!isFiringLaser) {
            return new Rectangle(0, 0, 0, 0);
        }
        int laserWidth = 60;
        int startX = (int)(x + width / 2 - laserWidth / 2);
        return new Rectangle(startX, 0, laserWidth, (int)y);
    }

    // Reduces the boss's health and sets the dying flag if health reaches zero.
    public void takeDamage() {
        if (isDying) return;

        health -= 3;
        if (health <= 0) {
            isDying = true;
            health = 0;
        }
    }
}