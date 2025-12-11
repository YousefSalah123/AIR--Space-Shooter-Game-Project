
    /*
     * To change this license header, choose License Headers in Project Properties.
     * To change this template file, choose Tools | Templates
     * and open the template in the editor.
     */

package Texture;
import java.awt.event.*;
import java.io.IOException;
import javax.media.opengl.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Random;
import javax.media.opengl.glu.GLU;

    public class TestListener  extends AnimListener {

        int animationIndex = 0;
        int maxWidth = 100;
        int maxHeight = 100;
        int x = maxWidth/2, y = maxHeight/2;
        int directionX=1;
        int directionY=1;
        int animationIndex2 = 0;
        private int frameCounter = 0;
        private float fishScale = 1.0f;
        boolean isShoot=false;
        double rotate=0.0;
        boolean isrotated=false;
        private final float FISH_SCALE_STEP = 0.15f;
        private final float FISH_SCALE_MAX = 3.0f;
        private final float FISH_SCALE_MIN = 0.4f;

        // Download enemy textures from https://craftpix.net/freebies/free-monster-2d-game-items/
        String textureNames[] = {"Man1.png","Man2.png","Man3.png","Man4.png","B1.png","B2.png","B3.png","B4.png","B5.png","Arrow.png","B6.png",
                "Back.png"};
        TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
        int textures[] = new int[textureNames.length];

        /*
         5 means gun in array pos
         x and y coordinate for gun
         */
        class FallingItem {
            float x, y, speed, scale;
            int texIndex;

            FallingItem(float x, float y, float speed, int texIndex, float scale) {
                this.x = x;
                this.y = y;
                this.speed = speed;
                this.texIndex = texIndex;
                this.scale = scale;
            }

            void update() {
                this.y -= this.speed; // تنزل لتحت
            }
        }
        ArrayList<TestListener.FallingItem> items = new ArrayList<>();
        Random rnd = new Random();
        int spawnCounter = 0;
        public void init(GLAutoDrawable gld) {

            GL gl = gld.getGL();
            gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);    //This Will Clear The Background Color To Black

            gl.glEnable(GL.GL_TEXTURE_2D);  // Enable Texture Mapping
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glGenTextures(textureNames.length, textures, 0);

            for(int i = 0; i < textureNames.length; i++){
                try {
                    texture[i] = TextureReader.readTexture(assetsFolderName + "//" + textureNames[i] , true);
                    gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);

//                mipmapsFromPNG(gl, new GLU(), texture[i]);
                    new GLU().gluBuild2DMipmaps(
                            GL.GL_TEXTURE_2D,
                            GL.GL_RGBA, // Internal Texel Format,
                            texture[i].getWidth(), texture[i].getHeight(),
                            GL.GL_RGBA, // External format from image,
                            GL.GL_UNSIGNED_BYTE,
                            texture[i].getPixels() // Imagedata
                    );
                } catch( IOException e ) {
                    System.out.println(e);
                    e.printStackTrace();
                }
            }
        }

        public void display(GLAutoDrawable gld) {

            GL gl = gld.getGL();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);       //Clear The Screen And The Depth Buffer
            gl.glLoadIdentity();

            DrawBackground(gl);

            handleKeyPress();
            if(isShoot){
                fire(gl,1);
            }
            animationIndex = animationIndex % 4;


//        DrawGraph(gl);
            DrawSprite(gl, x, y, animationIndex, 1);
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
                animationIndex2 = (animationIndex2 + 1) % 4;
                frameCounter = 0;
            }

            // 7. رسم السمكة
            DrawSprite(gl, x, y, animationIndex2, fishScale);

        }
        // دالة إنشاء عنصر جديد
        private void spawnNewItem() {
            float sx = 5 + rnd.nextFloat() * (maxWidth - 10);
            float sy = maxHeight + 10f; // تبدأ من فوق الشاشة
            float speed = 0.5f + rnd.nextFloat() * 1.0f;
            float scale = 0.6f + rnd.nextFloat() * 0.4f;
            items.add(new TestListener.FallingItem(sx, sy, speed, 4, scale));
        }

        // دالة تحديث العناصر والتصادم
        private void updateItems(GL gl) {
            Iterator<TestListener.FallingItem> it = items.iterator();
            while (it.hasNext()) {
                TestListener.FallingItem f = it.next();
                f.update(); // حرك العنصر لتحت

                // ارسم العنصر
                DrawSprite(gl, (int) f.x, (int) f.y, f.texIndex, f.scale);

                // فحص التصادم مع السمكة
                if (collidesWithFish(f)) {
                        fishScale += FISH_SCALE_STEP;
                        if (fishScale > FISH_SCALE_MAX) fishScale = FISH_SCALE_MAX;
                    it.remove(); // امسح العنصر بعد الأكل
                    continue;
                }

                // امسح العنصر لو نزل تحت الشاشة
                if (f.y < -10) {
                    it.remove();
                }
            }
        }

