import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;

public class TopRowColors {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get("image/images/boss/boss2/\u6b7b\u4ea1\u52a8\u4f5c");
        Files.list(root)
            .filter(p -> p.toString().toLowerCase().endsWith(".png"))
            .sorted()
            .forEach(p -> {
                try {
                    BufferedImage img = ImageIO.read(p.toFile());
                    int c1 = img.getRGB(0,0);
                    int c2 = img.getRGB(img.getWidth()-1,0);
                    int a1 = (c1 >>> 24) & 0xFF;
                    int a2 = (c2 >>> 24) & 0xFF;
                    if (a1 > 0 || a2 > 0) {
                        Map<Integer, Integer> freq = new HashMap<>();
                        for (int x = 0; x < img.getWidth(); x++) {
                            int argb = img.getRGB(x, 0);
                            freq.put(argb, freq.getOrDefault(argb, 0) + 1);
                        }
                        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(freq.entrySet());
                        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
                        System.out.println(p.getFileName() + " top row unique=" + list.size());
                        for (int i = 0; i < Math.min(8, list.size()); i++) {
                            Map.Entry<Integer, Integer> e = list.get(i);
                            int argb = e.getKey();
                            int a = (argb >>> 24) & 0xFF;
                            int r = (argb >>> 16) & 0xFF;
                            int g = (argb >>> 8) & 0xFF;
                            int b = argb & 0xFF;
                            System.out.printf("  A=%d RGB=(%d,%d,%d) count=%d hex=%08X%n", a, r, g, b, e.getValue(), argb);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
    }
}