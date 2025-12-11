package mygame.engine;

import mygame.objects.*;

import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * GameManager is the heart of the game. Responsibilities:
 * - Manage the player, enemies, bullets, items, and bosses
 * - Update game state every frame
 * - Render all game elements
 * - Handle collisions, levels, and special attacks
 */
public class GameManager {

    // --- Main game objects ---
    public Player player;
    public ArrayList<Enemy> enemies;
    public ArrayList<Bullet> bullets;
    public ArrayList<Item> items;

    // --- Game state ---
    public int score = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;
    public boolean isGameRunning = false; // Game starts paused

    // --- Level & boss management ---
    private int currentLevel = 1;
    private int scoreForNextBoss = 150;
    private boolean bossActive = false;
    private Boss boss = null;

    // --- Timing ---
    private long lastSpawnTime = 0;
    private long lastAutoShotTime = 0;
    private int fireRate = 300; // Auto fire rate in ms

    // --- Constructor ---
    public GameManager() {
        player = new Player(375, 50); // Initial player position
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        items = new ArrayList<>();
    }

    // ==========================
    // Update the game state every frame
    // ==========================
    public void update() {
        if (!isGameRunning || gameOver || gameWon) return;

        long currentTime = System.currentTimeMillis();

        // Update player
        player.update();

        // Auto-fire bullets if special/laser not active
        if (!player.isSpecialAttackActive && !player.isLaserBeamActive
                && currentTime - lastAutoShotTime > fireRate) {
            playerShoot();
            lastAutoShotTime = currentTime;
        }

        // Spawn special bullets if special attack is active
        if (player.isSpecialAttackActive) {
            spawnSpecialBullets();
        }

        // Destroy all enemies if special attack used
        if (!player.isSpecialAttackActive && !player.specialAttackUsedOnEnemies) {
            for (Enemy e : enemies) {
                if (e.isAlive()) {
                    e.setAlive(false);
                    score += 10;
                }
            }
            enemies.clear();
            player.specialAttackUsedOnEnemies = true;
        }

        // --- Update bullets ---
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            if (!b.isAlive()) bullets.remove(i);
        }

        // --- Update items ---
        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            item.update();

