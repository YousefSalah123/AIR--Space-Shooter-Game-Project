package com.mygame.engine;

import com.mygame.objects.*;
import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;


public class GameManager {
    // متغير لتخزين وقت آخر ضربة تلقاها اللاعب
    private long lastDamageTime = 0;
    // متغيرات التحكم في كثافة الأعداء
    private long lastEnemySpawnTime = 0; // وقت آخر ظهور لعدو
    private int baseSpawnDelay = 2000;   // الوقت الأساسي بين كل عدو (2000 = ثانيتين) - زود الرقم عشان تقلل الأعداء أكتر
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
    int currentLevel = 1;
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
        if (player.isDying) return;

        // --- التعديل الجديد: فترة السماح ---
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime < 1000) { // 1000ms = 1 second cooldown
            return; // لو لسه معداش ثانية، متحسبش الضربة واخرج
        }
        lastDamageTime = currentTime; // سجل وقت الضربة دي
        // -----------------------------------

        if (player.isShieldActive) { player.activateShieldManual(); return; }

        player.setHealth(player.getHealth() - 1); // نقص الصحة

        // (اختياري) ممكن تطبع الصحة عشان تتأكد
        System.out.println("Player Hit! HP: " + player.getHealth());

        if (player.getHealth() <= 0) {
            player.isDying = true;
        }
    }
    public void render(GL gl, int[] textures) {
        // 1. شاشة البداية (إذا اللعبة لم تبدأ بعد)
        if (!isGameRunning) {
            drawStartScreen(gl);
            return;
        }

        // اللعبة جارية (ليست خسارة وليست فوز)
        if (!gameOver && !gameWon) {

            // =============================================================
            // 2. رسم اللاعب
            // =============================================================
            // ملاحظة: اللاعب يرسم البار الخاص به داخل كلاس Player.java
            player.render(gl, textures);

            // =============================================================
            // 3. رسم الطلقات (Bullets)
            // =============================================================
            for (Bullet b : bullets) {
                b.render(gl, textures);
            }

            // =============================================================
            // 4. رسم العناصر (Items)
            // =============================================================
            for (Item item : items) {
                item.render(gl, textures);
            }

            // =============================================================
            // 5. رسم الأعداء المتوسطين (Middle Enemies) + Health Bar
            // =============================================================
            for (MiddleEnemy me : activeMiddleEnemies) {
                // أ) رسم صورة العدو
                me.render(gl, textures);

                // ب) رسم شريط الصحة (Manual Rendering)
                gl.glDisable(GL.GL_TEXTURE_2D);

                // إعدادات الحجم (شريط صغير)
                float barTotalWidth = 40f;
                float barHeight = 4f;

                // حساب التمركز: (مكان العدو + نص عرضه) - (نص عرض البار)
                // ملحوظة: نستخدم 60 كعرض تقريبي للعدو المتوسط إذا لم يكن المتغير width متاحاً
                // لو المتغير width متاح في MiddleEnemy استخدم: me.width
                float enemyWidth = 35;
                float barStartX = me.x -19;
                float barStartY = me.y + enemyWidth; // فوق العدو بمسافة

                float percent = (float)me.health / (float)me.maxHealth;

                // الخلفية الحمراء
                gl.glColor3f(0.6f, 0.0f, 0.0f);
                gl.glRectf(barStartX, barStartY, barStartX + barTotalWidth, barStartY + barHeight);

                // النسبة الخضراء
                if (percent > 0) {
                    gl.glColor3f(0.0f, 1.0f, 0.0f);
                    gl.glRectf(barStartX, barStartY, barStartX + (barTotalWidth * percent), barStartY + barHeight);
                }

                // استعادة التكستشر
                gl.glColor3f(1, 1, 1);
                gl.glEnable(GL.GL_TEXTURE_2D);
            }

            // =============================================================
            // 6. رسم الزعيم (Boss)
            // =============================================================
            if (bossActive && boss != null) {
                boss.render(gl, textures);
                // الزعيم عادة يرسم البار الخاص به داخلياً أو يمكنك إضافته هنا بنفس الطريقة
            }

            // =============================================================
            // 7. رسم الأعداء العاديين (Enemies) + Health Bar
            // =============================================================
            for (Enemy e : enemies) {
                // أ) رسم صورة العدو
                e.render(gl, textures);

                // ب) رسم شريط الصحة المصغر
                gl.glDisable(GL.GL_TEXTURE_2D);

                float barTotalWidth = 30f;  // أصغر قليلاً من المتوسط
                float barHeight = 4f;

                // تمركز البار
                float barStartX = e.getX() + (e.width - barTotalWidth) / 2;
                float barStartY = e.getY() + e.height + 5;

                float percent = (float)e.health / (float)e.maxHealth;

                // رسم الخلفية (أحمر)
                gl.glColor3f(0.6f, 0.0f, 0.0f);
                gl.glRectf(barStartX, barStartY, barStartX + barTotalWidth, barStartY + barHeight);

                // رسم الصحة (أخضر)
                if (percent > 0) {
                    gl.glColor3f(0.0f, 1.0f, 0.0f);
                    gl.glRectf(barStartX, barStartY, barStartX + (barTotalWidth * percent), barStartY + barHeight);
                }

                // استعادة التكستشر والألوان
                gl.glColor3f(1, 1, 1);
                gl.glEnable(GL.GL_TEXTURE_2D);
            }

            // =============================================================
            // 8. رسم واجهة المستخدم (HUD)
            // =============================================================
            drawPlayerPowerIndicators(gl);


        } else if (gameWon) {
            // شاشة الفوز
            gl.glClearColor(0, 0.5f, 0, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            // يمكنك رسم رسالة "You Won" هنا
        } else {
            // شاشة الخسارة
            gl.glClearColor(0.5f, 0, 0, 1);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            // يمكنك رسم رسالة "Game Over" هنا
        }
    }// لتوفير المساحة سأفترض أنك تملك باقي الكلاس، فقط استبدل playerTakeDamage و update
    // في GameManager
    public void activateShield2() {
        if (player != null && !player.isDying) {
            player.activateShield();
        }
    }
    private void updateWaveLogic() { /* نفس الكود السابق */ if (bossActive || isLevelTransitioning) return; if (!activeMiddleEnemies.isEmpty()) return; if (System.currentTimeMillis() - waveTimer > 5000) { waveStep++; switch (waveStep) { case 1: spawnSquadV(400, 750, 5); break; case 2: spawnSquadSide(true, 4); break; case 3: spawnSquadSide(false, 4); break; case 4: if (!middleWaveSpawned) { spawnMiddleEnemies(); } else { waveStep++; } break; case 5: spawnBoss(); break; } waveTimer = System.currentTimeMillis(); } }
    private void spawnMiddleEnemies() { activeMiddleEnemies.clear(); int hp = 25 + (currentLevel * 10); MiddleEnemy left = new MiddleEnemy(250, 750, 1, currentLevel); left.health = hp; left.maxHealth = hp; left.shotDelay = 1200 - (currentLevel * 100); activeMiddleEnemies.add(left); MiddleEnemy right = new MiddleEnemy(550, 750, 2, currentLevel); right.health = hp; right.maxHealth = hp; right.shotDelay = 1600 - (currentLevel * 100); activeMiddleEnemies.add(right); middleWaveSpawned = true; }
    private void spawnBoss() { System.out.println("BOSS LEVEL " + currentLevel + " INCOMING!"); boss = new Boss(350, 700, currentLevel); bossActive = true; bullets.clear(); }
    public void checkCollisions() {
        // 1. حماية اللاعب: لو اللاعب بيموت، وقف حساب التصادمات
        if (player.isDying) return;

        Rectangle pRect = player.getBounds();

        // =================================================================
        // A. تصادم الأعداء المتوسطين (Middle Enemies)
        // =================================================================
        for (int i = 0; i < activeMiddleEnemies.size(); i++) {
            MiddleEnemy me = activeMiddleEnemies.get(i);
            Rectangle meRect = new Rectangle((int) me.getX() - 30, (int) me.getY() - 30, 60, 60);

            // 1. جسم اللاعب ضد جسم العدو
            if (pRect.intersects(meRect)) {
                if (player.isShieldActive) {
                    // --- حالة الدرع مفعل ---
                    // العدو يتضرر بشدة (أو يموت) واللاعب سليم
                    me.setHealth(me.getHealth() - 100);
                } else {
                    // --- حالة بدون درع ---
                    // اللاعب يتضرر
                    player.setHealth(player.getHealth() - 30);
                    if (player.getHealth() <= 0) {
                        player.setHealth(0);
                        player.isDying = true;
                    }
                    // العدو يتضرر ويموت
                    me.setHealth(me.getHealth() - 100);
                }

                // التحقق من موت العدو
                if (me.getHealth() <= 0) {
                    activeMiddleEnemies.remove(i);
                    spawnRandomItem(me.getX(), me.getY());
                    score += 500;
                    i--;
                    continue;
                }
            }

            // 2. ليزر اللاعب ضد العدو
            if (player.isLaserBeamActive && player.getLaserBounds().intersects(meRect)) {
                me.setHealth(me.getHealth() - 2); // ضرر مستمر
            }

            // 3. رصاص اللاعب ضد العدو
            for (Bullet b : bullets) {
                if (!b.isAlive() || b.isEnemyBullet()) continue;
                if (b.getBounds().intersects(meRect)) {
                    me.setHealth(me.getHealth() - 5);
                    b.setAlive(false);
                }
            }
        }

        // =================================================================
        // B. تصادم الزعيم (Boss)
        // =================================================================
        if (bossActive && boss != null && !boss.isDying) {
            Rectangle bossRect = boss.getBounds();

            // 1. ليزر الزعيم ضد اللاعب
            if (boss.isFiringLaser && boss.getLaserBounds().intersects(pRect)) {
                if (!player.isShieldActive) {
                    player.setHealth(player.getHealth() - 3); // ضرر سريع
                    if (player.getHealth() <= 0) player.isDying = true;
                }
            }

            // 2. ليزر اللاعب ضد الزعيم
            if (player.isLaserBeamActive && player.getLaserBounds().intersects(bossRect)) {
                boss.takeDamage();
            }

            // 3. جسم اللاعب ضد جسم الزعيم
            if (pRect.intersects(bossRect)) {
                // إرجاع اللاعب للخلف
                player.setY(player.getY() - 30);

                if (player.isShieldActive) {
                    // الدرع يحمي اللاعب، والزعيم يأخذ ضرر بسيط من الاصطدام بالدرع
                    boss.takeDamage();
                } else {
                    // بدون درع: كارثة للاعب
                    player.setHealth(player.getHealth() - 40);
                    if (player.getHealth() <= 0) {
                        player.setHealth(0);
                        player.isDying = true;
                    }
                    boss.takeDamage();
                }
            }
        }

        // =================================================================
        // C. ليزر اللاعب الخارق (ضد الأعداء الصغار والرصاص)
        // =================================================================
        if (player.isLaserBeamActive) {
            Rectangle lRect = player.getLaserBounds();

            // ضد الأعداء العاديين
            for (Enemy e : enemies) {
                if (e.isAlive() && lRect.intersects(e.getBounds())) {
                    e.setHealth(e.getHealth() - 10);
                    if (e.getHealth() <= 0) {
                        e.setAlive(false);
                        score += 50;
                        spawnRandomItem(e.getX(), e.getY());
                    }
                }
            }

            // ضد رصاص العدو (الليزر يحرق الرصاص)
            for (Bullet b : bullets) {
                if (b.isEnemyBullet() && lRect.intersects(b.getBounds())) {
                    b.setAlive(false);
                }
            }
        }

        // =================================================================
        // D. منطق الرصاص (Bullets)
        // =================================================================
        for (Bullet b : bullets) {
            if (!b.isAlive()) continue;
            Rectangle bRect = b.getBounds();

            if (!b.isEnemyBullet()) {
                // --- رصاص اللاعب ---
                if (bossActive && boss != null && !boss.isDying && bRect.intersects(boss.getBounds())) {
                    boss.takeDamage();
                    b.setAlive(false);
                } else {
                    for (Enemy e : enemies) {
                        if (e.isAlive() && bRect.intersects(e.getBounds())) {
                            b.setAlive(false);
                            e.setHealth(e.getHealth() - 15); // ضرر الطلقة
                            if (e.getHealth() <= 0) {
                                e.setAlive(false);
                                score += 50;
                                spawnRandomItem(e.getX(), e.getY());
                            }
                            break;
                        }
                    }
                }
            } else {
                // --- رصاص العدو (ضد اللاعب) ---
                if (bRect.intersects(pRect)) {
                    b.setAlive(false); // الرصاصة تختفي

                    if (!player.isShieldActive) {
                        // لو مفيش درع، دمج للاعب
                        player.setHealth(player.getHealth() - 3);
                        if (player.getHealth() <= 0) {
                            player.setHealth(0);
                            player.isDying = true;
                        }
                    }
                    // لو فيه درع، الرصاصة اختفت خلاص واللاعب سليم
                }
            }
        }

        // =================================================================
        // E. اصطدام الأعداء الصغيرة بجسم اللاعب
        // =================================================================
        for (Enemy e : enemies) {
            if (e.isAlive() && pRect.intersects(e.getBounds())) {

                e.setAlive(false); // العدو ينفجر في الحالتين

                if (player.isShieldActive) {
                    // الدرع شغال: سكور إضافي واللاعب سليم
                    score += 50;
                } else {
                    // الدرع طافي: اللاعب يتضرر
                    player.setHealth(player.getHealth() - 20);
                    if (player.getHealth() <= 0) {
                        player.setHealth(0);
                        player.isDying = true;
                    }
                }
            }
        }

        // =================================================================
        // F. التقاط الهدايا (Items) - الجزء المضاف
        // =================================================================
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (pRect.intersects(item.getBounds())) {
                // تطبيق تأثير الهدية
                applyItem(item.getType());

                // حذف الهدية من القائمة
                items.remove(i);
                i--; // تعديل العداد
            }
        }
    }    private void handleBossDefeat() { bossActive = false; boss = null; score += 500; enemies.clear(); activeMiddleEnemies.clear(); bullets.clear(); isLevelTransitioning = true; player.triggerFlyOff(); }
    private void startNextLevel() {
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
        // التعديل هنا: استعادة الصحة بالكامل (Full Health Restore)
        // ============================================================
        // بدلاً من زيادة 20 فقط، نجعلها تساوي الماكس
        player.setHealth(player.MAX_HEALTH);
        // أو لو بتستخدم Setters:
        // player.setHealth(Player.MAX_HEALTH);

        // تأمين إضافي: تصفير حالات الموت والأنيميشن لضمان ظهور الصورة السليمة
        player.isDying = false;
        player.animationFinished = false;
        // ============================================================

        if (currentLevel > 3) gameWon = true;
    }
    // دالة مساعدة لاختيار صورة عشوائية للأعداء (من 21 لـ 23)
    private int getRandomEnemyTexture() {
        // المصفوفة تبدأ بـ 21 (enem1) وتنتهي بـ 23 (enem3)
        // Math.random() * 3 يعطي 0 أو 1 أو 2
        return 21 + (int)(Math.random() * 3);
    }

    private void spawnContinuousRandomEnemies() {
        if (isLevelTransitioning) return;
        long currentTime = System.currentTimeMillis();

        // -----------------------------------------------------------
        // تعديل 1: زيادة الفواصل الزمنية (تقليل الزحمة)
        // -----------------------------------------------------------
        // وقت البوس: كل 7 ثواني (عشان نركز مع البوس)
        // وقت الميدل بوس: كل 3.5 ثانية
        // الوقت العادي: كل 2000 مللي ثانية (ثانيتين) بدل 800
        long spawnDelay = (bossActive) ? 6000 : (!activeMiddleEnemies.isEmpty()) ? 3000 : 2000;

        if (currentTime - lastRandomSpawnTime > spawnDelay) {
            double rand = Math.random();

            // -----------------------------------------------------------
            // تعديل 2: تقليل فرصة ظهور الأسراب (Groups)
            // -----------------------------------------------------------
            // خليناها 0.8 يعني: 80% أعداء فردية و 20% بس أسراب
            // (كانت 0.6 وده كان بيخلي الأسراب كتير جداً)
            if (rand < 0.8) {
                // توليد عدو فردي
                double typeRand = Math.random();
                Enemy.TypesOfEnemies type = (typeRand < 0.4) ? Enemy.TypesOfEnemies.STRAIGHT :
                        (typeRand < 0.7) ? Enemy.TypesOfEnemies.CHASER :
                                Enemy.TypesOfEnemies.CIRCLE_PATH;
                float spawnX = 50 + (float)(Math.random() * 700);

                // الكود زي ما هو بالظبط عشان منبوظش الرسم او البار
                enemies.add(new Enemy(spawnX, 700, 60, type, player, getRandomEnemyTexture()));
            }
            else {
                // توليد سرب (Squad)
                if (Math.random() < 0.5) {
                    float centerX = 150 + (float)(Math.random() * 500);
                    spawnSquadV(centerX, 750, 5);
                } else {
                    boolean fromLeft = Math.random() < 0.5;
                    spawnSquadSide(fromLeft, 4);
                }

                // -----------------------------------------------------------
                // تعديل 3: راحة طويلة بعد السرب
                // -----------------------------------------------------------
                // بعد ما السرب ينزل، ندي اللاعب 3.5 ثانية راحة (كانت 1.5)
                lastRandomSpawnTime = currentTime + 3000;
                return;
            }
            lastRandomSpawnTime = currentTime;
        }
    }
    private void spawnSquadV(float centerX, float startY, int count) {
        // عشان السرب كله يبقى نفس الشكل، نختار الصورة مرة واحدة بره اللوب
        int squadTexture = getRandomEnemyTexture();

        for (int i = 0; i < count; i++) {
            float offsetX = (i % 2 == 0) ? (i * 40) : -(i * 40);
            float offsetY = i * 30;
            enemies.add(new Enemy(centerX + offsetX, startY + offsetY, 60, Enemy.TypesOfEnemies.SQUAD_V, player, squadTexture));
        }
    }

    private void spawnSquadSide(boolean fromLeft, int count) {
        // توحيد شكل السرب
        int squadTexture = getRandomEnemyTexture();

        for (int i = 0; i < count; i++) {
            float startX = fromLeft ? -50 - (i * 60) : 850 + (i * 60);
            float startY = 550 + (i * 20);
            Enemy.TypesOfEnemies type = fromLeft ? Enemy.TypesOfEnemies.SQUAD_ENTER_LEFT : Enemy.TypesOfEnemies.SQUAD_ENTER_RIGHT;
            enemies.add(new Enemy(startX, startY, 60, type, player, squadTexture));
        }
    }
    public void fireFanShots(float startX, float startY) { int[] angles = {-30, -15, 0, 15, 30};
        float bulletSpeed = 7.0f; for (int angle : angles) { double rad = Math.toRadians(angle);
            float dx = (float) (bulletSpeed * Math.sin(rad)); float dy = (float) (-bulletSpeed * Math.cos(rad));
            bullets.add(new Bullet(startX, startY - 20, dx, dy, true,6)); } }
    public void fireHomingShot(float startX, float startY) { float bulletSpeed = 8.0f; float dx = (player.getX() + player.getWidth()/2) - startX;
        float dy = (player.getY() + player.getHeight()/2) - startY; double angle = Math.atan2(dy, dx);
        bullets.add(new Bullet(startX, startY, (float)(bulletSpeed * Math.cos(angle)), (float)(bulletSpeed * Math.sin(angle)), true,6)); }
    private void enemyShootPattern(Enemy e) { float ex = e.getX() + e.getWidth()/2; float ey = e.getY();
        if (e.getType() == Enemy.TypesOfEnemies.CIRCLE_PATH) { for (int k = 0; k < 360; k += 60) { float rad = (float) Math.toRadians(k);
            bullets.add(new Bullet(ex, ey, (float)Math.cos(rad)*5, (float)Math.sin(rad)*5, true,6)); } } else { float dx = (player.getX() + player.getWidth()/2) - ex; float dy = (player.getY() + player.getHeight()/2) - ey; float len = (float) Math.sqrt(dx*dx + dy*dy); bullets.add(new Bullet(ex, ey, (dx/len)*7, (dy/len)*7, true,6)); } }
    private void spawnRandomItem(float x, float y) { if (Math.random() > 0.35) return;
        int rand = new Random().nextInt(100); Item.ItemType type = Item.ItemType.GOLD_COIN;
        if (currentLevel == 1) { if (rand < 40) type = Item.ItemType.HEALTH; else if (rand < 70) type = Item.ItemType.RAPID_FIRE; }
        else { if (rand < 30) type = Item.ItemType.RAPID_FIRE; else if (rand < 50) type = Item.ItemType.HEALTH; }
        items.add(new Item(x, y, type)); }
    private void applyItem(Item.ItemType type) { switch (type) { case HEALTH: player.setHealth(Math.min(Player.MAX_HEALTH, player.getHealth() + 50)); break; case RAPID_FIRE: player.upgradeWeapon(); break; case GOLD_COIN: score += 100; break; } }
    public void playerShoot() {
        // حساب نقطة انطلاق الرصاصة (منتصف اللاعب)
        float sx = player.getX() + player.getWidth() / 2 - 5;
        float sy = player.getY() + player.getHeight();

        // رقم صورة طلقة اللاعب (حسب ترتيب مصفوفة الصور في GameListener)
        // تأكد أن رقم 6 هو صورة "Bullet v6.png" عندك
        int playerBulletIndex = 25;

        if (player.weaponLevel == 1) {
            // سلاح مستوى 1: طلقة واحدة
            bullets.add(new Bullet(sx, sy, 0, 15, false, playerBulletIndex));
        }
        else if (player.weaponLevel == 2) {
            // سلاح مستوى 2: طلقتين متوازيتين
            bullets.add(new Bullet(sx - 15, sy, 0, 15, false, playerBulletIndex));
            bullets.add(new Bullet(sx + 15, sy, 0, 15, false, playerBulletIndex));
        }
        else {
            // سلاح مستوى 3: ثلاث طلقات (واحدة مستقيمة واتنين بزاوية)
            bullets.add(new Bullet(sx, sy, 0, 15, false, playerBulletIndex));      // النص
            bullets.add(new Bullet(sx, sy, -4, 14, false, playerBulletIndex));     // شمال
            bullets.add(new Bullet(sx, sy, 4, 14, false, playerBulletIndex));      // يمين
        }
    }    public void fireLaser() { player.activateLaserBeam(); }
    public void activateShield() {
        // كان مكتوب player.activateShieldManual(); وده غلط
        // الصح:
        if (player != null && !player.isDying) {
            player.activateShield(); // استدعاء دالة الـ Cooldown
        }
    }
    private void drawPlayerPowerIndicators(GL gl) { float baseX = 20; float baseY = 20;
        gl.glColor3f(player.isShieldAvailable ? 0 : 0.3f, player.isShieldAvailable ? 1 : 0.3f, player.isShieldAvailable ? 1 : 0.3f);
        gl.glBegin(GL.GL_QUADS); gl.glVertex2f(baseX, baseY); gl.glVertex2f(baseX + 20, baseY);
        gl.glVertex2f(baseX + 20, baseY + 20); gl.glVertex2f(baseX, baseY + 20); gl.glEnd();
        gl.glColor3f(player.isLaserAvailable ? 0 : 0.3f, player.isLaserAvailable ? 1 : 0.3f, player.isLaserAvailable ? 0 : 0.3f);
        gl.glBegin(GL.GL_QUADS); gl.glVertex2f(baseX + 30, baseY);
        gl.glVertex2f(baseX + 50, baseY); gl.glVertex2f(baseX + 50, baseY + 20); gl.glVertex2f(baseX + 30, baseY + 20);
        gl.glEnd(); gl.glColor3f(player.specialAmmo > 0 ? 1 : 0.3f, player.specialAmmo > 0 ? 0.8f : 0.3f, 0); gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(baseX + 60, baseY); gl.glVertex2f(baseX + 80, baseY); gl.glVertex2f(baseX + 80, baseY + 20);
        gl.glVertex2f(baseX + 60, baseY + 20); gl.glEnd(); }
    private void drawStartScreen(GL gl) { gl.glColor3f(0.1f, 0.1f, 0.1f);
        gl.glBegin(GL.GL_QUADS); gl.glVertex2f(0, 0); gl.glVertex2f(800, 0);
        gl.glVertex2f(800, 600); gl.glVertex2f(0, 600); gl.glEnd(); gl.glColor3f(0, 1, 0);
        gl.glBegin(GL.GL_TRIANGLES); gl.glVertex2f(350, 250); gl.glVertex2f(350, 350);
        gl.glVertex2f(450, 300); gl.glEnd(); }
    private void spawnSpecialBullets() { for (int i = 0; i < 3; i++)
        bullets.add(new Bullet((float) (Math.random() * 800), 0, (float) (Math.random() * 6) - 3, 10, false,6)); }
}