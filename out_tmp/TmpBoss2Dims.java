import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class TmpBoss2Dims {
  public static void main(String[] args) throws Exception {
    String[] dirs = {"move","attack","换子弹","下蹲","死亡动作"};
    for (String name : dirs) {
      File dir = new File("Game1/image/images/boss/boss2/" + name);
      File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
      if (files == null) { System.out.println(name + " missing"); continue; }
      Arrays.sort(files, Comparator.comparing(File::getName));
      int minW=Integer.MAX_VALUE,maxW=0,minH=Integer.MAX_VALUE,maxH=0;
      for (File f : files) {
        BufferedImage img = ImageIO.read(f);
        minW=Math.min(minW,img.getWidth()); maxW=Math.max(maxW,img.getWidth());
        minH=Math.min(minH,img.getHeight()); maxH=Math.max(maxH,img.getHeight());
      }
      System.out.println(name + " count=" + files.length + " w=" + minW + "-" + maxW + " h=" + minH + "-" + maxH);
    }
  }
}
