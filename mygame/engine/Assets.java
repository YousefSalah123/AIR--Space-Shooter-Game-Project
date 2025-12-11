package mygame.engine;

import Texture.TextureReader;
import javax.media.opengl.GL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Assets {

    // هنا نحدد أسماء الصور التي سنستخدمها في الكود (مفتاح)
    // وأسماء الملفات الحقيقية في الفولدر (قيمة)
    private static final Map<String, String> textureFiles = new HashMap<>();
    private static final Map<String, TextureReader.Texture> textures = new HashMap<>();
    private static final Map<String, Integer> textureIds = new HashMap<>();

    // تعريف الثوابت (عشان ما نغلطش في الكتابة)
    public static final String BACKGROUND = "BackWar";
    public static final String HERO = "Hero";
    public static final String HERO_DIE_1 = "Hero2";
    public static final String HERO_DIE_2 = "Hero3";
    public static final String HERO_DIE_3 = "Hero4";
    public static final String ENEMY_BASIC = "enemy1";
    public static final String ENEMY_MIDDLE = "enemy3";
    public static final String BULLET = "bullet";
    public static final String HEART = "heart";
    public static final String COIN = "coin";
    public static final String LASER = "laser";
    public static final String BOSS1 = "boss1";
    // ... ضيف براحتك هنا

    // 1. تسجيل الملفات (نربط الاسم بالملف)
    static {
        textureFiles.put(BACKGROUND, "BackWar.png");
        textureFiles.put(HERO, "Hero.png");
        textureFiles.put(HERO_DIE_1, "Hero2.png");
        textureFiles.put(HERO_DIE_2, "Hero3.png");
        textureFiles.put(HERO_DIE_3, "Hero4.png");
        textureFiles.put(ENEMY_BASIC, "enemy1.png");
        textureFiles.put(ENEMY_MIDDLE, "enemy3.png");
        textureFiles.put(BULLET, "Bullet v6.png");
        textureFiles.put(HEART, "heart.png");
        textureFiles.put(COIN, "coin.png");
        textureFiles.put(LASER, "laser.png"); // لو موجودة
        textureFiles.put(BOSS1, "Boss1.png");
        // ضيف باقي الصور هنا...
    }

    // 2. دالة التحميل (تستدعى مرة واحدة في GameListener)
    public static void loadTextures(GL gl) {
        int[] ids = new int[textureFiles.size()];
        gl.glGenTextures(textureFiles.size(), ids, 0);

        int index = 0;
        for (String key : textureFiles.keySet()) {
            try {
                String fileName = textureFiles.get(key);
                TextureReader.Texture texture = TextureReader.readTexture("Assets//" + fileName, true);

                // حفظ الـ ID والـ Texture
                textures.put(key, texture);
                textureIds.put(key, ids[index]);

                // إعدادات OpenGL
                gl.glBindTexture(GL.GL_TEXTURE_2D, ids[index]);
                gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture.getPixels());
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

                System.out.println("Loaded: " + fileName + " -> ID: " + ids[index]);
                index++;
            } catch (IOException e) {
                System.err.println("Failed to load: " + textureFiles.get(key));
                e.printStackTrace();
            }
        }
    }

    // 3. دالة الاستدعاء (نستخدمها في الرسم)
    public static int getTexture(String key) {
        return textureIds.getOrDefault(key, -1); // لو مش موجودة ترجع -1
    }
}