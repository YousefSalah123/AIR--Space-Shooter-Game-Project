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

    // Initializes the SoundManager, setting up the sound effect map and loading all audio assets.
    public SoundManager() {
        soundEffects = new HashMap<>();

        // Adjusting paths to be Relative Paths
        // Path starts with / and means starting from the src folder

        // Load sounds with new names and new paths
        loadSound("Boss_laser", "/mygame/sounds/Boss_laser.wav");
//            loadSound("BulletsWave", "/mygame/sounds/BulletsWave.wav");
        loadSound("coin", "/mygame/sounds/coin.wav");
        loadSound("enemy_laser", "/mygame/sounds/enemy_laser.wav");
        loadSound("explosion", "/mygame/sounds/explosion.wav");
        loadSound("game_over", "/mygame/sounds/gameover.wav");
//            loadSound("laser", "/mygame/sounds/Laser1.wav"); // Super laser
        loadSound("LevelComplete", "/mygame/sounds/LevelComplete.wav");
        loadSound("Player_laser", "/mygame/sounds/Player_laser.wav");
        loadSound("powerup", "/mygame/sounds/powerup.wav");
        loadSound("shield", "/mygame/sounds/shield.wav");

        // Background music
        loadMusic("/mygame/sounds/music_loop.wav");
    }

    // Function to load a short sound effect using resources (Classpath).
    private void loadSound(String name, String path) {
        try {
            // Use getResource to access the file within the project, regardless of location
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

    // Function to load background music using resources (Classpath).
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

            // Volume reduction
            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-10.0f);

        } catch (Exception e) {
            System.err.println("Error loading music: " + path);
            e.printStackTrace();
        }
    }

    // --- Playback Functions ---

    // Plays a sound effect by its name. If the sound is already playing, it restarts it from the beginning.
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

    // Starts playing the background music in a continuous loop, if it is not muted.
    public void playMusic() {
        if (backgroundMusic != null && !isMuted) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
        }
    }

    // Stops the currently playing background music.
    public void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    // Toggles the mute state. Stops music if muted, or resumes/starts music if unmuted.
    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            stopMusic();
        } else {
            playMusic();
        }
    }
}