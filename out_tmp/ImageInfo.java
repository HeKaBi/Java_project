import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageInfo {
    public static void main(String[] args) throws Exception {
        for (String path : args) {
            BufferedImage img = ImageIO.read(new File(path));
            if (img == null) {
                System.out.println(path + " -> null");
                continue;
            }
            int minX = img.getWidth(), minY = img.getHeight(), maxX = -1, maxY = -1, alphaCount = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int a = (img.getRGB(x, y) >>> 24) & 0xff;
                    if (a > 16) {
                        alphaCount++;
                        if (x < minX) minX = x;
                        if (y < minY) minY = y;
                        if (x > maxX) maxX = x;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            System.out.println(path + " -> " + img.getWidth() + "x" + img.getHeight()
                + ", opaque=" + alphaCount
                + ", bbox=" + minX + "," + minY + " - " + maxX + "," + maxY);
        }
    }
}
