package com.mygame.engine;

import com.mygame.objects.*;
import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GameManager {
    // ... (المتغيرات كما هي) ...
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
    private int currentLevel = 1;
    private int waveStep = 0;
    private long waveTimer = 0;
    private boolean bossActive = false;
    private Boss boss = null;
    private boolean isLevelTransitioning = false;
    private long lastAutoShotTime = 0;
    private int fireRate = 300;
    private long lastRandomSpawnTime = 0;

    public GameManager() {
        player = new Player(375, 50);
        activeMiddleEnemies = new ArrayList<>();
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        items = new ArrayList<>();
    }

    public void update() {
        if (!isGameRunning || gameOver || gameWon) return;

        // ------------------------------------------
        // التحقق من حالة موت اللاعب (تعديل مهم)
        // ------------------------------------------
        if (player.isDying) {
            // إذا انتهى الأنيميشن بالكامل، نعلن انتهاء اللعبة
            if (player.animationFinished) {
                player.setAlive(false);
                gameOver = true;
            }
            // لا نوقف التحديث بالكامل لكي يستمر رسم الأنيميشن،
            // لكن نمنع تحديث حركة اللاعب داخل الكلاس الخاص به
            // لا نعمل Return هنا لنسمح برسم الانيميشن في render
        }

        // ... (باقي كود التحديث كما هو) ...
        long currentTime = System.currentTimeMillis();

        player.update();
        if (isLevelTransitioning) {
            if (player.getY() > 700) startNextLevel();
            return;
        }

        // منع إطلاق النار إذا كان اللاعب يموت
        if (!player.isFlyingOff && !player.isSpecialAttackActive && !player.isLaserBeamActive
                && !player.isDying // <-- إضافة هذا الشرط
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
            if (player.getBounds().intersects(item.getBounds())) {
                applyItem(item.getType());
                items.remove(i);
            } else if (item.getY() < -50) items.remove(i);
        }

        // 5. Update Boss (Modified for Death Animation)
        if (bossActive && boss != null) {
            boss.update(); // تحديث الحركة والأنيميشن
            boss.shootLogic(bullets); // إطلاق النار (لن يعمل إذا كان Boss.isDying)

            // التحقق مما إذا كان الزعيم مات وانتهى العرض
            if (boss.isDying && boss.animationFinished) {
                boss.setAlive(false); // الآن نقتله فعلياً
                handleBossDefeat();   // ننتقل للمستوى التالي
            }
        }

        // 6. Update Middle Enemies
        for (int i = activeMiddleEnemies.size() - 1; i >= 0; i--) {
            MiddleEnemy me = activeMiddleEnemies.get(i);
            me.update(800);
            if (currentTime - me.lastShotTime > me.shotDelay) {
                if (me.type == 1) fireFanShots(me.x, me.y);
                else fireHomingShot(me.x, me.y);
                me.lastShotTime = currentTime;
            }
            if (me.health <= 0) {
                score += 500;
                spawnRandomItem(me.x, me.y);
                activeMiddleEnemies.remove(i);
            }
        }

        // 7. Update Regular Enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update();
            if (e.readyToFire()) enemyShootPattern(e);
            if (!e.isAlive()) enemies.remove(i);
        }

        updateWaveLogic();
        spawnContinuousRandomEnemies();
        checkCollisions();
    }

    // ... (باقي الدوال WaveLogic, Spawn, etc. كما هي) ...
    // سأضع فقط الدوال التي تحتاج تغيير مباشر للتوافق مع التعديل

    public void playerTakeDamage() {
        if (!isGameRunning) return;
        if (player.isDying) return; // إذا كان يموت بالفعل لا تضربه مرة أخرى

        if (player.isShieldActive) { player.activateShieldManual(); return; }

        player.setHealth(player.getHealth() - 1);

        if (player.getHealth() <= 0) {
            // التعديل هنا: بدلاً من إنهاء اللعبة فوراً، نبدأ حالة الموت
            player.isDying = true;
            // لا نضع gameOver = true هنا، نتركها للـ update عند انتهاء الأنيميشن
        }
    }

    // تأكد من تمرير textures في render
    public void render(GL gl, int[] textures) {
        if (!isGameRunning) { drawStartScreen(gl); return; }

        if (!gameOver && !gameWon) {
            player.render(gl, textures); // اللاعب يرسم نفسه (حي أو يموت)
            for (Bullet b : bullets) b.render(gl, textures);
            for (Enemy e : enemies) e.render(gl, textures);
            for (Item item : items) item.render(gl, textures);
            for (MiddleEnemy me : activeMiddleEnemies) me.render(gl, textures);
            if (bossActive && boss != null) boss.render(gl, textures);
            drawPlayerPowerIndicators(gl);
        } else if (gameWon) {
            gl.glClearColor(0, 0.5f, 0, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        } else {
            gl.glClearColor(0.5f, 0, 0, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }
    }

    // ... (باقي الكلاس Helper Methods و Collisions كما هي في نسختك السابقة) ...
    // لتوفير المساحة سأفترض أنك تملك باقي الكلاس، فقط استبدل playerTakeDamage و update
    private void updateWaveLogic() { /* نفس الكود السابق */ if (bossActive || isLevelTransitioning) return; if (!activeMiddleEnemies.isEmpty()) return; if (System.currentTimeMillis() - waveTimer > 5000) { waveStep++; switch (waveStep) { case 1: spawnSquadV(400, 750, 5); break; case 2: spawnSquadSide(true, 4); break; case 3: spawnSquadSide(false, 4); break; case 4: if (!middleWaveSpawned) { spawnMiddleEnemies(); } else { waveStep++; } break; case 5: spawnBoss(); break; } waveTimer = System.currentTimeMillis(); } }
    private void spawnMiddleEnemies() { activeMiddleEnemies.clear(); int hp = 25 + (currentLevel * 10); MiddleEnemy left = new MiddleEnemy(250, 750, 1, currentLevel); left.health = hp; left.maxHealth = hp; left.shotDelay = 1200 - (currentLevel * 100); activeMiddleEnemies.add(left); MiddleEnemy right = new MiddleEnemy(550, 750, 2, currentLevel); right.health = hp; right.maxHealth = hp; right.shotDelay = 1600 - (currentLevel * 100); activeMiddleEnemies.add(right); middleWaveSpawned = true; }
    private void spawnBoss() { System.out.println("BOSS LEVEL " + currentLevel + " INCOMING!"); boss = new Boss(350, 700, currentLevel); bossActive = true; bullets.clear(); }
    private void checkCollisions() {
        // 1. حماية اللاعب: إذا كان يموت، لا تحسب أي تصادمات
        if (player.isDying) return;

        Rectangle pRect = player.getBounds();

        // -------------------------------------------------
        // 2. Middle Enemies (تصادم الأعداء المتوسطين)
        // -------------------------------------------------
        for (MiddleEnemy me : activeMiddleEnemies) {
            // تصحيح مكان الـ Hitbox (لأن x,y هما المركز)
            Rectangle meRect = new Rectangle((int) me.x - 30, (int) me.y - 30, 60, 60);

            // أ) تصادم جسد اللاعب مع العدو
            if (pRect.intersects(meRect)) {
                playerTakeDamage();
            }

            // ب) تصادم ليزر اللاعب مع العدو
            if (player.isLaserBeamActive && player.getLaserBounds().intersects(meRect)) {
                me.health -= 2; // ضرر الليزر
            }

            // ج) تصادم رصاص اللاعب مع العدو
            for (Bullet b : bullets) {
                if (!b.isAlive() || b.isEnemyBullet()) continue;
                if (b.getBounds().intersects(meRect)) {
                    me.health -= 2; // ضرر الرصاصة
                    b.setAlive(false);
                }
            }
        }

        // -------------------------------------------------
        // 3. Boss Collision (تصادم الزعيم)
        // -------------------------------------------------
        // نضيف شرط !boss.isDying لمنع ضرب الزعيم وهو ينفجر
        if (bossActive && boss != null && !boss.isDying) {
            // أ) ليزر الزعيم ضد اللاعب
            if (boss.isFiringLaser && boss.getLaserBounds().intersects(pRect)) {
                playerTakeDamage();
            }

            // ب) ليزر اللاعب ضد جسد الزعيم
            if (player.isLaserBeamActive && player.getLaserBounds().intersects(boss.getBounds())) {
                boss.takeDamage();
            }

            // ج) جسد اللاعب ضد جسد الزعيم
            if (pRect.intersects(boss.getBounds())) {
                playerTakeDamage();
                boss.takeDamage();
            }
        }

        // -------------------------------------------------
        // 4. Player Laser vs Regular Enemies & Bullets
        // -------------------------------------------------
        if (player.isLaserBeamActive) {
            Rectangle lRect = player.getLaserBounds();

            // ضد الأعداء العاديين
            for (Enemy e : enemies) {
                if (e.isAlive() && lRect.intersects(e.getBounds())) {
                    e.setAlive(false);
                    score += 10;
                    spawnRandomItem(e.getX(), e.getY());
                }
            }

            // ضد رصاص الأعداء (الليزر يدمر الرصاص)
            for (Bullet b : bullets) {
                if (b.isEnemyBullet() && lRect.intersects(b.getBounds())) {
                    b.setAlive(false);
                }
            }
        }

        // -------------------------------------------------
        // 5. Bullets Logic (منطق الرصاص العام)
        // -------------------------------------------------
        for (Bullet b : bullets) {
            if (!b.isAlive()) continue;
            Rectangle bRect = b.getBounds();

            if (!b.isEnemyBullet()) {
                // --- رصاص اللاعب ---

                // ضد الزعيم (فقط إذا لم يكن يموت)
                if (bossActive && boss != null && !boss.isDying && bRect.intersects(boss.getBounds())) {
                    boss.takeDamage();
                    b.setAlive(false);
                }
                // ضد الأعداء العاديين
                else {
                    for (Enemy e : enemies) {
                        if (e.isAlive() && bRect.intersects(e.getBounds())) {
                            e.setAlive(false);
                            b.setAlive(false);
                            score += 10;
                            spawnRandomItem(e.getX(), e.getY());
                            break; // رصاصة واحدة تقتل عدواً واحداً
                        }
                    }
                }
            } else {
                // --- رصاص العدو ---
                // يضرب اللاعب (تم التحقق من أن اللاعب لا يموت في أول الدالة)
                if (bRect.intersects(pRect)) {
                    b.setAlive(false);
                    playerTakeDamage();
                }
            }
        }

        // -------------------------------------------------
        // 6. Regular Enemies Body vs Player Body
        // -------------------------------------------------
        for (Enemy e : enemies) {
            if (e.isAlive() && pRect.intersects(e.getBounds())) {
                e.setAlive(false); // العدو يموت عند الاصطدام
                playerTakeDamage(); // اللاعب يتضرر
            }
        }
    }
    private void handleBossDefeat() { bossActive = false; boss = null; score += 500; enemies.clear(); activeMiddleEnemies.clear(); bullets.clear(); isLevelTransitioning = true; player.triggerFlyOff(); }
    private void startNextLevel() { isLevelTransitioning = false; currentLevel++; waveStep = 0; waveTimer = System.currentTimeMillis(); middleWaveSpawned = false; activeMiddleEnemies.clear(); bullets.clear(); items.clear(); bossActive = false; boss = null; player.resetPosition(); player.setHealth(Math.min(Player.MAX_HEALTH, player.getHealth() + 20)); if (currentLevel > 3) gameWon = true; }
    private void spawnContinuousRandomEnemies() { if (isLevelTransitioning) return; long currentTime = System.currentTimeMillis(); long spawnDelay = (bossActive) ? 3000 : (!activeMiddleEnemies.isEmpty()) ? 2000 : 800; if (currentTime - lastRandomSpawnTime > spawnDelay) { double rand = Math.random(); if (rand < 0.6) { double typeRand = Math.random(); Enemy.TypesOfEnemies type = (typeRand < 0.4) ? Enemy.TypesOfEnemies.STRAIGHT : (typeRand < 0.7) ? Enemy.TypesOfEnemies.CHASER : Enemy.TypesOfEnemies.CIRCLE_PATH; float spawnX = 50 + (float)(Math.random() * 700); enemies.add(new Enemy(spawnX, 700, 40, type, player)); } else { if (Math.random() < 0.5) { float centerX = 150 + (float)(Math.random() * 500); spawnSquadV(centerX, 750, 5); } else { boolean fromLeft = Math.random() < 0.5; spawnSquadSide(fromLeft, 4); } lastRandomSpawnTime = currentTime + 1500; return; } lastRandomSpawnTime = currentTime; } }
    private void spawnSquadV(float centerX, float startY, int count) { for (int i = 0; i < count; i++) { float offsetX = (i % 2 == 0) ? (i * 40) : -(i * 40); float offsetY = i * 30; enemies.add(new Enemy(centerX + offsetX, startY + offsetY, 40, Enemy.TypesOfEnemies.SQUAD_V, player)); } }
    private void spawnSquadSide(boolean fromLeft, int count) { for (int i = 0; i < count; i++) { float startX = fromLeft ? -50 - (i * 60) : 850 + (i * 60); float startY = 550 + (i * 20); Enemy.TypesOfEnemies type = fromLeft ? Enemy.TypesOfEnemies.SQUAD_ENTER_LEFT : Enemy.TypesOfEnemies.SQUAD_ENTER_RIGHT; enemies.add(new Enemy(startX, startY, 40, type, player)); } }
    public void fireFanShots(float startX, float startY) { int[] angles = {-30, -15, 0, 15, 30}; float bulletSpeed = 7.0f; for (int angle : angles) { double rad = Math.toRadians(angle); float dx = (float) (bulletSpeed * Math.sin(rad)); float dy = (float) (-bulletSpeed * Math.cos(rad)); bullets.add(new Bullet(startX, startY - 20, dx, dy, true)); } }
    public void fireHomingShot(float startX, float startY) { float bulletSpeed = 8.0f; float dx = (player.getX() + player.getWidth()/2) - startX; float dy = (player.getY() + player.getHeight()/2) - startY; double angle = Math.atan2(dy, dx); bullets.add(new Bullet(startX, startY, (float)(bulletSpeed * Math.cos(angle)), (float)(bulletSpeed * Math.sin(angle)), true)); }
    private void enemyShootPattern(Enemy e) { float ex = e.getX() + e.getWidth()/2; float ey = e.getY(); if (e.getType() == Enemy.TypesOfEnemies.CIRCLE_PATH) { for (int k = 0; k < 360; k += 60) { float rad = (float) Math.toRadians(k); bullets.add(new Bullet(ex, ey, (float)Math.cos(rad)*5, (float)Math.sin(rad)*5, true)); } } else { float dx = (player.getX() + player.getWidth()/2) - ex; float dy = (player.getY() + player.getHeight()/2) - ey; float len = (float) Math.sqrt(dx*dx + dy*dy); bullets.add(new Bullet(ex, ey, (dx/len)*7, (dy/len)*7, true)); } }
    private void spawnRandomItem(float x, float y) { if (Math.random() > 0.35) return; int rand = new Random().nextInt(100); Item.ItemType type = Item.ItemType.GOLD_COIN; if (currentLevel == 1) { if (rand < 40) type = Item.ItemType.HEALTH; else if (rand < 70) type = Item.ItemType.RAPID_FIRE; } else { if (rand < 30) type = Item.ItemType.RAPID_FIRE; else if (rand < 50) type = Item.ItemType.HEALTH; } items.add(new Item(x, y, type)); }
    private void applyItem(Item.ItemType type) { switch (type) { case HEALTH: player.setHealth(Math.min(Player.MAX_HEALTH, player.getHealth() + 20)); break; case RAPID_FIRE: player.upgradeWeapon(); break; case GOLD_COIN: score += 100; break; } }
    public void playerShoot() { float sx = player.getX() + player.getWidth() / 2 - 5; float sy = player.getY() + player.getHeight(); if (player.weaponLevel == 1) bullets.add(new Bullet(sx, sy, 0, 15, false)); else if (player.weaponLevel == 2) { bullets.add(new Bullet(sx - 15, sy, 0, 15, false)); bullets.add(new Bullet(sx + 15, sy, 0, 15, false)); } else { bullets.add(new Bullet(sx, sy, 0, 15, false)); bullets.add(new Bullet(sx, sy, -4, 14, false)); bullets.add(new Bullet(sx, sy, 4, 14, false)); } }
    public void fireLaser() { player.activateLaserBeam(); }
    public void activateShield() { player.activateShieldManual(); }
    private void drawPlayerPowerIndicators(GL gl) { float baseX = 20; float baseY = 20; gl.glColor3f(player.isShieldAvailable ? 0 : 0.3f, player.isShieldAvailable ? 1 : 0.3f, player.isShieldAvailable ? 1 : 0.3f); gl.glBegin(GL.GL_QUADS); gl.glVertex2f(baseX, baseY); gl.glVertex2f(baseX + 20, baseY); gl.glVertex2f(baseX + 20, baseY + 20); gl.glVertex2f(baseX, baseY + 20); gl.glEnd(); gl.glColor3f(player.isLaserAvailable ? 0 : 0.3f, player.isLaserAvailable ? 1 : 0.3f, player.isLaserAvailable ? 0 : 0.3f); gl.glBegin(GL.GL_QUADS); gl.glVertex2f(baseX + 30, baseY); gl.glVertex2f(baseX + 50, baseY); gl.glVertex2f(baseX + 50, baseY + 20); gl.glVertex2f(baseX + 30, baseY + 20); gl.glEnd(); gl.glColor3f(player.specialAmmo > 0 ? 1 : 0.3f, player.specialAmmo > 0 ? 0.8f : 0.3f, 0); gl.glBegin(GL.GL_QUADS); gl.glVertex2f(baseX + 60, baseY); gl.glVertex2f(baseX + 80, baseY); gl.glVertex2f(baseX + 80, baseY + 20); gl.glVertex2f(baseX + 60, baseY + 20); gl.glEnd(); }
    private void drawStartScreen(GL gl) { gl.glColor3f(0.1f, 0.1f, 0.1f); gl.glBegin(GL.GL_QUADS); gl.glVertex2f(0, 0); gl.glVertex2f(800, 0); gl.glVertex2f(800, 600); gl.glVertex2f(0, 600); gl.glEnd(); gl.glColor3f(0, 1, 0); gl.glBegin(GL.GL_TRIANGLES); gl.glVertex2f(350, 250); gl.glVertex2f(350, 350); gl.glVertex2f(450, 300); gl.glEnd(); }
    private void spawnSpecialBullets() { for (int i = 0; i < 3; i++) bullets.add(new Bullet((float) (Math.random() * 800), 0, (float) (Math.random() * 6) - 3, 10, false)); }
}