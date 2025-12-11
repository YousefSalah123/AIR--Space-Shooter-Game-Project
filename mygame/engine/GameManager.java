package com.mygame.engine;

import com.mygame.objects.*;
import javax.media.opengl.GL;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class GameManager {

    // --- 1. الكائنات (Game Objects) ---
    public Player player;
    public ArrayList<Enemy> enemies;
    public ArrayList<Bullet> bullets;
    public ArrayList<GoldCoin> coins;

    // --- 2. الزعيم (Boss) ---
    private Boss boss = null;
    private boolean bossActive = false; // هل الزعيم موجود الآن؟

    // --- 3. حالة اللعبة (Game State) ---
    private int score = 0;
    private int lives = 30;
    private boolean gameOver = false;
    private boolean gameWon = false;

    // --- 4. إدارة المستويات ---
    private int currentLevel = 1;
    private int scoreForNextBoss = 50; // السكور المطلوب لظهور أول زعيم

    // --- 5. التحكم في الوقت والضرب ---
    private long lastSpawnTime = 0;

    // متغيرات الضرب المستمر
    private boolean isFiring = false; // هل زر الضرب مضغوط؟
    private long lastShotTime = 0;    // متى خرجت آخر رصاصة
    private int fireRate = 200;       // سرعة الضرب (كل 200 مللي ثانية)

    public GameManager() {
        // تهيئة القوائم
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        coins = new ArrayList<>();

        // وضع اللاعب في منتصف أسفل الشاشة
        player = new Player(375, 50, 1);
    }

    /**
     * دالة التحديث الرئيسية (Loop)
     */
    public void update() {
        if (gameOver || gameWon) return;

        // --- 1. منطق إطلاق النار التلقائي (Auto Fire) ---
        long currentTime = System.currentTimeMillis();
        // فقط نفحص هل مر الوقت الكافي (fireRate) لإطلاق رصاصة جديدة؟
        if (currentTime - lastShotTime > fireRate) {
            playerShoot();
            lastShotTime = currentTime;
        }

        // 2. تحديث اللاعب
        player.update();

        // 3. تحديث الرصاص
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            if (b.getY() > 600 || b.getY() < -50 || b.getX() < 0 || b.getX() > 800 || !b.isAlive()) {
                bullets.remove(i);
            }
        }

        // ... (باقي الكود: تحديث العملات، الأعداء، الزعيم، التصادم... كما هو تماماً) ...

        // (تأكد أن تنسخ باقي الدالة update من الكود السابق)
        // ...

        // 4. تحديث العملات
        for (int i = coins.size() - 1; i >= 0; i--) {
            GoldCoin c = coins.get(i);
            c.update();
            if (c.getY() < -50 || !c.isAlive()) coins.remove(i);
        }

        // 5. منطق الزعيم والأعداء
        if (bossActive && boss != null) {
            boss.update();
            boss.shootLogic(bullets);
            if (!boss.isAlive()) handleBossDefeat();
        } else {
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy e = enemies.get(i);
                e.update();
                if (e.readyToFire()) bullets.add(new Bullet(e.getX() + e.width/2, e.getY(), true));
                if (e.getY() < -50 || !e.isAlive()) enemies.remove(i);
            }
            if (score >= scoreForNextBoss && enemies.isEmpty()) spawnBoss();
            else if (score < scoreForNextBoss) spawnEnemies();
        }

        checkCollisions();
    }

    /**
     * دالة الرسم: ترسم كل شيء
     */
    public void render(GL gl) {
        if (!gameOver) {
            player.render(gl);
        }

        for (Bullet b : bullets) b.render(gl);
        for (GoldCoin c : coins) c.render(gl);

        if (bossActive && boss != null) {
            boss.render(gl);
        } else {
            for (Enemy e : enemies) e.render(gl);
        }
    }

    /**
     * منطق التصادم الشامل
     */
    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();

        // --- أولاً: فحص الرصاص ---
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            if (!b.isAlive()) continue;
            Rectangle bulletBounds = b.getBounds();

            // أ. رصاصة لاعب (تضرب الأعداء أو الزعيم)
            if (!b.isEnemyBullet()) {

                // 1. ضد الزعيم
                if (bossActive && boss != null) {
                    if (bulletBounds.intersects(boss.getBounds())) {
                        b.setAlive(false);
                        boss.takeDamage();
                    }
                }
                // 2. ضد الأعداء العاديين
                else {
                    for (Enemy e : enemies) {
                        if (e.isAlive() && bulletBounds.intersects(e.getBounds())) {
                            b.setAlive(false);
                            e.setAlive(false);
                            score += 10;
                            break;
                        }
                    }
                }
            }
            // ب. رصاصة عدو/زعيم (تضرب اللاعب)
            else {
                if (bulletBounds.intersects(playerBounds)) {
                    b.setAlive(false);
                    playerHit();
                }
            }
        }

        // --- ثانياً: فحص اصطدام الأجساد ---
        if (bossActive && boss != null) {
            if (playerBounds.intersects(boss.getBounds())) {
                playerHit();
            }
        } else {
            for (Enemy e : enemies) {
                if (e.isAlive() && playerBounds.intersects(e.getBounds())) {
                    e.setAlive(false);
                    playerHit();
                }
            }
        }

        // --- ثالثاً: تجميع العملات ---
        for (GoldCoin c : coins) {
            if (c.isAlive() && c.getBounds().intersects(playerBounds)) {
                c.setAlive(false);
                score += 50;
                System.out.println("Coin Collected! Score: " + score);
            }
        }
    }

    private void handleBossDefeat() {
        bossActive = false;
        boss = null;
        score += 1000 * currentLevel;
        System.out.println("LEVEL " + currentLevel + " COMPLETE!");

        if (currentLevel < 3) {
            currentLevel++;
            scoreForNextBoss = score + 50;
            System.out.println("STARTING LEVEL " + currentLevel);
        } else {
            gameWon = true;
            System.out.println("YOU WIN THE GAME!");
        }
    }

    private void spawnBoss() {
        System.out.println("WARNING: BOSS LEVEL " + currentLevel + " APPROACHING!");
        boss = new Boss(350, 700, currentLevel);
        bossActive = true;
        enemies.clear();
        bullets.clear();
    }

    private void spawnEnemies() {
        long currentTime = System.currentTimeMillis();
        // زيادة الصعوبة: كلما زاد المستوى، قل وقت الانتظار
        int spawnRate = 1500 - ((currentLevel - 1) * 400);

        if (currentTime - lastSpawnTime > spawnRate) {
            Random rand = new Random();
            float randX = rand.nextInt(750);
            int chance = rand.nextInt(100);

            if (chance < 10) {
                coins.add(new GoldCoin(randX, 600));
            }
            else if (chance < 30 + (currentLevel * 10)) {
                enemies.add(new Enemy(randX, 600, 30, 2, player)); // Chaser
            }
            else {
                float randSize = 30 + rand.nextInt(30);
                int randType = rand.nextInt(2);
                enemies.add(new Enemy(randX, 600, randSize, randType, player));
            }
            lastSpawnTime = currentTime;
        }
    }

    private void playerHit() {
        lives--;
        System.out.println("Player Hit! Lives: " + lives);
        if (lives <= 0) {
            gameOver = true;
            System.out.println("GAME OVER");
        }
    }

    // دالة تفعيل/إيقاف الضرب (يستدعيها الـ Listener)
    public void setFiring(boolean firing) {
        this.isFiring = firing;
    }

    // دالة إطلاق رصاصة واحدة
    private void playerShoot() {
        if (!gameOver && !gameWon) {
            bullets.add(new Bullet(player.getX() + 22, player.getY() + 50, false));
        }
    }

    // Getters
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public int getCurrentLevel() { return currentLevel; }
}