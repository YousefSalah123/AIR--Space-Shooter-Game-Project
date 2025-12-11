package mygame.engine;

import mygame.Game;
import mygame.objects.*;

import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import com.sun.opengl.util.GLUT; // تأكد من عمل Import لهذا الكلاس للنصوص



public class GameManager {
    final Game game;
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
    // متغير للتأكد من تشغيل صوت موت البوس مرة واحدة فقط
    private boolean bossDeathSoundPlayed = false;
    // Sound Manager
    public SoundManager soundManager;
    private boolean showLevelScreen = false; // هل نعرض شاشة الليفل الآن؟
    private long levelScreenTimer = 0;       // مؤقت العرض
    private GLUT glut=new GLUT();

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

    public void update() {
        if (!isGameRunning) return; // إذا اللعبة متوقفة، تجاهل
        if (gameOver || gameWon) {
            isGameRunning = false;
            game.handleGameOver(gameWon, score);   // إرسال النتيجة إلى Game
            return;
        }

        // ------------------------------------------
        // التحقق من حالة موت اللاعب (تعديل مهم)
        // ------------------------------------------
        if (player.isDying) {
            // إذا انتهى الأنيميشن بالكامل، نعلن انتهاء اللعبة
            if (player.animationFinished) {
                player.setAlive(false);
                // Fix: Play game over sound only once
                if (!gameOver) {
                    soundManager.playSound("game_over");
                    gameOver = true;
                }
            }
            // لا نوقف التحديث بالكامل لكي يستمر رسم الأنيميشن،
            // لكن نمنع تحديث حركة اللاعب داخل الكلاس الخاص به
            // لا نعمل Return هنا لنسمح برسم الانيميشن في render
        }

        // ... (باقي كود التحديث كما هو) ...
        long currentTime = System.currentTimeMillis();

        player.update();
        if (isLevelTransitioning) {
            // عندما يغادر اللاعب الشاشة (بعد قتل البوس)
            if (player.getY() > 700) {

                // إذا لم نكن نعرض الشاشة بعد، ابدأ عرضها
                if (!showLevelScreen) {
                    showLevelScreen = true;
                    levelScreenTimer = System.currentTimeMillis();
                }
                else {
                    // نحن الآن نعرض الشاشة، ننتظر لمدة 3 ثواني (3000 ملي ثانية)
                    if (System.currentTimeMillis() - levelScreenTimer > 3000) {
                        showLevelScreen = false; // إخفاء الشاشة
                        startNextLevel();        // بدء المستوى الجديد فعلياً
                    }
                }
            }
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
           if (item.getY() < -50) items.remove(i);
        }

        // 5. Update Boss (Modified for Death Animation)
        if (bossActive && boss != null) {
            boss.update(); // تحديث الحركة والأنيميشن
            // --- التعديل هنا: تمرير player كباراميتر ثالث ---
            boss.shootLogic(bullets, soundManager);
            if (boss.isDying && !bossDeathSoundPlayed) {
                soundManager.stopMusic(); // وقف مزيكا اللعب العادية
                soundManager.playSound("LevelComplete"); // شغل موسيقى الفوز فوراً
                bossDeathSoundPlayed = true; // عشان ما يشتغلش تاني في الفريم اللي بعده
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

            // لو خلص أنيميشن الموت -> احذفه الآن
            if (me.readyToRemove) {
                score += 500;
                spawnRandomItem(me.x, me.y);
                activeMiddleEnemies.remove(i);
                continue;
            }

            // لو بيموت -> ما تخليهوش يضرب نار
            if (me.isDying) continue;

            if (currentTime - me.lastShotTime > me.shotDelay) {
                if (me.type == 1) fireFanShots(me.x, me.y);
                else fireHomingShot(me.x, me.y);
                soundManager.playSound("enemy_laser"); // Sound Effect
                me.lastShotTime = currentTime;
            }
            // تمت إزالة شرط (me.health <= 0) من هنا، سيتم التعامل معه في التصادمات
        }

        // تحديث دالة update للأعداء العاديين
        // 7. Update Regular Enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update();

            // لو خلص أنيميشن الموت -> احذفه
            if (e.readyToRemove) {
                enemies.remove(i);
                continue;
            }

            // لو بيموت -> ما تخليهوش يضرب
            if (e.isDying) continue;

            if (e.readyToFire()) {
                enemyShootPattern(e);
                soundManager.playSound("enemy_laser"); // Sound Effect
            }

            // إزالة العدو لو خرج بره الشاشة (وليس بسبب الموت)
            if (!e.isAlive() && !e.isDying) enemies.remove(i);
        }

        updateWaveLogic();
        spawnContinuousRandomEnemies();
        checkCollisions();
    }