            // Player picks up item
            if (player.getBounds().intersects(item.getBounds())) {
                applyItem(item.getType());
                items.remove(i);
            } else if (item.getY() < -50) {
                items.remove(i); // Remove items off-screen
            }
        }

        // --- Update boss if active ---
        if (bossActive && boss != null) {
            boss.update();
            boss.shootLogic(bullets);

            if (!boss.isAlive()) handleBossDefeat();
        } else {
            // Check for boss spawn
            if (score >= scoreForNextBoss && !bossActive) {
                spawnBoss();
            }
        }

        // --- Update normal enemies ---
        spawnEnemiesLogic(); // Enemies spawn continuously even during boss fight

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update();

            if (e.readyToFire()) {
                bullets.add(new Bullet(e.getX() + e.getWidth() / 2, e.getY(), true));
            }

            if (!e.isAlive()) enemies.remove(i);
        }

        // --- Check collisions ---
        checkCollisions();
    }

    // ==========================
    // Render the game
    // ==========================
    public void render(GL gl) {
        if (!isGameRunning) {
            drawStartScreen(gl);
            return;
        }

        if (!gameOver && !gameWon) {
            player.render(gl);
            for (Bullet b : bullets) b.render(gl);
            for (Enemy e : enemies) e.render(gl);
            for (Item item : items) item.render(gl);
            if (bossActive && boss != null) boss.render(gl);

            drawPlayerPowerIndicators(gl); // Draw shields, laser, special attack
        } else if (gameWon) {
            gl.glClearColor(0, 0.5f, 0, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        } else {
            gl.glClearColor(0.5f, 0, 0, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }
    }

    // ==========================
    // Collision handling
    // ==========================
    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();

        // Boss laser hits player
        if (bossActive && boss != null && boss.isFiringLaser) {
            if (boss.getLaserBounds().intersects(playerBounds)) playerTakeDamage();
        }

        // Player laser beam
        if (player.isLaserBeamActive) {
            Rectangle laserRect = player.getLaserBounds();

            if (bossActive && boss != null && laserRect.intersects(boss.getBounds())) {
                boss.takeDamage();
            }

            for (Enemy e : enemies) {
                if (e.isAlive() && laserRect.intersects(e.getBounds())) {
                    e.setAlive(false);
                    score += 10;
                    spawnRandomItem(e.getX(), e.getY());
                }
            }

            for (Bullet b : bullets) {
                if (b.isEnemyBullet() && laserRect.intersects(b.getBounds())) {
                    b.setAlive(false);
                }
            }
        }

        // Normal bullet collisions
        for (Bullet b : bullets) {
            if (!b.isAlive()) continue;
            Rectangle bRect = b.getBounds();

            if (!b.isEnemyBullet()) {
                if (bossActive && boss != null && bRect.intersects(boss.getBounds())) {
                    boss.takeDamage();
                    b.setAlive(false);
                } else {
                    for (Enemy e : enemies) {
                        if (e.isAlive() && bRect.intersects(e.getBounds())) {
                            e.setAlive(false);
                            b.setAlive(false);
                            score += 10;
                            spawnRandomItem(e.getX(), e.getY());
                            break;
                        }
                    }
                }
            } else if (bRect.intersects(playerBounds)) {
                b.setAlive(false);
                playerTakeDamage();
            }
        }

        // Player collides with enemies
        for (Enemy e : enemies) {
            if (e.isAlive() && playerBounds.intersects(e.getBounds())) {
                e.setAlive(false);
                playerTakeDamage();
            }
        }

        // Player collides with boss
        if (bossActive && boss != null && playerBounds.intersects(boss.getBounds())) {
            playerTakeDamage();
            boss.takeDamage();
        }
    }

    // ==========================
    // Spawn random items
    // ==========================
    private void spawnRandomItem(float x, float y) {
        if (Math.random() > 0.40) return;

        int rand = new Random().nextInt(100);
        Item.ItemType type;

        if (currentLevel == 1) {
            if (rand < 50) type = Item.ItemType.HEALTH;
            else if (rand < 80) type = Item.ItemType.GOLD_COIN;
            else type = Item.ItemType.RAPID_FIRE;
        } else {
            if (rand < 30) type = Item.ItemType.RAPID_FIRE;
            else if (rand < 50) type = Item.ItemType.HEALTH;
            else if (rand < 80) type = Item.ItemType.GOLD_COIN;
            else type = Item.ItemType.RAPID_FIRE;
        }

        items.add(new Item(x, y, type));
    }

    // ==========================
    // Apply item effects
    // ==========================
    private void applyItem(Item.ItemType type) {
        switch (type) {
            case HEALTH:
                player.setHealth(Math.min(Player.MAX_HEALTH, player.getHealth() + 1));
                System.out.println("Extra Life! Health is now: " + player.getHealth());
                break;
            case RAPID_FIRE:
                player.upgradeWeapon();
                break;
            case GOLD_COIN:
                score += 5;
                break;
        }
    }

    // ==========================
    // Boss defeat handling
    // ==========================
    private void handleBossDefeat() {
        bossActive = false;
        boss = null;
        score += 170;

        player.specialAttackAvailable = true;

        System.out.println("Boss Defeated! Level " + currentLevel + " Complete.");
        currentLevel++;

        if (currentLevel > 3) {
            gameWon = true;
            System.out.println("CONGRATULATIONS! YOU WON THE GAME!");
            return;
        }

        scoreForNextBoss += 200 * currentLevel;

        // Boss rewards
        player.setHealth(Math.min(Player.MAX_HEALTH, player.getHealth() + 2));
        player.refillLaser();
        player.addShieldInventory();
    }

    // ==========================
    // Enemy spawn logic
    // ==========================
    private void spawnEnemiesLogic() {
        long currentTime = System.currentTimeMillis();
        long spawnDelay = bossActive ? 2000 : 500;

        if (currentTime - lastSpawnTime > spawnDelay) {
            Enemy.TypesOfEnemies type;

            if (bossActive) type = Enemy.TypesOfEnemies.STRAIGHT;
            else {
                double rand = Math.random();
                if (rand < 0.5) type = Enemy.TypesOfEnemies.STRAIGHT;
                else if (rand < 0.8) type = Enemy.TypesOfEnemies.CHASER;
                else type = Enemy.TypesOfEnemies.WAVY;
            }

            int numEnemies = 1 + (int) (Math.random() * 2);
            for (int i = 0; i < numEnemies; i++) {
                float spawnX = 50 + (float) (Math.random() * (800 - 50));
                float spawnY = 600;
                enemies.add(new Enemy(spawnX, spawnY, 40, type, player));
            }

            lastSpawnTime = currentTime;
        }
    }

    // ==========================
    // Spawn boss
    // ==========================
    private void spawnBoss() {
        System.out.println("WARNING: BOSS APPROACHING!");
        boss = new Boss(350, 700, currentLevel);
        bossActive = true;
        bullets.clear(); // clear old bullets
    }

    // ==========================
    // Player shoots normal bullets
    // ==========================
    public void playerShoot() {
        float startX = player.getX() + player.getWidth() / 2 - 5;
        float startY = player.getY() + player.getHeight();

        if (player.weaponLevel == 1) bullets.add(new Bullet(startX, startY, 0, 15, false));
        else if (player.weaponLevel == 2) {
            bullets.add(new Bullet(startX - 15, startY, 0, 15, false));
            bullets.add(new Bullet(startX + 15, startY, 0, 15, false));
        } else { // Level 3
            bullets.add(new Bullet(startX, startY, 0, 15, false));
            bullets.add(new Bullet(startX, startY, -4, 14, false));
            bullets.add(new Bullet(startX, startY, 4, 14, false));
        }
    }

    // ==========================
    // Fire laser
    // ==========================
    public void fireLaser() {
        player.activateLaserBeam();
    }

    // ==========================
    // Activate shield
    // ==========================
    public void activateShield() {
        player.activateShieldManual();
    }

    // ==========================
    // Player takes damage
    // ==========================
    public void playerTakeDamage() {
        if (!isGameRunning) return;

        if (player.isShieldActive) {
            player.activateShieldManual();
            System.out.println("Shield absorbed the damage!");
            return;
        }

        player.setHealth(player.getHealth() - 2);
        System.out.println("Player health: " + player.getHealth());

        if (player.getHealth() <= 0) {
            player.setAlive(false);
            gameOver = true;
            System.out.println("GAME OVER!");
        }
    }

    // ==========================
    // Draw indicators (shield, laser, special)
    // ==========================
    private void drawPlayerPowerIndicators(GL gl) {
        float baseX = 20;
        float baseY = 20;

        // Shield
        gl.glColor3f(player.isShieldAvailable ? 0.0f : 0.3f,
                player.isShieldAvailable ? 1.0f : 0.3f,
                player.isShieldAvailable ? 1.0f : 0.3f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(baseX, baseY);
        gl.glVertex2f(baseX + 20, baseY);
        gl.glVertex2f(baseX + 20, baseY + 20);
        gl.glVertex2f(baseX, baseY + 20);
        gl.glEnd();

        // Laser
        gl.glColor3f(player.isLaserAvailable ? 0.0f : 0.3f,
                player.isLaserAvailable ? 1.0f : 0.3f,
                player.isLaserAvailable ? 0.0f : 0.3f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(baseX + 30, baseY);
        gl.glVertex2f(baseX + 50, baseY);
        gl.glVertex2f(baseX + 50, baseY + 20);
        gl.glVertex2f(baseX + 30, baseY + 20);
        gl.glEnd();

        // Special attack
        gl.glColor3f(player.specialAttackAvailable ? 1.0f : 0.3f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(baseX + 60, baseY);
        gl.glVertex2f(baseX + 80, baseY);
        gl.glVertex2f(baseX + 80, baseY + 20);
        gl.glVertex2f(baseX + 60, baseY + 20);
        gl.glEnd();
    }

    // ==========================
    // Draw start screen
    // ==========================
    private void drawStartScreen(GL gl) {
        // Background
        gl.glColor3f(0.1f, 0.1f, 0.1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(800, 0);
        gl.glVertex2f(800, 600);
        gl.glVertex2f(0, 600);
        gl.glEnd();

        // Play button (green triangle)
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(350, 250);
        gl.glVertex2f(350, 350);
        gl.glVertex2f(450, 300);
        gl.glEnd();

        // Optional frame around the button
        gl.glColor3f(1, 1, 1);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(300, 200);
        gl.glVertex2f(500, 200);
        gl.glVertex2f(500, 400);
        gl.glVertex2f(300, 400);
        gl.glEnd();
    }

    // ==========================
    // Spawn special bullets
    // ==========================
    private void spawnSpecialBullets() {
        int numBullets = 5;
        float screenWidth = 800;
        float startY = 0;

        for (int i = 0; i < numBullets; i++) {
            float startX = (i + 0.5f) * (screenWidth / numBullets);
            float speedY = 10 + (float) (Math.random() * 5);
            float speedX = -3 + (float) (Math.random() * 6);
            bullets.add(new Bullet(startX, startY, speedX, speedY, false));
        }
    }
}