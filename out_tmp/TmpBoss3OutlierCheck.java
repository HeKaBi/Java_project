import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class TmpBoss3OutlierCheck {
  public static void main(String[] args) throws Exception {
    File dir = new File("Game1/image/images/boss/boss3/attack");
    File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
    Arrays.sort(files, Comparator.comparing(File::getName));
    int kept = 0;
    int dropped = 0;
    List<Integer> widths = new ArrayList<>();
    List<Integer> heights = new ArrayList<>();
    for (File f : files) {
      BufferedImage img = ImageIO.read(f);
      widths.add(img.getWidth());
      heights.add(img.getHeight());
    }
    Collections.sort(widths);
    Collections.sort(heights);
    int medianWidth = widths.get(widths.size() / 2);
    int medianHeight = heights.get(heights.size() / 2);
    int maxWidth = Math.max(480, medianWidth * 3);
    int maxHeight = Math.max(480, medianHeight * 3);
    for (File f : files) {
      BufferedImage img = ImageIO.read(f);
      if (img.getWidth() > maxWidth || img.getHeight() > maxHeight) {
        dropped++;
      } else {
        kept++;
      }
    }
    System.out.println("boss3 attack kept=" + kept + " dropped=" + dropped + " threshold=" + maxWidth + "x" + maxHeight);
  }
}
