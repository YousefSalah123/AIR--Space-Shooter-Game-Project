package mygame.engine;
import com.sun.opengl.util.GLUT;
import mygame.Game;
import mygame.objects.*;
import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GameManager {
    final Game game;
    // Variable to store the time of the player's last damage taken
    private long lastDamageTime = 0;
    // Variables to control enemy density
    private long lastEnemySpawnTime = 0; // Time of last enemy spawn
    private int baseSpawnDelay = 2000;   // Base time between each enemy (2000 = 2 seconds) - Increase the number to further reduce enemies
    // ... (Variables as they are) ...
    public Player player;
    public ArrayList<Enemy> enemies;
    public ArrayList<Bullet> bullets;
    public ArrayList<Item> items;
    public ArrayList<MiddleEnemy> activeMiddleEnemies;
    public boolean middleWaveSpawned = false;
    public int score = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;
    public boolean isGameRunning = false;
    int currentLevel = 1;
    private int waveStep = 0;
    private long waveTimer = 0;
    private boolean bossActive = false;
    private Boss boss = null;
    private boolean isLevelTransitioning = false;
    private long lastAutoShotTime = 0;
    private int fireRate = 300;
    private long lastRandomSpawnTime = 0;
    // Variable to ensure the boss death sound is played only once
    private boolean bossDeathSoundPlayed = false;
    // Sound Manager
    public SoundManager soundManager;
    private boolean showLevelScreen = false; // Should we display the level screen now?
    private long levelScreenTimer = 0;       // Display timer
    private GLUT glut = new GLUT();

    // Constructor for the GameManager. Initializes the player and all lists, and starts background music.
    public GameManager(Game game) {
        this.game = game;
        player = new Player(375, 50);
        activeMiddleEnemies = new ArrayList<>();
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        items = new ArrayList<>();
        // Initialize Sound
        soundManager = new SoundManager();
        soundManager.playMusic();
    }

    // The main update loop for the game state. Handles player death, level transitions,
    // and calls update methods for all game objects (player, bullets, enemies, etc.).
    public void update() {
        if (!isGameRunning) return; // If the game is paused, ignore
        if (gameOver || gameWon) {
            isGameRunning = false;
            game.handleGameOver(gameWon, score);   // Send the score to Game
            return;
        }

        // ------------------------------------------
        // Check Player Death State (Important modification)
        // ------------------------------------------
        if (player.isDying) {
            // If the animation is completely finished, declare game over
            if (player.animationFinished) {
                player.setAlive(false);
                // Fix: Play game over sound only once
                if (!gameOver) {
                    soundManager.playSound("game_over");
                    gameOver = true;
                }
            }
            // We don't stop the full update so that the animation continues to be rendered,
            // but we prevent player movement update within their class
            // We do not Return here to allow the animation to be drawn in render
        }

        // ... (The rest of the update code as is) ...
        long currentTime = System.currentTimeMillis();

        player.update();
        if (isLevelTransitioning) {
            // When the player leaves the screen (after killing the boss)
            if (player.getY() > 700) {

                // If we are not displaying the screen yet, start displaying it
                if (!showLevelScreen) {
                    showLevelScreen = true;
                    levelScreenTimer = System.currentTimeMillis();
                } else {
                    // We are now displaying the screen, wait for 3 seconds (3000 milliseconds)
                    if (System.currentTimeMillis() - levelScreenTimer > 3000) {
                        showLevelScreen = false; // Hide the screen
                        startNextLevel();        // Actually start the new level
                    }
                }
            }
            return;
        }

        // Prevent firing if the player is dying
        if (!player.isFlyingOff && !player.isSpecialAttackActive && !player.isLaserBeamActive
                && !player.isDying // <-- Adding this condition
                && currentTime - lastAutoShotTime > fireRate) {
            playerShoot();
            lastAutoShotTime = currentTime;
        }

        if (player.isSpecialAttackActive) spawnSpecialBullets();

        // 3. Update Bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            if (!b.isAlive()) bullets.remove(i);
        }

        // 4. Update Items
        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            item.update();
            if (item.getY() < -50) items.remove(i);
        }

        // 5. Update Boss (Modified for Death Animation)
        if (bossActive && boss != null) {
            boss.update(); // Update movement and animation
            // --- Modification here: passing player as the third parameter ---
            boss.shootLogic(bullets, soundManager);
            if (boss.isDying && !bossDeathSoundPlayed) {
                soundManager.stopMusic(); // Stop regular gameplay music
                soundManager.playSound("LevelComplete"); // Play win music immediately
                bossDeathSoundPlayed = true; // To prevent it from playing again in the next frame
            }
            if (boss.isDying && boss.animationFinished) {
                boss.setAlive(false);
                handleBossDefeat();
            }
        }

        // 6. Update Middle Enemies
        for (int i = activeMiddleEnemies.size() - 1; i >= 0; i--) {
            MiddleEnemy me = activeMiddleEnemies.get(i);
            me.update(800);

            // If death animation finished -> remove it now
            if (me.readyToRemove) {
                score += 500;
                spawnRandomItem(me.x, me.y);
                activeMiddleEnemies.remove(i);
                continue;
            }

            // If dying -> prevent it from shooting
            if (me.isDying) continue;

            if (currentTime - me.lastShotTime > me.shotDelay) {
                if (me.type == 1) fireFanShots(me.x, me.y);
                else fireHomingShot(me.x, me.y);
                soundManager.playSound("enemy_laser"); // Sound Effect
                me.lastShotTime = currentTime;
            }
            // The condition (me.health <= 0) was removed from here, it will be handled in collisions
        }

        // Update function for regular enemies
        // 7. Update Regular Enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update();

            // If death animation finished -> remove it
            if (e.readyToRemove) {
                enemies.remove(i);
                continue;
            }

            // If dying -> prevent it from shooting
            if (e.isDying) continue;

            if (e.readyToFire()) {
                enemyShootPattern(e);
                soundManager.playSound("enemy_laser"); // Sound Effect
            }

            // Remove enemy if it goes off screen (and not due to death)
            if (!e.isAlive() && !e.isDying) enemies.remove(i);
        }

        updateWaveLogic();
        spawnContinuousRandomEnemies();
        checkCollisions();
    }

    // Handles the player taking damage, implementing a brief invincibility period (cooldown) after being hit,
