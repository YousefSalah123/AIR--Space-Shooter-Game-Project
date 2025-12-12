package mygame.GUI;

import mygame.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel for the Pause button/ESC key, with a transparent design and colored border.
 */
public class PauseButtonPanel extends JPanel {

    private final Game game;
    private final JLabel pauseLabel;

    // ⭐ New Constant Colors:
    private static final Color TEXT_COLOR = new Color(0x32FFC8); // Bright Cyan/Neon Green
    private static final Color BORDER_COLOR = new Color(0x32FFC8); // Same color as text for the border
    private static final Color HOVER_COLOR = new Color(0xFF44AA); // Bright Yellow/Fuchsia on hover

    // Constructor: Initializes the panel, sets up the styled "ESC" label, and adds mouse listeners
    // to handle the pause functionality and hover effects.
    public PauseButtonPanel(Game game) {
        this.game = game;

        // --- 1. Set up the base panel (Full Transparency) ---
        setOpaque(false);
        setLayout(new BorderLayout());

        // --- 2. Set up the JLabel for the "ESC" text ---
        pauseLabel = new JLabel("ESC", SwingConstants.CENTER);
        pauseLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        pauseLabel.setForeground(TEXT_COLOR);
        pauseLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ⭐ Add a Border to mimic a button shape without a filled background
        // Use a compound border: empty padding + colored line border
        pauseLabel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 2), // Outer colored line border
                        BorderFactory.createEmptyBorder(6, 12, 6, 12)    // Inner padding
                )
        );

        // --- 3. Add MouseListener to trigger pause/unpause ---
        pauseLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (game != null) {
                    game.togglePause();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // ⭐ Hover effect for text color (Bright Yellow/Fuchsia)
                pauseLabel.setForeground(HOVER_COLOR);

                // ⭐ Change border color on hover
                pauseLabel.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(HOVER_COLOR, 2),
                                BorderFactory.createEmptyBorder(6, 12, 6, 12)
                        )
                );
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // ⭐ Restore original color (Cyan)
                pauseLabel.setForeground(TEXT_COLOR);

                // ⭐ Restore original border color
                pauseLabel.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                                BorderFactory.createEmptyBorder(6, 12, 6, 12)
                        )
                );
            }
        });

        this.add(pauseLabel, BorderLayout.CENTER);

        this.setPreferredSize(new Dimension(85, 35));
    }

    // Ensures transparency is maintained by calling the super method.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}