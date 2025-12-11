package mygame.objects;

import javax.media.opengl.GL;

/**
 * Bullet class represents all projectiles in the game.
 * Supports player bullets, enemy bullets, and angled bullets for bosses.
 */
public class Bullet extends GameObject {

    private final boolean isEnemyBullet; // True if bullet belongs to enemy
    private float speedX = 0;            // Horizontal speed (for angled bullets)

    /**
     * Straight bullet constructor (used for regular player/enemy shots)
     */
    public Bullet(float x, float y, boolean isEnemyBullet) {
        super(x, y, 5, 15);
        this.isEnemyBullet = isEnemyBullet;
        this.speed = isEnemyBullet ? -7.0f : 15.0f; // Direction depends on owner
    }

    /**
     * Angled bullet constructor (used for bosses)
     */
    public Bullet(float x, float y, float speedX, float speedY, boolean isEnemyBullet) {
        super(x, y, 8, 20); // Larger bullet for bosses
        this.isEnemyBullet = isEnemyBullet;
        this.speed = speedY;
        this.speedX = speedX;
    }

    /**
     * Update bullet position each frame
     */
    @Override
    public void update() {
        y += speed;   // Vertical movement
        x += speedX;  // Horizontal movement

        // Remove bullet if off-screen
        if (y > 600 || y < -50 || x < 0 || x > 800) setAlive(false);
    }

    /**
     * Render bullet with color depending on owner
     */
    @Override
    public void render(GL gl) {
        if (isEnemyBullet) gl.glColor3f(1, 0, 0); // Red for enemies
        else gl.glColor3f(1, 1, 0);               // Yellow for player

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();
    }

    public boolean isEnemyBullet() {
        return isEnemyBullet;
    }
}