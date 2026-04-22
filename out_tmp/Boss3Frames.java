import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Boss3Frames {
    public static void main(String[] args) throws Exception {
        File dir = new File("image/images/boss/boss3/attack");
        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
        if (files == null) return;
        java.util.Arrays.sort(files, java.util.Comparator.comparing(File::getName));
        for (File f : files) {
            BufferedImage img = ImageIO.read(f);
            System.out.println(f.getName() + " -> " + img.getWidth() + "x" + img.getHeight());
        }
    }
}
