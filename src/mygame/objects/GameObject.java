package mygame.objects;

import javax.media.opengl.GL;
import java.awt.*;

// Abstract base class for all movable or visible objects in the game (ships, bullets, enemies, etc.).
// Provides common properties (position, size, speed, life status) and core rendering functionality.
public abstract class GameObject {

    protected float x, y;
    protected float speed;
    public float width, height;
    protected boolean isAlive = true;

    // Constructor: Initializes the object with position and dimensions.
    public GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Abstract method for updating the object's state (movement, behavior). Must be implemented by subclasses.
    public abstract void update();

    // Abstract method for rendering the object. Must be implemented by subclasses.
    // The texture array is passed for subclasses to select their specific texture.
    public abstract void render(GL gl, int[] textures);

    // Helper method to draw a textured quad using OpenGL (for convenience in subclasses).
    protected void drawTexture(GL gl, int textureId, float x, float y, float w, float h) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glColor3f(1, 1, 1);

        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(x, y + h);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(x + w, y + h);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(x + w, y);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(x, y);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
    }

    // Returns a Rectangle object representing the current bounds for collision detection.
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, (int) width, (int) height);
    }

    // Setter methods
    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setAlive(boolean alive) { isAlive = alive; }

    // Getter methods
    public boolean isAlive() { return isAlive; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getSpeed() { return speed; }
}