    // ... (باقي الدوال WaveLogic, Spawn, etc. كما هي) ...
    // سأضع فقط الدوال التي تحتاج تغيير مباشر للتوافق مع التعديل

    public void playerTakeDamage(int amount) {
        if (!isGameRunning) return;
        if (player.isDying) return;

        // --- التعديل الجديد: فترة السماح ---
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime < 1000) { // 1000ms = 1 second cooldown
            return; // لو لسه معداش ثانية، متحسبش الضربة واخرج
        }
        lastDamageTime = currentTime; // سجل وقت الضربة دي
        // -----------------------------------

        if (player.isShieldActive) {
            player.activateShieldManual();
            return;
        }

        player.setHealth(player.getHealth() - amount); // نقص الصحة

        // (اختياري) ممكن تطبع الصحة عشان تتأكد
        System.out.println("Player Hit! HP: " + player.getHealth());

        if (player.getHealth() <= 0) {
            player.isDying = true;
        }
    }

    public void render(GL gl, int[] textures) {
        // لو اللعبة مش شغالة (أو لسه بتبدأ)، ما ترسمش حاجة قديمة
        if (!isGameRunning) {
            return;
        }

        // =============================================================
        //  رسم عناصر اللعبة (سواء بنلعب أو خسرنا، الخلفية تفضل موجودة)
        // =============================================================

        // 1. رسم اللاعب
        player.render(gl, textures);

        // 2. رسم الطلقات
        for (Bullet b : bullets) {
            b.render(gl, textures);
        }

        // 3. رسم العناصر (Items)
        for (Item item : items) {
            item.render(gl, textures);
        }

        // 4. رسم الأعداء المتوسطين (مكبرين) + Health Bar
        for (MiddleEnemy me : activeMiddleEnemies) {
            // --- تكبير حجم العدو المتوسط بصرياً فقط (دون تغيير الكلاس) ---
            gl.glPushMatrix(); // حفظ الإحداثيات الحالية
            gl.glTranslatef(me.x, me.y, 0); // نقل نقطة الرسم لمنتصف العدو
            gl.glScalef(1.5f, 1.5f, 1f);    // تكبير الحجم مرة ونصف (1.5x)
            gl.glTranslatef(-me.x, -me.y, 0); // إرجاع نقطة الرسم لمكانها

            me.render(gl, textures); // رسم العدو (سيظهر كبيراً الآن)

            gl.glPopMatrix(); // استعادة الإحداثيات الطبيعية لباقي العناصر

            // --- رسم بار الصحة (معدل المكان) ---
            gl.glDisable(GL.GL_TEXTURE_2D);

            float barTotalWidth = 40f;
            // بما أننا كبرنا العدو، لازم نرفع البار لفوق شوية (كان 35 خليناه 55)
            float barStartY = me.y + 55;
            float barStartX = me.x - 19;

            float percent = (float) me.health / (float) me.maxHealth;

            // الخلفية الحمراء
            gl.glColor3f(0.6f, 0.0f, 0.0f);
            gl.glRectf(barStartX, barStartY, barStartX + barTotalWidth, barStartY + 4f);

            // النسبة الخضراء
            if (percent > 0) {
                gl.glColor3f(0.0f, 1.0f, 0.0f);
                gl.glRectf(barStartX, barStartY, barStartX + (barTotalWidth * percent), barStartY + 4f);
            }
            gl.glColor3f(1, 1, 1);
            gl.glEnable(GL.GL_TEXTURE_2D);
        }

        // 5. رسم الزعيم (Boss)
        if (bossActive && boss != null) {
            boss.render(gl, textures);
        }

        // 6. رسم الأعداء العاديين + Health Bar
        for (Enemy e : enemies) {
            e.render(gl, textures);

            // رسم بار الصحة للعدو العادي
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

        // 7. رسم واجهة المستخدم (HUD)
        if (player.isAlive()) {
            drawPlayerHUD(gl, textures);
        }

        if (showLevelScreen) {
            drawNextLevelScreen(gl);
        }
    }
    private void drawNextLevelScreen(GL gl) {
        // 1. إعداد الرسم ثنائي الأبعاد بدون تكستشر
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // 2. رسم خلفية سوداء نصف شفافة تغطي الشاشة
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(800, 0);
        gl.glVertex2f(800, 600);
        gl.glVertex2f(0, 600);
        gl.glEnd();

        // 3. إعداد النص (لون أبيض)
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        // تحديد المستوى القادم (الحالي + 1)
        int nextLvl = currentLevel + 1;
        String text = "LEVEL " + nextLvl;
        if (nextLvl > 3) text = "VICTORY!"; // إذا انتهت اللعبة

        // 4. رسم النص في منتصف الشاشة باستخدام GLUT
        // ملاحظة: نقوم بضبط الموقع يدوياً تقريباً
        gl.glRasterPos2f(320, 300);
        glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, text);

        // نص إضافي صغير "Get Ready"
        if (nextLvl <= 3) {
            gl.glColor3f(1.0f, 1.0f, 0.0f); // أصفر
            gl.glRasterPos2f(340, 270);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "GET READY");
        }

        // إعادة الإعدادات كما كانت
        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1, 1, 1);
    }
    // =============================================================
    // الدوال المساعدة لرسم الـ HUD الجديد (ضعها داخل GameManager)
    // =============================================================

    private void drawPlayerHUD(GL gl, int[] textures) {
        // 1. رسم بار الصحة الثابت
        drawHealthBarOnly(gl);

        // 2. تفعيل الصور والشفافية (حل مشكلة الخلفية السوداء)
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        // هذا السطر هو السحر لإخفاء الخلفية السوداء:
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // إعدادات المكان
        float startX = 15;
        float startY = 20;

        // --- حل مشكلة المط ---
        // جرب تجعل الطول يساوي العرض (مربع) لترى الشكل الحقيقي للصورة
        float iconWidth = 50;
        float iconHeight = 40; // اجعلها مساوية للعرض مبدئياً
        float padding = 7;

        // ---------------------------------------------------
        // 1. أيقونة الليزر (Index 41)
        // ---------------------------------------------------
        if (player.canUseLaser) gl.glColor3f(1.0f, 1.0f, 1.0f); // منور
        else gl.glColor3f(0.3f, 0.3f, 0.3f);                    // مطفي

        if (textures.length > 41) {
            drawIcon(gl, textures[41], startX, startY, iconWidth, iconHeight);
        }

        // ---------------------------------------------------
        // 2. أيقونة الدرع (Index 42)
        // ---------------------------------------------------
        if (player.canUseShield) gl.glColor3f(1.0f, 1.0f, 1.0f);
        else gl.glColor3f(0.3f, 0.3f, 0.3f);

        float pos2 = startX + iconWidth + padding;
        if (textures.length > 42) {
            drawIcon(gl, textures[42], pos2, startY, iconWidth, iconHeight);
        }

        // ---------------------------------------------------
        // 3. أيقونة السوبر (Index 43)
        // ---------------------------------------------------
        if (player.canUseSuper) gl.glColor3f(1.0f, 1.0f, 1.0f);
        else gl.glColor3f(0.3f, 0.3f, 0.3f);

        float pos3 = startX + (iconWidth + padding) * 2;
        if (textures.length > 43) {
            drawIcon(gl, textures[43], pos3, startY, iconWidth, iconHeight);
        }

        // تنظيف الإعدادات
        gl.glColor3f(1, 1, 1);
        gl.glDisable(GL.GL_BLEND); // نوقف الدمج عشان مياثرش على اللي بعده غلط
    }

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

    private void drawIcon(GL gl, int textureId, float x, float y, float size) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex2f(x, y);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex2f(x + size, y);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex2f(x + size, y + size);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex2f(x, y + size);
        gl.glEnd();
    }

    private void drawHealthBarOnly(GL gl) {
        gl.glDisable(GL.GL_TEXTURE_2D);

        // إعدادات المكان (أعلى اليسار)
        float barX = 20;
        // التعديل هنا: 570 تعني في الأعلى (لأن الشاشة ارتفاعها 600)
        float barY = 570;

        float barWidth = 200; // جعلناه أعرض قليلاً ليناسب مكان الـ HUD
        float barHeight = 15;

        // الخلفية (أحمر غامق)
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glRectf(barX, barY, barX + barWidth, barY + barHeight);

        // الصحة الحالية (أخضر)
        float hpPercent = (float) player.getHealth() / Player.MAX_HEALTH;
        if (hpPercent < 0) hpPercent = 0;

        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glRectf(barX, barY, barX + (barWidth * hpPercent), barY + barHeight);

        // إطار أبيض
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // إعادة تفعيل الصور
        gl.glEnable(GL.GL_TEXTURE_2D);
    }// لتوفير المساحة سأفترض أنك تملك باقي الكلاس، فقط استبدل playerTakeDamage و update

    // في GameManager
    public void activateShield2() {
        if (player != null && !player.isDying) {
            player.activateShield();
        }
    }

    private void updateWaveLogic() { /* نفس الكود السابق */
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

    private void spawnMiddleEnemies() {
        activeMiddleEnemies.clear();

        // 1. تحديد الصحة (أقوى من الأعداء العاديين)
        // العدو العادي صحته 100
        int hp;
        if (currentLevel == 1) {
            hp = 100;      // صحة عادية
        } else if (currentLevel == 2) {
            hp = 130;      // أقوى مرة ونصف
        } else {
            hp = 150;      // دبابة (صعب القتل)
        }

        // 2. تحديد العدد حسب المستوى
        int enemyCount;
        if (currentLevel == 1) enemyCount = 2;
        else if (currentLevel == 2) enemyCount = 3;
        else enemyCount = 4;

        // 3. حساب المسافات للتوزيع
        float screenWidth = 800f;
        float spacing = screenWidth / (enemyCount + 1);
        float startY = 750;

        for (int i = 0; i < enemyCount; i++) {
            float x = spacing * (i + 1);
            int type = (i % 2) + 1;

            MiddleEnemy me = new MiddleEnemy(x, startY, type, currentLevel);
            me.health = hp;
            me.maxHealth = hp;

            // تنويع وقت الضرب
            me.shotDelay = (1200 - (currentLevel * 100)) + (i * 150);

            activeMiddleEnemies.add(me);
        }

        middleWaveSpawned = true;
        System.out.println("Spawned " + enemyCount + " Middle Enemies (HP: " + hp + ")");
    }    private void spawnBoss() {
        System.out.println("BOSS LEVEL " + currentLevel + " INCOMING!");
        boss = new Boss(350, 700, currentLevel);
        bossActive = true;
        bossDeathSoundPlayed = false; // <-- ضيف السطر ده مهم جداً
        bullets.clear();
    }

    public void checkCollisions() {
        // 1. حماية اللاعب: لو اللاعب بيموت، وقف حساب التصادمات
        if (player.isDying) return;

        Rectangle pRect = player.getBounds();

        // =================================================================
        // A. تصادم الأعداء المتوسطين (Middle Enemies)
        // =================================================================
        for (int i = 0; i < activeMiddleEnemies.size(); i++) {
            MiddleEnemy me = activeMiddleEnemies.get(i);

            // تعديل هام: لو العدو بيموت (أنيميشن)، نتجاهله تماماً في التصادم
            if (me.isDying) continue;

            Rectangle meRect = new Rectangle((int) me.getX() - 30, (int) me.getY() - 30, 60, 60);

            // 1. جسم اللاعب ضد جسم العدو
            if (pRect.intersects(meRect)) {
                // صوت الاصطدام القوي
                soundManager.playSound("explosion");

                if (player.isShieldActive) {
                    // --- حالة الدرع مفعل ---
                    // العدو يتضرر بشدة
                    me.setHealth(me.getHealth() - 100);
                } else {
                    // --- حالة بدون درع ---
                    // اللاعب يتضرر (نستخدم الدالة الجديدة اللي فيها Cooldown)
                    playerTakeDamage(5);

                    // العدو يتضرر بشدة
                    me.setHealth(me.getHealth() - 100);
                }

                // التحقق من موت العدو
                if (me.getHealth() <= 0) {
                    me.setHealth(0); // منع الصحة السالبة
                    // لا نحذف العدو هنا، بل نبدأ الأنيميشن
                    me.startDeath();
                    score += 50;
                    // نوقف اللوب للحظة عشان ما يحصلش أخطاء حسابية في نفس الفريم
                    continue;
                }
            }

            // 2. ليزر اللاعب ضد العدو
            if (player.isLaserBeamActive && player.getLaserBounds().intersects(meRect)) {
                me.setHealth(me.getHealth() - 2); // ضرر مستمر
                if (me.getHealth() <= 0) {
                    me.setHealth(0);
                    me.startDeath();
                    // صوت انفجار العدو
                    soundManager.playSound("explosion");
                }
            }

            // 3. رصاص اللاعب ضد العدو
            for (Bullet b : bullets) {
                if (!b.isAlive() || b.isEnemyBullet()) continue;
                if (b.getBounds().intersects(meRect)) {
                    me.setHealth(me.getHealth() - 5);
                    b.setAlive(false); // الطلقة تختفي

                    if (me.getHealth() <= 0) {
                        me.setHealth(0);
                        me.startDeath();
                        // صوت انفجار العدو
                        soundManager.playSound("explosion");
                    }
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
                    // ضرر الليزر سريع
                    playerTakeDamage(3);
                    if (player.getHealth() <= 0) {
                        player.isDying = true;
                        soundManager.playSound("explosion"); // صوت تدمير اللاعب
                    }
                }
            }

            // 2. ليزر اللاعب ضد الزعيم
            if (player.isLaserBeamActive && player.getLaserBounds().intersects(bossRect)) {
                boss.takeDamage();
            }

            // 3. جسم اللاعب ضد جسم الزعيم
            if (pRect.intersects(bossRect)) {
                player.setY(player.getY() - 30); // ارتداد
                soundManager.playSound("explosion"); // صوت ارتطام

                if (player.isShieldActive) {
                    boss.takeDamage();
                } else {
                    playerTakeDamage(40);
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
                // إضافة شرط !e.isDying
                if (e.isAlive() && !e.isDying && lRect.intersects(e.getBounds())) {
                    e.health -= 10;
                    if (e.health <= 0) {
                        e.health = 0;
                        // تعديل: تشغيل الموت بدلاً من الحذف
                        score += 50;
                        spawnRandomItem(e.getX(), e.getY());
                        e.startDeath();
                        soundManager.playSound("explosion"); // صوت الانفجار
                    }
                }
            }

            // ضد رصاص العدو
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
                        // إضافة شرط !e.isDying
                        if (e.isAlive() && !e.isDying && bRect.intersects(e.getBounds())) {
                            b.setAlive(false);
                            e.health -= 15; // ضرر الطلقة
                            if (e.health <= 0) {
                                e.health = 0;
                                // تعديل: تشغيل الموت
                                score += 50;
                                spawnRandomItem(e.getX(), e.getY());
                                e.startDeath();
                                soundManager.playSound("explosion"); // صوت الانفجار
                            }
                            break;
                        }
                    }
                }
            } else {
                // --- رصاص العدو (ضد اللاعب) ---
                if (bRect.intersects(pRect)) {
                    b.setAlive(false);

                    if (!player.isShieldActive) {
                        // هنا ممكن نستخدم playerTakeDamage بس الرصاصة بتختفي أصلاً، فالطريقة القديمة شغالة كويس
                        player.setHealth(player.getHealth() - 2);
                        playerTakeDamage(2);
                    }
                }
            }
        }

        // =================================================================
        // E. اصطدام الأعداء الصغيرة بجسم اللاعب
        // =================================================================
        for (Enemy e : enemies) {
            // إضافة شرط !e.isDying
            if (e.isAlive() && !e.isDying && pRect.intersects(e.getBounds())) {
                score += 50;
                // العدو هيموت لأنه خبط في اللاعب
                e.startDeath();
                soundManager.playSound("explosion"); // صوت الانفجار

                if (!player.isShieldActive) {
                    playerTakeDamage(5);
                    // اللاعب يتضرر
                }
            }
        }

        // =================================================================
        // F. التقاط الهدايا (Items) - (تعديل الأصوات هنا)
        // =================================================================
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (pRect.intersects(item.getBounds())) {
                applyItem(item.getType());

                // تشغيل الصوت المناسب حسب النوع
                if (item.getType() == Item.ItemType.GOLD_COIN) {
                    soundManager.playSound("coin");
                } else {
                    soundManager.playSound("powerup"); // للقلب والترقية
                }

                items.remove(i);
                i--;
            }
        }
    }


    private void handleBossDefeat() {
        bossActive = false;
        boss = null;
        score += 500;

        // تنظيف الشاشة من الأعداء والرصاص والهدايا
        enemies.clear();
        activeMiddleEnemies.clear();
        bullets.clear();
        items.clear(); // يفضل مسح الهدايا أيضاً

        // تفعيل وضع الانتقال
        isLevelTransitioning = true;
        player.triggerFlyOff();

    }
    // دالة لرسم واجهة المستخدم (HUD) - البار الثابت
    private void drawPlayerHUD(GL gl) {
        // نوقف التكستشر عشان نرسم ألوان سادة
        gl.glDisable(GL.GL_TEXTURE_2D);

        // إعدادات المكان (فوق على الشمال)
        float barX = 20;   // مسافة من الشمال
        float barY = 560;  // مسافة من تحت (يعني قريب من سقف الشاشة 600)
        float barWidth = 200; // عرض البار
        float barHeight = 20; // سمك البار

        // 1. رسم الخلفية (أحمر غامق - الجزء الفاضي)
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // 2. رسم الصحة الحالية (أخضر - الجزء المليان)
        // بنحسب النسبة: الصحة الحالية / الصحة القصوى
        float hpPercent = (float) player.getHealth() / player.MAX_HEALTH;

        // حماية: عشان البار ميرسمش بالسالب لو الصحة تحت الصفر
        if (hpPercent < 0) hpPercent = 0;

        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + (barWidth * hpPercent), barY);
        gl.glVertex2f(barX + (barWidth * hpPercent), barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // 3. (إضافي) إطار أبيض عشان الشكل يبان أنضف
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f); // تخانة الخط
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(barX, barY);
        gl.glVertex2f(barX + barWidth, barY);
        gl.glVertex2f(barX + barWidth, barY + barHeight);
        gl.glVertex2f(barX, barY + barHeight);
        gl.glEnd();

        // نرجع التكستشر عشان باقي اللعبة
        gl.glEnable(GL.GL_TEXTURE_2D);

        // نرجع اللون للأبيض عشان الصور متتأثرش بالأخضر
        gl.glColor3f(1, 1, 1);
    }

    private void startNextLevel() {
        showLevelScreen=false;
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
        player.resetAbilities();
    }

    // دالة مساعدة لاختيار صورة عشوائية للأعداء (من 21 لـ 23)
    private int getRandomEnemyTexture() {
        // المصفوفة تبدأ بـ 21 (enem1) وتنتهي بـ 23 (enem3)
        // Math.random() * 3 يعطي 0 أو 1 أو 2
        return 21 + (int) (Math.random() * 3);
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
                float spawnX = 50 + (float) (Math.random() * 700);

                // الكود زي ما هو بالظبط عشان منبوظش الرسم او البار
                enemies.add(new Enemy(spawnX, 700, 60, type, player, getRandomEnemyTexture()));
            } else {
                // توليد سرب (Squad)
                if (Math.random() < 0.5) {
                    float centerX = 150 + (float) (Math.random() * 500);
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

    public void resetGame() {
        // إعادة ضبط اللاعب
        player.setHealth(Player.MAX_HEALTH);
        player.setAlive(true);
        player.resetPosition();
        player.animationFinished = false;

        // إعادة ضبط الأعداء والطلقات والعناصر
        enemies.clear();
        activeMiddleEnemies.clear();
        bullets.clear();
        items.clear();

        // إعادة ضبط متغيرات اللعبة
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

    public void fireHomingShot(float startX, float startY) {
        float bulletSpeed = 8.0f;
        float dx = (player.getX() + player.getWidth() / 2) - startX;
        float dy = (player.getY() + player.getHeight() / 2) - startY;
        double angle = Math.atan2(dy, dx);
        bullets.add(new Bullet(startX, startY, (float) (bulletSpeed * Math.cos(angle)), (float) (bulletSpeed * Math.sin(angle)), true, 6));
    }

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

    public void playerShoot() {
        // حساب نقطة انطلاق الرصاصة
        float sx = player.getX() + player.getWidth() / 2 - 5;
        float sy = player.getY() + player.getHeight();

        int playerBulletIndex = 25;

        // تشغيل الصوت مرة واحدة أساسية (عشان نضمن إن فيه صوت هيطلع فوراً)
        // ده بيحل مشكلة "التأخير" اللي قلقت منها
        soundManager.playSound("Player_laser");

        if (player.weaponLevel == 1) {
            bullets.add(new Bullet(sx, sy, 0, 15, false, playerBulletIndex));
        }
        else if (player.weaponLevel == 2) {
            bullets.add(new Bullet(sx - 15, sy, 0, 15, false, playerBulletIndex));
            bullets.add(new Bullet(sx + 15, sy, 0, 15, false, playerBulletIndex));

            // (اختياري وغير مستحب) لو عايز تأكيد صوتي، ممكن تشغله تاني هنا
            // بس ده هيعمل صوت عالي جداً
            // soundManager.playSound("Player_laser");
        }
        else {
            bullets.add(new Bullet(sx, sy, 0, 15, false, playerBulletIndex));
            bullets.add(new Bullet(sx, sy, -4, 14, false, playerBulletIndex));
            bullets.add(new Bullet(sx, sy, 4, 14, false, playerBulletIndex));

            // (اختياري وغير مستحب)
            // soundManager.playSound("Player_laser");
        }
    }

    public void fireLaser() {
        if (player.canUseLaser && !player.isLaserBeamActive) {
            player.activateLaserBeam();

            // --- تشغيل صوت الليزر الخاص ---
            soundManager.playSound("laser");
        }
    }
    public void activateShield() {
        // 1. التأكد من وجود اللاعب وأنه ليس ميتاً
        if (player != null && !player.isDying) {

            // 2. التحقق من أن الدرع متاح وغير مفعل حالياً (عشان الصوت ما يشتغلش عالفاضي)
            if (player.canUseShield && !player.isShieldActive) {

                // 3. تفعيل الدرع (يقوم بخصم القدرة داخل كلاس Player)
                player.activateShield();

                // 4. تشغيل صوت الدرع
                soundManager.playSound("shield");
            }
        }
    }

    private void drawPlayerPowerIndicators(GL gl) {
        float baseX = 20;
        float baseY = 20;
        gl.glColor3f(player.isShieldAvailable ? 0 : 0.3f, player.isShieldAvailable ? 1 : 0.3f, player.isShieldAvailable ? 1 : 0.3f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(baseX, baseY);
        gl.glVertex2f(baseX + 20, baseY);
        gl.glVertex2f(baseX + 20, baseY + 20);
        gl.glVertex2f(baseX, baseY + 20);
        gl.glEnd();
        gl.glColor3f(player.isLaserAvailable ? 0 : 0.3f, player.isLaserAvailable ? 1 : 0.3f, player.isLaserAvailable ? 0 : 0.3f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(baseX + 30, baseY);
        gl.glVertex2f(baseX + 50, baseY);
        gl.glVertex2f(baseX + 50, baseY + 20);
        gl.glVertex2f(baseX + 30, baseY + 20);
        gl.glEnd();
        gl.glColor3f(player.specialAmmo > 0 ? 1 : 0.3f, player.specialAmmo > 0 ? 0.8f : 0.3f, 0);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(baseX + 60, baseY);
        gl.glVertex2f(baseX + 80, baseY);
        gl.glVertex2f(baseX + 80, baseY + 20);
        gl.glVertex2f(baseX + 60, baseY + 20);
        gl.glEnd();
    }

    private void drawStartScreen(GL gl) {
        gl.glColor3f(0.1f, 0.1f, 0.1f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(800, 0);
        gl.glVertex2f(800, 600);
        gl.glVertex2f(0, 600);
        gl.glEnd();
        gl.glColor3f(0, 1, 0);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(350, 250);
        gl.glVertex2f(350, 350);
        gl.glVertex2f(450, 300);
        gl.glEnd();
    }

    private void spawnSpecialBullets() {

        for (int i = 0; i < 3; i++)
            bullets.add(new Bullet((float) (Math.random() * 800), 0, (float) (Math.random() * 6) - 3, 10, false, 6));
    }

}