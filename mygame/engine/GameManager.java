package com.mygame.engine;

import com.mygame.objects.*;
import javax.media.opengl.GL;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class GameManager {

    public Player player;
    public ArrayList<Enemy> enemies;
    public ArrayList<Bullet> bullets;
    public ArrayList<PowerUp> powerUps;

    public int score = 0;
    public int lives = 100;
    private boolean gameOver = false;
    private boolean gameWon = false;

    // إدارة المستويات
    private int currentLevel = 1;
    private int scoreForNextBoss = 150;
    private boolean bossActive = false;
    private Boss boss = null;

    private long lastSpawnTime = 0;
    private long lastAutoShotTime = 0; // وقت آخر طلقة خرجت
    private int fireRate = 300;
    public boolean isGameRunning = false; // اللعبة واقفة في البداية

    public GameManager() {
        player = new Player(375, 50);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        powerUps = new ArrayList<>();
    }

    public void update() {
        if (!isGameRunning) return;

        if (gameOver || gameWon) return;

        // --- كود الضرب التلقائي (Auto Fire) ---
        long currentTime = System.currentTimeMillis();

        // هل مر الوقت الكافي لإطلاق رصاصة جديدة؟
        if (currentTime - lastAutoShotTime > fireRate) {
            playerShoot(); // أطلق النار
            lastAutoShotTime = currentTime; // سجل وقت الطلقة دي
        }

        player.update();

        // 1. تحديث الرصاص
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            if (!b.isAlive()) bullets.remove(i);
        }

        // 2. تحديث الهدايا
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp p = powerUps.get(i);
            p.update();
            if (player.getBounds().intersects(p.getBounds())) {
                applyPowerUp(p.getType());
                powerUps.remove(i);
            } else if (p.getY() < -50) powerUps.remove(i);
        }

        // 3. منطق الزعيم (Boss)
        if (bossActive && boss != null) {
            boss.update();
            boss.shootLogic(bullets);
            if (!boss.isAlive()) handleBossDefeat();
        } else {
            // التحقق من ظهور الزعيم
            if (score >= scoreForNextBoss && !bossActive) {
                spawnBoss();
            }
        }

        // 4. منطق الأعداء العاديين (يتم استدعاؤه دائمًا حتى لو البوس موجود)
        spawnEnemiesLogic(); // <--- التغيير هنا: التدفق لا يتوقف

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update();
            if (e.readyToFire()) bullets.add(new Bullet(e.getX() + e.getWidth()/2, e.getY(), true));
            if (!e.isAlive()) enemies.remove(i);
        }

        // 5. التصادمات
        checkCollisions();
    }

    public void render(GL gl) {
        if (!isGameRunning) {
            drawStartScreen(gl);
            return; // ومتكملش رسم اللعبة
        }

        if (!gameOver && !gameWon) {
            player.render(gl);
            for (Bullet b : bullets) b.render(gl);
            for (Enemy e : enemies) e.render(gl);
            for (PowerUp p : powerUps) p.render(gl);
            if (bossActive && boss != null) boss.render(gl);
        } else if (gameWon) {
            // هنا ممكن ترسم رسالة الفوز لو عندك TextRenderer
            // حالياً هنكتفي بالكونسول ونخلي الشاشة خضراء مثلاً
            gl.glClearColor(0, 0.5f, 0, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        } else if (gameOver) {
            gl.glClearColor(0.5f, 0, 0, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }
    }

    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();
        if (bossActive && boss != null && boss.isFiringLaser) {
            // لو اللاعب لمس ليزر الزعيم
            if (boss.getLaserBounds().intersects(playerBounds)) {
                handlePlayerHit(); // اللاعب ينضرب
            }
        }
        // تصادم الليزر المستمر
        if (player.isLaserBeamActive) {
            Rectangle laserRect = player.getLaserBounds();
            if (bossActive && boss != null && laserRect.intersects(boss.getBounds())) {
                boss.takeDamage();
            }
            for (Enemy e : enemies) {
                if (e.isAlive() && laserRect.intersects(e.getBounds())) {
                    e.setAlive(false);
                    score += 10;
                    spawnRandomPowerUp(e.getX(), e.getY());
                }
            }
            for (Bullet b : bullets) {
                if (b.isEnemyBullet() && laserRect.intersects(b.getBounds())) b.setAlive(false);
            }
        }

        // تصادم الرصاص العادي
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
                            spawnRandomPowerUp(e.getX(), e.getY());
                            break;
                        }
                    }
                }
            } else {
                if (bRect.intersects(playerBounds)) {
                    b.setAlive(false);
                    handlePlayerHit();
                }
            }
        }

        // تصادم الأجساد
        if (!player.isShieldActive) {
            for (Enemy e : enemies) {
                if (e.isAlive() && playerBounds.intersects(e.getBounds())) {
                    e.setAlive(false);
                    handlePlayerHit();
                }
            }
            if (bossActive && boss != null && playerBounds.intersects(boss.getBounds())) {
                handlePlayerHit();
            }
        }
    }

    private void spawnRandomPowerUp(float x, float y) {
        if (Math.random() > 0.40) return; // نسبة السقوط 40%

        int rand = new Random().nextInt(100);
        int type;

        // --- توزيع الهدايا حسب المستوى ---

        if (currentLevel == 1) {
            // في المستوى الأول: ممنوع ترقية السلاح (Triple Shot)
            // البديل: نكثر القلوب والليزر شوية
            if (rand < 50) type = 0;       // 50% قلوب (عشان البداية تكون سهلة)
            else if (rand < 80) type = 3;  // 30% ذخيرة ليزر (ممتعة ومحدودة)
            else type = 2;                 // 20% درع
            // لاحظ مفيش type = 1 هنا خالص
        }
        else {
            // في المستويات العليا (2 و 3): كل حاجة متاحة
            if (rand < 30) type = 1;       // 30% ترقية سلاح (الآن متاحة!)
            else if (rand < 50) type = 0;  // 20% قلوب
            else if (rand < 80) type = 3;  // 30% ليزر
            else type = 2;                 // 20% درع
        }

        powerUps.add(new PowerUp(x, y, type));
    }

    private void applyPowerUp(int type) {
        switch (type) {
            case 0: lives++; System.out.println("Extra Life!"); break;
            case 1: player.upgradeWeapon(); break;
            case 2: player.addShieldInventory(); break; // يضاف للمخزون
        }
    }

    private void handleBossDefeat() {
        bossActive = false;
        boss = null;
        score += 170;

        System.out.println("Boss Defeated! Level " + currentLevel + " Complete.");
        currentLevel++; // زيادة المستوى

        // شرط الفوز النهائي
        if (currentLevel > 3) {
            gameWon = true;
            System.out.println("CONGRATULATIONS! YOU WON THE GAME!");
            return;
        }

        scoreForNextBoss += 200 * currentLevel;

        // مكافآت قتل الزعيم
        lives += 2; // قلبين هدية
        player.refillLaser();
        player.addShieldInventory(); // درع هدية للمخزون
    }

    private void handlePlayerHit() {
        if (!player.isShieldActive) {
            lives--;
            System.out.println("Hit! Lives left: " + lives);
            if (lives <= 0) gameOver = true;
        }
    }
    private void spawnEnemiesLogic() {
        long currentTime = System.currentTimeMillis();

        // --- التحكم في الزحمة ---
        // لو الزعيم موجود: نزل عدو كل 4000 مللي ثانية (4 ثواني) -> هدوء
        // لو عادي: نزل عدو كل 1000 مللي ثانية (ثانية واحدة) -> أكشن
        long spawnDelay = bossActive ? 4000 : 1000;

        if (currentTime - lastSpawnTime > spawnDelay) {

            // نزل عدو جديد
            // (يفضل والزعيم موجود ننزل النوع 1 الضعيف بس، عشان متبقاش مستحيلة)
            int type;
            if (bossActive) {
                type = 1; // عدو عادي مستقيم
            } else {
                // تنويع عادي
                double rand = Math.random();
                if (rand < 0.6) type = 1;
                else if (rand < 0.8) type = 2;
                else type = 3;
            }

            // تحديد مكان عشوائي
            float spawnX = (float)(Math.random() * 750);
            if (type == 3) spawnX = 100 + (float)(Math.random() * 550); // عشان الموجة

            enemies.add(new Enemy(spawnX, 650, 40, type, player));

            lastSpawnTime = currentTime;
        }
    }

    private void spawnBoss() {
        System.out.println("WARNING: BOSS APPROACHING!");
        boss = new Boss(350, 700, currentLevel);
        bossActive = true;
        // مسحت enemies.clear() عشان يفضلوا موجودين
        bullets.clear();
    }

    // دوال التحكم
    public void playerShoot() {
        float startX = player.getX() + player.getWidth() / 2 - 5;
        float startY = player.getY() + player.getHeight();
        if (player.weaponLevel == 1) bullets.add(new Bullet(startX, startY, 0, 15, false));
        else if (player.weaponLevel == 2) {
            bullets.add(new Bullet(startX - 15, startY, 0, 15, false));
            bullets.add(new Bullet(startX + 15, startY, 0, 15, false));
        } else {
            bullets.add(new Bullet(startX, startY, 0, 15, false));
            bullets.add(new Bullet(startX, startY, -4, 14, false));
            bullets.add(new Bullet(startX, startY, 4, 14, false));
        }
    }

    public void fireLaser() { player.activateLaserBeam(); }
    public void activateShield() { player.activateShieldManual(); } // دالة جديدة
    private void drawStartScreen(GL gl) {
        // 1. خلفية غامقة شوية
        gl.glColor3f(0.1f, 0.1f, 0.1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(800, 0);
        gl.glVertex2f(800, 600);
        gl.glVertex2f(0, 600);
        gl.glEnd();

        // 2. زرار Play (مثلث أخضر كبير في النص)
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(350, 250); // الركن السفلي الأيسر
        gl.glVertex2f(350, 350); // الركن العلوي الأيسر
        gl.glVertex2f(450, 300); // الركن الأيمن (السهم)
        gl.glEnd();

        // (اختياري) دائرة حول المثلث
        gl.glColor3f(1, 1, 1);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(300, 200);
        gl.glVertex2f(500, 200);
        gl.glVertex2f(500, 400);
        gl.glVertex2f(300, 400);
        gl.glEnd();
    }
}