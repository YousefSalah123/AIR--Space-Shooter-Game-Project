package mygame;

import com.sun.opengl.util.FPSAnimator;
import mygame.GUI.ArcadeGameUI;
import mygame.GUI.EndLevelFrame;
import mygame.GUI.GameHUD;
import mygame.GUI.PauseMenuFrame;
import mygame.engine.GameListener;
import mygame.engine.GameManager;
import mygame.engine.HighScoreManagment;
import mygame.objects.Player;

import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;


public class Game extends JFrame {

    private GLCanvas glCanvas;
    private GameListener listener;
    private FPSAnimator animator;
    private ArcadeGameUI mainMenu;
    private GameManager manager;

    // ⭐ سيتم تخزين اسم اللاعب هنا
    private static String playerName = "UNKNOWN";

    public static String getPlayerName() {
        return playerName;
    }

    public static void setPlayerName(String name) {
        Game.playerName = name;  // ← ← ← التصحيح هنا
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
        setLayout(new BorderLayout());

        manager = new GameManager(this);
        listener = new GameListener(manager);

        glCanvas = new GLCanvas();
        glCanvas.addGLEventListener(listener);
        glCanvas.addKeyListener(listener);
        glCanvas.setFocusable(true);
        add(glCanvas, BorderLayout.CENTER);

        animator = new FPSAnimator(glCanvas, 60);

        showMainMenu();

    }

    public void showMainMenu() {
        this.setVisible(false);

        if (animator.isAnimating()) animator.stop();

        mainMenu = new ArcadeGameUI();

        // ⭐ عند البدء.. (ArcadeGameUI سوف تستدعي Game.setPlayerName)
        mainMenu.setStartGameAction(e -> startActualGame());

        mainMenu.setVisible(true);
    }

    public void startActualGame() {
        if (mainMenu != null) mainMenu.setVisible(false);

        manager.resetGame();

        this.setVisible(true);

        glCanvas.requestFocusInWindow();

        if (!animator.isAnimating()) animator.start();
    }

    /**
     * يتم استدعاؤها عند انتهاء الجولة
     */
    public void handleGameOver(boolean victory, int score) {

        if (animator.isAnimating()) animator.stop();

        this.setVisible(false);

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

        // استخدام مصفوفة للتحايل على الـ Final variable
        // تأكد من عمل Import صحيح لـ PauseMenuFrame حسب الباكيج عندك
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

                    // لو عدت اللعبة والصوت مكتوم، ممكن تسيبه مكتوم أو تشغله حسب رغبتك
                    // هنا سيبه زي ما هو

                    startActualGame();
                },
                // 3. Back to Menu (هنا الإصلاح)
                e -> {
                    menuHolder[0].dispose();
                    listener.resetKeys();

                    // --- لو الصوت مكتوم، شغله تاني قبل ما ترجع للقائمة ---
                    if (mygame.GUI.PauseMenuFrame.isMuted) {
                        toggleSound(); // اعكس الحالة في SoundManager
                        mygame.GUI.PauseMenuFrame.isMuted = false; // اعكس الأيقونة في الـ GUI
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
        // نستدعي الدالة الموجودة في SoundManager عبر الـ manager
        if (manager != null && manager.soundManager != null) {
            manager.soundManager.toggleMute();
        }
    }
}