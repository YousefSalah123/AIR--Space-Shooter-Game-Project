package mygame.objects;
import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;

/**
 * Boss class represents the main enemy boss in the game.
 * Each boss has multiple levels with increasing size, health, speed, and attacks.
 * Handles movement, shooting, laser attacks, health bar, and visual effects (eyes, rage state).
 */
public class Boss extends GameObject {

    private int health;        // Current health
    private int maxHealth;     // Maximum health
    private int level;         // Boss level (difficulty scaling)

    // --- Movement Settings ---
    private float moveSpeed = 2.0f;
    private int direction = 1; // Horizontal movement direction

    // --- Laser Attack ---
    public boolean isFiringLaser = false; // Is boss firing laser?
    private long lastLaserTime = 0;       // Tracks laser activation
    private boolean isLaserCoolingDown = false;

    // --- Bullet Shooting ---
    private long lastShotTime = 0;        // Last time bullets were fired

    /**
     * Constructor initializes boss with position and level.
     * Boss properties (size, health, speed) scale with level.
     */
    public Boss(float x, float y, int level) {
        super(x, y, 50, 50);
        this.level = level;

        // Set boss attributes based on level
        if (level == 1) {
            this.maxHealth = 200;
            this.width = 80;
            this.height = 80;
            this.moveSpeed = 2.0f;
        } else if (level == 2) {
            this.maxHealth = 600;
            this.width = 110;
            this.height = 110;
            this.moveSpeed = 3.0f;
        } else {
            this.maxHealth = 1200;
            this.width = 150;
            this.height = 150;
            this.moveSpeed = 4.0f;
        }

        this.health = maxHealth;
    }

    /**
     * Update method handles boss logic every frame:
     * - Horizontal movement (bouncing left/right)
     * - Vertical shake (for level 3 boss)
     * - Laser activation logic
     * - Rage behavior if health is low
     */
    @Override
    public void update() {
        // --- 1. Movement ---
        if (!isFiringLaser) {
            float currentSpeed = (health < maxHealth / 2) ? moveSpeed * 1.5f : moveSpeed; // Enrage if low HP
            x += currentSpeed * direction;

            // Bounce from screen edges
            if (x > (800 - width) || x < 0) direction *= -1;

            // Shake vertically
            float shakeAmount = (level == 3) ? 40 : 20;
            y = (600 - height - 50) + (float) Math.sin(System.currentTimeMillis() / 400.0) * shakeAmount;
        } else {
            // Slight random shake while firing laser
            x += (Math.random() * 6) - 3;
        }

        // --- 2. Laser activation (level 3 boss only) ---
        if (level == 3 && health < (maxHealth * 0.20)) {
            manageLaserLogic();
        } else {
            isFiringLaser = false;
        }
    }

    /**
     * Handles laser firing logic, duration, and cooldown.
     * Laser lasts for a set time, then enters cooldown before firing again.
     */
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

    /**
     * Boss shooting logic for bullets.
     * Level determines the number of bullets and firing rate.
     * Boss does not shoot while laser is active.
     */
    public void shootLogic(ArrayList<Bullet> bullets) {
        if (isFiringLaser) return;

        long currentTime = System.currentTimeMillis();
        int fireRate = 1000 - (level * 100);

        if (currentTime - lastShotTime > fireRate) {
            bullets.add(new Bullet(x + width / 2, y, 0, -10, true)); // Middle shot
            if (level >= 2) { // Side shots for higher levels
                bullets.add(new Bullet(x, y, -3, -8, true));
                bullets.add(new Bullet(x + width, y, 3, -8, true));
            }
            lastShotTime = currentTime;
        }
    }

    /**
     * Renders boss on screen, including:
     * - Body color based on health and level
     * - Eyes and pupils (red when angry or laser firing)
     * - Health bar above boss
     * - Laser beam (for level 3)
     */
    @Override
    public void render(GL gl) {
        if (isFiringLaser && level == 3) drawBossLaser(gl);

        // Change color based on health
        if (health < maxHealth * 0.30) gl.glColor3f(1.0f, 0.0f, 0.0f);       // Red
        else if (health < maxHealth * 0.50) gl.glColor3f(1.0f, 0.5f, 0.0f);  // Orange
        else {
            if (level == 1) gl.glColor3f(0.0f, 0.8f, 0.0f);      // Green
            else if (level == 2) gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
            else gl.glColor3f(0.6f, 0.0f, 0.8f);                 // Purple
        }

        // Draw boss body
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        drawEyes(gl);
        drawHealthBar(gl);
    }