// and checks if the player's shield is active before reducing health.
    public void playerTakeDamage(int amount) {
        if (!isGameRunning) return;
        if (player.isDying) return;

        // --- New Modification: Invincibility Period (Cooldown) ---
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime < 1000) { // 1000ms = 1 second cooldown
            return; // If less than one second has passed, ignore the hit and exit
        }
        lastDamageTime = currentTime; // Record the time of this hit
        // -----------------------------------

        if (player.isShieldActive) {
            player.activateShieldManual();
            return;
        }

        player.setHealth(player.getHealth() - amount); // Decrease health

        // (Optional) You can print the health to verify
        System.out.println("Player Hit! HP: " + player.getHealth());

        if (player.getHealth() <= 0) {
            player.isDying = true;
        }
    }

    // Renders all active game objects and UI elements to the screen using OpenGL.
// This includes the player, bullets, items, enemies (with health bars), and the HUD.
    public void render(GL gl, int[] textures) {
        // If the game is not running (or just starting), don't draw anything old
        if (!isGameRunning) {
            return;
        }

        // =============================================================
        //  Drawing Game Elements (Background remains even if game over/won)
        // =============================================================

        // 1. Draw Player
        player.render(gl, textures);

        // 2. Draw Bullets
        for (Bullet b : bullets) {
            b.render(gl, textures);
        }

        // 3. Draw Items
        for (Item item : items) {
            item.render(gl, textures);
        }

        // 4. Draw Middle Enemies (Enlarged) + Health Bar
        for (MiddleEnemy me : activeMiddleEnemies) {
            // --- Visually enlarge the Middle Enemy only (without changing the class) ---
            gl.glPushMatrix(); // Save current coordinates
            gl.glTranslatef(me.x, me.y, 0); // Translate origin to the center of the enemy
            gl.glScalef(1.5f, 1.5f, 1f);    // Enlarge the size by one and a half times (1.5x)
            gl.glTranslatef(-me.x, -me.y, 0); // Restore the origin to its place

            me.render(gl, textures); // Draw the enemy (it will appear large now)

            gl.glPopMatrix(); // Restore normal coordinates for the rest of the elements

            // --- Draw Health Bar (Adjusted position) ---
            gl.glDisable(GL.GL_TEXTURE_2D);

            float barTotalWidth = 40f;
            // Since we enlarged the enemy, we must raise the bar a bit (was 35, now 55)
            float barStartY = me.y + 55;
            float barStartX = me.x - 19;

            float percent = (float) me.health / (float) me.maxHealth;

            // Red background
            gl.glColor3f(0.6f, 0.0f, 0.0f);
            gl.glRectf(barStartX, barStartY, barStartX + barTotalWidth, barStartY + 4f);

            // Green percentage
            if (percent > 0) {
                gl.glColor3f(0.0f, 1.0f, 0.0f);
                gl.glRectf(barStartX, barStartY, barStartX + (barTotalWidth * percent), barStartY + 4f);
            }
            gl.glColor3f(1, 1, 1);
            gl.glEnable(GL.GL_TEXTURE_2D);
        }

        // 5. Draw Boss
        if (bossActive && boss != null) {
            boss.render(gl, textures);
        }

        // 6. Draw Regular Enemies + Health Bar
        for (Enemy e : enemies) {
            e.render(gl, textures);

            // Draw Health Bar for regular enemy
            gl.glDisable(GL.GL_TEXTURE_2D);
            float barTotalWidth = 30f;
            float barStartX = e.getX() + (e.width - barTotalWidth) / 2;
            float barStartY = e.getY() + e.height + 5;
            float percent = (float) e.health / (float) e.maxHealth;

            gl.glColor3f(0.6f, 0.0f, 0.0f);
            gl.glRectf(barStartX, barStartY, barStartX + barTotalWidth, barStartY + 4f);

            if (percent > 0) {
                gl.glColor3f(0.0f, 1.0f, 0.0f);
                gl.glRectf(barStartX, barStartY, barStartX + (barTotalWidth * percent), barStartY + 4f);
            }
            gl.glColor3f(1, 1, 1);
            gl.glEnable(GL.GL_TEXTURE_2D);
        }

        // 7. Draw User Interface (HUD)
        if (player.isAlive()) {
            drawPlayerHUD(gl, textures);
        }

        if (showLevelScreen) {
            drawNextLevelScreen(gl);
        }
    }

    // Draws the level transition screen, displaying the next level number or a "VICTORY!" message
