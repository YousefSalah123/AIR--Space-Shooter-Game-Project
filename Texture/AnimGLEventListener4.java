package Texture;

import Texture.*;
import java.awt.event.*;
import java.io.IOException;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Random;

public class AnimGLEventListener4 extends AnimListener {

    // أبعاد اللعبة المنطقية
    int maxWidth = 100;
    int maxHeight = 100;
    private static boolean isUp = false;
    private static boolean isDown = false;
    private static boolean isLeft = false;
    private static boolean isRight = true;
    private static boolean isMovingRight = true;
    int direction=1;
    // مكان السمكة
    int x = maxWidth / 2, y = maxHeight / 2;

    // سرعة السمكة الأوتوماتيكية (يمين/شمال)
    int fishSpeedX = 2;

    // متغيرات الأنيميشن والحجم
    int animationIndex = 0;
    private int frameCounter = 0;
    private float fishScale = 1.0f;
    private final float FISH_SCALE_STEP = 0.15f;
    private final float FISH_SCALE_MAX = 3.0f;
    private final float FISH_SCALE_MIN = 0.4f;

    // أسماء الصور (ضفت الأحمر والأزرق عشان اللعبة تشتغل)
    // 0-3: Fish, 4: Red, 5: Blue, 6: Background
    String textureNames[] = {
            "fish.png", "fish2.png", "fish3.png", "fish4.png",
            "red.png", "blue.png",
            "Bjpg.png"
    };
    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];

    // كلاس العناصر اللي بتقع
    class FallingItem {
        float x, y, speed, scale;
        boolean isBlue;
        int texIndex;

        FallingItem(float x, float y, float speed, boolean isBlue, int texIndex, float scale) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.isBlue = isBlue;
            this.texIndex = texIndex;
            this.scale = scale;
        }

        void update() {
            this.y -= this.speed; // تنزل لتحت
        }
    }

    ArrayList<FallingItem> items = new ArrayList<>();
    Random rnd = new Random();
    int spawnCounter = 0;

    public void init(GLAutoDrawable gld) {

        GL gl = gld.getGL();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glGenTextures(textureNames.length, textures, 0);

        for (int i = 0; i < textureNames.length; i++) {
            try {
                texture[i] = TextureReader.readTexture(assetsFolderName + "//" + textureNames[i], true);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);

                new GLU().gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D,
                        GL.GL_RGBA,
                        texture[i].getWidth(), texture[i].getHeight(),
                        GL.GL_RGBA,
                        GL.GL_UNSIGNED_BYTE,
                        texture[i].getPixels()
                );
            } catch (IOException e) {
                System.out.println("Error loading texture: " + textureNames[i]);
                e.printStackTrace();
            }
        }


        items.clear();
    }

    public void display(GLAutoDrawable gld) {

        GL gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        // 1. رسم الخلفية (رقم 6 في المصفوفة)
        DrawBackground(gl);

        // 2. تحديث حركة السمكة الأوتوماتيكية (يمين وشمال)
//        updateFishAnimation();

        // 3. التحكم اليدوي (فوق وتحت بس)
        handleKeyPress();

        // 4. إنشاء عناصر جديدة تقع
        spawnCounter++;
        if (spawnCounter % 30 == 0) {
            spawnNewItem();
        }

        // 5. تحديث ورسم العناصر الساقطة وفحص التصادم
        updateItems(gl);

        // 6. تبديل فريمات السمكة (عشان تحرك ديلها)
        frameCounter++;
        if (frameCounter % 8 == 0) {
            animationIndex = (animationIndex + 1) % 4;
            frameCounter = 0;
        }

        // 7. رسم السمكة
        DrawSprite(gl, x, y, animationIndex, fishScale);
    }

    // دالة حركة السمكة الأوتوماتيكية
