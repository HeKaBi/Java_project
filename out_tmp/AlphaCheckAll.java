import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;

public class AlphaCheckAll {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get("image/images/boss/boss2");
        Files.walk(root)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().toLowerCase().endsWith(".png"))
            .sorted()
            .forEach(p -> {
                try {
                    BufferedImage img = ImageIO.read(p.toFile());
                    int w = img.getWidth();
                    int h = img.getHeight();
                    int c1 = img.getRGB(0,0);
                    int c2 = img.getRGB(w-1,0);
                    int c3 = img.getRGB(0,h-1);
                    int c4 = img.getRGB(w-1,h-1);
                    int a1 = (c1 >>> 24) & 0xFF;
                    int a2 = (c2 >>> 24) & 0xFF;
                    int a3 = (c3 >>> 24) & 0xFF;
                    int a4 = (c4 >>> 24) & 0xFF;
                    if (a1 > 0 || a2 > 0 || a3 > 0 || a4 > 0) {
                        Path rel = root.relativize(p);
                        System.out.printf("%s | corners alpha=(%d,%d,%d,%d) | corners=(%08X,%08X,%08X,%08X)%n",
                                rel, a1, a2, a3, a4, c1, c2, c3, c4);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
    }
}