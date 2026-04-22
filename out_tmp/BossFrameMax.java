import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class BossFrameMax {
    public static void main(String[] args) throws Exception {
        for (String dirName : args) {
            File dir = new File(dirName);
            File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
            if (files == null) continue;
            int maxW = 0, maxH = 0;
            String maxFile = "";
            for (File f : files) {
                BufferedImage img = ImageIO.read(f);
                if (img == null) continue;
                if (img.getWidth() * img.getHeight() > maxW * maxH) {
                    maxW = img.getWidth(); maxH = img.getHeight(); maxFile = f.getPath();
                }
            }
            System.out.println(dirName + " max=" + maxW + "x" + maxH + " file=" + maxFile);
        }
    }
}
