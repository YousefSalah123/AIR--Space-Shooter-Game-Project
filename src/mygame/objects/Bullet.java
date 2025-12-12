package mygame.objects;

import javax.media.opengl.GL;

// Represents a Bullet object (either fired by the player or an enemy),
// handling its movement, dimensions, and dynamic rotation during rendering.
public class Bullet extends GameObject {

    private final boolean isEnemyBullet;
    private float speedX = 0;
    private int textureIndex;

    // Constructor: Initializes the bullet's position, speed vectors, type, and texture index.
    // Dimensions are set based on whether it is an enemy bullet or not.
    public Bullet(float x, float y, float speedX, float speedY, boolean isEnemyBullet, int textureIndex) {
        super(x, y,
                isEnemyBullet ? 15 : 30,  // Width
                isEnemyBullet ? 25 : 50   // Height
        );

        this.speedX = speedX;
        this.speed = speedY; // Here speed represents speedY
        this.isEnemyBullet = isEnemyBullet;
        this.textureIndex = textureIndex;
    }

    // Updates the bullet's position based on its speed vectors.
    @Override
    public void update() {
        y += speed;
        x += speedX;

        // Check if the bullet is out of screen bounds (with a small margin)
        if (y > 700 || y < -50 || x < -50 || x > 850) {
            setAlive(false);
        }
    }

    // Renders the bullet using its assigned texture, dynamically rotating it
    // to align with its current direction of movement (speedX and speedY).
    @Override
    public void render(GL gl, int[] textures) {
        // Guard: Do not draw if the bullet is dead
        if (!isAlive) return;

        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[textureIndex]);

        gl.glPushMatrix();

        // 1. Translate the drawing point to the center of the bullet (Pivot Point)
        // This is crucial for the bullet to rotate around its center
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        gl.glTranslated(centerX, centerY, 0);

        // 2. Dynamically calculate the rotation angle
        // Math.atan2(y, x) calculates the angle in radians based on the movement vector
        // We use speed (which represents Y) and speedX
        double angleRad = Math.atan2(speed, speedX);
        double angleDeg = Math.toDegrees(angleRad);

        // 3. Apply the rotation
        // We subtract 90 degrees because the 0 angle in math points right,
        // while bullet images are typically drawn pointing up.
        // (If your images are inverted, try +90 instead of -90)
        gl.glRotated(angleDeg - 90, 0, 0, 1);

        // 4. Draw the bullet
        // Since we translated to the center, we draw the quad around the new (0,0) point
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2d(-width / 2, -height / 2);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2d(width / 2, -height / 2);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2d(width / 2, height / 2);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2d(-width / 2, height / 2);
        gl.glEnd();

        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    // Returns true if the bullet was fired by an enemy.
    public boolean isEnemyBullet() { return isEnemyBullet; }
}