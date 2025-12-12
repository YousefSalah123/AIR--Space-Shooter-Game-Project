package Texture;

import com.sun.opengl.util.BufferUtil;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Image loading class that converts BufferedImages into a data
 * structure that can be easily passed to OpenGL.
 * @author Pepijn Van Eeckhoudt
 */

public class TextureReader {
  public static Texture readTexture(String filename) throws IOException {
    return readTexture(filename, false);
  }

  public static Texture readTexture(String filename, boolean storeAlphaChannel) throws IOException {
    BufferedImage bufferedImage;
    if (filename.endsWith(".bmp")) {
      bufferedImage = BitmapLoader.loadBitmap(filename);
    } else {
      bufferedImage = readImage(filename);
    }
    return readPixels(bufferedImage, storeAlphaChannel);
  }

  private static BufferedImage readImage(String resourceName) throws IOException {
    return ImageIO.read(ResourceRetriever.getResourceAsStream(resourceName));
  }
    private static Texture readPixels(BufferedImage img, boolean storeAlphaChannel) {
        int width = img.getWidth();
        int height = img.getHeight();

        // 1. استخدام مصفوفة مباشرة بدلاً من PixelGrabber البطيء
        int[] packedPixels = new int[width * height];

        // هذه الدالة أسرع بـ 10 مرات من PixelGrabber
        img.getRGB(0, 0, width, height, packedPixels, 0, width);

        int bytesPerPixel = storeAlphaChannel ? 4 : 3;
        ByteBuffer unpackedPixels = BufferUtil.newByteBuffer(packedPixels.length * bytesPerPixel);

        // 2. تحويل البيانات (Flip Y for OpenGL)
        for (int row = height - 1; row >= 0; row--) {
            for (int col = 0; col < width; col++) {
                int packedPixel = packedPixels[row * width + col];

                unpackedPixels.put((byte) ((packedPixel >> 16) & 0xFF)); // Red
                unpackedPixels.put((byte) ((packedPixel >> 8) & 0xFF));  // Green
                unpackedPixels.put((byte) ((packedPixel >> 0) & 0xFF));  // Blue

                if (storeAlphaChannel) {
                    unpackedPixels.put((byte) ((packedPixel >> 24) & 0xFF)); // Alpha
                }
            }
        }

        unpackedPixels.flip();
        return new Texture(unpackedPixels, width, height);
    }

  public static class Texture 
  {
    private ByteBuffer pixels;
    private int width;
    private int height;

    public Texture( ByteBuffer pixels, int width, int height ) 
      {
	this.height = height;
	this.pixels = pixels;
	this.width = width;
      }

    public int getWidth() 
      {
	return width;
      }
    
    public int getHeight() 
      {
	return height;
      }

    public ByteBuffer getPixels() 
      {
	return pixels;
      }

  }
}
