import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class TmpBoss3Info {
  public static void main(String[] args) throws Exception {
    String[] dirs = {"Game1/image/images/boss/boss3/walking", "Game1/image/images/boss/boss3/attack", "Game1/image/images/boss/boss3/die"};
    for (String dirPath : dirs) {
      File dir = new File(dirPath);
      File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
      if (files == null) { System.out.println(dirPath + " -> none"); continue; }
      Arrays.sort(files, Comparator.comparing(File::getName));
      int minW=Integer.MAX_VALUE,maxW=0,minH=Integer.MAX_VALUE,maxH=0;
      int whiteCorners=0;
      for (File f : files) {
        BufferedImage img = ImageIO.read(f);
        int w=img.getWidth(), h=img.getHeight();
        minW=Math.min(minW,w);maxW=Math.max(maxW,w);minH=Math.min(minH,h);maxH=Math.max(maxH,h);
        if (isWhite(img.getRGB(0,0)) || isWhite(img.getRGB(w-1,0)) || isWhite(img.getRGB(0,h-1)) || isWhite(img.getRGB(w-1,h-1))) {
          whiteCorners++;
        }
      }
      System.out.println(dirPath + " count=" + files.length + " w=" + minW + "-" + maxW + " h=" + minH + "-" + maxH + " whiteCornerFrames=" + whiteCorners);
    }
  }
  private static boolean isWhite(int argb) {
    int a=(argb>>>24)&255; int r=(argb>>>16)&255; int g=(argb>>>8)&255; int b=argb&255;
    return a>240 && r>235 && g>235 && b>235;
  }
}