// over a semi-transparent black overlay.
    private void drawNextLevelScreen(GL gl) {
        // 1. Setup for 2D drawing without textures
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // 2. Draw a semi-transparent black background covering the screen
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(800, 0);
        gl.glVertex2f(800, 600);
        gl.glVertex2f(0, 600);
        gl.glEnd();

        // 3. Setup text (white color)
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        // Determine the next level (current + 1)
        int nextLvl = currentLevel + 1;
        String text = "LEVEL " + nextLvl;
        if (nextLvl > 3) text = "VICTORY!"; // If the game is finished

        // 4. Draw the text in the middle of the screen using GLUT
        // Note: We adjust the position manually (approximately)
        gl.glRasterPos2f(320, 300);
        glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, text);

        // Small extra text "Get Ready"
        if (nextLvl <= 3) {
            gl.glColor3f(1.0f, 1.0f, 0.0f); // Yellow
            gl.glRasterPos2f(340, 270);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "GET READY");
        }

        // Restore settings
        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1, 1, 1);
    }

    // Draws the Player's Head-Up Display (HUD), including status icons for special abilities (Laser, Shield, Super).
    private void drawPlayerHUD(GL gl, int[] textures) {
        // 1. Draw the static Health Bar
        drawHealthBarOnly(gl);

        // 2. Enable textures and blending (Fix for black background issue)
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        // This line is the magic for hiding the black background:
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // Position settings
        float startX = 15;
        float startY = 40;

        // --- Fix for stretching issue ---
        // Try making the height equal to the width (square) to see the actual shape of the image
        float iconWidth = 50;
        float iconHeight = 40; // Keep it proportional to the width initially
        float padding = 7;

        // ---------------------------------------------------
        // 1. Laser Icon (Index 41)
        // ---------------------------------------------------
        if (player.canUseLaser) gl.glColor3f(1.0f, 1.0f, 1.0f); // Lit up
        else gl.glColor3f(0.3f, 0.3f, 0.3f);                    // Dimmed

        if (textures.length > 41) {
            drawIcon(gl, textures[41], startX, startY, iconWidth, iconHeight);
        }

        // ---------------------------------------------------
        // 2. Shield Icon (Index 42)
        // ---------------------------------------------------
        if (player.canUseShield) gl.glColor3f(1.0f, 1.0f, 1.0f);
        else gl.glColor3f(0.3f, 0.3f, 0.3f);

        float pos2 = startX + iconWidth + padding;
        if (textures.length > 42) {
            drawIcon(gl, textures[42], pos2, startY, iconWidth, iconHeight);
        }

        // ---------------------------------------------------
        // 3. Super Icon (Index 43)
        // ---------------------------------------------------
        if (player.canUseSuper) gl.glColor3f(1.0f, 1.0f, 1.0f);
        else gl.glColor3f(0.3f, 0.3f, 0.3f);

        float pos3 = startX + (iconWidth + padding) * 2;
        if (textures.length > 43) {
            drawIcon(gl, textures[43], pos3, startY, iconWidth, iconHeight);
        }

        // Clean up settings
        gl.glColor3f(1, 1, 1);
        gl.glDisable(GL.GL_BLEND); // Stop blending so it doesn't affect subsequent drawing incorrectly
    }

    // Draws a texture (icon) at a specified location and size using GL_QUADS for 2D rendering.
    private void drawIcon(GL gl, int textureId, float x, float y, float width, float height) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex2f(x, y);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex2f(x + width, y);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex2f(x + width, y + height);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex2f(x, y + height);
        gl.glEnd();
    }

    // Draws the player's health bar in the top-left corner of the screen using solid colors (no textures).
    private void drawHealthBarOnly(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);

        // Position settings (Top Left)
        float barX = 20;
        // Modification here: 570 means near the top (since screen height is 600)
        float barY = 570;

        float barWidth = 200; // Made slightly wider to fit the HUD area
        float barHeight = 15;

        // Background (Dark Red)
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glRectf(barX, barY, barX + barWidth, barY + barHeight);

        // Current Health (Green)
        float hpPercent = (float) player.getHealth() / Player.MAX_HEALTH;
        if (hpPercent < 0) hpPercent = 0;

        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glRectf(barX, barY, barX + (barWidth * hpPercent), barY + barHeight);

        // White border
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // Re-enable textures
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    // Activates the player's shield, provided the player exists and is not currently in the dying state.
    public void activateShield2() {
        if (player != null && !player.isDying) {
            player.activateShield();
        }
    }

    // Manages the progression of enemy waves, spawning groups of enemies or the boss based on the current step.
