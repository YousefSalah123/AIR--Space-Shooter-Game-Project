package mygame2.GUI;

import javax.swing.*;
import java.awt.*;

public class GameHUD extends JPanel {

    private JLabel player1Score, player2Score, timerLabel;
    private JLabel p1Health, p2Health;
    private boolean isMultiplayer;

    public GameHUD(boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
        setLayout(null);
        setOpaque(false); // لازم تكون شفافة عشان تظهر فوق اللعبة

        // ============ PLAYER 1 ============
        player1Score = new JLabel("P1: 0000");
        player1Score.setForeground(Color.WHITE);
        player1Score.setFont(new Font("Monospaced", Font.BOLD, 18));
        player1Score.setBounds(20, 10, 150, 25);
        add(player1Score);

        p1Health = new JLabel("♥♥♥");
        p1Health.setForeground(Color.RED);
        p1Health.setFont(new Font("SansSerif", Font.BOLD, 24));
        p1Health.setBounds(20, 40, 100, 30);
        add(p1Health);

        // ============ LEVEL / TIMER ============
        timerLabel = new JLabel("LEVEL 1", SwingConstants.CENTER);
        timerLabel.setForeground(Color.YELLOW);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        timerLabel.setBounds(350, 10, 200, 40);
        add(timerLabel);

        // ============ PLAYER 2 ============
        if (isMultiplayer) {
            player2Score = new JLabel("P2: 0000");
            player2Score.setForeground(Color.WHITE);
            player2Score.setFont(new Font("Monospaced", Font.BOLD, 18));
            player2Score.setBounds(720, 10, 150, 25);
            add(player2Score);

            p2Health = new JLabel("♥♥♥");
            p2Health.setForeground(Color.RED);
            p2Health.setFont(new Font("SansSerif", Font.BOLD, 24));
            p2Health.setBounds(760, 40, 100, 30);
            add(p2Health);
        }
    }

    public void updateHUD(int p1Score, int p2Score, int level, int p1HealthVal, int p2HealthVal) {
        player1Score.setText("P1: " + p1Score);
        timerLabel.setText("LEVEL " + level);
        p1Health.setText(repeatString("♥", p1HealthVal));

        if (isMultiplayer && player2Score != null) {
            player2Score.setText("P2: " + p2Score);
            p2Health.setText(repeatString("♥", p2HealthVal));
        }
    }

    private String repeatString(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }
}