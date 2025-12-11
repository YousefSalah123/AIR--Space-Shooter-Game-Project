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

    private static File getScoreFile() {
        // المسار المطلق لدليل العمل (CWD) الحالي المعين بواسطة بيئة التشغيل
        String userDir = System.getProperty("user.dir");

        // إنشاء كائن الملف المشترك (CWD + اسم الملف)
        File scoreFile = new File(userDir, HIGH_SCORES_FILE);
        return scoreFile;
    }

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

    private static void saveScores(List<ScoreEntry> scores) {
        // 🛑 استخدام getScoreFile()
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

    public static void addScore(String name, int score) {
        List<ScoreEntry> scores = loadScores();

        // تنظيف الاسم وإزالة الفواصل التي قد تكسر تنسيق الملف
        String safeName = name.trim().replace(",", "").toUpperCase();
        if (safeName.isEmpty()) safeName = "UNKNOWN PILOT";

        scores.add(new ScoreEntry(safeName, score));

        scores.sort(ScoreEntry.SCORE_COMPARATOR);

        // تقليم القائمة إلى أعلى 5 نتائج
        while (scores.size() > MAX_SCORES) {
            scores.remove(scores.size() - 1);
        }

        saveScores(scores);
    }

    public static class ScoreEntry {
        public final String name;
        public final int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        // Comparator لفرز النتائج (الأعلى أولاً)
        public static final Comparator<ScoreEntry> SCORE_COMPARATOR =
                Comparator.comparingInt((ScoreEntry entry) -> entry.score).reversed();
    }
}