// Progression pauses during boss fights, level transitions, or while mid-level enemies are active.
    private void updateWaveLogic() { /* Same previous code */
        if (bossActive || isLevelTransitioning) return;
        if (!activeMiddleEnemies.isEmpty()) return;
        if (System.currentTimeMillis() - waveTimer > 5000) {
            waveStep++;
            switch (waveStep) {
                case 1:
                    spawnSquadV(400, 750, 5);
                    break;
                case 2:
                    spawnSquadSide(true, 4);
                    break;
                case 3:
                    spawnSquadSide(false, 4);
                    break;
                case 4:
                    if (!middleWaveSpawned) {
                        spawnMiddleEnemies();
                    } else {
                        waveStep++;
                    }
                    break;
                case 5:
                    spawnBoss();
                    break;
            }
            waveTimer = System.currentTimeMillis();
        }
    }

    // Spawns a wave of powerful 'MiddleEnemy' units, calculating their health and count based on the current level.
// They are evenly spaced across the top of the screen.
    private void spawnMiddleEnemies() {
        activeMiddleEnemies.clear();

        // 1. Determine Health (Stronger than regular enemies)
        // Regular enemy health is 100
        int hp;
        if (currentLevel == 1) {
            hp = 100;      // Normal health
        } else if (currentLevel == 2) {
            hp = 130;      // 1.5 times stronger (correction: 130 is slightly stronger)
        } else {
            hp = 150;      // Tank (hard to kill)
        }

        // 2. Determine count based on the level
        int enemyCount;
        if (currentLevel == 1) enemyCount = 2;
        else if (currentLevel == 2) enemyCount = 3;
        else enemyCount = 4;

        // 3. Calculate spacing for distribution
        float screenWidth = 800f;
        float spacing = screenWidth / (enemyCount + 1);
        float startY = 750;

        for (int i = 0; i < enemyCount; i++) {
            float x = spacing * (i + 1);
            int type = (i % 2) + 1;

            MiddleEnemy me = new MiddleEnemy(x, startY, type, currentLevel);
            me.health = hp;
            me.maxHealth = hp;

            // Varying shooting time
            me.shotDelay = (1200 - (currentLevel * 100)) + (i * 150);

            activeMiddleEnemies.add(me);
        }

        middleWaveSpawned = true;
        System.out.println("Spawned " + enemyCount + " Middle Enemies (HP: " + hp + ")");
    }

    // Initiates the boss fight for the current level, setting the boss's properties,
