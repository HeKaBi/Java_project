import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class TmpBoss3Outlier {
  public static void main(String[] args) throws Exception {
    File dir = new File("Game1/image/images/boss/boss3/attack");
    File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
    Arrays.sort(files, Comparator.comparing(File::getName));
    for (File f : files) {
      BufferedImage img = ImageIO.read(f);
      int w=img.getWidth(), h=img.getHeight();
      if (w > 500 || h > 500) {
        System.out.println(f.getName() + " -> " + w + "x" + h);
      }
    }
  }
}
