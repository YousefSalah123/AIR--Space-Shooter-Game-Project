package com.mygame.objects;

import javax.media.opengl.GL;
import java.awt.Rectangle;

// Abstract class to enforce OOP principles
public abstract class GameObject {
    // 1. Common Attributes
    protected float x, y;       // Position
    protected float speed;      // Movement speed
    public float width;
    protected float height; // For collision & rendering
    protected boolean isAlive = true; // If false, remove from game

    public GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // 2. Abstract Methods (Child classes MUST implement these)
    public abstract void update(); // Logic (Movement, AI)
    public abstract void render(GL gl); // Drawing (JOGL code)

    // 3. Collision Logic (Bounding Box)
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, (int)width, (int)height);
    }

    // 4. Getters & Setters
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }
    public float getX() { return x; }
    public float getY() { return y; }
}