// activating the boss flag, and clearing existing bullets.
    private void spawnBoss() {
        System.out.println("BOSS LEVEL " + currentLevel + " INCOMING!");
        boss = new Boss(350, 700, currentLevel);
        bossActive = true;
        bossDeathSoundPlayed = false; // <-- Add this line, it's very important
        bullets.clear();
    }

    // Checks for and processes all collision events between game objects (player, enemies, bullets, boss, items).
    public void checkCollisions() {
        // 1. Player Protection: If the player is dying, stop calculating collisions
        if (player.isDying) return;

        Rectangle pRect = player.getBounds();

        // =================================================================
        // A. Middle Enemies Collisions
        // =================================================================
        for (int i = 0; i < activeMiddleEnemies.size(); i++) {
            MiddleEnemy me = activeMiddleEnemies.get(i);

            // Important Note: If the enemy is dying (animation), ignore it completely for collision checks
            if (me.isDying) continue;

            Rectangle meRect = new Rectangle((int) me.getX() - 30, (int) me.getY() - 30, 60, 60);

            // 1. Player body vs. Enemy body
            if (pRect.intersects(meRect)) {
                // Strong collision sound
                soundManager.playSound("explosion");

                if (player.isShieldActive) {
                    // --- Shield active state ---
                    // Enemy takes heavy damage
                    me.setHealth(me.getHealth() - 100);
                } else {
                    // --- No shield state ---
                    // Player takes damage (using the new function with Cooldown)
                    playerTakeDamage(15);

                    // Enemy takes heavy damage
                    me.setHealth(me.getHealth() - 100);
                }

                // Check for enemy death
                if (me.getHealth() <= 0) {
                    me.setHealth(0); // Prevent negative health
                    // Do not remove the enemy here, start the animation instead
                    me.startDeath();
                    score += 50;
                    // Stop the loop momentarily to prevent calculation errors in the same frame
                    continue;
                }
            }

            // 2. Player Laser vs. Enemy
            if (player.isLaserBeamActive && player.getLaserBounds().intersects(meRect)) {
                me.setHealth(me.getHealth() - 2); // Continuous damage
                if (me.getHealth() <= 0) {
                    me.setHealth(0);
                    me.startDeath();
                    // Enemy explosion sound
                    soundManager.playSound("explosion");
                }
            }

            // 3. Player Bullets vs. Enemy
            for (Bullet b : bullets) {
                if (!b.isAlive() || b.isEnemyBullet()) continue;
                if (b.getBounds().intersects(meRect)) {
                    me.setHealth(me.getHealth() - 5);
                    b.setAlive(false); // Bullet disappears

                    if (me.getHealth() <= 0) {
                        me.setHealth(0);
                        me.startDeath();
                        // Enemy explosion sound
                        soundManager.playSound("explosion");
                    }
                }
            }
        }

        // =================================================================
        // B. Boss Collisions
        // =================================================================
        if (bossActive && boss != null && !boss.isDying) {
            Rectangle bossRect = boss.getBounds();

            // 1. Boss Laser vs. Player
            if (boss.isFiringLaser && boss.getLaserBounds().intersects(pRect)) {
                if (!player.isShieldActive) {
                    // Fast laser damage
                    playerTakeDamage(3);
                    if (player.getHealth() <= 0) {
                        player.isDying = true;
                        soundManager.playSound("explosion"); // Player destruction sound
                    }
                }
            }

            // 2. Player Laser vs. Boss
            if (player.isLaserBeamActive && player.getLaserBounds().intersects(bossRect)) {
                boss.takeDamage();
            }

            // 3. Player body vs. Boss body
            if (pRect.intersects(bossRect)) {
                player.setY(player.getY() - 30); // Bounce back
                soundManager.playSound("explosion"); // Collision sound

                if (player.isShieldActive) {
                    boss.takeDamage();
                } else {
                    playerTakeDamage(40);
                    boss.takeDamage();
                }
            }
        }

        // =================================================================
        // C. Player Super Laser (vs. Small Enemies and Bullets)
        // =================================================================
        if (player.isLaserBeamActive) {
            Rectangle lRect = player.getLaserBounds();

            // Against Regular Enemies
            for (Enemy e : enemies) {
                // Added !e.isDying condition
                if (e.isAlive() && !e.isDying && lRect.intersects(e.getBounds())) {
                    e.health -= 10;
                    if (e.health <= 0) {
                        e.health = 0;
                        // Modification: start death instead of removal
                        score += 50;
                        spawnRandomItem(e.getX(), e.getY());
                        e.startDeath();
                        soundManager.playSound("explosion"); // Explosion sound
                    }
                }
            }

            // Against Enemy Bullets
            for (Bullet b : bullets) {
                if (b.isEnemyBullet() && lRect.intersects(b.getBounds())) {
                    b.setAlive(false);
                }
            }
        }

        // =================================================================
        // D. Bullets Logic
        // =================================================================
        for (Bullet b : bullets) {
            if (!b.isAlive()) continue;
            Rectangle bRect = b.getBounds();

            if (!b.isEnemyBullet()) {
                // --- Player Bullets ---
                if (bossActive && boss != null && !boss.isDying && bRect.intersects(boss.getBounds())) {
                    boss.takeDamage();
                    b.setAlive(false);
                } else {
                    for (Enemy e : enemies) {
                        // Added !e.isDying condition
                        if (e.isAlive() && !e.isDying && bRect.intersects(e.getBounds())) {
                            b.setAlive(false);
                            e.health -= 15; // Bullet damage
                            if (e.health <= 0) {
                                e.health = 0;
                                // Modification: start death
                                score += 50;
                                spawnRandomItem(e.getX(), e.getY());
                                e.startDeath();
                                soundManager.playSound("explosion"); // Explosion sound
                            }
                            break;
                        }
                    }
                }
            } else {
                // --- Enemy Bullets (vs. Player) ---
                if (bRect.intersects(pRect)) {
                    b.setAlive(false);

                    if (!player.isShieldActive) {
                        // Here we could use playerTakeDamage, but the bullet disappears anyway, so the old method works well
                        player.setHealth(player.getHealth() - 2);
                        playerTakeDamage(2);
                    }
                }
            }
        }

        // =================================================================
        // E. Small Enemies colliding with Player body
        // =================================================================
        for (Enemy e : enemies) {
            // Added !e.isDying condition
            if (e.isAlive() && !e.isDying && pRect.intersects(e.getBounds())) {
                score += 50;
                // Enemy will die because it hit the player
                e.startDeath();
                soundManager.playSound("explosion"); // Explosion sound

                if (!player.isShieldActive) {
                    playerTakeDamage(8);
                    // Player takes damage
                }
            }
        }

        // =================================================================
        // F. Item Pickup (Sound modification here)
        // =================================================================
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (pRect.intersects(item.getBounds())) {
                applyItem(item.getType());

                // Play the appropriate sound based on type
                if (item.getType() == Item.ItemType.GOLD_COIN) {
                    soundManager.playSound("coin");
                } else {
                    soundManager.playSound("powerup"); // For heart and upgrade
                }

                items.remove(i);
                i--;
            }
        }
    }


    // Handles the sequence of events immediately following the Boss's defeat.
