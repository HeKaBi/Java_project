import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class TmpBossDirInfo {
  public static void main(String[] args) throws Exception {
    String[] dirs = {"Game1/image/images/boss/boss1/attack", "Game1/image/images/boss/boss1/die", "Game1/image/images/boss/boss2/move", "Game1/image/images/boss/boss2/attack", "Game1/image/images/boss/boss2/???", "Game1/image/images/boss/boss2/??", "Game1/image/images/boss/boss2/????"};
    for (String dirPath : dirs) {
      File dir = new File(dirPath);
      File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
      System.out.print(dirPath + " -> ");
      if (files == null) { System.out.println("missing"); continue; }
      Arrays.sort(files, Comparator.comparing(File::getName));
      BufferedImage img = files.length == 0 ? null : ImageIO.read(files[0]);
      System.out.println("count=" + files.length + (img == null ? "" : " sample=" + img.getWidth() + "x" + img.getHeight()));
    }
  }
}
