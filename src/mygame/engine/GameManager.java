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

    // --- Multiplayer Variables ---
    public Player player1;
    public Player player2; // The second player
    public boolean isMultiplayer = true; // Toggle for multiplayer
    public static int SHARED_SPECIAL_ABILITIES = 3; // Shared pool for special moves (Super Wave)

    // Shooting Timers for both players
    private long lastP1ShotTime = 0;
    private long lastP2ShotTime = 0;
    private int fireRate = 300;

    // Damage Timers (Invincibility Frames)
    private long lastP1DamageTime = 0;
    private long lastP2DamageTime = 0;

    // Variables to control enemy density
    private long lastEnemySpawnTime = 0;
    private int baseSpawnDelay = 2000;

    // ⭐ Boss Mechanics Variables
    private boolean bossEnraged = false; // Did boss enter rage mode?
    private long lastMinionSpawnTime = 0; // Timer for summoning minions

    // ⭐ Kamikaze Enemies List (To track fast enemies)
    private ArrayList<Enemy> kamikazeEnemies = new ArrayList<>();

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
    private long lastRandomSpawnTime = 0;
    private boolean bossDeathSoundPlayed = false;

    // Sound Manager
    public SoundManager soundManager;
    private boolean showLevelScreen = false;
    private long levelScreenTimer = 0;
    private GLUT glut = new GLUT();
    // Variable to regulate laser hits on the boss
    private long lastBossLaserHitTime = 0;
    public boolean isMenuState = false;


    // Constructor
    public GameManager(Game game) {
        this.game = game;

        // Initialize Players
        // Player 1 (Blue) at left-center
        player1 = new Player(300, 50, false);

        // Player 2 (Red) at right-center
        if (isMultiplayer) {
            player2 = new Player(500, 50, true);
        }

        activeMiddleEnemies = new ArrayList<>();
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        items = new ArrayList<>();

        // Initialize Sound
        soundManager = new SoundManager();
        soundManager.playMusic();
    }

    public void update() {
        if (isMenuState) return;

        if (!isGameRunning) return;
        if (gameOver || gameWon) {
            isGameRunning = false;
            game.handleGameOver(gameWon, score);
            return;
        }

        // 1. Check Death State
        boolean p1Dying = player1.isDying;
        boolean p2Dying = (isMultiplayer && player2.isDying);

        if (p1Dying || p2Dying) {
            if ((p1Dying && player1.animationFinished) || (p2Dying && player2.animationFinished)) {
                player1.setAlive(false);
                if(player2 != null) player2.setAlive(false);

                if (!gameOver) {
                    soundManager.playSound("game_over");
                    gameOver = true;
                }
            }
        }

        long currentTime = System.currentTimeMillis();

        // 2. Update Players
        player1.update();
        if (isMultiplayer && player2 != null) {
            player2.update();
        }

        // 3. Level Transition Logic
        if (isLevelTransitioning) {
            if (player1.getY() > 700) {
                if (!showLevelScreen) {
                    showLevelScreen = true;
                    levelScreenTimer = System.currentTimeMillis();
                } else {
                    if (System.currentTimeMillis() - levelScreenTimer > 3000) {
                        showLevelScreen = false;
                        startNextLevel();
                    }
                }
            }
            return;
        }

        // 4. Shooting Logic
        if (!player1.isFlyingOff && !player1.isSpecialAttackActive && !player1.isLaserBeamActive
                && !player1.isDying && currentTime - lastP1ShotTime > fireRate) {
            playerShoot(player1);
            lastP1ShotTime = currentTime;
        }

        if (isMultiplayer && player2 != null && !player2.isFlyingOff && !player2.isSpecialAttackActive
                && !player2.isLaserBeamActive && !player2.isDying && currentTime - lastP2ShotTime > fireRate) {
            playerShoot(player2);
            lastP2ShotTime = currentTime;
        }

        if (player1.isSpecialAttackActive || (isMultiplayer && player2.isSpecialAttackActive)) {
            if(Math.random() < 0.3) spawnSpecialBullets();
        }

        // 5. Update Bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            if (!b.isAlive()) bullets.remove(i);
        }

        // 6. Update Items
        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            item.update();
            if (item.getY() < -50) items.remove(i);
        }

        // 7. Update Boss
        if (bossActive && boss != null) {
            boss.update();
            boss.shootLogic(bullets, soundManager);

            // Apply Boss Mechanics
            updateBossMechanics();

            if (boss.isDying && !bossDeathSoundPlayed) {
                soundManager.stopMusic();
                soundManager.playSound("LevelComplete");
                bossDeathSoundPlayed = true;
            }
            if (boss.isDying && boss.animationFinished) {
                boss.setAlive(false);
                handleBossDefeat();
            }
        }

        // 8. Update Middle Enemies
        for (int i = activeMiddleEnemies.size() - 1; i >= 0; i--) {
            MiddleEnemy me = activeMiddleEnemies.get(i);
            me.update(800);

            if (me.readyToRemove) {
                score += 500;
                spawnRandomItem(me.x, me.y);
                activeMiddleEnemies.remove(i);
                continue;
            }
            if (me.isDying) continue;

            long currentDelay = isMultiplayer ? (long)(me.shotDelay * 0.7) : me.shotDelay;

            if (currentTime - me.lastShotTime > currentDelay) {
                if (me.type == 1) fireFanShots(me.x, me.y);
                else fireHomingShot(me.x, me.y);
                soundManager.playSound("enemy_laser");
                me.lastShotTime = currentTime;
            }
        }

        // 9. Update Regular Enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update();

            // ⭐ KAMIKAZE LOGIC: Extra Movement Speed
            if (kamikazeEnemies.contains(e) && e.isAlive() && !e.isDying) {
                Player target = getClosestPlayer(e.getX(), e.getY());
                float dx = (target.getX() + target.getWidth()/2) - (e.getX() + e.getWidth()/2);
                float dy = (target.getY() + target.getHeight()/2) - (e.getY() + e.getHeight()/2);
                float dist = (float)Math.sqrt(dx*dx + dy*dy);

                // Move extra 4 pixels per frame towards player (Fast!)
                if (dist > 0) {
                    e.setX(e.getX() + (dx / dist) * 3);
                    e.setY(e.getY() + (dy / dist) * 3);
                }
            }

            if (e.readyToRemove) {
                enemies.remove(i);
                kamikazeEnemies.remove(e); // Clean up
                continue;
            }
            if (e.isDying) continue;

            if (e.readyToFire()) {
                enemyShootPattern(e);
                soundManager.playSound("enemy_laser");
            }

            if (!e.isAlive() && !e.isDying) {
                enemies.remove(i);
                kamikazeEnemies.remove(e); // Clean up
            }
        }

        updateWaveLogic();
        spawnContinuousRandomEnemies();

        // 10. Check Collisions
        checkCollisions();
    }

    private void updateBossMechanics() {
        if (!bossActive || boss == null || boss.isDying) return;

        // 1. Rage Mode (If health < 30%)
        if (boss.health < boss.maxHealth * 0.3 && !bossEnraged) {
            bossEnraged = true;
            boss.setSpeed((int)(boss.getSpeed()* 1.5));
            soundManager.playSound("powerup");
        }

        // 2. Minion Summoning (Every 8 seconds)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMinionSpawnTime > 8000) {
            spawnBossMinions();
            lastMinionSpawnTime = currentTime;
        }
    }

    // ⭐ Updated: Minions come from sides (Off-screen)
    private void spawnBossMinions() {
        // Spawn from Left (-60) and Right (860)
        // Texture 21 (Standard Enemy)
        int tex = 21;

        // From Left
        enemies.add(new Enemy(-60, boss.getY(), 60, Enemy.TypesOfEnemies.CHASER, player1, tex));
        // From Right
        enemies.add(new Enemy(860, boss.getY(), 60, Enemy.TypesOfEnemies.CHASER, player1, tex));
    }

    // ⭐ New Method: Spawn Kamikaze (Fast Suicide Enemy)
    private void spawnKamikaze(float x, float y) {
        // Use Texture 20 (Distinct look)
        // CHASER type to track player
        Enemy kamikaze = new Enemy(x, y, 60, Enemy.TypesOfEnemies.CHASER, player1, 20);
        enemies.add(kamikaze);
        kamikazeEnemies.add(kamikaze); // Add to special list for speed boost logic
    }

    public void checkCollisions() {
        checkIndividualPlayerCollision(player1);
        if (isMultiplayer && player2 != null) {
            checkIndividualPlayerCollision(player2);
        }
    }

    private void checkIndividualPlayerCollision(Player p) {
        if (p.isDying) return;

        Rectangle pRect = p.getBounds();

        // A. Middle Enemies
        for (MiddleEnemy me : activeMiddleEnemies) {
            if (me.isDying) continue;
            Rectangle meRect = new Rectangle((int) me.getX() - 30, (int) me.getY() - 30, 60, 60);

            if (pRect.intersects(meRect)) {
                soundManager.playSound("explosion");
                if (p.isShieldActive) {
                    me.setHealth(me.getHealth() - 100);
                } else {
                    playerTakeDamage(p, 15);
                    me.setHealth(me.getHealth() - 100);
                }
                if (me.getHealth() <= 0) {
                    me.setHealth(0);
                    me.startDeath();
                    score += 50;
                }
            }

            if (p.isLaserBeamActive && p.getLaserBounds().intersects(meRect)) {
                me.setHealth(me.getHealth() - 2);
                if (me.getHealth() <= 0) {
                    me.setHealth(0);
                    me.startDeath();
                    soundManager.playSound("explosion");
                }
            }
        }

        // B. Boss
        if (bossActive && boss != null && !boss.isDying) {
            Rectangle bossRect = boss.getBounds();

            if (boss.isFiringLaser && boss.getLaserBounds().intersects(pRect)) {
                if (!p.isShieldActive) {
                    playerTakeDamage(p, 3);
                    if (p.getHealth() <= 0) {
                        p.isDying = true;
                        soundManager.playSound("explosion");
                    }
                }
            }

            // Player Laser vs Boss
            if (p.isLaserBeamActive && p.getLaserBounds().intersects(bossRect)) {
                long currentTime = System.currentTimeMillis();

                // Modification: Deduct health only if 150ms has passed (approx 6 hits/sec)
                if (currentTime - lastBossLaserHitTime > 150) {
                    boss.takeDamage();
                    if (isMultiplayer) {
                        boss.takeDamage();
                    }
                    lastBossLaserHitTime = currentTime;

                    // Additional visual or sound effect (Optional)
                    // soundManager.playSound("hit");
                }
            }

            if (pRect.intersects(bossRect)) {
                p.setY(p.getY() - 30);
                soundManager.playSound("explosion");
                if (p.isShieldActive) {
                    boss.takeDamage();
                } else {
                    playerTakeDamage(p, 40);
                    boss.takeDamage();
                }
            }
        }

        // C. Super Laser
        if (p.isLaserBeamActive) {
            Rectangle lRect = p.getLaserBounds();
            for (Enemy e : enemies) {
                if (e.isAlive() && !e.isDying && lRect.intersects(e.getBounds())) {
                    e.health -= 10;
                    if (e.health <= 0) {
                        e.health = 0;
                        score += 50;
                        spawnRandomItem(e.getX(), e.getY());
                        e.startDeath();
                        soundManager.playSound("explosion");
                    }
                }
            }
            for (Bullet b : bullets) {
                if (b.isEnemyBullet() && lRect.intersects(b.getBounds())) {
                    b.setAlive(false);
                }
            }
        }

        // D. Bullets
        for (Bullet b : bullets) {
            if (!b.isAlive()) continue;
            Rectangle bRect = b.getBounds();

            if (!b.isEnemyBullet()) {
                if (bossActive && boss != null && !boss.isDying && bRect.intersects(boss.getBounds())) {
                    boss.takeDamage();
                    b.setAlive(false);
                } else {
                    for (Enemy e : enemies) {
                        if (e.isAlive() && !e.isDying && bRect.intersects(e.getBounds())) {
                            b.setAlive(false);
                            e.health -= 15;
                            if (e.health <= 0) {
                                e.health = 0;
                                score += 50;
                                spawnRandomItem(e.getX(), e.getY());
                                e.startDeath();
                                soundManager.playSound("explosion");
                            }
                            break;
                        }
                    }
                    for(MiddleEnemy me : activeMiddleEnemies) {
                        if(!me.isDying && bRect.intersects(new Rectangle((int)me.getX()-30, (int)me.getY()-30, 60, 60))) {
                            b.setAlive(false);
                            me.health -= 5;
                            if(me.health <= 0) { me.health = 0; me.startDeath(); soundManager.playSound("explosion"); }
                        }
                    }
                }
            } else {
                if (bRect.intersects(pRect)) {
                    b.setAlive(false);
                    if (!p.isShieldActive) {
                        playerTakeDamage(p, 2);
                    }
                }
            }
        }

        // E. Enemies Body
        for (Enemy e : enemies) {
            if (e.isAlive() && !e.isDying && pRect.intersects(e.getBounds())) {
                score += 50;
                e.startDeath();
                soundManager.playSound("explosion");
                if (!p.isShieldActive) {
                    // ⭐ KAMIKAZE DAMAGE: High Damage
                    if (kamikazeEnemies.contains(e)) {
                        playerTakeDamage(p, 30); // Explosion!
                    } else {
                        playerTakeDamage(p, 8);
                    }
                }
            }
        }

        // F. Items
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (pRect.intersects(item.getBounds())) {
                applyItem(p, item.getType());
                if (item.getType() == Item.ItemType.GOLD_COIN) {
                    soundManager.playSound("coin");
                } else {
                    soundManager.playSound("powerup");
                }
                items.remove(i);
                i--;
            }
        }
    }

    public void playerTakeDamage(Player p, int amount) {
        if (!isGameRunning || p.isDying) return;

        long currentTime = System.currentTimeMillis();

        if (p == player1) {
            if (currentTime - lastP1DamageTime < 1000) return;
            lastP1DamageTime = currentTime;
        } else {
            if (currentTime - lastP2DamageTime < 1000) return;
            lastP2DamageTime = currentTime;
        }

        if (p.isShieldActive) {
            p.activateShieldManual();
            return;
        }

        p.setHealth(p.getHealth() - amount);
        System.out.println((p == player1 ? "P1" : "P2") + " Hit! HP: " + p.getHealth());

        if (p.getHealth() <= 0) {
            p.isDying = true;
        }
    }

    public void render(GL gl, int[] textures) {
        if (isMenuState) return;

        if (!isGameRunning) return;

        // 1. Draw Players
        player1.render(gl, textures);
        if (isMultiplayer && player2 != null) {
            player2.render(gl, textures);
        }

        // 2. Draw Bullets
        for (Bullet b : bullets) b.render(gl, textures);

        // 3. Draw Items
        for (Item item : items) item.render(gl, textures);

        // 4. Draw Middle Enemies
        for (MiddleEnemy me : activeMiddleEnemies) {
            gl.glPushMatrix();
            gl.glTranslatef(me.x, me.y, 0);
            gl.glScalef(1.5f, 1.5f, 1f);
            gl.glTranslatef(-me.x, -me.y, 0);
            me.render(gl, textures);
            gl.glPopMatrix();
            drawEnemyHealthBar(gl, me.x - 19, me.y + 55, 40f, me.health, me.maxHealth);
        }

        // 5. Draw Boss
        if (bossActive && boss != null) {
            // ⭐ Visual Indication for Rage: Pure Red
            if (bossEnraged) {
                // Modulate ensures color mixes with texture
                gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
                gl.glColor3f(1.0f, 0.0f, 0.0f); // Pure Red
            } else {
                gl.glColor3f(1, 1, 1); // Normal White
            }

            boss.render(gl, textures);

            gl.glColor3f(1, 1, 1); // Reset color immediately
        }

        // 6. Draw Regular Enemies
        for (Enemy e : enemies) {
            e.render(gl, textures);
            drawEnemyHealthBar(gl, e.getX() + (e.width - 30f)/2, e.getY() + e.height + 5, 30f, e.health, e.maxHealth);
        }
        // 7. HUD
        if (!gameOver) {
            drawPlayerHUD(gl, textures, player1, 15, 40, 15, 570, Game.getPlayerName());
            if (isMultiplayer && player2 != null) {
                drawHealthBarOnly(gl, player2, 590, 570, Game.getPlayer2Name());
            }
        }

        if (showLevelScreen) drawNextLevelScreen(gl);
    }

    private void drawEnemyHealthBar(GL gl, float x, float y, float w, int hp, int max) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor3f(0.6f, 0.0f, 0.0f);
        gl.glRectf(x, y, x + w, y + 4f);
        float percent = (float) hp / (float) max;
        if (percent > 0) {
            gl.glColor3f(0.0f, 1.0f, 0.0f);
            gl.glRectf(x, y, x + (w * percent), y + 4f);
        }
        gl.glColor3f(1, 1, 1);
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawPlayerHUD(GL gl, int[] textures, Player p, float iconX, float iconY, float barX, float barY, String label) {
        drawHealthBarOnly(gl, p, barX, barY, label);

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        float iconWidth = 50;
        float iconHeight = 40;
        float padding = 7;

        if (p.canUseLaser) gl.glColor3f(1, 1, 1); else gl.glColor3f(0.3f, 0.3f, 0.3f);
        if (textures.length > 41) drawIcon(gl, textures[41], iconX, iconY, iconWidth, iconHeight);

        if (p.canUseShield) gl.glColor3f(1, 1, 1); else gl.glColor3f(0.3f, 0.3f, 0.3f);
        if (textures.length > 42) drawIcon(gl, textures[42], iconX + iconWidth + padding, iconY, iconWidth, iconHeight);

        if (p.canUseSuper) gl.glColor3f(1, 1, 1); else gl.glColor3f(0.3f, 0.3f, 0.3f);
        if (textures.length > 43) drawIcon(gl, textures[43], iconX + (iconWidth + padding) * 2, iconY, iconWidth, iconHeight);

        gl.glColor3f(1, 1, 1);
        gl.glDisable(GL.GL_BLEND);
    }

    private void drawHealthBarOnly(GL gl, Player p, float barX, float barY, String label) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        float barWidth = 180;
        float barHeight = 23;

        // Background (Red)
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glRectf(barX, barY, barX + barWidth, barY + barHeight);

        // Health (Green)
        float hpPercent = (float) p.getHealth() / Player.MAX_HEALTH;
        if (hpPercent < 0) hpPercent = 0;
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glRectf(barX, barY, barX + (barWidth * hpPercent), barY + barHeight);

        // Border (White)
        gl.glColor3f(1, 1, 1);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // FIX: Name appears in yellow/white
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        // FIX: Adjust position to be "inside" the bar
        gl.glRasterPos2f(barX + 5, barY + 5);

        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, label);

        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void drawIcon(GL gl, int textureId, float x, float y, float width, float height) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(x, y);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(x + width, y);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(x + width, y + height);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(x, y + height);
        gl.glEnd();
    }

    private void drawNextLevelScreen(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0); gl.glVertex2f(800, 0);
        gl.glVertex2f(800, 600); gl.glVertex2f(0, 600);
        gl.glEnd();

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        int nextLvl = currentLevel + 1;
        String text = (nextLvl > 3) ? "VICTORY!" : "LEVEL " + nextLvl;
        gl.glRasterPos2f(320, 300);
        glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, text);

        if (nextLvl <= 3) {
            gl.glColor3f(1.0f, 1.0f, 0.0f);
            gl.glRasterPos2f(340, 270);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "GET READY");
        }
        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1, 1, 1);
    }

    private void spawnSpecialBullets() {
        for (int i = 0; i < 3; i++)
            bullets.add(new Bullet((float) (Math.random() * 800), 0, (float) (Math.random() * 6) - 3, 10, false, 6));
    }

    public void fireLaser() {
        if (player1.canUseLaser && !player1.isLaserBeamActive) {
            player1.activateLaserBeam();
            soundManager.playSound("laser");
        }
    }

    public void playerShoot(Player p) {
        float sx = p.getX() + p.getWidth() / 2 - 5;
        float sy = p.getY() + p.getHeight();
        int playerBulletIndex = 25;

        soundManager.playSound("Player_laser");

        if (p.weaponLevel == 1) {
            bullets.add(new Bullet(sx, sy, 0, 15, false, playerBulletIndex));
        } else if (p.weaponLevel == 2) {
            bullets.add(new Bullet(sx - 15, sy, 0, 15, false, playerBulletIndex));
            bullets.add(new Bullet(sx + 15, sy, 0, 15, false, playerBulletIndex));
        } else {
            bullets.add(new Bullet(sx, sy, 0, 15, false, playerBulletIndex));
            bullets.add(new Bullet(sx, sy, -4, 14, false, playerBulletIndex));
            bullets.add(new Bullet(sx, sy, 4, 14, false, playerBulletIndex));
        }
    }

    private void applyItem(Player p, Item.ItemType type) {
        switch (type) {
            case HEALTH:
                p.setHealth(Math.min(Player.MAX_HEALTH, p.getHealth() + 50));
                break;
            case RAPID_FIRE:
                p.upgradeWeapon();
                break;
            case GOLD_COIN:
                score += 100;
                break;
        }
    }

    public Player getClosestPlayer(float x, float y) {
        if (!isMultiplayer || player2 == null || player2.isDying) return player1;
        if (player1.isDying) return player2;

        double d1 = Math.hypot(player1.getX() - x, player1.getY() - y);
        double d2 = Math.hypot(player2.getX() - x, player2.getY() - y);
        return (d1 < d2) ? player1 : player2;
    }

    public void fireHomingShot(float startX, float startY) {
        Player target = getClosestPlayer(startX, startY);
        float bulletSpeed = 8.0f;
        float dx = (target.getX() + target.getWidth() / 2) - startX;
        float dy = (target.getY() + target.getHeight() / 2) - startY;
        double angle = Math.atan2(dy, dx);
        bullets.add(new Bullet(startX, startY, (float) (bulletSpeed * Math.cos(angle)), (float) (bulletSpeed * Math.sin(angle)), true, 6));
    }

    private void enemyShootPattern(Enemy e) {
        float ex = e.getX() + e.getWidth() / 2;
        float ey = e.getY();
        // Spinner Pattern
        if (e.getType() == Enemy.TypesOfEnemies.SPINNER) {
            // Fires 8 bullets in a circle
            for (int angle = 0; angle < 360; angle += 45) {
                double rad = Math.toRadians(angle);
                float speed = 6.0f;
                bullets.add(new Bullet(ex, ey, (float)(Math.cos(rad)*speed), (float)(Math.sin(rad)*speed), true, 6));
            }
            return; // Important: Prevents standard shooting
        }
        if (e.getType() == Enemy.TypesOfEnemies.CIRCLE_PATH) {
            for (int k = 0; k < 360; k += 60) {
                float rad = (float) Math.toRadians(k);
                bullets.add(new Bullet(ex, ey, (float) Math.cos(rad) * 5, (float) Math.sin(rad) * 5, true, 6));
            }
        } else {
            Player target = getClosestPlayer(ex, ey);
            float dx = (target.getX() + target.getWidth() / 2) - ex;
            float dy = (target.getY() + target.getHeight() / 2) - ey;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            bullets.add(new Bullet(ex, ey, (dx / len) * 7, (dy / len) * 7, true, 6));
        }
    }

    private void updateWaveLogic() {
        if (bossActive || isLevelTransitioning) return;
        if (!activeMiddleEnemies.isEmpty()) return;
        if (System.currentTimeMillis() - waveTimer > 5000) {
            waveStep++;
            switch (waveStep) {
                case 1: spawnSquadV(400, 750, 5); break;
                case 2: spawnSquadSide(true, 4); break;
                case 3: spawnSquadSide(false, 4); break;
                case 4: if (!middleWaveSpawned) spawnMiddleEnemies(); else waveStep++; break;
                case 5: spawnBoss(); break;
            }
            waveTimer = System.currentTimeMillis();
        }
    }

    private void spawnMiddleEnemies() {
        activeMiddleEnemies.clear();
        int hp = (currentLevel == 1) ? 100 : (currentLevel == 2) ? 130 : 150;
        int enemyCount = (currentLevel == 1) ? 2 : (currentLevel == 2) ? 3 : 4;
        float spacing = 800f / (enemyCount + 1);
        for (int i = 0; i < enemyCount; i++) {
            float x = spacing * (i + 1);
            int type = (i % 2) + 1;
            MiddleEnemy me = new MiddleEnemy(x, 750, type, currentLevel);

            if (isMultiplayer) {
                me.health = (int)(hp * 1.8);
                me.maxHealth = me.health;
            } else {
                me.health = hp;
                me.maxHealth = hp;
            }

            me.shotDelay = (1200 - (currentLevel * 100)) + (i * 150);
            activeMiddleEnemies.add(me);
        }
        middleWaveSpawned = true;
    }

    private void spawnBoss() {
        boss = new Boss(350, 700, currentLevel);

        bossEnraged = false;
        lastMinionSpawnTime = System.currentTimeMillis();

        if (isMultiplayer) {
            boss.maxHealth *= 2;
            boss.health = boss.maxHealth;
        }

        bossActive = true;
        bossDeathSoundPlayed = false;
        bullets.clear();
    }

    private void handleBossDefeat() {
        bossActive = false; boss = null; score += 500;
        enemies.clear(); activeMiddleEnemies.clear(); bullets.clear(); items.clear();
        isLevelTransitioning = true;
        player1.triggerFlyOff();
        if(isMultiplayer && player2 != null) player2.triggerFlyOff();
    }

    private void startNextLevel() {
        showLevelScreen = false;
        isLevelTransitioning = false;
        currentLevel++;
        waveStep = 0;
        waveTimer = System.currentTimeMillis();
        middleWaveSpawned = false;

        // Reset Boss timers and variables
        bossActive = false;
        boss = null;
        bossEnraged = false; // Important: Reset Rage Mode
        bossDeathSoundPlayed = false; // Important: Reset sound flag for next boss

        // Cleanup objects
        activeMiddleEnemies.clear();
        bullets.clear();
        items.clear();
        kamikazeEnemies.clear();

        // Reset Player positions
        player1.resetPosition();
        if (!isMultiplayer) {
            player1.setX(360);
        } else {
            player1.setX(300);
        }

        if (isMultiplayer && player2 != null) {
            player2.resetPosition();
        }

        // Recharge abilities
        player1.resetAbilities();
        if (isMultiplayer && player2 != null) player2.resetAbilities();

        // Check for Victory or Continue
        if (currentLevel > 3) {
            gameWon = true;
            // Do not play music here; game over screen handles it
        } else {
            // FIX: Restart background music for the new level
            soundManager.playMusic();
        }
    }

    private int getRandomEnemyTexture() { return 21 + (int) (Math.random() * 3); }
    private void spawnContinuousRandomEnemies() {
        if (isLevelTransitioning) return;
        long currentTime = System.currentTimeMillis();

        // Reduce wait time during boss fight for excitement
        long spawnDelay = (bossActive) ? 2500 : (!activeMiddleEnemies.isEmpty()) ? 3000 : 2000;

        // Multiplayer: Enemies spawn 30% faster
        if (isMultiplayer) {
            spawnDelay = (long)(spawnDelay * 0.7);
        }

        if (currentTime - lastRandomSpawnTime > spawnDelay) {
            double rand = Math.random();

            // 80% Chance for single enemy (Varied)
            if (rand < 0.8) {
                double typeRand = Math.random();
                Enemy.TypesOfEnemies type;

                // New probability distribution
                if (typeRand < 0.3) type = Enemy.TypesOfEnemies.CHASER;         // 30%
                else if (typeRand < 0.5) type = Enemy.TypesOfEnemies.STRAIGHT;  // 20%
                else if (typeRand < 0.7) type = Enemy.TypesOfEnemies.CIRCLE_PATH; // 20%
                else if (typeRand < 0.8) type = Enemy.TypesOfEnemies.ZIGZAG;    // 10%
                else type = Enemy.TypesOfEnemies.SPINNER;


                float spawnX = 50 + (float) (Math.random() * 700);

                // Assign distinct textures for new enemies
                int texIndex = getRandomEnemyTexture();
                if (type == Enemy.TypesOfEnemies.ZIGZAG) texIndex = 22; // Distinct Snake look
                if (type == Enemy.TypesOfEnemies.SPINNER) texIndex = 23; // Distinct Spinner look

                enemies.add(new Enemy(spawnX, 700, 60, type, player1, texIndex));

            } else {
                // 20% Chance for Squads or Special Attacks
                double squadRand = Math.random();

                if (squadRand < 0.3) {
                    // Fast Suicide Attack (Kamikaze)
                    float kX = 50 + (float)(Math.random() * 700);
                    spawnKamikaze(kX, 700);
                }
                else if (squadRand < 0.65) {
                    // V-Shape Squad
                    spawnSquadV(150 + (float) (Math.random() * 500), 750, 5);
                }
                else {
                    // Side Squad
                    spawnSquadSide(Math.random() < 0.5, 4);
                }

                // Slight delay after squad spawn to give player a chance
                lastRandomSpawnTime = currentTime + 3000;
                return;
            }
            lastRandomSpawnTime = currentTime;
        }
    }

    private void spawnSquadV(float centerX, float startY, int count) {
        int squadTexture = getRandomEnemyTexture();
        for (int i = 0; i < count; i++) {
            float offsetX = (i % 2 == 0) ? (i * 40) : -(i * 40);
            float offsetY = i * 30;
            enemies.add(new Enemy(centerX + offsetX, startY + offsetY, 60, Enemy.TypesOfEnemies.SQUAD_V, player1, squadTexture));
        }
    }

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

    public void resetGame() {
        // FIX: Re-create players entirely to clear any stuck movement flags or states
        // Instead of resetting position, recreate player to clear stuck states
        player1 = new Player(300, 50, false);

        if (!isMultiplayer) {
            player1.setX(360);
        } else {
            player1.setX(300);
        }

        // Re-create Player 2 if needed
        if (isMultiplayer) {
            player2 = new Player(500, 50, true);
        } else {
            player2 = null; // Ensure P2 is null in single player
        }

        // Reset all game states
        isLevelTransitioning = false;
        showLevelScreen = false;
        bossEnraged = false;
        kamikazeEnemies.clear();

        enemies.clear();
        activeMiddleEnemies.clear();
        bullets.clear();
        items.clear();

        score = 0;
        currentLevel = 1;
        waveStep = 0;
        bossActive = false;
        boss = null;
        middleWaveSpawned = false;
        gameOver = false;
        gameWon = false;
        isGameRunning = true;

        // Restart music from beginning
        soundManager.stopMusic();
        soundManager.playMusic();
    }

    private void spawnSquadSide(boolean fromLeft, int count) {
        int squadTexture = getRandomEnemyTexture();
        for (int i = 0; i < count; i++) {
            float startX = fromLeft ? -50 - (i * 60) : 850 + (i * 60);
            float startY = 550 + (i * 20);
            Enemy.TypesOfEnemies type = fromLeft ? Enemy.TypesOfEnemies.SQUAD_ENTER_LEFT : Enemy.TypesOfEnemies.SQUAD_ENTER_RIGHT;
            enemies.add(new Enemy(startX, startY, 60, type, player1, squadTexture));
        }
    }

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
}