// This includes clearing remaining game objects, rewarding the player, and triggering the level transition.
    private void handleBossDefeat() {
        bossActive = false;
        boss = null;
        score += 500;

        // Clear the screen of remaining enemies, bullets, and items
        enemies.clear();
        activeMiddleEnemies.clear();
        bullets.clear();
        items.clear(); // Recommended to clear items as well

        // Activate transition mode
        isLevelTransitioning = true;
        player.triggerFlyOff();

    }

    // Resets the game state and advances the player to the next level after a boss defeat/transition.
    private void startNextLevel() {
        showLevelScreen = false;
        isLevelTransitioning = false;
        currentLevel++;
        waveStep = 0;
        waveTimer = System.currentTimeMillis();

        middleWaveSpawned = false;
        activeMiddleEnemies.clear();
        bullets.clear();
        items.clear();

        bossActive = false;
        boss = null;

        player.resetPosition();

        // ============================================================
        // Modification here: Full Health Restore
        // ============================================================
        // Instead of only increasing by 20, set it equal to MAX_HEALTH
        player.setHealth(player.MAX_HEALTH);
        // Or if using Setters:
        // player.setHealth(Player.MAX_HEALTH);

        // Extra safety: reset death and animation states to ensure the correct sprite is shown
        player.isDying = false;
        player.animationFinished = false;
        // ============================================================

        if (currentLevel > 3) gameWon = true;
        player.resetAbilities();
    }

    // Helper function to select a random texture image index for regular enemies (from 21 to 23).
    private int getRandomEnemyTexture() {
        // Array starts at 21 (enem1) and ends at 23 (enem3)
        // Math.random() * 3 gives 0, 1, or 2
        return 21 + (int) (Math.random() * 3);
    }

    // Spawns continuous enemies (either singles or squads) at a rate controlled by the game state (boss fight, mid-boss wave, or normal).
    private void spawnContinuousRandomEnemies() {
        if (isLevelTransitioning) return;
        long currentTime = System.currentTimeMillis();

        // -----------------------------------------------------------
        // Modification 1: Increase time intervals (reduce clutter/density)
        // -----------------------------------------------------------
        // Boss time: every 6 seconds (to focus on the boss)
        // Mid-boss time: every 3 seconds
        // Normal time: every 2000 milliseconds (2 seconds) instead of 800
        long spawnDelay = (bossActive) ? 6000 : (!activeMiddleEnemies.isEmpty()) ? 3000 : 2000;

        if (currentTime - lastRandomSpawnTime > spawnDelay) {
            double rand = Math.random();

            // -----------------------------------------------------------
            // Modification 2: Reduce the chance of Squads (Groups) appearing
            // -----------------------------------------------------------
            // Set to 0.8, meaning: 80% individual enemies and only 20% squads
            // (It was 0.6, which made squads too frequent)
            if (rand < 0.8) {
                // Spawn individual enemy
                double typeRand = Math.random();
                Enemy.TypesOfEnemies type = (typeRand < 0.4) ? Enemy.TypesOfEnemies.STRAIGHT :
                        (typeRand < 0.7) ? Enemy.TypesOfEnemies.CHASER :
                                Enemy.TypesOfEnemies.CIRCLE_PATH;
                float spawnX = 50 + (float) (Math.random() * 700);

                // Code remains exactly the same to avoid breaking drawing or the health bar
                enemies.add(new Enemy(spawnX, 700, 60, type, player, getRandomEnemyTexture()));
            } else {
                // Spawn Squad
                if (Math.random() < 0.5) {
                    float centerX = 150 + (float) (Math.random() * 500);
                    spawnSquadV(centerX, 750, 5);
                } else {
                    boolean fromLeft = Math.random() < 0.5;
                    spawnSquadSide(fromLeft, 4);
                }

                // -----------------------------------------------------------
                // Modification 3: Long rest after a Squad
                // -----------------------------------------------------------
                // After a squad spawns, give the player a 3 second rest (was 1.5)
                lastRandomSpawnTime = currentTime + 3000;
                return;
            }
            lastRandomSpawnTime = currentTime;
        }
    }

    // Spawns a V-formation squad of enemies centered around a specific X coordinate.
