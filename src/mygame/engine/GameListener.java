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
    // Constructor to initialize the GameListener with a GameManager
    public GameListener(GameManager manager) {
        this.manager = manager;
    }

    GameManager manager;
    boolean[] keys = new boolean[256];

    // --- Variables for the scrolling background ---
    public static float backgroundY = 0;
    public static int currentBgPart = 0;

    // **Important Update:** Variable to monitor level change to fix flickering
    private int lastRenderedLevel = -1;

    int lvl1_BgStart = 28;
    int lvl2_BgStart = 32;
    int lvl3_BgStart = 36;
    // --- Loading variables ---
    boolean isLoading = true;
    int loadedAssetsIndex = 0;

    int coverTextureID;
    TextureReader.Texture coverTexture;

    // Texture names as they are in the codec
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
            "L1.0.png", // 28 (32 in old comment, corrected to 28)
            "L1.1.png", // 29 (33 in old comment)
            "L1.2.png",// 30 (34 in old comment)
            "L1.3.png" , // 31 (35 in old comment)

            // --- Level 2 background images (Start Index: 32) ---

            "B2.png", // 32
            "B1.png", // 33
            "B3.png",// 34
            "B4.png" , // 35
            // --- Level 3 background images (Start Index: 36) ---
            "L3.0.png", // 36
            "L3.1.png", // 37
            "L3.2.png", // 38
            "L3.3.png",  // 39


            "bulletup.png",  // 40
            // --- Add these icons at the end ---
            "laserIcon.png",  // 41
            "ShieldICon.png", // 42
            "bulletIcon.png",   // 43
            "numeralX.png", // 44

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

            // --- Death animation images (added at the end) ---
            // Index 55, 56 -> Enemy 1 Death
            "enemy1.1.png", //55
            "enemy1.2.png", //56

            // Index 57, 58 -> Enemy 2 Death
            "enemy2.1.png", //57
            "enemy2.2.png", //58

            // Index 59, 60 -> Enemy 3 Death (for types 20 and 23)
            "enemy3.1.png", //59
            "enemy3.3.png", //60

            // Index 61, 62, 63, 64, 65, 66, 67 -> Boss Level 3
            "Boss3.png",    //61
            "Boss3.1.png",  //62
            "Boss3.2.png",  //63
            "Boss3.3.png",  //64
            "Boss3.4.png",  //65
            "Boss3.5.png",  //66
            "Boss3.6.png"   //67

    };

    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];

    // Initializes OpenGL settings, coordinate system, blending, and loads the cover image.
    @Override
    public void init(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        GLU glu = new GLU();

        // 1. Important step: immediately color the screen black before doing anything else
        // So that if loading takes time, the user sees a black screen, not white
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Clear the buffer immediately to apply the black color
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        // 2. Coordinate settings
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0.0, 800.0, 0.0, 600.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // 3. Reserve texture IDs for all textures
        gl.glGenTextures(textureNames.length, textures, 0);

        int[] tempID = new int[1];
        gl.glGenTextures(1, tempID, 0);
        coverTextureID = tempID[0];

        // 4. Load the cover image (the heaviest operation) after screen setup
        try {
            // Tip: Ensure that the size of Front.png does not exceed 1024x1024 for faster loading
            coverTexture = TextureReader.readTexture("Assets/Front.png", true);

            gl.glBindTexture(GL.GL_TEXTURE_2D, coverTextureID);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, coverTexture.getWidth(), coverTexture.getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, coverTexture.getPixels());
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

            // Settings to prevent repetition
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

        } catch (IOException e) {
            System.err.println("Error Loading Cover Image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Called for rendering the graphics. Handles the loading screen state and the main game loop.
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
                // After loading, set the background color to white as desired
                gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
                manager.isGameRunning = true;
            }
        } else {
            drawBackground(gl);
            drawScore(gl);
            manager.player.handleInput(keys);
            manager.update();
            manager.render(gl, textures);
        }
    }

    // Draws the current score display in the top-left corner.
    public void drawScore(GL gl) {
        float x = 20; // Distance from the left
        float y = 600 - 20 - 48; // Distance from the top
        float size = 32; // Size of the coin and 'x' images
        float padding = 5; // Spacing between elements

        // 1. Draw coin image (Texture Index 24)
        drawTexture(gl, textures[24], x, y, size, size);

        // 2. Draw 'x' (Texture Index 44)
        float x2 = x + size + padding;
        drawTexture(gl, textures[44], x2, y, size/2, size); // 'x' is slightly smaller

        // 3. Draw score digits using numeral textures (indices 45 to 54)
        float x3 = x2 + size/2 + padding;
        String scoreStr = manager.score + "";
        for (char c : scoreStr.toCharArray()) {
            int num = c - '0'; // Convert character to digit
            drawTexture(gl, textures[45 + num], x3, y, size/2, size);
            x3 += size/2 + 2; // Increase spacing between digits
        }
    }


    // Function to draw a texture using GL_QUADS.
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

    // Loads a single texture at the given index.
    private void loadOneTexture(GL gl, int i) {
        try {
            texture[i] = TextureReader.readTexture("Assets//" + textureNames[i], true);
            gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, texture[i].getWidth(), texture[i].getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels());
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            // Prevents bleeding from the edges when filtering
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Draws the game's initial loading screen with the cover image and a progress bar.
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

        // Draw progress bar background (semi-transparent black)
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
        gl.glEnable(GL.GL_BLEND);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x - 5, y - 5);
        gl.glVertex2f(x + barWidth + 5, y - 5);
        gl.glVertex2f(x + barWidth + 5, y + barHeight + 5);
        gl.glVertex2f(x - 5, y + barHeight + 5);
        gl.glEnd();
        gl.glDisable(GL.GL_BLEND);

        // Draw empty part of the progress bar (dark red)
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + barWidth, y);
        gl.glVertex2f(x + barWidth, y + barHeight);
        gl.glVertex2f(x, y + barHeight);
        gl.glEnd();

        // Draw filled part of the progress bar (light blue)
        gl.glColor3f(0.0f, 0.8f, 1.0f);
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + (barWidth * percentage), y);
        gl.glVertex2f(x + (barWidth * percentage), y + barHeight);
        gl.glVertex2f(x, y + barHeight);
        gl.glEnd();

        // Draw progress bar border (white)
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

    // Draws the continuously scrolling background for the current game level.
    public void drawBackground(GL gl) {
        // 1. Monitor level change to reset counters immediately (prevents flickering during transition)
        if (manager.currentLevel != lastRenderedLevel) {
            backgroundY = 0;
            currentBgPart = 0;
            lastRenderedLevel = manager.currentLevel;
        }

        gl.glEnable(GL.GL_BLEND);
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        // 2. Determine the set of images based on the current level
        int startTextureIndex;
        int imagesPerLevel = 4; // Number of images per level

        if (manager.currentLevel == 1) {
            startTextureIndex = lvl1_BgStart;
        } else if (manager.currentLevel == 2) {
            startTextureIndex = lvl2_BgStart;
        } else {
            // Level 3 (or later)
            startTextureIndex = lvl3_BgStart;
        }

        // 3. Update background movement
        float scrollSpeed = 2.0f; // Movement speed
        backgroundY -= scrollSpeed;

        int height = 600; // Screen height

        // 4. Infinite looping logic
        // When the image goes off the bottom, reset coordinates and increment the counter
        if (backgroundY <= -height) {
            backgroundY += height;
            currentBgPart++;
        }

        // 5. Calculate indices using Modulo (%) to ensure seamless looping (0->1->2->3->0)
        // This prevents the "image disappearance" or "flickering" error
        int currentImgIndex = startTextureIndex + (currentBgPart % imagesPerLevel);
        int nextImgIndex = startTextureIndex + ((currentBgPart + 1) % imagesPerLevel);

        // Safety check (Null Safety)
        if (currentImgIndex >= textures.length || nextImgIndex >= textures.length) return;
        if (texture[currentImgIndex] == null || texture[nextImgIndex] == null) return;

        // 6. Prepare for drawing
        // Use int and overlap to prevent visible seams
        int y = Math.round(backgroundY);
        int overlap = 1; // 1 pixel overlap to hide seams

        gl.glPushMatrix();

        // --- Draw the current image (bottom) ---
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[currentImgIndex]);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(0, y);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(800, y);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(800, y + height);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(0, y + height);
        gl.glEnd();

        // --- Draw the next image (top) ---
        // Draw it slightly overlapping (y + height - overlap) to cover any gap
        int y2 = y + height - overlap;

        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[nextImgIndex]);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(0, y2);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(800, y2);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(800, y2 + height);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(0, y2 + height);
        gl.glEnd();

        gl.glPopMatrix();

        // Keep Blend enabled for the rest of the game elements
        gl.glEnable(GL.GL_BLEND);
    }

    // Called when the GLAutoDrawable is reshaped.
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

    // Called when the display mode or device changes.
    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    // Handles key press events, setting the corresponding key in the 'keys' array to true and managing game actions.
    @Override
    public void keyPressed(KeyEvent e) {
        if (isLoading) return;

        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = true;

        // --- (Removed Enter key code from here) ---
        // The game now starts via the Start button in the GUI

        if (manager.isGameRunning) {
            if (e.getKeyCode() == KeyEvent.VK_Z && !manager.player.isSpecialAttackActive) manager.fireLaser();
            if (e.getKeyCode() == KeyEvent.VK_X) manager.activateShield();
            if (e.getKeyCode() == KeyEvent.VK_SPACE) manager.player.activateSpecialAttack();
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) manager.game.togglePause();
        }
    }

    // Handles key release events, setting the corresponding key in the 'keys' array to false.
    @Override
    public void keyReleased(KeyEvent e) {
        if (isLoading) return;
        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = false;
    }

    // Handles key typed events (currently unused).
    @Override
    public void keyTyped(KeyEvent e) {}

    // Resets all keys in the 'keys' array to false.
    public void resetKeys() {
        Arrays.fill(keys, false);
    }

    // Handles mouse click events (currently prints to console in mousePressed).
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    // Handles mouse button pressed events (currently prints cursor coordinates).
    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println(e.getX() + " " + e.getY());
    }

    // Handles mouse button released events.
    @Override
    public void mouseReleased(MouseEvent e) {

    }

    // Handles mouse enters component events.
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    // Handles mouse exits component events.
    @Override
    public void mouseExited(MouseEvent e) {

    }
}