//    private void updateFishAnimation() {
//        x += fishSpeedX;
//
//        // خبطت يمين
//        if (x > maxWidth - 10) {
//            x = maxWidth - 10;
//            fishSpeedX = -Math.abs(fishSpeedX); // اعكس لليسار
//        }
//        // خبطت شمال
//        if (x < 0) {
//            x = 0;
//            fishSpeedX = Math.abs(fishSpeedX); // اعكس لليمين
//        }
//    }

    // دالة إنشاء عنصر جديد
    private void spawnNewItem() {
        float sx = 5 + rnd.nextFloat() * (maxWidth - 10);
        float sy = maxHeight + 10f; // تبدأ من فوق الشاشة
        float speed = 0.5f + rnd.nextFloat() * 1.0f;
        boolean blue = rnd.nextFloat() < 0.5f;
        int texIdx = blue ? 5 : 4; // 5=Blue, 4=Red
        float scale = 0.6f + rnd.nextFloat() * 0.4f;
        items.add(new FallingItem(sx, sy, speed, blue, texIdx, scale));
    }

    // دالة تحديث العناصر والتصادم
    private void updateItems(GL gl) {
        Iterator<FallingItem> it = items.iterator();
        while (it.hasNext()) {
            FallingItem f = it.next();
            f.update(); // حرك العنصر لتحت

            // ارسم العنصر
            DrawSprite(gl, (int) f.x, (int) f.y, f.texIndex, f.scale);

            // فحص التصادم مع السمكة
            if (collidesWithFish(f)) {
                if (f.isBlue) {
                    fishScale += FISH_SCALE_STEP;
                    if (fishScale > FISH_SCALE_MAX) fishScale = FISH_SCALE_MAX;
                } else {
                    fishScale -= FISH_SCALE_STEP;
                    if (fishScale < FISH_SCALE_MIN) fishScale = FISH_SCALE_MIN;
                }
                it.remove(); // امسح العنصر بعد الأكل
                continue;
            }

            // امسح العنصر لو نزل تحت الشاشة
            if (f.y < -10) {
                it.remove();
            }
        }
    }

    // منطق التصادم
    private boolean collidesWithFish(FallingItem f) {
        // حساب أبعاد السمكة والعنصر
        float fishHalfW = 0.1f * fishScale * maxWidth / 2.0f;
        float fishHalfH = 0.1f * fishScale * maxHeight / 2.0f;

        float itemHalfW = 0.1f * f.scale * maxWidth / 2.0f;
        float itemHalfH = 0.1f * f.scale * maxHeight / 2.0f;

        float dx = Math.abs(x - f.x);
        float dy = Math.abs(y - f.y);

        return dx <= (fishHalfW + itemHalfW) && dy <= (fishHalfH + itemHalfH);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public void DrawSprite(GL gl, int x, int y, int index, float scale) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);

        gl.glPushMatrix();
        gl.glTranslated(x / (maxWidth / 2.0) - 0.9, y / (maxHeight / 2.0) - 0.9, 0);
        gl.glScaled(0.1 * scale*direction, 0.1 * scale, 1);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
    }

    public void DrawBackground(GL gl) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[texture.length-1]); // الخلفية رقم 6

        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
    }

//    public void handleKeyPress() {
//        // التحكم يدوي فقط في Y (فوق وتحت)
//        if (isKeyPressed(KeyEvent.VK_DOWN)) {
//            if (y > 0) {
//                y--;
//            }
//        }
//        if (isKeyPressed(KeyEvent.VK_UP)) {
//            if (y < maxHeight - 10) {
//                y++;
//            }
//        }
//
//        if (isKeyPressed(KeyEvent.VK_LEFT)) {
//            isLeft = true;
//            isRight = false;
//            if (x > 0) {
//                x--;
//            }
//        }
//        if(isLeft){
//            x--;
//        }
//
//        if (isKeyPressed(KeyEvent.VK_RIGHT)) {
//            isRight = true;
//            isLeft = false;
//            if (x < maxWidth - 10) {
//                x++;
//            }
//        }
//        if (isRight) {
//            x++;
//        }
//
//    }
//    public void handleKeyPress() {
//
//        // ------------------------------------------------
//        // 1. التحكم الرأسي (يدوي: لازم تفضل دايس)
//        // ------------------------------------------------
//        if (isKeyPressed(KeyEvent.VK_DOWN)) {
//            if (y > 0) {
//                y--;
//            }
//        }
//        if (isKeyPressed(KeyEvent.VK_UP)) {
//            if (y < maxHeight - 10) {
//                y++;
//            }
//        }
//
//        // ------------------------------------------------
//        // 2. التحكم الأفقي (تغيير الاتجاه فقط)
//        // ------------------------------------------------
//
//        // لو داس يمين -> خلي الاتجاه يمين
//        if (isKeyPressed(KeyEvent.VK_RIGHT)) {
//            isMovingRight = true;
//        }
//
//        // لو داس شمال -> خلي الاتجاه شمال
//        if (isKeyPressed(KeyEvent.VK_LEFT)) {
//            isMovingRight = false;
//        }
//
//        // ------------------------------------------------
//        // 3. تطبيق الحركة الأفقية (مستمرة بناءً على الاتجاه)
//        // ------------------------------------------------
//
//        if (isMovingRight) {
//            // ماشي يمين (نتأكد إنه مخرجش بره الحدود)
//            if (x < maxWidth - 10) {
//                x++;
//            }
//        } else {
//            // ماشي شمال (نتأكد إنه مخرجش بره الحدود)
//            if (x > 0) {
//                x--;
//            }
//        }
//    }

