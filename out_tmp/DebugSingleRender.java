import com.tedu.manager.*;
import com.tedu.show.*;
import com.tedu.element.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class DebugSingleRender {
    public static void main(String[] args) throws Exception {
        GameLoad.loadImg();
        GameLoad.loadObj();

        Enemy enemy = (Enemy) GameLoad.getObj("enemy").createElement("650,520,enemy,2");
        BufferedImage img1 = new BufferedImage(1000, 640, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = img1.createGraphics();
        enemy.showElement(g1);
        g1.dispose();
        ImageIO.write(img1, "png", new File("out_tmp/enemy_only.png"));

        PaoPao play = (PaoPao) GameLoad.getObj("paopao").createElement("300,560,paopao");
        BufferedImage img2 = new BufferedImage(1000, 640, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img2.createGraphics();
        play.showElement(g2);
        g2.dispose();
        ImageIO.write(img2, "png", new File("out_tmp/player_only.png"));
    }
}
