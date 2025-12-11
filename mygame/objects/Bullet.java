package com.mygame.objects;

import javax.media.opengl.GL;

public class Bullet extends GameObject {

    private final boolean isEnemyBullet;
    private float speedX = 0;

    public Bullet(float x, float y, boolean isEnemyBullet) {
        super(x, y, 10, 20); // Adjusted size for sprite
        this.isEnemyBullet = isEnemyBullet;
        this.speed = isEnemyBullet ? -7.0f : 15.0f;
    }

    public Bullet(float x, float y, float speedX, float speedY, boolean isEnemyBullet) {
        super(x, y, 15, 25);
        this.isEnemyBullet = isEnemyBullet;
        this.speed = speedY;
        this.speedX = speedX;
    }

    @Override
    public void update() {
        y += speed;
        x += speedX;
        if (y > 700 || y < -50 || x < -50 || x > 850) {
            setAlive(false);
        }
    }

    @Override
    public void render(GL gl, int[] textures) {
        // Texture Index 3 is Bullet ("4.png")
        // يمكن تغيير اللون لتمييز رصاص العدو
        if (isEnemyBullet) gl.glColor3f(1.0f, 0.5f, 0.5f); // Reddish tint
        else gl.glColor3f(1.0f, 1.0f, 1.0f); // Normal

        drawTexture(gl, textures[6], x, y, width, height);

        gl.glColor3f(1,1,1); // Reset color
    }

    public boolean isEnemyBullet() { return isEnemyBullet; }
}