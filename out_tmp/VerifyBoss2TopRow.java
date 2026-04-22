import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VerifyBoss2TopRow {
    private static int opaqueTopRowPixels(BufferedImage img) {
        int count = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            int a = (img.getRGB(x, 0) >>> 24) & 0xFF;
            if (a > 0) count++;
        }
        return count;
    }

    public static void main(String[] args) throws Exception {
        Path root = Paths.get("image/images/boss/boss2/\u6b7b\u4ea1\u52a8\u4f5c");
        String[] targets = {
            "\u5c4f\u5e55\u622a\u56fe 2026-04-21 230307.png",
            "\u5c4f\u5e55\u622a\u56fe 2026-04-21 230313.png",
            "\u5c4f\u5e55\u622a\u56fe 2026-04-21 230318.png"
        };

        for (String name : targets) {
            Path file = root.resolve(name);
            BufferedImage img = ImageIO.read(file.toFile());
            int w = img.getWidth();
            int h = img.getHeight();
            int c1 = img.getRGB(0, 0);
            int c2 = img.getRGB(w - 1, 0);
            int a1 = (c1 >>> 24) & 0xFF;
            int a2 = (c2 >>> 24) & 0xFF;
            System.out.printf("%s | topOpaque=%d | cornerAlpha=(%d,%d)%n", name, opaqueTopRowPixels(img), a1, a2);
        }
    }
}