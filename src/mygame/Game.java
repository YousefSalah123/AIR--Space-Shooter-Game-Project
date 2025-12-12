package mygame;

import com.sun.opengl.util.FPSAnimator;
import mygame.GUI.ArcadeGameUI;
import mygame.GUI.EndLevelFrame;
import mygame.GUI.PauseButtonPanel;
import mygame.engine.GameListener;
import mygame.engine.GameManager;
import mygame.engine.HighScoreManagment;

import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;


// Main class for the game application. Extends JFrame and manages the game loop,
// UI layering, state transitions (main menu, game play, game over), and pause functionality.
public class Game extends JFrame {
    private GLCanvas glCanvas;
    private GameListener listener;
    private FPSAnimator animator;
    private ArcadeGameUI mainMenu;
    private GameManager manager;
    private JLayeredPane layeredPane;
    private PauseButtonPanel pauseButtonPanel;

    private static String playerName = "UNKNOWN";

    // Returns the current player's name.
    public static String getPlayerName() {
        return playerName;
    }

    // Sets the player's name.
    public static void setPlayerName(String name) {
        Game.playerName = name;
    }

    // Main entry point for the application.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }

    // Constructor: Sets up the main JFrame, initializes game components (Manager, Listener),
    // and configures the layered pane for UI elements over the OpenGL canvas.
    public Game() {
        super("Galactic Air Mission");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        manager = new GameManager(this);
        listener = new GameListener(manager);

        // 1. GLCanvas setup
        glCanvas = new GLCanvas();
        glCanvas.addGLEventListener(listener);
        glCanvas.addKeyListener(listener);
        glCanvas.setFocusable(true);

        // 2. PauseButtonPanel setup
        pauseButtonPanel = new PauseButtonPanel(this);
        // Hide the button immediately upon creation
        pauseButtonPanel.setVisible(false);

        // Set the appropriate dimensions and location for the "ESC" button (top right)
        // (x=700, y=10, width=85, height=35)
        pauseButtonPanel.setBounds(700, 10, 85, 35);

        // 3. JLayeredPane setup
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 600));

        // 4. Add components to JLayeredPane
        // a. Add GLCanvas (Bottom layer)
        glCanvas.setBounds(0, 0, 800, 600);
        layeredPane.add(glCanvas, JLayeredPane.DEFAULT_LAYER);

        // b. Add Pause Button (Top layer)
        layeredPane.add(pauseButtonPanel, JLayeredPane.PALETTE_LAYER);


        // 5. Add layeredPane to JFrame
        this.add(layeredPane, BorderLayout.CENTER);


        animator = new FPSAnimator(glCanvas, 60);

        showMainMenu();
    }

    // Displays the main menu screen and stops the game animator.
    public void showMainMenu() {
        this.setVisible(false);

        if (animator.isAnimating()) animator.stop();

        // Hide the button
        if (pauseButtonPanel != null) {
            pauseButtonPanel.setVisible(false);
        }

        mainMenu = new ArcadeGameUI(this);

        mainMenu.setStartGameAction(e -> startActualGame());

        mainMenu.setVisible(true);
    }

    // Starts the actual game loop, resets the game state, shows the main frame,
    // and starts the FPS animator.
    public void startActualGame() {
        if (mainMenu != null) mainMenu.setVisible(false);

        manager.resetGame();

        this.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            if (pauseButtonPanel != null) {
                pauseButtonPanel.setVisible(true);
            }
        });

        glCanvas.requestFocusInWindow();

        if (!animator.isAnimating()) animator.start();
    }

    /**
     * Called when the round ends (Game Over or Victory).
     * Handles stopping the game, displaying the EndLevelFrame, and managing high scores.
     */
    public void handleGameOver(boolean victory, int score) {

        if (animator.isAnimating()) animator.stop();

        this.setVisible(false);

        // Hide the button
        if (pauseButtonPanel != null) {
            pauseButtonPanel.setVisible(false);
        }

        final EndLevelFrame[] holder = new EndLevelFrame[1];

        holder[0] = new EndLevelFrame(
                victory,
                score,
                e -> {  // Retry
                    holder[0].dispose();
                    startActualGame();
                },
                e -> {  // Back to Menu
                    holder[0].dispose();
                    showMainMenu();
                    HighScoreManagment.addScore(getPlayerName(), manager.score);
                }
        );

        holder[0].setVisible(true);
    }

    // Toggles the pause state, stopping the animator and displaying the PauseMenuFrame.
    public void togglePause() {
        if (animator.isAnimating()) animator.stop();

        final mygame.GUI.PauseMenuFrame[] menuHolder = new mygame.GUI.PauseMenuFrame[1];

        menuHolder[0] = new mygame.GUI.PauseMenuFrame(
                // 1. Resume
                e -> {
                    menuHolder[0].dispose();
                    listener.resetKeys();
                    if (!animator.isAnimating()) animator.start();
                    glCanvas.requestFocusInWindow();
                },
                // 2. Restart
                e -> {
                    menuHolder[0].dispose();
                    listener.resetKeys();

                    startActualGame();
                },
                // 3. Back to Menu
                e -> {
                    menuHolder[0].dispose();
                    listener.resetKeys();

                    // If sound is muted, unmute it before returning to the main menu
                    if (mygame.GUI.PauseMenuFrame.isMuted) {
                        toggleSound();
                        mygame.GUI.PauseMenuFrame.isMuted = false;
                    }

                    this.setVisible(false);
                    showMainMenu();
                },
                // 4. Toggle Sound
                e -> toggleSound()
        );

        menuHolder[0].setVisible(true);
    }

    // Function to toggle the sound state (ON/OFF) via the SoundManager.
    public void toggleSound() {
        if (manager != null && manager.soundManager != null) {
            manager.soundManager.toggleMute();
        }
    }
}