    /**
     * Draws boss eyes, changing pupil color when angry or firing laser
     */
    private void drawEyes(GL gl) {
        gl.glColor3f(0, 0, 0); // Base eye color black
        float eyeSize = width * 0.2f;
        float eyeY = y + (height * 0.4f);

        // Draw left and right eyes
        gl.glBegin(GL.GL_QUADS);
        // Left eye
        gl.glVertex2f(x + (width * 0.2f), eyeY);
        gl.glVertex2f(x + (width * 0.2f) + eyeSize, eyeY);
        gl.glVertex2f(x + (width * 0.2f) + eyeSize, eyeY + eyeSize);
        gl.glVertex2f(x + (width * 0.2f), eyeY + eyeSize);
        // Right eye
        gl.glVertex2f(x + width - (width * 0.2f) - eyeSize, eyeY);
        gl.glVertex2f(x + width - (width * 0.2f), eyeY);
        gl.glVertex2f(x + width - (width * 0.2f), eyeY + eyeSize);
        gl.glVertex2f(x + width - (width * 0.2f) - eyeSize, eyeY + eyeSize);
        gl.glEnd();

        // Red pupils when angry or firing laser
        boolean isAngry = (health < maxHealth * 0.30) || isFiringLaser;
        if (isAngry) {
            gl.glColor3f(1, 0, 0);
            float pupilSize = eyeSize / 2;
            gl.glBegin(GL.GL_QUADS);
            // Left pupil
            gl.glVertex2f(x + (width * 0.2f) + pupilSize / 2, eyeY + pupilSize / 2);
            gl.glVertex2f(x + (width * 0.2f) + pupilSize * 1.5f, eyeY + pupilSize / 2);
            gl.glVertex2f(x + (width * 0.2f) + pupilSize * 1.5f, eyeY + pupilSize * 1.5f);
            gl.glVertex2f(x + (width * 0.2f) + pupilSize / 2, eyeY + pupilSize * 1.5f);
            // Right pupil
            gl.glVertex2f(x + width - (width * 0.2f) - pupilSize * 1.5f, eyeY + pupilSize / 2);
            gl.glVertex2f(x + width - (width * 0.2f) - pupilSize / 2, eyeY + pupilSize / 2);
            gl.glVertex2f(x + width - (width * 0.2f) - pupilSize / 2, eyeY + pupilSize * 1.5f);
            gl.glVertex2f(x + width - (width * 0.2f) - pupilSize * 1.5f, eyeY + pupilSize * 1.5f);
            gl.glEnd();
        }
    }

    /**
     * Draws the boss laser beam for level 3 boss
     */
    private void drawBossLaser(GL gl) {
        float laserWidth = 20 + (level * 2);

        gl.glEnable(GL.GL_BLEND);
        // Core white beam
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.9f);
        gl.glBegin(GL.GL_QUADS);
        float laserX = x + width / 2;
        gl.glVertex2f(laserX - (laserWidth / 3), y);
        gl.glVertex2f(laserX + (laserWidth / 3), y);
        gl.glVertex2f(laserX + (laserWidth / 3), 0);
        gl.glVertex2f(laserX - (laserWidth / 3), 0);
        gl.glEnd();

        // Glow red beam
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.6f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(laserX - laserWidth, y);
        gl.glVertex2f(laserX + laserWidth, y);
        gl.glVertex2f(laserX + laserWidth, 0);
        gl.glVertex2f(laserX - laserWidth, 0);
        gl.glEnd();
        gl.glDisable(GL.GL_BLEND);
    }

    /**
     * Draws boss health bar above boss
     */
    private void drawHealthBar(GL gl) {
        float barWidth = width * 1.2f;
        float barX = x - (width * 0.1f);
        float healthPercent = (float) health / maxHealth;

        // Background gray
        gl.glColor3f(0.5f, 0.5f, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, y + height + 10);
        gl.glVertex2f(barX + barWidth, y + height + 10);
        gl.glVertex2f(barX + barWidth, y + height + 15);
        gl.glVertex2f(barX, y + height + 15);
        gl.glEnd();

        // Green foreground
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, y + height + 10);
        gl.glVertex2f(barX + (barWidth * healthPercent), y + height + 10);
        gl.glVertex2f(barX + (barWidth * healthPercent), y + height + 15);
        gl.glVertex2f(barX, y + height + 15);
        gl.glEnd();
    }

    /**
     * Returns boss laser bounding rectangle for collision
     */
    public Rectangle getLaserBounds() {
        if (!isFiringLaser) return new Rectangle(0, 0, 0, 0);
        float laserWidth = 20 + (level * 10);
        return new Rectangle((int) (x + width / 2 - laserWidth), 0, (int) (laserWidth * 2), (int) y);
    }

    /**
     * Reduces boss health when hit. If health <= 0, marks boss dead
     */
    public void takeDamage() {
        int damageAmount = 7; // Fixed damage for all levels
        health -= damageAmount;
        if (health <= 0) setAlive(false);
    }

    public int getHealth() {
        return health;
    }
}