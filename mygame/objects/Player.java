package mygame.objects;
import javax.media.opengl.GL;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * The Player class represents the player's spaceship in the game.
 * It handles movement, health, special attacks, laser, shield, and weapon upgrades.
 * Extends GameObject, which provides basic position, size, and speed properties.
 */
public class Player extends GameObject {

    // Screen boundaries for player movement
    private final float SCREEN_WIDTH = 800;
    private final float SCREEN_HEIGHT = 600;

    // Maximum health the player can have
    public static final int MAX_HEALTH = 15;

    // --- Special Attack System ---
    public boolean specialAttackAvailable = true;      // Whether the special attack can be used
    public boolean isSpecialAttackActive = false;     // Whether the special attack is currently active
    private long specialAttackEndTime = 0;            // Time when the special attack ends
    public boolean specialAttackUsedOnEnemies = false;// Prevent repeated damage from a single activation

    // Player health
    private int health;

    // Weapon upgrade level (affects bullet patterns)
    public int weaponLevel = 1;

    // --- Shield System ---
    public boolean isShieldActive = false;            // Whether the shield is currently active
    public boolean isShieldAvailable = true;          // Whether the player has a shield in inventory
    private long shieldEndTime = 0;                   // Time when shield deactivates

    // --- Laser System ---
    public boolean isLaserBeamActive = false;         // Whether laser beam is active
    public boolean isLaserAvailable = true;           // Whether the player can use the laser
    private long laserEndTime = 0;                    // Time when laser deactivates

    /**
     * Constructor initializes player at a given position with default health and speed.
     */
    public Player(float x, float y) {
        super(x, y, 50, 50); // width=50, height=50
        this.speed = 8.0f;
        this.health = MAX_HEALTH;
    }

    /**
     * Update method runs every frame.
     * Handles movement bounds, shield duration, laser duration, and special attack duration.
     */
    @Override
    public void update() {
        // Keep player inside screen boundaries
        if (x < 0) x = 0;
        if (x > SCREEN_WIDTH - width) x = SCREEN_WIDTH - width;
        if (y < 10) y = 10;
        if (y > SCREEN_HEIGHT - height) y = SCREEN_HEIGHT - height;

        // Check if shield duration expired
        if (isShieldActive && System.currentTimeMillis() > shieldEndTime) {
            isShieldActive = false;
            System.out.println("Shield Deactivated!");
        }

        // Check if laser duration expired
        if (isLaserBeamActive && System.currentTimeMillis() > laserEndTime) {
            isLaserBeamActive = false;
            System.out.println("Laser Beam Ended!");
        }

        // Check if special attack duration expired
        if (isSpecialAttackActive && System.currentTimeMillis() > specialAttackEndTime) {
            isSpecialAttackActive = false;
            System.out.println("Special Attack Ended!");
        }
    }

    // ----------------- CONTROL METHODS -----------------

    /**
     * Activates shield manually if available.
     * Shield lasts for a fixed duration and is consumed on use.
     */
    public void activateShieldManual() {
        if (isShieldAvailable && !isShieldActive) {
            isShieldActive = true;
            isShieldAvailable = false; // consume shield
            shieldEndTime = System.currentTimeMillis() + 7000; // 7 seconds
            System.out.println("Shield Activated Manually!");
        } else {
            System.out.println("No Shield Available!");
        }
    }

    /**
     * Adds a shield to the player's inventory.
     * Typically used as a reward or power-up.
     */
    public void addShieldInventory() {
        isShieldAvailable = true;
        System.out.println("Shield Added to Inventory (Press X to use)");
    }

    /**
     * Activates the laser beam if available.
     * Laser lasts for a fixed duration and disables further use until refilled.
     */
    public void activateLaserBeam() {
        if (isLaserAvailable && !isLaserBeamActive) {
            isLaserBeamActive = true;
            isLaserAvailable = false; // laser consumed
            laserEndTime = System.currentTimeMillis() + 3500; // 3.5 seconds
        }
    }

    /**
     * Refills laser availability for the player.
     * Usually called after defeating a boss or collecting a power-up.
     */
    public void refillLaser() {
        isLaserAvailable = true;
    }

    /**
     * Upgrades the player's weapon, increasing bullet spread/power.
     * Maximum weapon level is 3.
     */
    public void upgradeWeapon() {
        if (weaponLevel < 3) weaponLevel++;
    }

