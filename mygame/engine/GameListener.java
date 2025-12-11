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

    // تأكد أن أسماء الصور هنا تطابق تماماً الملفات الموجودة في فولدر Assets
    String textureNames[] = {
            "Star1.png", // 0
            "Hero.png", // 1
            "Hero2.png",
            "Hero3.png",
            "Hero4.png",
            "enemy1.png",    // 2 enemy=>5
            "Bullet v6.png",    // 3 bullet=>6
            "Boss1.png",// 4 Boss of level 1=>7
            "Boss1.1.png",
            "Boss1.2.png",
            "Boss1.4.png",
            "Boss1.6.png",
            "Boss2.png",// 4 Boss of level 2=>12
            "Boss2.1.png",
            "Boss2.2.png",
            "Boss2.3.png",
            "Boss2.4.png",
            "Boss2.5.png",
            "Boss2.5.png",
            "heart.png",    // 5 item=>19
            "enemy3.png",     // 6 middle boss=>20
            "enemy1.png",
            "enemy2.png",
            "enemy3.png",
            "coin.png"



    };

    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];
    public void init(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        GLU glu = new GLU();

        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // إعداد الإحداثيات
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0.0, 800.0, 0.0, 600.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnable(GL.GL_TEXTURE_2D);
        // تفعيل الشفافية
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glGenTextures(textureNames.length, textures, 0);

        for (int i = 0; i < textureNames.length; i++) {
            try {
                long startTime = System.currentTimeMillis(); // بداية الوقت

                texture[i] = TextureReader.readTexture("Assets" + "//" + textureNames[i], true);

                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
                gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, texture[i].getWidth(), texture[i].getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels());
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

                long endTime = System.currentTimeMillis(); // نهاية الوقت
                System.out.println("Loaded: " + textureNames[i] + " in " + (endTime - startTime) + "ms");

            } catch (IOException e) {
                System.out.println("Error loading texture: " + textureNames[i]);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        // 1. رسم الخلفية
        drawBackground(gl);

        // 2. تحديث اللعبة
        manager.player.handleInput(keys);
        manager.update();

        // 3. رسم اللعبة (نمرر مصفوفة الصور)
        manager.render(gl, textures);
    }

    public void drawBackground(GL gl){
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[0]); // صورة الخلفية
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);

        // تعديل الإحداثيات لتملأ الشاشة (0,0) إلى (800,600)
        // Texture Coordinates (0,0) -> Image Bottom-Left
        // Vertex Coordinates (0,0) -> Screen Bottom-Left

        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex2f(0, 0); // Bottom-Left

        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex2f(800, 0); // Bottom-Right

        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex2f(800, 600); // Top-Right

        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex2f(0, 600); // Top-Left

        gl.glEnd();
        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // يمكن تركها فارغة لأننا ثبتنا الأبعاد في init
    }

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = true;
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!manager.isGameRunning) manager.isGameRunning = true;
        }
        if (manager.isGameRunning) {
            if (e.getKeyCode() == KeyEvent.VK_Z && !manager.player.isSpecialAttackActive) manager.fireLaser();
            if (e.getKeyCode() == KeyEvent.VK_X) manager.activateShield();
            if (e.getKeyCode() == KeyEvent.VK_SPACE) manager.player.activateSpecialAttack();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = false;
    }
    @Override
    public void keyTyped(KeyEvent e) {}
}