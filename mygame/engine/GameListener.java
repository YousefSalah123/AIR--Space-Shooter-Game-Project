package mygame.engine;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * GameListener is the core OpenGL event handler for the game.
 * Responsibilities:
 * 1. Handle rendering (drawing objects on screen)
 * 2. Handle game updates (via GameManager)
 * 3. Handle keyboard input for player controls
 */
public class GameListener implements GLEventListener, KeyListener {

    // The main game logic manager
    GameManager manager = new GameManager();

    // Array to track key presses for smooth movement
    boolean[] keys = new boolean[256];

    // -----------------------------
    // 1. Initialization of OpenGL
    // -----------------------------
    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        // Set the background color to black
        gl.glClearColor(0, 0, 0, 1);

        // Set up a 2D orthographic projection
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, 800, 0, 600, -1, 1); // left, right, bottom, top, near, far
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    // -----------------------------
    // 2. Main render & update loop
    // -----------------------------
    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        // Clear the screen before drawing
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        // --- Update player input ---
        manager.player.handleInput(keys); // Uses key array for smooth movement

        // --- Update game logic ---
        manager.update(); // Moves bullets, enemies, bosses, items, etc.

        // --- Draw everything ---
        manager.render(gl); // Render player, enemies, bullets, items, boss, UI
    }

    // -----------------------------
    // 3. Handle key presses
    // -----------------------------
    @Override
    public void keyPressed(KeyEvent e) {
        // Mark the key as pressed in the array
        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = true;

        // --- Start game with ENTER ---
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!manager.isGameRunning) {
                manager.isGameRunning = true; // Start the game!
            }
        }

        // --- Game controls (only active while game is running) ---
        if (manager.isGameRunning) {
            if (e.getKeyCode() == KeyEvent.VK_Z) {
                // Fire laser if not already active
                if (!manager.player.isSpecialAttackActive) manager.fireLaser();
            }
            if (e.getKeyCode() == KeyEvent.VK_X) {
                manager.activateShield(); // Activate shield manually
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                manager.player.activateSpecialAttack(); // Trigger special attack
            }
        }
    }

    // -----------------------------
    // 4. Handle key releases
    // -----------------------------
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() < 256) keys[e.getKeyCode()] = false;

        // Note: Removed SPACE key release logic to avoid conflicts
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used in this game (needed by KeyListener interface)
    }

    // -----------------------------
    // 5. Window reshape (resize) event
    // -----------------------------
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // Not used here, but required by GLEventListener
    }

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
        // Deprecated in modern JOGL, left empty
    }
}