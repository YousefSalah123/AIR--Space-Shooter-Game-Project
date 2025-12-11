package com.mygame.engine;

import Texture.AnimListener;
import Texture.TextureReader;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class GameListener extends AnimListener implements GLEventListener, KeyListener {

    GameManager manager = new GameManager();
    boolean[] keys = new boolean[256];

    // --- متغيرات الخلفية المتحركة ---
    public static float backgroundY = 0;
    public static int currentBgPart = 0;

    // **تحديث هام:** متغير لمراقبة تغيير المستوى لإصلاح الوميض
    private int lastRenderedLevel = -1;

    int lvl1_BgStart = 28;
    int lvl2_BgStart = 32;
    int lvl3_BgStart = 36;
    // --- متغيرات التحميل ---
    boolean isLoading = true;
    int loadedAssetsIndex = 0;

    int coverTextureID;
    TextureReader.Texture coverTexture;

    // أسماء الصور كما هي في كودك
    String textureNames[] = {
            "Star1.png",        // 0
            "Hero.png",         // 1
            "Hero2.png",        // 2
            "Hero3.png",        // 3
            "Hero4.png",        // 4
            "enemy1.png",       // 5
            "Bullet v6.png",    // 6
            "Boss1.png",        // 7
            "Boss1.1.png",      // 8
            "Boss1.2.png",      // 9
            "Boss1.4.png",      // 10
            "Boss1.6.png",      // 11
            "Boss2.png",        // 12
            "Boss2.1.png",      // 13
            "Boss2.2.png",      // 14
            "Boss2.3.png",      // 15
            "Boss2.4.png",      // 16
            "Boss2.5.png",      // 17
            "Boss2.5.png",      // 18
            "heart.png",        // 19
            "enemy3.png",       // 20
            "enemy1.png",       // 21
            "enemy2.png",       // 22
            "enemy3.png",       // 23
            "coin.png",         // 24
            "BulletHero.png",   // 25
            "Boss1.png",        // 26
            "Shield.png",       // 27

            // --- صور خلفيات المستوى الأول (Start Index: 28) ---
            "o1.png", // 32
            "sea2.png", // 33
            "o3.png",// 34
            "sea2.png" , // 35

            // --- صور خلفيات المستوى الثاني (Start Index: 32) ---
            "Lvl1_Part1.png", // 32
            "Lvl1_Part1.png", // 33
            "Lvl1_Part1.png", // 34
            "Lvl1_Part1.png",  // 35


            "o1.png", // 36
            "sea2.png", // 37
            "o3.png",// 38
            "sea2.png" , // 39

            "PowerUp.png"  // 40
    };

    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];

    public void init(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        GLU glu = new GLU();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0.0, 800.0, 0.0, 600.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glGenTextures(textureNames.length, textures, 0);

        int[] tempID = new int[1];
        gl.glGenTextures(1, tempID, 0);
        coverTextureID = tempID[0];

        try {
            // تحميل صورة الغلاف (المسار كما عندك)
            coverTexture = TextureReader.readTexture("\\Assets\\Front.png", true);
            gl.glBindTexture(GL.GL_TEXTURE_2D, coverTextureID);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, coverTexture.getWidth(), coverTexture.getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, coverTexture.getPixels());
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            // منع bleeding من حواف التكسشر
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        if (isLoading) {
            drawLoadingScreen(gl);

            if (loadedAssetsIndex < textureNames.length) {
                loadOneTexture(gl, loadedAssetsIndex);
                loadedAssetsIndex++;
                try { Thread.sleep(30); } catch (InterruptedException e) {}
            } else {
                isLoading = false;
                // بعد التحميل خلى لون الخلفية أبيض كما عندك
                gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
                manager.isGameRunning = true;
            }
        } else {
            drawBackground(gl);
            manager.player.handleInput(keys);
            manager.update();
            manager.render(gl, textures);
        }
    }

    private void loadOneTexture(GL gl, int i) {
        try {
            texture[i] = TextureReader.readTexture("Assets//" + textureNames[i], true);
            gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, texture[i].getWidth(), texture[i].getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels());
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            // يمنع bleeding من الأطراف عند الفلترة
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawLoadingScreen(GL gl) {
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, coverTextureID);
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(0, 0);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(800, 0);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(800, 600);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(0, 600);
        gl.glEnd();

        gl.glDisable(GL.GL_TEXTURE_2D);

        float percentage = (float) loadedAssetsIndex / textureNames.length;
        float barWidth = 600;
        float barHeight = 20;
        float x = (800 - barWidth) / 2;
        float y = 50;

        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
        gl.glEnable(GL.GL_BLEND);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x - 5, y - 5);
        gl.glVertex2f(x + barWidth + 5, y - 5);
        gl.glVertex2f(x + barWidth + 5, y + barHeight + 5);
        gl.glVertex2f(x - 5, y + barHeight + 5);
        gl.glEnd();
        gl.glDisable(GL.GL_BLEND);

        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + barWidth, y);
        gl.glVertex2f(x + barWidth, y + barHeight);
        gl.glVertex2f(x, y + barHeight);
        gl.glEnd();

        gl.glColor3f(0.0f, 0.8f, 1.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + (barWidth * percentage), y);
        gl.glVertex2f(x + (barWidth * percentage), y + barHeight);
        gl.glVertex2f(x, y + barHeight);
        gl.glEnd();

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + barWidth, y);
        gl.glVertex2f(x + barWidth, y + barHeight);
        gl.glVertex2f(x, y + barHeight);
        gl.glEnd();

        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    public void drawBackground(GL gl) {
        // 1. مراقبة تغيير المستوى لتصفير العدادات فوراً (يمنع الوميض عند الانتقال)
        if (manager.currentLevel != lastRenderedLevel) {
            backgroundY = 0;
            currentBgPart = 0;
            lastRenderedLevel = manager.currentLevel;
        }

        gl.glEnable(GL.GL_BLEND);
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        // 2. تحديد مجموعة الصور بناءً على المستوى الحالي
        int startTextureIndex;
        int imagesPerLevel = 4; // عدد الصور لكل مستوى

        if (manager.currentLevel == 1) {
            startTextureIndex = lvl1_BgStart;
        } else if (manager.currentLevel == 2) {
            startTextureIndex = lvl2_BgStart;
        } else {
            // المستوى الثالث (أو ما يليه)
            startTextureIndex = lvl3_BgStart;
        }

        // 3. تحديث حركة الخلفية
        float scrollSpeed = 2.0f; // سرعة التحرك
        backgroundY -= scrollSpeed;

        int height = 600; // ارتفاع الشاشة

        // 4. منطق الدوران اللانهائي
        // عندما تخرج الصورة من الأسفل، نعيد الإحداثيات ونزيد العداد
        if (backgroundY <= -height) {
            backgroundY += height;
            currentBgPart++;
        }

        // 5. حساب الفهارس باستخدام Modulo (%) لضمان التكرار السلس (0->1->2->3->0)
        // هذا يمنع خطأ "اختفاء الصورة" أو "الوميض"
        int currentImgIndex = startTextureIndex + (currentBgPart % imagesPerLevel);
        int nextImgIndex = startTextureIndex + ((currentBgPart + 1) % imagesPerLevel);

        // حماية من الأخطاء (Null Safety)
        if (currentImgIndex >= textures.length || nextImgIndex >= textures.length) return;
        if (texture[currentImgIndex] == null || texture[nextImgIndex] == null) return;

        // 6. تجهيز الرسم
        // نستخدم int و overlap لمنع الخطوط الفاصلة
        int y = Math.round(backgroundY);
        int overlap = 1; // تداخل 1 بيكسل لإخفاء اللحامات

        gl.glPushMatrix();

        // --- رسم الصورة الحالية (أسفل) ---
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[currentImgIndex]);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(0, y);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(800, y);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(800, y + height);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(0, y + height);
        gl.glEnd();

        // --- رسم الصورة التالية (أعلى) ---
        // نرسمها متداخلة قليلاً (y + height - overlap) لتغطية أي فراغ
        int y2 = y + height - overlap;

        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[nextImgIndex]);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(0, y2);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(800, y2);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(800, y2 + height);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(0, y2 + height);
        gl.glEnd();

        gl.glPopMatrix();

        // ترك الـ Blend مفعلاً لباقي عناصر اللعبة
        gl.glEnable(GL.GL_BLEND);
    }    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (isLoading) return;

        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = true;

        if (manager.isGameRunning) {
            if (e.getKeyCode() == KeyEvent.VK_Z && !manager.player.isSpecialAttackActive) manager.fireLaser();
            if (e.getKeyCode() == KeyEvent.VK_X) manager.activateShield();
            if (e.getKeyCode() == KeyEvent.VK_SPACE) manager.player.activateSpecialAttack();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (isLoading) return;
        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = false;
    }
    @Override
    public void keyTyped(KeyEvent e) {}
}
