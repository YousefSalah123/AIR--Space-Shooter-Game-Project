package com.mygame.objects;

import javax.media.opengl.GL;

public class Enemy extends GameObject {

    public enum TypesOfEnemies {
        STRAIGHT, CHASER, SQUAD_V, SQUAD_ENTER_LEFT, SQUAD_ENTER_RIGHT, CIRCLE_PATH
    }

    private TypesOfEnemies type;
    private Player playerTarget;
    private float startX, startY;
    private int timeAlive = 0;
    private int textureIndex;

    // --- NEW VARIABLES FOR HEALTH ---
    public int health = 100;
    public int maxHealth = 100;

    public Enemy(float x, float y, float size, TypesOfEnemies type, Player player, int textureIndex) {
        super(x, y, size, size);
        this.type = type;
        this.playerTarget = player;
        this.startX = x;
        this.startY = y;
        this.speed = 3.0f;
        this.textureIndex = textureIndex;

        // Initialize health
        this.health = 100;
        this.maxHealth = 100;
    }

    @Override
    public void update() {
        // ... (Keep existing update logic exactly as it is) ...
        timeAlive++;
        switch (type) {
            case STRAIGHT: y -= speed; break;
            case SQUAD_V: y -= speed; break;
            case CHASER:
                y -= speed;
                if (playerTarget != null) {
                    if (x < playerTarget.getX()) x += 2.0f;
                    if (x > playerTarget.getX()) x -= 2.0f;
                }
                break;
            case SQUAD_ENTER_LEFT:
                x += 3.0f;
                y = startY - (timeAlive * 2.5f) + (float)(Math.sin(timeAlive * 0.02) * 30);
                break;
            case SQUAD_ENTER_RIGHT:
                x -= 3.0f;
                y = startY - (timeAlive * 2.5f) + (float)(Math.sin(timeAlive * 0.02) * 30);
                break;
            case CIRCLE_PATH:
                y -= 2.0f;
                x = startX + (float)(Math.sin(timeAlive * 0.02) * 80);
                break;
        }

        if (y < -100 || x < -200 || x > 1000) setAlive(false);
    }

    // We will handle rendering in GameManager to ensure the bar is drawn ON TOP
    @Override
    public void render(GL gl, int[] textures) {
        drawTexture(gl, textures[textureIndex], x, y, width, height);
    }

    // Getters
    public int getTextureIndex() { return textureIndex; }
    public boolean readyToFire() { return Math.random() < 0.003; }
    public TypesOfEnemies getType() { return type; }
}