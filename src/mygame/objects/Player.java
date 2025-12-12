package mygame.objects;
import javax.media.opengl.GL;
import java.awt.*;
import java.awt.event.KeyEvent;

// Represents the Player's ship, handling movement, health, special abilities
// (shield, laser, super attack), death, and level transitions.
public class Player extends GameObject {

    public final float SCREEN_WIDTH = 800;
    public final float SCREEN_HEIGHT = 600;
    public static final int MAX_HEALTH = 100;

    // --- Shield Variables ---
    public boolean isShieldActive = false;
    private long shieldStartTime = 0;
    private final long SHIELD_DURATION = 5000;

    // --- NEW: Ability Availability Flags (One-time use per level) ---
    public boolean canUseShield = true;
    public boolean canUseLaser = true;
    public boolean canUseSuper = true;

    // --- Transition State ---
    public boolean isFlyingOff = false;

    // --- Death Animation State ---
    public boolean isDying = false;
    public boolean animationFinished = false;
    private int dieFrameCounter = 0;
    private int dieFrameDelay = 10;
    private int currentTextureIndex = 1;

    // --- Abilities ---
    public boolean specialAttackAvailable = true;
    public boolean isSpecialAttackActive = false;
    private long specialAttackEndTime = 0;
    public boolean specialAttackUsedOnEnemies = false;
    public int specialAmmo = 1;

    private int health;
    public int weaponLevel = 1;

    // --- Laser ---
    public boolean isShieldAvailable = true;

    public boolean isLaserBeamActive = false;
    public boolean isLaserAvailable = true;
    private long laserEndTime = 0;


    // Constructor: Initializes the player ship's position, size, speed, and health.
    public Player(float x, float y) {
        super(x, y, 80, 80);
        this.speed = 8.0f;
        this.health = MAX_HEALTH;
    }

    // Resets all one-time-use ability flags and active states, typically called at the start of a new level.
    public void resetAbilities() {
        // Reset flags to true (Available again at start of level)
        canUseShield = true;
        canUseLaser = true;
        canUseSuper = true;

        // Reset active states
        isShieldActive = false;
        isLaserBeamActive = false;
        isSpecialAttackActive = false;

        // Reset Ammo
        specialAmmo = 1;
    }

    // Helper for UI (Deprecated logic but kept for compatibility)
    public boolean isShieldReady() {
        return canUseShield && !isShieldActive;
    }

    // Updates the player's state, handling movement limits, fly-off sequence,
    // and the expiration timers for special abilities (Shield, Laser, Super).
    @Override
    public void update() {
        if (isDying) return;

        // 1. Fly Off Logic (used for exiting the level)
        if (isFlyingOff) {
            float targetX = (SCREEN_WIDTH / 2) - (width / 2);
            x += (targetX - x) * 0.05f;
            y += 7.0f;
            return;
        }

        // 2. Screen Boundaries (limits player movement)
        if (x < 0) x = 0;
        if (x > SCREEN_WIDTH - width) x = SCREEN_WIDTH - width;
        if (y < 10) y = 10;
        if (y > SCREEN_HEIGHT - height) y = SCREEN_HEIGHT - height;

        // 3. Timers
        long now = System.currentTimeMillis();

        // Shield Expiration
        if (isShieldActive) {
            if (now > shieldStartTime + SHIELD_DURATION) {
                isShieldActive = false;
                System.out.println("Shield Deactivated!");
            }
        }

        // Laser Expiration
        if (isLaserBeamActive && now > laserEndTime) {
            isLaserBeamActive = false;
        }

        // Super Expiration
        if (isSpecialAttackActive && now > specialAttackEndTime) {
            isSpecialAttackActive = false;
        }
    }

    // Activates the shield if it is available (canUseShield flag is true).
    public void activateShield() {
        if (canUseShield && !isShieldActive) {
            isShieldActive = true;
            canUseShield = false; // Consume ability
            shieldStartTime = System.currentTimeMillis();
            System.out.println("Shield Activated!");
        } else {
            System.out.println("Shield not available!");
        }
    }

    // Activates the laser beam if it is available (canUseLaser flag is true).
    public void activateLaserBeam() {
        if (canUseLaser && !isLaserBeamActive) {
            isLaserBeamActive = true;
            canUseLaser = false; // Consume ability
            laserEndTime = System.currentTimeMillis() + 3500;
        }
    }

    // Activates the super attack if it is available (canUseSuper flag is true).
    public void activateSpecialAttack() {
        if (canUseSuper && !isSpecialAttackActive) {
            isSpecialAttackActive = true;
            canUseSuper = false; // Consume ability
            specialAttackEndTime = System.currentTimeMillis() + 2000;
            specialAttackUsedOnEnemies = false;
        }
    }

    // A manual/helper method to activate the shield (delegates to the main activation method).
    public void activateShieldManual() {
        activateShield();
    }


    // Triggers the player's upward fly-off sequence for level completion, and disables active abilities.
    public void triggerFlyOff() {
        isFlyingOff = true;
        isShieldActive = false;
        isLaserBeamActive = false;
    }

