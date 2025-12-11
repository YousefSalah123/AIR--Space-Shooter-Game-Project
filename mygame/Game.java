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

    private static String playerName = "UNKNOWN";

    public static String getPlayerName() {
        return playerName;
    }

    public static void setPlayerName(String name) {
        Game.playerName = name;
    }

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

        // 1. إعداد GLCanvas
        glCanvas = new GLCanvas();
        glCanvas.addGLEventListener(listener);
        glCanvas.addKeyListener(listener);
        glCanvas.setFocusable(true);

        // 2. إعداد PauseButtonPanel
        pauseButtonPanel = new PauseButtonPanel(this);
        // ⭐ إخفاء الزر فوراً عند الإنشاء
        pauseButtonPanel.setVisible(false);

        // ⭐ تحديد الأبعاد والموقع المناسب لزر "ESC" (أعلى اليمين)
        // (x=700, y=10, width=85, height=35)
        pauseButtonPanel.setBounds(700, 10, 85, 35);

        // 3. إعداد JLayeredPane
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 600));

        // 4. إضافة المكونات إلى JLayeredPane
        // أ. إضافة GLCanvas (الطبقة السفلية)
        glCanvas.setBounds(0, 0, 800, 600);
        layeredPane.add(glCanvas, JLayeredPane.DEFAULT_LAYER);

        // ب. إضافة زر الإيقاف المؤقت (الطبقة العلوية)
        layeredPane.add(pauseButtonPanel, JLayeredPane.PALETTE_LAYER);


        // 5. إضافة layeredPane إلى JFrame
        this.add(layeredPane, BorderLayout.CENTER);


        animator = new FPSAnimator(glCanvas, 60);

        showMainMenu();
    }

    public void showMainMenu() {
        this.setVisible(false);

        if (animator.isAnimating()) animator.stop();

        // إخفاء الزر
        if (pauseButtonPanel != null) {
            pauseButtonPanel.setVisible(false);
        }

        mainMenu = new ArcadeGameUI(this);

        mainMenu.setStartGameAction(e -> startActualGame());

        mainMenu.setVisible(true);
    }

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
     * يتم استدعاؤها عند انتهاء الجولة
     */
    public void handleGameOver(boolean victory, int score) {

        if (animator.isAnimating()) animator.stop();

        this.setVisible(false);

        // إخفاء الزر
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

                    // --- لو الصوت مكتوم، شغله تاني قبل ما ترجع للقائمة ---
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

    // دالة لتبديل حالة الصوت (ON/OFF)
    public void toggleSound() {
        if (manager != null && manager.soundManager != null) {
            manager.soundManager.toggleMute();
        }
    }
}