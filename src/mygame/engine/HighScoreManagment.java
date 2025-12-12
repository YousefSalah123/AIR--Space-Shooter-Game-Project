package mygame.engine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class HighScoreManagment {
    private static final String HIGH_SCORES_FILE = "highScores.txt";
    public static final int MAX_SCORES = 5;

    // Returns the File object representing the high scores file, located in the application's current working directory.
    private static File getScoreFile() {
        // The absolute path to the current Working Directory (CWD) set by the runtime environment
        String userDir = System.getProperty("user.dir");

        // Create the shared File object (CWD + filename)
        File scoreFile = new File(userDir, HIGH_SCORES_FILE);
        return scoreFile;
    }

    // Loads the high scores from the file, parsing each line into a ScoreEntry.
    // Scores are sorted in descending order after loading.
    public static List<ScoreEntry> loadScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        File scoreFile = getScoreFile();

        System.out.println("DEBUG: Looking for highscores file at: " + scoreFile.getAbsolutePath());

        try (Scanner scanner = new Scanner(scoreFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    try {
                        int score = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        scores.add(new ScoreEntry(name, score));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed score line: " + line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("High scores file not found. Starting with empty list.");
        }

        scores.sort(ScoreEntry.SCORE_COMPARATOR);
        return scores;
    }

    // Saves the provided list of scores back to the high scores file.
    // Each entry is written as "score,name" on a new line.
    private static void saveScores(List<ScoreEntry> scores) {
        // 🛑 Use getScoreFile()
        File scoreFile = getScoreFile();

        System.out.println("DEBUG: Saving highscores file at: " + scoreFile.getAbsolutePath());

        try (PrintWriter writer = new PrintWriter(scoreFile)) {
            for (ScoreEntry score : scores) {
                writer.println(score.score + "," + score.name);
            }
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }

    // Adds a new score entry to the list of high scores.
    // It loads the current scores, adds the new one, sorts the list, trims it to MAX_SCORES, and saves it.
    public static void addScore(String name, int score) {
        List<ScoreEntry> scores = loadScores();

        // Clean the name and remove commas which could break the file format
        String safeName = name.trim().replace(",", "").toUpperCase();
        if (safeName.isEmpty()) safeName = "UNKNOWN PILOT";

        scores.add(new ScoreEntry(safeName, score));

        scores.sort(ScoreEntry.SCORE_COMPARATOR);

        // Trim the list to the top 5 scores
        while (scores.size() > MAX_SCORES) {
            scores.remove(scores.size() - 1);
        }

        saveScores(scores);
    }

    // Inner class representing a single high score entry (player name and score).
    public static class ScoreEntry {
        public final String name;
        public final int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        // Comparator for sorting scores (Highest first)
        public static final Comparator<ScoreEntry> SCORE_COMPARATOR =
                Comparator.comparingInt((ScoreEntry entry) -> entry.score).reversed();
    }
}