    // ----------------- RENDERING -----------------

    /**
     * Renders the player on screen.
     * Draws player ship, laser (if active), health bar, and shield (if active).
     */
    @Override
    public void render(GL gl) {
        if (isLaserBeamActive) drawLaserBeam(gl);

        // Draw player ship as a triangle
        gl.glColor3f(0.1f, 0.1f, 0.8f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(x + width / 2, y + height);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glEnd();

        drawHealthBar(gl);

        // Draw shield as a circle outline if active
        if (isShieldActive) {
            gl.glColor3f(0.0f, 1.0f, 1.0f);
            gl.glBegin(GL.GL_LINE_LOOP);
            float cx = x + width / 2, cy = y + height / 2;
            for (int i = 0; i < 360; i += 20) {
                double angle = Math.toRadians(i);
                gl.glVertex2d(cx + Math.cos(angle) * 40, cy + Math.sin(angle) * 40);
            }
            gl.glEnd();
        }
    }

    /**
     * Draws the player's health bar above the ship.
     * Health is clamped between 0 and MAX_HEALTH.
     */
    private void drawHealthBar(GL gl) {
        float barWidth = width * 1.2f;
        float barX = x - (width * 0.1f);

        float healthPercent = Math.max(0f, Math.min((float) health / Player.MAX_HEALTH, 1f));

        // Draw background (gray)
        gl.glColor3f(0.5f, 0.5f, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, y - 5);
        gl.glVertex2f(barX + barWidth, y - 5);
        gl.glVertex2f(barX + barWidth, y - 10);
        gl.glVertex2f(barX, y - 10);
        gl.glEnd();

        // Draw foreground (green) proportional to health
        float greenWidth = (healthPercent >= 1f) ? barWidth : barWidth * healthPercent;
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, y - 5);
        gl.glVertex2f(barX + greenWidth, y - 5);
        gl.glVertex2f(barX + greenWidth, y - 10);
        gl.glVertex2f(barX, y - 10);
        gl.glEnd();
    }

    /**
     * Draws the laser beam, consisting of a core and a glow.
     * Uses blending for transparency effect.
     */
    private void drawLaserBeam(GL gl) {
        gl.glEnable(GL.GL_BLEND);

        // Core (thin white)
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.9f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x + width / 2 - 3, y + height);
        gl.glVertex2f(x + width / 2 + 3, y + height);
        gl.glVertex2f(x + width / 2 + 3, 600);
        gl.glVertex2f(x + width / 2 - 3, 600);
        gl.glEnd();

        // Glow (cyan, transparent)
        gl.glColor4f(0.0f, 1.0f, 1.0f, 0.5f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x + width / 2 - 10, y + height);
        gl.glVertex2f(x + width / 2 + 10, y + height);
        gl.glVertex2f(x + width / 2 + 10, 600);
        gl.glVertex2f(x + width / 2 - 10, 600);
        gl.glEnd();

        gl.glDisable(GL.GL_BLEND);
    }

    /**
     * Returns the bounding rectangle of the laser for collision detection.
     */
    public Rectangle getLaserBounds() {
        return new Rectangle((int) (x + width / 2 - 10), (int) (y + height), 20, 600);
    }

    // ----------------- INPUT -----------------

    /**
     * Handles keyboard input to move the player.
     * Supports arrow keys: UP, DOWN, LEFT, RIGHT.
     */
    public void handleInput(boolean[] keys) {
        if (keys[KeyEvent.VK_UP]) y += speed;
        if (keys[KeyEvent.VK_DOWN]) y -= speed;
        if (keys[KeyEvent.VK_LEFT]) x -= speed;
        if (keys[KeyEvent.VK_RIGHT]) x += speed;
    }

    /**
     * Activates the special attack if available.
     * Lasts for a fixed duration and affects all enemies on screen.
     */
    public void activateSpecialAttack() {
        if (specialAttackAvailable && !isSpecialAttackActive) {
            specialAttackAvailable = false; // consume special attack
            isSpecialAttackActive = true;
            specialAttackEndTime = System.currentTimeMillis() + 2000; // 2 seconds
            specialAttackUsedOnEnemies = false; // reset usage flag
            System.out.println("SPECIAL ATTACK ACTIVATED!");
        } else {
            System.out.println("Special Attack not available!");
        }
    }

    // ----------------- GETTERS / SETTERS -----------------
    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}