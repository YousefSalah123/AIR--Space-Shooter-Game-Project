package mygame.objects;
import javax.media.opengl.GL;

/**
 * Enemy class represents all types of enemy objects.
 * Supports straight, wavy, and chaser behaviors.
 */
public class Enemy extends GameObject {

    public enum TypesOfEnemies {STRAIGHT, WAVY, CHASER}

    private TypesOfEnemies type;   // Enemy behavior type
    private Player playerTarget;   // Target player for chaser behavior

    // --- Wavy movement variables ---
    private float startX; // Original horizontal position (wave center)
    private float angle = 0; // Angle for sine wave calculation

    /**
     * Constructor initializes position, size, type, and target player
     */
    public Enemy(float x, float y, float size, TypesOfEnemies type, Player player) {
        super(x, y, size, size);
        this.type = type;
        this.playerTarget = player;
        this.startX = x;

        // Set speed based on enemy type
        switch (type) {
            case CHASER:
                this.speed = 4.0f;
                break;
            case WAVY:
                this.speed = 3.0f;
                break;
            case STRAIGHT:
                this.speed = 2.0f;
                break;
        }
    }

    /**
     * Update enemy position based on type
     * STRAIGHT: moves downward
     * CHASER: moves downward + horizontally toward player
     * WAVY: moves downward + oscillates horizontally
     */
    @Override
    public void update() {
        switch (type) {
            case STRAIGHT:
                y -= speed;
                break;
            case CHASER:
                y -= speed;
                if (playerTarget != null) {
                    if (x < playerTarget.getX()) x += 1.5f;
                    if (x > playerTarget.getX()) x -= 1.5f;
                }
                break;
            case WAVY:
                y -= speed;
                angle += 0.05f;
                x = startX + (float) (Math.sin(angle) * 80);
                break;
        }

        // Remove enemy if it goes off-screen
        if (y < -50) setAlive(false);
    }

    /**
     * Render enemy on screen using different colors per type
     */
    @Override
    public void render(GL gl) {
        switch (type) {
            case STRAIGHT:
                gl.glColor3f(1.0f, 0.0f, 0.0f);
                break;       // Red
            case CHASER:
                gl.glColor3f(1.0f, 0.5f, 0.0f);
                break;         // Orange
            case WAVY:
                gl.glColor3f(1.0f, 0.0f, 1.0f);
                break;           // Magenta
        }

        // Draw enemy body
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();

        // Draw simple eyes for enemy
        gl.glColor3f(0, 0, 0);
        gl.glBegin(GL.GL_QUADS);
        // Left eye
        gl.glVertex2f(x + 10, y + 10);
        gl.glVertex2f(x + 15, y + 10);
        gl.glVertex2f(x + 15, y + 20);
        gl.glVertex2f(x + 10, y + 20);
        // Right eye
        gl.glVertex2f(x + width - 15, y + 10);
        gl.glVertex2f(x + width - 10, y + 10);
        gl.glVertex2f(x + width - 10, y + 20);
        gl.glVertex2f(x + width - 15, y + 20);
        gl.glEnd();
    }

    /**
     * Small chance to fire a bullet (used by GameManager)
     */
    public boolean readyToFire() {
        return Math.random() < 0.005;
    }

    public TypesOfEnemies getType() {
        return type;
    }
}