package mygame.engine;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private Map<String, Clip> soundEffects;
    private Clip backgroundMusic;
    private boolean isMuted = false;

    public SoundManager() {
        soundEffects = new HashMap<>();

        // تعديل المسارات لتكون نسبية (Relative Paths)
        // المسار يبدأ بـ / ويعني البدء من مجلد الـ src

            // تحميل الأصوات بالأسماء الجديدة والمسارات الجديدة
            loadSound("Boss_laser", "/mygame/sounds/Boss_laser.wav");
//            loadSound("BulletsWave", "/mygame/sounds/BulletsWave.wav");
            loadSound("coin", "/mygame/sounds/coin.wav");
            loadSound("enemy_laser", "/mygame/sounds/enemy_laser.wav");
            loadSound("explosion", "/mygame/sounds/explosion.wav");
            loadSound("game_over", "/mygame/sounds/gameover.wav");
//            loadSound("laser", "/mygame/sounds/Laser1.wav"); // الليزر الخارق
            loadSound("LevelComplete", "/mygame/sounds/LevelComplete.wav");
            loadSound("Player_laser", "/mygame/sounds/Player_laser.wav");
            loadSound("powerup", "/mygame/sounds/powerup.wav");
            loadSound("shield", "/mygame/sounds/shield.wav");

            // الموسيقى الخلفية
            loadMusic("/mygame/sounds/music_loop.wav");
        }

    // دالة لتحميل مؤثر صوتي قصير باستخدام الموارد (Resources)
    private void loadSound(String name, String path) {
        try {
            // استخدام getResource للوصول للملف داخل المشروع أينما كان
            URL soundURL = getClass().getResource(path);

            if (soundURL == null) {
                System.err.println("Sound file not found: " + path);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            soundEffects.put(name, clip);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound: " + path);
            e.printStackTrace();
        }
    }

    // دالة لتحميل الموسيقى باستخدام الموارد
    private void loadMusic(String path) {
        try {
            URL musicURL = getClass().getResource(path);

            if (musicURL == null) {
                System.err.println("Music file not found: " + path);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(musicURL);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);

            // تقليل الصوت
            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-10.0f);

        } catch (Exception e) {
            System.err.println("Error loading music: " + path);
            e.printStackTrace();
        }
    }

    // --- دوال التشغيل (كما هي) ---

    public void playSound(String name) {
        if (isMuted) return;

        Clip clip = soundEffects.get(name);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void playMusic() {
        if (backgroundMusic != null && !isMuted) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
        }
    }

    public void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            stopMusic();
        } else {
            playMusic();
        }
    }
}