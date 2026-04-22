import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class TmpWallProbe {
    private static boolean isGuide(int rgb) {
        int a = (rgb >>> 24) & 0xff;
        if (a < 24) return false;
        int r = (rgb >>> 16) & 0xff;
        int g = (rgb >>> 8) & 0xff;
        int b = rgb & 0xff;
        return r >= 180 && g <= 125 && b <= 125 && r - Math.max(g, b) >= 70;
    }
    private static void probe(String path) throws Exception {
        BufferedImage img = ImageIO.read(new File(path));
        int width = img.getWidth();
        int height = img.getHeight();
        int wallColumns = 0;
        int longestRun = 0;
        System.out.println("== " + path + " ==");
        for (int x = 0; x < width; x++) {
            int bestRun = 0;
            int current = 0;
            for (int y = 0; y < height; y++) {
                if (isGuide(img.getRGB(x, y))) {
                    current++;
                    bestRun = Math.max(bestRun, current);
                } else {
                    current = 0;
                }
            }
            if (bestRun >= 8) {
                wallColumns++;
                longestRun = Math.max(longestRun, bestRun);
                System.out.println("x=" + x + ", run=" + bestRun);
            }
        }
        System.out.println("wallColumns=" + wallColumns + ", longestRun=" + longestRun);
    }
    public static void main(String[] args) throws Exception {
        probe("Game1/image/images/??/mission1???.png");
        probe("Game1/image/images/??/??.png");
    }
}
