import com.tedu.manager.*;
import com.tedu.show.*;
import com.tedu.element.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class DebugRender {
    public static void main(String[] args) throws Exception {
        ElementManager em = ElementManager.getManager();
        em.init();
        GameRuntime.resetForNewGame();
        GameLoad.loadImg();
        GameLoad.loadObj();
        ElementObj map = GameLoad.getObj("map").createElement("0,0,map");
        em.addElement(map, GameElement.MAPS);
        GameLoad.loadPlay();
        ElementObj enemy = GameLoad.getObj("enemy").createElement("650,520,enemy,2");
        em.addElement(enemy, GameElement.ENEMY);

        GameMainJPanel panel = new GameMainJPanel();
        panel.setSize(GameJFrame.GameX, GameJFrame.GameY);
        BufferedImage img = new BufferedImage(GameJFrame.GameX, GameJFrame.GameY, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        panel.paint(g);
        g.dispose();
        ImageIO.write(img, "png", new File("out_tmp/debug_frame.png"));

        ElementObj play = em.getElementsByKey(GameElement.PLAY).get(0);
        System.out.println("player x=" + play.getX() + " y=" + play.getY() + " w=" + play.getW() + " h=" + play.getH());
        System.out.println("enemy x=" + enemy.getX() + " y=" + enemy.getY() + " w=" + enemy.getW() + " h=" + enemy.getH());
    }
}