// All enemies in the squad share the same visual texture.
    private void spawnSquadV(float centerX, float startY, int count) {
        // To ensure the entire squad has the same appearance, choose the texture once outside the loop
        int squadTexture = getRandomEnemyTexture();

        for (int i = 0; i < count; i++) {
            float offsetX = (i % 2 == 0) ? (i * 40) : -(i * 40);
            float offsetY = i * 30;
            enemies.add(new Enemy(centerX + offsetX, startY + offsetY, 60, Enemy.TypesOfEnemies.SQUAD_V, player, squadTexture));
        }
    }

    // Resets all necessary game variables, player state, and object lists to start a new game.
    public void resetGame() {
        // Reset Player
        player.setHealth(Player.MAX_HEALTH);
        player.setAlive(true);
        player.resetPosition();
        player.animationFinished = false;

        // Reset Enemies, Bullets, and Items
        enemies.clear();
        activeMiddleEnemies.clear();
        bullets.clear();
        items.clear();

        // Reset Game Variables
        score = 0;
        currentLevel = 1;
        waveStep = 0;
        bossActive = false;
        boss = null;
        middleWaveSpawned = false;
        gameOver = false;
        gameWon = false;
        isGameRunning = true;
    }


    // Spawns a squad of enemies that enters the screen from either the left or the right side in a staggered formation.
    private void spawnSquadSide(boolean fromLeft, int count) {
        // Unify the appearance of the squad
        int squadTexture = getRandomEnemyTexture();

        for (int i = 0; i < count; i++) {
            float startX = fromLeft ? -50 - (i * 60) : 850 + (i * 60);
            float startY = 550 + (i * 20);
            Enemy.TypesOfEnemies type = fromLeft ? Enemy.TypesOfEnemies.SQUAD_ENTER_LEFT : Enemy.TypesOfEnemies.SQUAD_ENTER_RIGHT;
            enemies.add(new Enemy(startX, startY, 60, type, player, squadTexture));
        }
    }

    // Spawns a 'fan' pattern of enemy bullets, firing five shots in a spread arc from the starting point.
    public void fireFanShots(float startX, float startY) {
        int[] angles = {-30, -15, 0, 15, 30};
        float bulletSpeed = 7.0f;
        for (int angle : angles) {
            double rad = Math.toRadians(angle);
            float dx = (float) (bulletSpeed * Math.sin(rad));
            float dy = (float) (-bulletSpeed * Math.cos(rad));
            bullets.add(new Bullet(startX, startY - 20, dx, dy, true, 6));
        }
    }

    // Spawns a single bullet that is aimed directly at the player's current position (a homing shot).
    public void fireHomingShot(float startX, float startY) {
        float bulletSpeed = 8.0f;
        float dx = (player.getX() + player.getWidth() / 2) - startX;
        float dy = (player.getY() + player.getHeight() / 2) - startY;
        double angle = Math.atan2(dy, dx);
        bullets.add(new Bullet(startX, startY, (float) (bulletSpeed * Math.cos(angle)), (float) (bulletSpeed * Math.sin(angle)), true, 6));
    }

    // Executes the specific shooting pattern for a given enemy based on its type.
