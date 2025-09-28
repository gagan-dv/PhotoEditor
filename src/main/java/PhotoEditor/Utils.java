package PhotoEditor;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
class Utils {
    static BufferedImage toARGB(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
            return src;
        }
        BufferedImage out = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }
    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean premultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, premultiplied, null);
    }
    static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
    static void saveImage(BufferedImage img, File f) throws IOException {
        String name = f.getName().toLowerCase();
        String format;
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            format = "jpg";
        } else if (name.endsWith(".png")) {
            format = "png";
        } else {
            f = new File(f.getParentFile(), f.getName() + ".png");
            format = "png";
        }
        ImageIO.write(img, format, f);
    }
}
