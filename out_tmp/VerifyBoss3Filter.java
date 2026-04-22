import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerifyBoss3Filter {
    public static void main(String[] args) throws Exception {
        File dir = new File("image/images/boss/boss3/attack");
        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
        Arrays.sort(files, java.util.Comparator.comparing(File::getName));
        int[] widths = new int[files.length];
        int[] heights = new int[files.length];
        List<int[]> dims = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            BufferedImage img = ImageIO.read(files[i]);
            widths[i] = img.getWidth();
            heights[i] = img.getHeight();
            dims.add(new int[] {img.getWidth(), img.getHeight()});
        }
        Arrays.sort(widths);
        Arrays.sort(heights);
        int medianWidth = widths[widths.length / 2];
        int medianHeight = heights[heights.length / 2];
        int maxWidth = Math.max(480, medianWidth * 3);
        int maxHeight = Math.max(480, medianHeight * 3);
        int kept = 0;
        int dropped = 0;
        for (int[] dim : dims) {
            if (dim[0] > maxWidth || dim[1] > maxHeight) dropped++; else kept++;
        }
        System.out.println("median=" + medianWidth + "x" + medianHeight + ", threshold=" + maxWidth + "x" + maxHeight + ", kept=" + kept + ", dropped=" + dropped);
    }
}
