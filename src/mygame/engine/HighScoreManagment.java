package mygame.engine;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManagment {

    private static final String SINGLE_FILE = "highscores_single.dat";
    private static final String MULTI_FILE = "highscores_multi.dat";
    public static final int MAX_SCORES = 5;

    public static class ScoreEntry implements Serializable, Comparable<ScoreEntry> {
        public String name;
        public int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public int compareTo(ScoreEntry o) {
            return o.score - this.score; // ترتيب تنازلي
        }
    }

    public static void addScore(String name, int score, boolean isMultiplayer) {
        String filename = isMultiplayer ? MULTI_FILE : SINGLE_FILE;
        List<ScoreEntry> scores = loadScores(isMultiplayer);

        scores.add(new ScoreEntry(name, score));
        Collections.sort(scores);

        if (scores.size() > MAX_SCORES) {
            scores.subList(MAX_SCORES, scores.size()).clear();
        }

        saveScoresToFile(scores, filename);
    }

    public static List<ScoreEntry> loadScores(boolean isMultiplayer) {
        String filename = isMultiplayer ? MULTI_FILE : SINGLE_FILE;
        ArrayList<ScoreEntry> scores = new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            scores = (ArrayList<ScoreEntry>) ois.readObject();
        } catch (FileNotFoundException e) {

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return scores;
    }

    private static void saveScoresToFile(List<ScoreEntry> scores, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}