// CIRCLE_PATH enemies fire a 360-degree spread, while others fire a direct, targeted shot at the player.
    private void enemyShootPattern(Enemy e) {
        float ex = e.getX() + e.getWidth() / 2;
        float ey = e.getY();
        if (e.getType() == Enemy.TypesOfEnemies.CIRCLE_PATH) {
            for (int k = 0; k < 360; k += 60) {
                float rad = (float) Math.toRadians(k);
                bullets.add(new Bullet(ex, ey, (float) Math.cos(rad) * 5, (float) Math.sin(rad) * 5, true, 6));
            }
        } else {
            float dx = (player.getX() + player.getWidth() / 2) - ex;
            float dy = (player.getY() + player.getHeight() / 2) - ey;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            bullets.add(new Bullet(ex, ey, (dx / len) * 7, (dy / len) * 7, true, 6));
        }
    }

    // Spawns a random item at the enemy's location upon defeat with a 35% chance.
// The probability of different item types (Health, Rapid Fire, Gold Coin) varies by current level.
    private void spawnRandomItem(float x, float y) {
        if (Math.random() > 0.35) return;
        int rand = new Random().nextInt(100);
        Item.ItemType type = Item.ItemType.GOLD_COIN;
        if (currentLevel == 1) {
            if (rand < 40) type = Item.ItemType.HEALTH;
            else if (rand < 70) type = Item.ItemType.RAPID_FIRE;
        } else {
            if (rand < 30) type = Item.ItemType.RAPID_FIRE;
            else if (rand < 50) type = Item.ItemType.HEALTH;
        }
        items.add(new Item(x, y, type));
    }

    // Applies the effect of a collected item to the player (e.g., restores health, upgrades weapon, or increases score).
    private void applyItem(Item.ItemType type) {
        switch (type) {
            case HEALTH:
                player.setHealth(Math.min(Player.MAX_HEALTH, player.getHealth() + 50));
                break;
            case RAPID_FIRE:
                player.upgradeWeapon();
                break;
            case GOLD_COIN:
                score += 100;
                break;
        }
    }

    // Handles the player firing standard bullets. The number and spread of bullets
// depends on the player's current weapon level, and the player laser sound is triggered once.
    public void playerShoot() {
        // Calculate bullet starting point
        float sx = player.getX() + player.getWidth() / 2 - 5;
        float sy = player.getY() + player.getHeight();

        int playerBulletIndex = 25;

        // Trigger the sound once primarily (to ensure a sound is played immediately)
        // This solves the "delay" issue you were concerned about
        soundManager.playSound("Player_laser");

        if (player.weaponLevel == 1) {
            bullets.add(new Bullet(sx, sy, 0, 15, false, playerBulletIndex));
        } else if (player.weaponLevel == 2) {
            bullets.add(new Bullet(sx - 15, sy, 0, 15, false, playerBulletIndex));
            bullets.add(new Bullet(sx + 15, sy, 0, 15, false, playerBulletIndex));

            // (Optional and discouraged) If you want auditory confirmation, you could play it again here
            // But this will result in a very loud sound
            // soundManager.playSound("Player_laser");
        } else {
            bullets.add(new Bullet(sx, sy, 0, 15, false, playerBulletIndex));
            bullets.add(new Bullet(sx, sy, -4, 14, false, playerBulletIndex));
            bullets.add(new Bullet(sx, sy, 4, 14, false, playerBulletIndex));

            // (Optional and discouraged)
            // soundManager.playSound("Player_laser");
        }
    }

    // Attempts to activate the player's super laser attack.
// It checks if the laser is available and not already active before triggering it and playing the sound.
    public void fireLaser() {
        if (player.canUseLaser && !player.isLaserBeamActive) {
            player.activateLaserBeam();

            // --- Trigger the special laser sound ---
            soundManager.playSound("laser");
        }
    }

    // Activates the player's defensive shield, provided the player is alive and the shield ability is available.
    public void activateShield() {
        // 1. Ensure the player exists and is not dying
        if (player != null && !player.isDying) {

            // 2. Check that the shield is available and not currently active (to prevent unnecessary sound play)
            if (player.canUseShield && !player.isShieldActive) {

                // 3. Activate the shield (deducts ability cost within the Player class)
                player.activateShield();

                // 4. Play the shield sound
                soundManager.playSound("shield");
            }
        }
    }

    // Spawns three special bullets that appear at random X-coordinates at the top of the screen
// and move downwards with a slight random horizontal movement. This is typically used for a special attack pattern.
    private void spawnSpecialBullets() {
        for (int i = 0; i < 3; i++)
            bullets.add(new Bullet((float) (Math.random() * 800), 0, (float) (Math.random() * 6) - 3, 10, false, 6));
    }
}