import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FixBoss2TopRow {
    private static int opaqueTopRowPixels(BufferedImage img) {
        int count = 0;
        int w = img.getWidth();
        for (int x = 0; x < w; x++) {
            int a = (img.getRGB(x, 0) >>> 24) & 0xFF;
            if (a > 0) {
                count++;
            }
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
            if (img == null) {
                System.out.println("skip (read failed): " + file);
                continue;
            }

            int before = opaqueTopRowPixels(img);
            int w = img.getWidth();
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, 0);
                img.setRGB(x, 0, argb & 0x00FFFFFF);
            }

            ImageIO.write(img, "png", file.toFile());

            BufferedImage verify = ImageIO.read(file.toFile());
            int after = verify == null ? -1 : opaqueTopRowPixels(verify);
            System.out.printf("%s | opaque top row: %d -> %d%n", name, before, after);
        }
    }
}