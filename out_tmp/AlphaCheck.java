import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;

public class AlphaCheck {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get("image/images/boss/boss2/attack");
        Files.list(root)
            .filter(p -> p.toString().toLowerCase().endsWith(".png"))
            .sorted()
            .forEach(p -> {
                try {
                    BufferedImage img = ImageIO.read(p.toFile());
                    int w = img.getWidth();
                    int h = img.getHeight();
                    int minA = 255;
                    int maxA = 0;
                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            int a = (img.getRGB(x, y) >>> 24) & 0xFF;
                            if (a < minA) minA = a;
                            if (a > maxA) maxA = a;
                        }
                    }
                    int c1 = img.getRGB(0,0);
                    int c2 = img.getRGB(w-1,0);
                    int c3 = img.getRGB(0,h-1);
                    int c4 = img.getRGB(w-1,h-1);
                    System.out.printf("%s | %dx%d | minA=%d maxA=%d | corners=(%08X,%08X,%08X,%08X)%n",
                            p.getFileName(), w, h, minA, maxA, c1, c2, c3, c4);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
    }
}