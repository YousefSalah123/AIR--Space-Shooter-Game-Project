package com.mygame.engine;

import com.mygame.objects.*;

import javax.media.opengl.GL;
import java.util.ArrayList;

public class GameManager {

    // قوائم الكائنات
    public Player player1;
    // public Player player2; // للملتي بلاير لاحقاً
    public ArrayList<Enemy> enemies = new ArrayList<>();
    public ArrayList<Bullet> bullets = new ArrayList<>();
    public ArrayList<GoldCoin> coins = new ArrayList<>();

    public GameManager() {
        // إنشاء اللاعب في منتصف الشاشة (بافتراض الشاشة 800x600)
        player1 = new Player(400, 50, "1");
    }

    public void update() {
        // 1. تحديث اللاعب
        player1.update();

        // 2. تحديث الرصاص
        for(int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update();
            if(!b.isAlive()) bullets.remove(i--);
        }

        // 3. تحديث الأعداء
        for(int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            e.update();
            if(!e.isAlive()) enemies.remove(i--);
        }

        // 4. TODO (Dev C): Random Spawning Logic (أضف أعداء جدد عشوائياً)

        // 5. TODO (Dev A): Check Collisions (افحص التصادمات)
        checkCollisions();
    }

    public void render(GL gl) {
        player1.render(gl);

        for(Bullet b : bullets) b.render(gl);
        for(Enemy e : enemies) e.render(gl);
        for(GoldCoin c : coins) c.render(gl);
    }

    private void checkCollisions() {
        // TODO (Dev A): منطق التصادم
        // Loop bullets vs enemies
        // Loop enemies vs player
    }

    // دالة لإطلاق النار (يستدعيها الـ Listener)
    public void playerShoot() {
        bullets.add(new Bullet(player1.getX() + 22, player1.getY() + 50));
    }
}