package mygame;

import com.sun.opengl.util.FPSAnimator;
import mygame.engine.GameListener;

import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;

/**
 * Main Game Window (JFrame)
 * - Sets up OpenGL canvas
 * - Handles keyboard input
 * - Runs the game loop at fixed FPS using FPSAnimator
 */
public class Game extends JFrame {

    private GLCanvas glCanvas;         // OpenGL rendering canvas
    private GameListener listener = new GameListener(); // Handles rendering & input
    private FPSAnimator animator;      // Controls frame updates

    // Entry point of the game
    public static void main(String[] args) {
        new Game();
    }

    // Constructor: sets up the JFrame and OpenGL canvas
    public Game() {
        super("Airplane Shooter 2D");

        // Close the window properly when the user exits
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Window size (matches your glOrtho dimensions in GameManager)
        setSize(800, 600);

        // Center the window on the screen
        setLocationRelativeTo(null);

        // Use BorderLayout for easy placement of canvas
        setLayout(new BorderLayout());

        // ---------------------------
        // 1. Create OpenGL Canvas
        // ---------------------------
        glCanvas = new GLCanvas();

        // ---------------------------
        // 2. Attach listener
        // ---------------------------
        glCanvas.addGLEventListener(listener); // Rendering logic
        glCanvas.addKeyListener(listener);     // Keyboard input for player control

        // ---------------------------
        // 3. Focus on the canvas
        // ---------------------------
        glCanvas.setFocusable(true);
        glCanvas.requestFocusInWindow(); // Ensures keyboard input works immediately

        // ---------------------------
        // 4. Add canvas to the JFrame
        // ---------------------------
        add(glCanvas, BorderLayout.CENTER);

        // Make the window visible
        setVisible(true);

        // ---------------------------
        // 5. Start the game loop (60 FPS)
        // ---------------------------
        animator = new FPSAnimator(glCanvas, 60);
        animator.start();
    }
}