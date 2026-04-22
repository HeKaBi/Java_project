import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class SimpleGuideProbe {
    private static boolean isGuide(int rgb) {
        int a = (rgb >>> 24) & 0xff;
        if (a < 24) return false;
        int r = (rgb >>> 16) & 0xff;
        int g = (rgb >>> 8) & 0xff;
        int b = rgb & 0xff;
        return r >= 180 && g <= 125 && b <= 125 && r - Math.max(g, b) >= 70;
    }
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("image/images/±³¾°/Ö¸µ¼.png"));
        int width = img.getWidth();
        int height = img.getHeight();
        int wallColumns = 0;
        int longestRun = 0;
        for (int x = 0; x < width; x++) {
            int bestRun = 0, current = 0;
            for (int y = 0; y < height; y++) {
                if (isGuide(img.getRGB(x, y))) {
                    current++;
                    if (current > bestRun) bestRun = current;
                } else {
                    current = 0;
                }
            }
            if (bestRun >= 10) {
                wallColumns++;
                if (bestRun > longestRun) longestRun = bestRun;
                if (wallColumns <= 40) {
                    System.out.println("wall-like column x=" + x + " run=" + bestRun);
                }
            }
        }
        System.out.println("width=" + width + ", height=" + height + ", wallColumns=" + wallColumns + ", longestRun=" + longestRun);
    }
}
