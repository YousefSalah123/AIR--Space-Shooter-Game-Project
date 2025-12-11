package com.mygame.objects;

import javax.media.opengl.GL;

public class Item extends GameObject {

    public enum ItemType {HEALTH, RAPID_FIRE, GOLD_COIN}
    private final ItemType type;

    public Item(float x, float y, ItemType type) {
        super(x, y, 30, 30);
        this.type = type;
        this.speed = 3.0f;
    }

    public ItemType getType() { return type; }

    @Override
    public void update() {
        y -= speed;
        if (y < -50) isAlive = false;
    }

    @Override
    public void render(GL gl, int[] textures) {
        // Texture Index 5 is Item ("6.png")
        // يمكن تغيير اللون لتمييز نوع العنصر
        switch (type) {
            case HEALTH: gl.glColor3f(1, 0.5f, 0.5f); break;
            case RAPID_FIRE: gl.glColor3f(0.5f, 1, 1); break;
            case GOLD_COIN: gl.glColor3f(1, 1, 0); break;
        }
        drawTexture(gl, textures[19], x, y, width, height);
        gl.glColor3f(1, 1, 1);
    }
}