    // Resets the player's position and all ability and state flags to their default values,
    // usually used after a death/restart.
    public void resetPosition() {
        isFlyingOff = false;
        isDying = false;
        animationFinished = false;
        currentTextureIndex = 1;
        this.x = 375;
        this.y = 50;
        canUseShield = true;
        canUseLaser = true;
        canUseSuper = true;
        specialAttackAvailable = true;
        isSpecialAttackActive = false;
        isShieldAvailable = true;
        isLaserBeamActive = false;
        isLaserAvailable = true;
    }

    // Handles key input for controlling the player's ship movement.
    public void handleInput(boolean[] keys) {
        if (isFlyingOff || isDying) return;

        if (keys[KeyEvent.VK_UP]) y += speed;
        if (keys[KeyEvent.VK_DOWN]) y -= speed;
        if (keys[KeyEvent.VK_LEFT]) x -= speed;
        if (keys[KeyEvent.VK_RIGHT]) x += speed;
    }

    // Increases the player's weapon level, up to a maximum of 3.
    public void upgradeWeapon() {
        if (weaponLevel < 3) weaponLevel++;
    }


    // Renders the player ship, including the laser, shield, engine flame,
    // and adjusting the ship's texture based on its current health (damage state) or death animation.
    @Override
    public void render(GL gl, int[] textures) {
        // 1. Draw Laser
        if (isLaserBeamActive && !isFlyingOff && !isDying) drawLaserBeam(gl);

        int textureToDraw;

        // 2. Select Texture based on Health (Damage State)
        if (!isDying) {
            float healthPercent = (float) health / (float) MAX_HEALTH;
            if (healthPercent > 0.75f) {
                textureToDraw = textures[1];
            } else if (healthPercent > 0.50f) {
                textureToDraw = textures[2];
            } else if (healthPercent > 0.25f) {
                textureToDraw = textures[3];
            } else {
                textureToDraw = textures[4];
            }
        } else {
            // Death Animation
            dieFrameCounter++;
            if (dieFrameCounter > dieFrameDelay) {
                dieFrameCounter = 0;
                if (currentTextureIndex < 2) currentTextureIndex = 2;

                if (currentTextureIndex < 4) {
                    currentTextureIndex++;
                } else {
                    animationFinished = true;
                }
            }
            if (currentTextureIndex < textures.length) {
                textureToDraw = textures[currentTextureIndex];
            } else {
                textureToDraw = textures[4];
            }
        }

        // 3. Draw Player
        if (!animationFinished) {
            drawTexture(gl, textureToDraw, x, y, width, height);
        }

        // 4. Draw Engine Flame (when flying off)
        if (isFlyingOff && !isDying) {
            gl.glEnable(GL.GL_BLEND);
            gl.glColor4f(1.0f, 0.5f, 0.0f, 0.8f);
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glVertex2f(x + width / 2 - 10, y);
            gl.glVertex2f(x + width / 2 + 10, y);
            gl.glVertex2f(x + width / 2, y - 40);
            gl.glEnd();
            gl.glDisable(GL.GL_BLEND);
            gl.glColor3f(1, 1, 1);
        }

        // 5. Draw Shield Texture Only
        if (isShieldActive) {
            drawShieldTexture(gl, textures);
        }
    }

    // Draws the shield texture around the player ship.
    private void drawShieldTexture(GL gl, int[] textures) {
        gl.glEnable(GL.GL_BLEND);
        if (textures.length > 27) {
            gl.glBindTexture(GL.GL_TEXTURE_2D, textures[27]);
        } else {
            return;
        }

        gl.glPushMatrix();
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        gl.glTranslated(centerX, centerY, 0);

        float sSize = width + 30;
        gl.glColor4f(1f, 1f, 1f, 0.8f);

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex2d(-sSize / 2, -sSize / 2);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex2d(sSize / 2, -sSize / 2);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex2d(sSize / 2, sSize / 2);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex2d(-sSize / 2, sSize / 2);
        gl.glEnd();

        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    // Helper method to draw a textured quad (replicated from GameObject, but kept as it was in the original code).
    protected void drawTexture(GL gl, int textureId, float x, float y, float w, float h) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glColor3f(1, 1, 1);
        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex2f(x, y + h);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex2f(x + w, y + h);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex2f(x + w, y);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex2f(x, y);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    // Draws the laser beam visualization.
    private void drawLaserBeam(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);

        float cx = x + width / 2;
        float yStart = y + height;
        float yEnd = 600;

        // Outer glow (Cyan/Blue)
        gl.glColor4f(0.0f, 1.0f, 1.0f, 0.4f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(cx - 15, yStart);
        gl.glVertex2f(cx + 15, yStart);
        gl.glVertex2f(cx + 15, yEnd);
        gl.glVertex2f(cx - 15, yEnd);
        gl.glEnd();

        // Inner core (White)
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.9f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(cx - 4, yStart);
        gl.glVertex2f(cx + 4, yStart);
        gl.glVertex2f(cx + 4, yEnd);
        gl.glVertex2f(cx - 4, yEnd);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_BLEND);
        gl.glColor3f(1, 1, 1);
    }

    // Returns the bounding box for the active laser beam for collision detection.
    public Rectangle getLaserBounds() {
        return new Rectangle((int) (x + width / 2 - 10), (int) (y + height), 20, 600);
    }

    // Returns the current health.
    public int getHealth() {
        return health;
    }

    // Sets the current health.
    public void setHealth(int health) {
        this.health = health;
    }
}