import com.tedu.manager.*;
import com.tedu.show.*;
import com.tedu.element.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class DebugEnemyInfo {
    public static void main(String[] args) throws Exception {
        ElementManager em = ElementManager.getManager();
        em.init();
        GameRuntime.resetForNewGame();
        GameLoad.loadImg();
        GameLoad.loadObj();
        Enemy enemy = (Enemy) GameLoad.getObj("enemy").createElement("650,520,enemy,2");
        Field currentFrameField = Enemy.class.getDeclaredField("currentFrame");
        currentFrameField.setAccessible(true);
        ImageIcon icon = (ImageIcon) currentFrameField.get(enemy);
        System.out.println("currentFrame=" + icon.getIconWidth() + "x" + icon.getIconHeight());
        ImageIO.write((BufferedImage) icon.getImage(), "png", new File("out_tmp/enemy_current_runtime.png"));

        Field runLeftField = Enemy.class.getDeclaredField("RUN_LEFT");
        runLeftField.setAccessible(true);
        java.util.List<ImageIcon> runLeft = (java.util.List<ImageIcon>) runLeftField.get(null);
        int i = 0;
        for (ImageIcon frame : runLeft) {
            System.out.println("RUN_LEFT[" + i + "]=" + frame.getIconWidth() + "x" + frame.getIconHeight());
            BufferedImage bi = new BufferedImage(frame.getIconWidth(), frame.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            bi.getGraphics().drawImage(frame.getImage(), 0, 0, null);
            ImageIO.write(bi, "png", new File("out_tmp/run_left_" + i + ".png"));
            i++;
        }
    }
}
