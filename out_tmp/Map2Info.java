import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Map2Info {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("image/images/±³¾°/map2.png"));
        System.out.println(img.getWidth() + "x" + img.getHeight());
    }
}
