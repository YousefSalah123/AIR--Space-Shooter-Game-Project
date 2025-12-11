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

    public Enemy(float x, float y, float size, TypesOfEnemies type, Player player) {
        super(x, y, size, size);
        this.type = type;
        this.playerTarget = player;
        this.startX = x;
        this.startY = y;
        this.speed = 3.0f;
    }

    @Override
    public void update() {
        timeAlive++;
        switch (type) {
            case STRAIGHT:
            case SQUAD_V:
                y -= speed;
                break;
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

    @Override
    public void render(GL gl, int[] textures) {
        // Texture Index 2 is Enemy ("3.png")
        drawTexture(gl, textures[5], x, y, width, height);
    }

    public boolean readyToFire() { return Math.random() < 0.003; }
    public TypesOfEnemies getType() { return type; }
}