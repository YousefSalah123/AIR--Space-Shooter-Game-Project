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

public class Game extends JFrame {
    private GLCanvas glCanvas;
    private GameListener listener;
    private FPSAnimator animator;
    private ArcadeGameUI mainMenu;
    private GameManager manager;
    private JLayeredPane layeredPane;
    private PauseButtonPanel pauseButtonPanel;

    private static String playerName = "PLAYER 1";
    private static String player2Name = "PLAYER 2";

    public static String getPlayerName() { return playerName; }
    public static void setPlayerName(String name) { Game.playerName = name; }
    public static String getPlayer2Name() { return player2Name; }
    public static void setPlayer2Name(String name) { Game.player2Name = name; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }

    public Game() {
        super("Galactic Air Mission");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        manager = new GameManager(this);
        listener = new GameListener(manager);

        glCanvas = new GLCanvas();
        glCanvas.addGLEventListener(listener);
        glCanvas.addKeyListener(listener);
        glCanvas.setFocusable(true);

        pauseButtonPanel = new PauseButtonPanel(this);
        pauseButtonPanel.setVisible(false);
        pauseButtonPanel.setBounds(690, 525, 85, 35);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 600));

        glCanvas.setBounds(0, 0, 800, 600);
        layeredPane.add(glCanvas, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(pauseButtonPanel, JLayeredPane.PALETTE_LAYER);

        this.add(layeredPane, BorderLayout.CENTER);

        animator = new FPSAnimator(glCanvas, 60);

        showMainMenu();
    }

    public void showMainMenu() {
        this.setVisible(true);

        // Optimization: Stop GL animation in the menu to conserve resources;
        // the menu handles its own Swing-based animation.
        if (animator.isAnimating()) animator.stop();

        if (pauseButtonPanel != null) pauseButtonPanel.setVisible(false);

        if (mainMenu != null) layeredPane.remove(mainMenu);

        mainMenu = new ArcadeGameUI(this);
        mainMenu.setBounds(0, 0, 800, 600);

        // Add to the Modal Layer (Topmost)
        layeredPane.add(mainMenu, JLayeredPane.MODAL_LAYER);

        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public void startActualGame(boolean isMultiplayer) {
        if (mainMenu != null) {
            mainMenu.setVisible(false);
            layeredPane.remove(mainMenu);
            layeredPane.repaint();
        }

        // Game Initialization
        manager.isMenuState = false; // Safety check
        manager.isMultiplayer = isMultiplayer;

        if (isMultiplayer && manager.player2 == null) {
            manager.player2 = new mygame.objects.Player(500, 50, true);
        }

        manager.resetGame();

        this.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            if (pauseButtonPanel != null) pauseButtonPanel.setVisible(true);
        });

        glCanvas.requestFocusInWindow();

        // Start the main game loop
        if (!animator.isAnimating()) animator.start();
    }

    public void startActualGame() {
        startActualGame(manager.isMultiplayer);
    }

    public void handleGameOver(boolean victory, int score) {
        if (animator.isAnimating()) animator.stop();
        this.setVisible(false);
        if (pauseButtonPanel != null) pauseButtonPanel.setVisible(false);

        final EndLevelFrame[] holder = new EndLevelFrame[1];
        holder[0] = new EndLevelFrame(
                victory, score,
                e -> { holder[0].dispose(); startActualGame(); },
                e -> {
                    holder[0].dispose(); showMainMenu();
                    String saveName = getPlayerName();
                    if (manager.isMultiplayer) saveName += " & " + getPlayer2Name();
                    HighScoreManagment.addScore(saveName, manager.score, manager.isMultiplayer);
                }
        );
        holder[0].setVisible(true);
    }

    public void togglePause() {
        if (animator.isAnimating()) animator.stop();
        final mygame.GUI.PauseMenuFrame[] menuHolder = new mygame.GUI.PauseMenuFrame[1];
        menuHolder[0] = new mygame.GUI.PauseMenuFrame(
                e -> {
                    menuHolder[0].dispose(); listener.resetKeys();
                    if (!animator.isAnimating()) animator.start();
                    glCanvas.requestFocusInWindow();
                },
                e -> { menuHolder[0].dispose(); listener.resetKeys(); startActualGame(); },
                e -> {
                    menuHolder[0].dispose(); listener.resetKeys();
                    if (mygame.GUI.PauseMenuFrame.isMuted) { toggleSound(); mygame.GUI.PauseMenuFrame.isMuted = false; }
                    showMainMenu();
                },
                e -> toggleSound()
        );
        menuHolder[0].setVisible(true);
    }

    public void toggleSound() {
        if (manager != null && manager.soundManager != null) {
            manager.soundManager.toggleMute();
        }
    }
}