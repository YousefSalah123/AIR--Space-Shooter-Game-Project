package mygame.objects;

import javax.media.opengl.GL;
import java.awt.*;

/**
 * Abstract base class for all game objects (player, enemies, bullets, items, bosses).
 * Provides position, size, speed, alive state, and basic collision logic.
 */
public abstract class GameObject {

    // --- Common attributes ---
    protected float x, y;       // Position (top-left corner)
    protected float speed;      // Movement speed
    public float width, height; // Dimensions for rendering and collision
    protected boolean isAlive = true; // Alive state; if false, remove from game

    /**
     * Constructor initializes position and size
     */
    public GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // --- Abstract methods ---

    /**
     * Update object logic every frame (movement, AI, etc.)
     */
    public abstract void update();

    /**
     * Render the object using OpenGL
     */
    public abstract void render(GL gl);

    // --- Collision detection ---

    /**
     * Returns bounding rectangle used for collision detection
     */
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, (int) width, (int) height);
    }

    // --- Getters and setters ---
    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getSpeed() {
        return speed;
    }
}