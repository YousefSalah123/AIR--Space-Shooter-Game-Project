package mygame.objects;

import javax.media.opengl.GL;

// Represents a collectible item in the game that grants the player a benefit.
public class Item extends GameObject {

    // Enum defining the different types of collectible items.
    public enum ItemType {HEALTH, RAPID_FIRE, GOLD_COIN}

    private final ItemType type;

    // Constructor: Initializes the item with position and its type.
    public Item(float x, float y, ItemType type) {
        // Adjusted size to 90x50 to prevent image stretching (as per original code logic)
        super(x, y, 90, 50);
        this.type = type;
        this.speed = 3.0f;
    }

    // Returns the type of the item.
    public ItemType getType() {
        return type;
    }

    // Updates the item's position (moving downwards) and checks if it has left the screen.
    @Override
    public void update() {
        y -= speed;
        // Disappear when out of screen
        if (y < -50) isAlive = false;
    }

    // Renders the item by selecting the appropriate texture based on its type.
    @Override
    public void render(GL gl, int[] textures) {
        int textureIndex;

        // Choose texture based on item type
        switch (type) {
            case HEALTH:
                // Index 19 is heart.png
                textureIndex = textures[19];
                break;

            case GOLD_COIN:
                // Index 24 is coin.png
                textureIndex = textures[24];
                break;

            case RAPID_FIRE:
                // --- Modification here ---
                // Index 40 is PowerUp.png
                textureIndex = textures[40];
                break;

            default:
                textureIndex = textures[24];
                break;
        }

        // Draw the item with white color (to display the original image colors)
        gl.glColor3f(1, 1, 1);
        drawTexture(gl, textureIndex, x, y, width, height);
    }
}