package mygame.objects;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends GameObject {

    public final float SCREEN_WIDTH = 800;
    public final float SCREEN_HEIGHT = 600;
    public static final int MAX_HEALTH = 10000;

    // --- Shield Variables ---
    public boolean isShieldActive = false;
    private long shieldStartTime = 0;
    public long lastShieldUseTime = 0;
    private final long SHIELD_DURATION = 5000;
    private final long SHIELD_COOLDOWN = 10000;

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
    public long lastSpecialAttackTime = 0;

    private int health;
    public int weaponLevel = 1;

    // --- Laser ---
    public boolean isShieldAvailable = true;
    private long shieldEndTime = 0;

    public boolean isLaserBeamActive = false;
    public boolean isLaserAvailable = true;
    private long laserEndTime = 0;
    private float shieldAngle = 0;
    public long lastLaserTime = 0;

    public Player(float x, float y) {
        super(x, y, 80, 80);
        this.speed = 8.0f;
        this.health = MAX_HEALTH;
    }

    // --- NEW METHOD: Reset Abilities (Called at start of levels) ---
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

    // Helper for UI
    public float getShieldCooldownPercent() {
        if (isShieldActive) return 1.0f;
        if (isShieldReady()) return 1.0f;
        return 0.0f;
    }

    @Override
    public void update() {
        if (isDying) return;

        // 1. Fly Off Logic
        if (isFlyingOff) {
            float targetX = (SCREEN_WIDTH / 2) - (width / 2);
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

    // --- Updated Activation Methods (Use Flags instead of Time) ---

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

    public void activateLaserBeam() {
        if (canUseLaser && !isLaserBeamActive) {
            isLaserBeamActive = true;
            canUseLaser = false; // Consume ability
            laserEndTime = System.currentTimeMillis() + 3500;
        }
    }

    public void activateSpecialAttack() {
        if (canUseSuper && !isSpecialAttackActive) {
            isSpecialAttackActive = true;
            canUseSuper = false; // Consume ability
            specialAttackEndTime = System.currentTimeMillis() + 2000;
            specialAttackUsedOnEnemies = false;
        }
    }

    // --- Manual / Helper Methods ---

    public void activateShieldManual() {
        activateShield();
    }

    public void addShieldInventory() {
        canUseShield = true; // Refill from item
    }

    public void refillLaser() {
        canUseLaser = true; // Refill from item
    }

    public void addSpecialAmmo(int amount) {
        canUseSuper = true; // Refill from item
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
        this.x = 375;
        this.y = 50;
    }

    public void handleInput(boolean[] keys) {
        if (isFlyingOff || isDying) return;

        if (keys[KeyEvent.VK_UP]) y += speed;
        if (keys[KeyEvent.VK_DOWN]) y -= speed;
        if (keys[KeyEvent.VK_LEFT]) x -= speed;
        if (keys[KeyEvent.VK_RIGHT]) x += speed;
    }

    public void upgradeWeapon() { if (weaponLevel < 3) weaponLevel++; }


    // --- Render ---
    @Override
    public void render(GL gl, int[] textures) {
        // 1. Draw Laser
        if (isLaserBeamActive && !isFlyingOff && !isDying) drawLaserBeam(gl);

        int textureToDraw;

        // 2. Select Texture based on Health
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

        // 4. Draw Engine Flame
        if (isFlyingOff && !isDying) {
            gl.glEnable(GL.GL_BLEND);
            gl.glColor4f(1.0f, 0.5f, 0.0f, 0.8f);
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glVertex2f(x + width/2 - 10, y);
            gl.glVertex2f(x + width/2 + 10, y);
            gl.glVertex2f(x + width/2, y - 40);
            gl.glEnd();
            gl.glDisable(GL.GL_BLEND);
            gl.glColor3f(1,1,1);
        }

        // 5. Draw Shield Texture Only
        if (isShieldActive) {
            drawShieldTexture(gl, textures);
        }
    }

    // --- Helper Methods (Kept as requested) ---

    private void drawShieldEffect(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glColor4f(0.0f, 1.0f, 1.0f, 0.5f);
        gl.glLineWidth(3.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        float radius = 40;
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        for (int i = 0; i < 360; i++) {
            double angle = Math.toRadians(i);
            gl.glVertex2d(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius);
        }
        gl.glEnd();
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1, 1, 1);
    }

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
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2d(-sSize/2, -sSize/2);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2d(sSize/2, -sSize/2);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2d(sSize/2, sSize/2);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2d(-sSize/2, sSize/2);
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
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(x, y + h);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(x + w, y + h);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(x + w, y);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(x, y);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    // Kept but unused in render
    private void drawHealthBar(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        float barTotalWidth = 40f;
        float barHeight = 4f;
        float barStartX = x + (width - barTotalWidth) / 2;
        float barStartY = y - 10;
        float healthPercent = Math.max(0f, Math.min((float) health / MAX_HEALTH, 1f));

        gl.glColor3f(0.6f, 0.0f, 0.0f);
        gl.glRectf(barStartX, barStartY, barStartX + barTotalWidth, barStartY + barHeight);

        if (healthPercent > 0) {
            gl.glColor3f(0.0f, 1.0f, 0.0f);
            gl.glRectf(barStartX, barStartY, barStartX + (barTotalWidth * healthPercent), barStartY + barHeight);
        }
        gl.glColor3f(1, 1, 1);
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    // Kept but unused in render
    private void drawShield(GL gl) {
        gl.glEnable(GL.GL_BLEND); gl.glColor4f(0.0f, 1.0f, 1.0f, 0.4f);
        float cx = x + width/2, cy = y + height/2; float radius = 45;
        gl.glBegin(GL.GL_POLYGON); for (int i = 0; i < 360; i += 20) { double angle = Math.toRadians(i); gl.glVertex2d(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius); } gl.glEnd();
        gl.glDisable(GL.GL_BLEND); gl.glColor3f(1,1,1);
    }

    private void drawLaserBeam(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);

        float cx = x + width / 2;
        float yStart = y + height;
        float yEnd = 600;

        gl.glColor4f(0.0f, 1.0f, 1.0f, 0.4f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(cx - 15, yStart);
        gl.glVertex2f(cx + 15, yStart);
        gl.glVertex2f(cx + 15, yEnd);
        gl.glVertex2f(cx - 15, yEnd);
        gl.glEnd();

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

    public Rectangle getLaserBounds() { return new Rectangle((int) (x + width / 2 - 10), (int) (y + height), 20, 600); }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
}