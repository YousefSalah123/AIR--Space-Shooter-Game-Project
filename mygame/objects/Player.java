package com.mygame.objects;


import javax.media.opengl.GL;

public class Player extends GameObject {

    // خصائص خاصة باللاعب
    private int lives = 3;
    private int score = 0;
    private String  playerID; // 1 for Arrows, 2 for WASD

    public Player(float x, float y, String playerID) {
        super(x, y, 50, 50); // حجم افتراضي 50x50
        this.playerID = playerID;
        this.speed = 5.0f;
    }

    @Override
    public void update() {

    }

    @Override
    public void render(GL gl) {
        // TODO (Dev B): ارسم الطائرة هنا (مؤقتاً مربع ملون)
        // ولاحقاً استبدلها بـ Texture
        gl.glColor3f(0, 0, 1); // أزرق
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + width, y);
        gl.glVertex2f(x + width, y + height);
        gl.glVertex2f(x, y + height);
        gl.glEnd();
    }

    // دوال للتحكم (يستدعيها الـ Listener)
    public void handleInput(boolean[] keys) {
        // TODO (Dev B): افحص المصفوفة وحرك اللاعب
    }
}