/*public void handleKeyPress() {
//
//    // ------------------------------------------------
//    // 1. التحكم الرأسي (يدوي: لازم تفضل دايس)
//    // ------------------------------------------------
//    if (isKeyPressed(KeyEvent.VK_DOWN)) {
//        if (y > 0) {
//            y--;
//        }
//    }
//    if (isKeyPressed(KeyEvent.VK_UP)) {
//        if (y < maxHeight - 10) {
//            y++;
//        }
//    }
//
//    // ------------------------------------------------
//    // 2. التحكم الأفقي (تغيير الاتجاه بالأسهم)
//    // ------------------------------------------------
//    if (isKeyPressed(KeyEvent.VK_RIGHT)) {
//        isMovingRight = true; // غير الاتجاه لليمين
//    }
//    if (isKeyPressed(KeyEvent.VK_LEFT)) {
//        isMovingRight = false; // غير الاتجاه لليسار
//    }
//
//    // ------------------------------------------------
//    // 3. تطبيق الحركة المستمرة (بناءً على الاتجاه الحالي)
//    // ------------------------------------------------
//    if (isMovingRight) {
//        // لو الاتجاه يمين.. زود X (طالما مخرجش من الشاشة)
//        if (x < maxWidth - 10) {
//            x++;
//        }
//    } else {
//        // لو الاتجاه يسار.. نقص X (طالما مخرجش من الشاشة)
//        if (x > 0) {
//            x--;
//        }
//    }
//}*/

    public void handleKeyPress() {

        // 1. التحكم الرأسي (يدوي)
        if (isKeyPressed(KeyEvent.VK_DOWN)) {
            if (y > 0) y--;
        }
        if (isKeyPressed(KeyEvent.VK_UP)) {
            if (y < maxHeight - 10) y++;
        }

        // 2. التحكم في الاتجاه (يدوي)
        if (isKeyPressed(KeyEvent.VK_RIGHT)) {
            isMovingRight = true;
        }
        if (isKeyPressed(KeyEvent.VK_LEFT)) {
            isMovingRight = false;
        }

        // 3. الحركة والارتداد (أوتوماتيك)
        if (isMovingRight) {
            // لو لسه موصلتش للحيطة اليمين.. كمل مشي
            if (x < maxWidth - 10) {
                x++;
            } else {
                // 🛑 وصلت للحيطة اليمين؟ اعكس الاتجاه فوراً
                isMovingRight = false;
                direction=-1;

            }
        } else {
            // لو لسه موصلتش للحيطة الشمال.. كمل مشي
            if (x > 0) {
                x--;
            } else {
                // 🛑 وصلت للحيطة الشمال؟ اعكس الاتجاه فوراً
                isMovingRight = true;
                direction=1;
            }
        }
    }

    public BitSet keyBits = new BitSet(256);

    @Override
    public void keyPressed(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        keyBits.set(keyCode);
    }

    @Override
    public void keyReleased(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        keyBits.clear(keyCode);
    }

    @Override
    public void keyTyped(final KeyEvent event) {
    }

    public boolean isKeyPressed(final int keyCode) {
        return keyBits.get(keyCode);
    }
}