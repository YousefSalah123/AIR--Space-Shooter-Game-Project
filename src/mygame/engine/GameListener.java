package mygame.engine;
import Texture.AnimListener;
import Texture.TextureReader;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Arrays;

public class GameListener extends AnimListener implements GLEventListener, KeyListener , MouseListener {

    GameManager manager;
    boolean[] keys = new boolean[256];

    // --- Variables for the scrolling background ---
    public static float backgroundY = 0;
    public static int currentBgPart = 0;
    private int lastRenderedLevel = -1;

    int lvl1_BgStart = 28;
    int lvl2_BgStart = 32;
    int lvl3_BgStart = 36;

    // --- Loading variables ---
    boolean isLoading = true;
    int loadedAssetsIndex = 0;

    int coverTextureID;
    TextureReader.Texture coverTexture;

    // Texture names
    String textureNames[] = {
            "Star1.png",        // 0
            "Hero.png",         // 1
            "Hero2.png",        // 2
            "Hero3.png",        // 3
            "Hero4.png",        // 4
            "enemy1.png",       // 5
            "Bullet v6.png",    // 6
            "BossFinal1.png",        // 7
            "BossFinal1.2.png",      // 8
            "BossFinal1.2.png",      // 9
            "BossFinal1.3.png",      // 10
            "BossFinal1.4.png",      // 11
            "Boss2.png",        // 12
            "Boss2.1.png",      // 13
            "Boss2.2.png",      // 14
            "Boss2.3.png",      // 15
            "Boss2.4.png",      // 16
            "Boss2.5.png",      // 17
            "Boss2.6.png",      // 18
            "heart.png",        // 19
            "enemy3.png",       // 20
            "enemy1.png",       // 21
            "enemy2.png",        // 22
            "enemy3.png",       // 23
            "coin.png",         // 24
            "BulletHero.png",   // 25
            "Boss2.6.png",        // 26
            "Shield.png",       // 27

            // --- Level 1 background images (Start Index: 28) ---
            "L1.0.png", // 28
            "L1.1.png", // 29
            "L1.2.png", // 30
            "L1.3.png", // 31

            // --- Level 2 background images (Start Index: 32) ---
            "B2.png", // 32
            "B1.png", // 33
            "B3.png", // 34
            "B4.png", // 35

            // --- Level 3 background images (Start Index: 36) ---
            "L3.0.png", // 36
            "L3.1.png", // 37
            "L3.2.png", // 38
            "L3.3.png", // 39

            "bulletup.png",   // 40
            "laserIcon.png",  // 41
            "ShieldICon.png", // 42
            "bulletIcon.png", // 43
            "numeralX.png",   // 44

            // numbers (index : 45)
            "numeral0.png", //45
            "numeral1.png",//46
            "numeral2.png",//47
            "numeral3.png",//48
            "numeral4.png",//49
            "numeral5.png",//50
            "numeral6.png",//51
            "numeral7.png",//52
            "numeral8.png",//53
            "numeral9.png",//54

            // --- Death animation images ---
            "enemy1.1.png", //55
            "enemy1.2.png", //56
            "enemy2.1.png", //57
            "enemy2.2.png", //58
            "enemy3.1.png", //59
            "enemy3.3.png", //60

            // Boss Level 3
            "Boss3.1.png",  //61
            "Boss3.2.png",  //62
            "Boss3.3.png",  //63
            "Boss3.4.png",  //64
            "Boss3.5.png",  //65
            "Boss3.6.png",  //66

            // --- MULTIPLAYER ASSET ---
            // Index 67: The Red Plane for Player 2
            // IMPORTANT: Make sure this file exists in Assets folder!
            "RedPlane2.png",   // 67 (Base / Healthy)
            "RedPlane2.png",  // 68 (Light Damage)
            "RedPlane3.png",  // 69 (Heavy Damage)
            "RedPlane4.png"   // 70 (Destroyed)
    };

    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];

    public GameListener(GameManager manager) {
        this.manager = manager;
    }

    @Override
    public void init(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        GLU glu = new GLU();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

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
            coverTexture = TextureReader.readTexture("Assets/Front.png", true);
            gl.glBindTexture(GL.GL_TEXTURE_2D, coverTextureID);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, coverTexture.getWidth(), coverTexture.getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, coverTexture.getPixels());
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

        } catch (IOException e) {
            System.err.println("Error Loading Cover Image: " + e.getMessage());
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
                gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
                manager.isGameRunning = true;
            }
        } else {
            drawBackground(gl);
            drawScore(gl);

            // --- Update Input for Both Players ---
            // The logic for what keys do what is inside Player.handleInput
            manager.player1.handleInput(keys);
            if (manager.isMultiplayer && manager.player2 != null) {
                manager.player2.handleInput(keys);
            }

            manager.update();
            manager.render(gl, textures);
        }
    }

    public void drawScore(GL gl) {
        float x = 20;
        float y = 600 - 20 - 48;
        float size = 32;
        float padding = 5;

        drawTexture(gl, textures[24], x, y, size, size); // Coin

        float x2 = x + size + padding;
        drawTexture(gl, textures[44], x2, y, size/2, size); // 'x'

        float x3 = x2 + size/2 + padding;
        String scoreStr = manager.score + "";
        for (char c : scoreStr.toCharArray()) {
            int num = c - '0';
            drawTexture(gl, textures[45 + num], x3, y, size/2, size);
            x3 += size/2 + 2;
        }
    }

    protected void drawTexture(GL gl, int textureId, float x, float y, float w, float h) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glColor3f(1, 1, 1);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0, 1); gl.glVertex2f(x, y + h);
        gl.glTexCoord2f(1, 1); gl.glVertex2f(x + w, y + h);
        gl.glTexCoord2f(1, 0); gl.glVertex2f(x + w, y);
        gl.glTexCoord2f(0, 0); gl.glVertex2f(x, y);
        gl.glEnd();
        gl.glDisable(GL.GL_BLEND);
    }

    private void loadOneTexture(GL gl, int i) {
        try {
            texture[i] = TextureReader.readTexture("Assets//" + textureNames[i], true);
            gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, texture[i].getWidth(), texture[i].getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels());
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawLoadingScreen(GL gl) {
        // رسم الخلفية
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

        // ⭐ FIX: تعديل أبعاد ومكان شريط التحميل
        float percentage = (float) loadedAssetsIndex / textureNames.length;
        float barWidth = 500; // قللنا العرض من 600 لـ 500 عشان يبعد عن الزر
        float barHeight = 20;

        // توسيط البار بناءً على العرض الجديد
        float x = (800 - barWidth) / 2;

        // رفعه قليلاً للأعلى (كان 50، خليناه 80) عشان يكون فوق مستوى الزر
        float y = 80;

        // رسم خلفية البار (الظل الأسود)
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
        gl.glEnable(GL.GL_BLEND);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x - 5, y - 5);
        gl.glVertex2f(x + barWidth + 5, y - 5);
        gl.glVertex2f(x + barWidth + 5, y + barHeight + 5);
        gl.glVertex2f(x - 5, y + barHeight + 5);
        gl.glEnd();
        gl.glDisable(GL.GL_BLEND);

        // رسم الإطار الأحمر
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + barWidth, y);
        gl.glVertex2f(x + barWidth, y + barHeight);
        gl.glVertex2f(x, y + barHeight);
        gl.glEnd();

        // رسم التقدم (الأزرق)
        gl.glColor3f(0.0f, 0.8f, 1.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + (barWidth * percentage), y);
        gl.glVertex2f(x + (barWidth * percentage), y + barHeight);
        gl.glVertex2f(x, y + barHeight);
        gl.glEnd();

        // رسم الإطار الأبيض
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
        if (manager.currentLevel != lastRenderedLevel) {
            backgroundY = 0;
            currentBgPart = 0;
            lastRenderedLevel = manager.currentLevel;
        }

        gl.glEnable(GL.GL_BLEND);
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        int startTextureIndex;
        int imagesPerLevel = 4;

        if (manager.currentLevel == 1) startTextureIndex = lvl1_BgStart;
        else if (manager.currentLevel == 2) startTextureIndex = lvl2_BgStart;
        else startTextureIndex = lvl3_BgStart;

        backgroundY -= 2.0f;
        int height = 600;

        if (backgroundY <= -height) {
            backgroundY += height;
            currentBgPart++;
        }

        int currentImgIndex = startTextureIndex + (currentBgPart % imagesPerLevel);
        int nextImgIndex = startTextureIndex + ((currentBgPart + 1) % imagesPerLevel);

        if (currentImgIndex >= textures.length || nextImgIndex >= textures.length) return;
        if (texture[currentImgIndex] == null || texture[nextImgIndex] == null) return;

        int y = Math.round(backgroundY);
        int overlap = 1;

        gl.glPushMatrix();

        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[currentImgIndex]);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(0, y);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(800, y);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(800, y + height);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(0, y + height);
        gl.glEnd();

        int y2 = y + height - overlap;
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[nextImgIndex]);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(0, y2);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(800, y2);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(800, y2 + height);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(0, y2 + height);
        gl.glEnd();

        gl.glPopMatrix();
        gl.glEnable(GL.GL_BLEND);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (isLoading) return;

        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = true;

        if (manager.isGameRunning) {
            // --- Player 1 Controls (Blue) ---
            if (e.getKeyCode() == KeyEvent.VK_Z) manager.player1.activateLaserBeam();

            if (e.getKeyCode() == KeyEvent.VK_X) {
                manager.player1.activateShieldManual();
                if (manager.isMultiplayer && manager.player2 != null) {
                    manager.player2.activateShieldManual();
                }
            }

            if (e.getKeyCode() == KeyEvent.VK_SPACE) manager.player1.activateSpecialAttack();

            // --- Player 2 Controls (Red) - Only if Multiplayer ---
            if (manager.isMultiplayer && manager.player2 != null) {
                // Ability Keys for P2: Q=Laser, E=Shield, F=Super
                if (e.getKeyCode() == KeyEvent.VK_Q) manager.player2.activateLaserBeam();
                if (e.getKeyCode() == KeyEvent.VK_E) manager.player2.activateShieldManual();
                if (e.getKeyCode() == KeyEvent.VK_F) manager.player2.activateSpecialAttack();
            }

            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) manager.game.togglePause();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (isLoading) return;
        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public void resetKeys() {
        Arrays.fill(keys, false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println(e.getX() + " " + e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}