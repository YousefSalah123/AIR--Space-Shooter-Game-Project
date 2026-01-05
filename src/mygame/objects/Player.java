package mygame.objects;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends GameObject {

    public final float SCREEN_WIDTH = 800;
    public final float SCREEN_HEIGHT = 600;
    public static final int MAX_HEALTH = 100;

    // --- Multiplayer Flag ---
    public boolean isPlayerTwo; // True = Player 2 (Red), False = Player 1 (Blue)

    // --- Shield Variables ---
    public boolean isShieldActive = false;
    private long shieldStartTime = 0;
    private final long SHIELD_DURATION = 5000;

    // --- Ability Availability Flags ---
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

    // Constructor: Now accepts 'isPlayerTwo' to identify the player
    public Player(float x, float y, boolean isPlayerTwo) {
        super(x, y, 70, 70); // Adjusted size slightly for better fit
        this.speed = 8.0f;
        this.health = MAX_HEALTH;
        this.isPlayerTwo = isPlayerTwo;
    }

    public void resetAbilities() {
        canUseShield = true;
        canUseLaser = true;
        canUseSuper = true;

        isShieldActive = false;
        isLaserBeamActive = false;
        isSpecialAttackActive = false;

        specialAmmo = 1;
    }

    public boolean isShieldReady() {
        return canUseShield && !isShieldActive;
    }

    @Override
    public void update() {
        if (isDying) return;

        // 1. Fly Off Logic
        if (isFlyingOff) {
            float targetX = (SCREEN_WIDTH / 2) - (width / 2);
            // Offset logic so they don't merge into one spot
            if (isPlayerTwo) targetX += 40;
            else targetX -= 40;

            x += (targetX - x) * 0.05f;
            y += 7.0f;
            return;
        }

        // 2. Screen Boundaries
        if (x < 0) x = 0;
        if (x > SCREEN_WIDTH - width) x = SCREEN_WIDTH - width;
        if (y < 10) y = 10;
        if (y > SCREEN_HEIGHT - height) y = SCREEN_HEIGHT - height;

        // 3. Timers
        long now = System.currentTimeMillis();

        if (isShieldActive) {
            if (now > shieldStartTime + SHIELD_DURATION) {
                isShieldActive = false;
            }
        }

        if (isLaserBeamActive && now > laserEndTime) {
            isLaserBeamActive = false;
        }

        if (isSpecialAttackActive && now > specialAttackEndTime) {
            isSpecialAttackActive = false;
        }
    }

    public void activateShield() {
        if (canUseShield && !isShieldActive) {
            isShieldActive = true;
            canUseShield = false;
            shieldStartTime = System.currentTimeMillis();
        }
    }

    public void activateLaserBeam() {
        if (canUseLaser && !isLaserBeamActive) {
            isLaserBeamActive = true;
            canUseLaser = false;
            laserEndTime = System.currentTimeMillis() + 3500;
        }
    }

    public void activateSpecialAttack() {
        if (canUseSuper && !isSpecialAttackActive) {
            isSpecialAttackActive = true;
            canUseSuper = false;
            specialAttackEndTime = System.currentTimeMillis() + 2000;
            specialAttackUsedOnEnemies = false;
        }
    }

    public void activateShieldManual() {
        activateShield();
    }

    public void triggerFlyOff() {
        isFlyingOff = true;
        isShieldActive = false;
        isLaserBeamActive = false;
    }

    public void resetPosition() {
        isFlyingOff = false;
        isDying = false;
        animationFinished = false;
        currentTextureIndex = 1;

        // Different spawn positions for Player 1 and Player 2
        if (isPlayerTwo) {
            this.x = 450;
        } else {
            this.x = 250;
        }
        this.y = 50;

        canUseShield = true;
        canUseLaser = true;
        canUseSuper = true;
        specialAttackAvailable = true;
        isSpecialAttackActive = false;
        isShieldAvailable = true;
        isLaserBeamActive = false;
        isLaserAvailable = true;
        this.health = MAX_HEALTH; // Restore health on reset
    }

    // --- Modified Input Handling for 2 Players ---
    public void handleInput(boolean[] keys) {
        if (isFlyingOff || isDying) return;

        if (!isPlayerTwo) {
            // Player 1 Controls (Arrows)
            if (keys[KeyEvent.VK_UP]) y += speed;
            if (keys[KeyEvent.VK_DOWN]) y -= speed;
            if (keys[KeyEvent.VK_LEFT]) x -= speed;
            if (keys[KeyEvent.VK_RIGHT]) x += speed;
        } else {
            // Player 2 Controls (W, A, S, D)
            if (keys[KeyEvent.VK_W]) y += speed;
            if (keys[KeyEvent.VK_S]) y -= speed;
            if (keys[KeyEvent.VK_A]) x -= speed;
            if (keys[KeyEvent.VK_D]) x += speed;
        }
    }

    public void upgradeWeapon() {
        if (weaponLevel < 3) weaponLevel++;
    }

    @Override
    public void render(GL gl, int[] textures) {
        // 1. Draw Laser (Common logic)
        if (isLaserBeamActive && !isFlyingOff && !isDying) drawLaserBeam(gl);

        // --- NEW LOGIC: Dynamic Texture Selection ---

        // Define the starting index for this player's sprite sheet
        // Player 1 starts at index 1 (Hero.png)
        // Player 2 starts at index 67 (RedPlane.png)
        int baseIndex = isPlayerTwo ? 67 : 1;

        int textureToDraw;

        if (!isDying) {
            // --- Health Logic ---
            float healthPercent = (float) health / (float) MAX_HEALTH;

            if (healthPercent > 0.75f) {
                textureToDraw = textures[baseIndex];     // Full Health
            } else if (healthPercent > 0.50f) {
                textureToDraw = textures[baseIndex + 1]; // Light Damage
            } else if (healthPercent > 0.25f) {
                textureToDraw = textures[baseIndex + 2]; // Heavy Damage
            } else {
                textureToDraw = textures[baseIndex + 3]; // Critical/Destroyed
            }

            // Sync animation state with current health (for smooth transition to death)
            if (healthPercent > 0.75f) currentTextureIndex = 0;
            else if (healthPercent > 0.50f) currentTextureIndex = 1;
            else if (healthPercent > 0.25f) currentTextureIndex = 2;
            else currentTextureIndex = 3;

        } else {
            // --- Death Animation Logic ---
            dieFrameCounter++;
            if (dieFrameCounter > dieFrameDelay) {
                dieFrameCounter = 0;

                // Advance the frame
                currentTextureIndex++;

                // Ensure we don't go backwards (animation always moves to more damage)
                // Start at least from index 2 (Heavy Damage) for death animation
                if (currentTextureIndex < 2) currentTextureIndex = 2;

                // Stop at index 3 (Destroyed state)
                if (currentTextureIndex > 3) {
                    currentTextureIndex = 3;
                    animationFinished = true;
                }
            }

            // Calculate final texture ID: Base + Offset (0, 1, 2, or 3)
            int finalIndex = baseIndex + currentTextureIndex;

            // Safety Check: Ensure we don't exceed array bounds
            if (finalIndex < textures.length) {
                textureToDraw = textures[finalIndex];
            } else {
                textureToDraw = textures[baseIndex]; // Fallback
            }
        }

        // 3. Draw Player Sprite
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

        // 5. Draw Shield
        if (isShieldActive) {
            drawShieldTexture(gl, textures);
        }
    }
    private void drawShieldTexture(GL gl, int[] textures) {
        gl.glEnable(GL.GL_BLEND);
        // Use a high index for shield (e.g., 27 or last available)
        int shieldIndex = (textures.length > 27) ? 27 : textures.length - 1;

        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[shieldIndex]);

        gl.glPushMatrix();
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        gl.glTranslated(centerX, centerY, 0);

        float sSize = width + 30;
        gl.glColor4f(1f, 1f, 1f, 0.8f); // Blueish shield for P1
        if (isPlayerTwo) gl.glColor4f(1f, 0.5f, 0.5f, 0.8f); // Reddish shield for P2

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

    private void drawLaserBeam(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);

        float cx = x + width / 2;
        float yStart = y + height;
        float yEnd = 600;

        // Laser Color: Blue for P1, Red for P2
        if (isPlayerTwo) gl.glColor4f(1.0f, 0.0f, 0.0f, 0.4f); // Red Glow
        else gl.glColor4f(0.0f, 1.0f, 1.0f, 0.4f); // Cyan Glow

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(cx - 15, yStart);
        gl.glVertex2f(cx + 15, yStart);
        gl.glVertex2f(cx + 15, yEnd);
        gl.glVertex2f(cx - 15, yEnd);
        gl.glEnd();

        // Core White
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

    public Rectangle getLaserBounds() {
        return new Rectangle((int) (x + width / 2 - 10), (int) (y + height), 20, 600);
    }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
}