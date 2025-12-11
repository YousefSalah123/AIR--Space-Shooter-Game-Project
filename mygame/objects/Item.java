package mygame.objects;

import javax.media.opengl.GL;

/**
 * Item class represents collectible objects in the game:
 * - Health packs
 * - Rapid fire power-ups
 * - Coins
 */
public class Item extends GameObject {

    public enum ItemType {HEALTH, RAPID_FIRE, GOLD_COIN}

    private final ItemType type; // Type of item

    /**
     * Constructor sets position, type, size, and falling speed
     */
    public Item(float x, float y, ItemType type) {
        super(x, y, 30, 30); // Standard size
        this.type = type;
        this.speed = 3.0f;    // Falling speed
    }

    public ItemType getType() {
        return type;
    }

    /**
     * Update item position (falling)
     */
    @Override
    public void update() {
        y -= speed;
        if (y < -50) isAlive = false; // Remove if off-screen
    }

    /**
     * Render item with color depending on type
     */
    @Override
    public void render(GL gl) {
        switch (type) {
            case HEALTH:
                gl.glColor3f(1, 0, 0);
                break;          // Red heart
            case RAPID_FIRE:
                gl.glColor3f(0, 1, 1);
                break;      // Cyan power-up
            case GOLD_COIN:
                gl.glColor3f(1, 0.84f, 0);
                break;   // Gold coin
        }

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();
    }
}