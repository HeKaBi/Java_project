import com.tedu.manager.*;
import com.tedu.element.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import javax.imageio.ImageIO;

public class DebugPlayerLeft {
    public static void main(String[] args) throws Exception {
        GameLoad.loadImg();
        GameLoad.loadObj();
        PaoPao play = (PaoPao) GameLoad.getObj("paopao").createElement("300,560,paopao");
        Field faceRight = PaoPao.class.getDeclaredField("faceRight");
        faceRight.setAccessible(true);
        faceRight.setBoolean(play, false);
        BufferedImage img = new BufferedImage(1000, 640, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        play.showElement(g);
        g.dispose();
        ImageIO.write(img, "png", new File("out_tmp/player_left_only.png"));
    }
}