//        // منطق التصادم
//        private boolean collidesWithFish(AnimGLEventListener4.FallingItem f) {
//            // حساب أبعاد السمكة والعنصر
//            float fishHalfW = 0.1f * fishScale * maxWidth / 2.0f;
//            float fishHalfH = 0.1f * fishScale * maxHeight / 2.0f;
//
//            float itemHalfW = 0.1f * f.scale * maxWidth / 2.0f;
//            float itemHalfH = 0.1f * f.scale * maxHeight / 2.0f;
//
//            float dx = Math.abs(x - f.x);
//            float dy = Math.abs(y - f.y);
//
//            return dx <= (fishHalfW + itemHalfW) && dy <= (fishHalfH + itemHalfH);
//        }

        public void fire(GL gl, double scale){
           DrawSprite(gl,x,y,9,1);
        }
        private boolean collidesWithFish(TestListener.FallingItem f) {
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

        public void DrawSprite(GL gl,int x, int y, int index, float scale){
            gl.glEnable(GL.GL_BLEND);
            gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);
            if(!isrotated) {
                gl.glEnable(GL.GL_BLEND);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);    // Turn Blending On

                gl.glPushMatrix();
                if (isrotated) gl.glRotated(rotate, 0, 0, 0);
                gl.glTranslated(x / (maxWidth / 2.0) - 0.9, y / (maxHeight / 2.0) - 0.9, 0);
                gl.glScaled(0.1 * scale * directionX, 0.1 * scale * directionY, 1);
                //System.out.println(x +" " + y);
                gl.glBegin(GL.GL_QUADS);
                // Front Face
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
            }

            if(isrotated) {
                gl.glPushMatrix();
                gl.glRotated(rotate,rotate,0,0);// Turn Blending On

                gl.glTranslated( x/(maxWidth/2.0) - 0.9, y/(maxHeight/2.0) - 0.9, 0);
                gl.glScaled(0.1*scale*directionX, 0.1*scale*directionY, 1);
                //System.out.println(x +" " + y);
                gl.glBegin(GL.GL_QUADS);
                // Front Face
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
                gl.glPopMatrix();
            }
        }

        public void DrawBackground(GL gl){
            gl.glEnable(GL.GL_BLEND);
            gl.glBindTexture(GL.GL_TEXTURE_2D, textures[textureNames.length-1]);	// Turn Blending On

            gl.glPushMatrix();
            gl.glBegin(GL.GL_QUADS);
            // Front Face
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

        /*
         * KeyListener
         */

        public void handleKeyPress() {

            if (isKeyPressed(KeyEvent.VK_LEFT)) {
                if (x > 0) {
                    x--;
                }
                animationIndex++;
                directionX=-1;
                isrotated=true;
                rotate=45*(Math.PI/180);
            }
            if (isKeyPressed(KeyEvent.VK_RIGHT)) {
                if (x < maxWidth-10) {
                    x++;
                }
                animationIndex++;
                directionX=1;
                isrotated=true;
                rotate=45*(Math.PI/180);

            }
            if (isKeyPressed(KeyEvent.VK_DOWN)) {
                if (y > 0) {
                    y--;
                }
                animationIndex++;
                directionY=-1;
            }
            if (isKeyPressed(KeyEvent.VK_UP)) {
                if (y < maxHeight-10) {
                    y++;
                }
                animationIndex++;
                directionY=1;
            }
            if (isKeyPressed(KeyEvent.VK_SPACE)) {
               isShoot=true;
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
            // don't care
        }

        public boolean isKeyPressed(final int keyCode) {
            return keyBits.get(keyCode);
        }
    }
