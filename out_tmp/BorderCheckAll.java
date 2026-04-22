import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;

public class BorderCheckAll {
    private static boolean isWhiteLike(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        return a >= 250 && r >= 245 && g >= 245 && b >= 245;
    }

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
                    int opaqueBorder = 0;
                    int whiteLikeBorder = 0;

                    for (int x = 0; x < w; x++) {
                        int t = img.getRGB(x, 0);
                        int b = img.getRGB(x, h - 1);
                        if (((t >>> 24) & 0xFF) > 0) opaqueBorder++;
                        if (((b >>> 24) & 0xFF) > 0) opaqueBorder++;
                        if (isWhiteLike(t)) whiteLikeBorder++;
                        if (isWhiteLike(b)) whiteLikeBorder++;
                    }
                    for (int y = 1; y < h - 1; y++) {
                        int l = img.getRGB(0, y);
                        int r = img.getRGB(w - 1, y);
                        if (((l >>> 24) & 0xFF) > 0) opaqueBorder++;
                        if (((r >>> 24) & 0xFF) > 0) opaqueBorder++;
                        if (isWhiteLike(l)) whiteLikeBorder++;
                        if (isWhiteLike(r)) whiteLikeBorder++;
                    }

                    if (opaqueBorder > 20 || whiteLikeBorder > 5) {
                        Path rel = root.relativize(p);
                        System.out.printf("%s | opaqueBorder=%d | whiteLikeBorder=%d%n", rel, opaqueBorder, whiteLikeBorder);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
    }
}