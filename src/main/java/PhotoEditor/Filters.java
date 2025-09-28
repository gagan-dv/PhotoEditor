package PhotoEditor;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
class Filters {
    interface Op {
        BufferedImage apply(BufferedImage src);
    }

    static BufferedImage toGrayscale(BufferedImage src) {
        ColorConvertOp op = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY), null
        );
        BufferedImage out = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB
        );
        op.filter(src, out);
        return out;
    }
    static BufferedImage toSepia(BufferedImage src) {
        BufferedImage out = Utils.deepCopy(src);
        int w = out.getWidth(), h = out.getHeight();

        int[] pixels = out.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i] >>> 24) & 0xFF;
            int r = (pixels[i] >>> 16) & 0xFF;
            int g = (pixels[i] >>> 8) & 0xFF;
            int b = pixels[i] & 0xFF;

            int tr = Utils.clamp((int) (0.393 * r + 0.769 * g + 0.189 * b));
            int tg = Utils.clamp((int) (0.349 * r + 0.686 * g + 0.168 * b));
            int tb = Utils.clamp((int) (0.272 * r + 0.534 * g + 0.131 * b));

            pixels[i] = (a << 24) | (tr << 16) | (tg << 8) | tb;
        }
        out.setRGB(0, 0, w, h, pixels, 0, w);
        return out;
    }
    static BufferedImage blur3x3(BufferedImage src) {
        float[] kernel = {
                1 / 9f, 1 / 9f, 1 / 9f,
                1 / 9f, 1 / 9f, 1 / 9f,
                1 / 9f, 1 / 9f, 1 / 9f
        };
        return convolve(src, kernel);
    }
    static BufferedImage sharpen3x3(BufferedImage src) {
        float[] kernel = {
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0
        };
        return convolve(src, kernel);
    }

    static BufferedImage rotate(BufferedImage src, double radians) {
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int w = src.getWidth(), h = src.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransform at = new AffineTransform();
        at.translate(newW / 2.0, newH / 2.0);
        at.rotate(radians);
        at.translate(-w / 2.0, -h / 2.0);

        g.drawRenderedImage(src, at);
        g.dispose();

        return out;
    }

    static BufferedImage flipHorizontal(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, w, h, w, 0, 0, h, null);
        g.dispose();
        return out;
    }
    static BufferedImage flipVertical(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, w, h, 0, h, w, 0, null);
        g.dispose();
        return out;
    }
    static BufferedImage adjust(BufferedImage src, int brightness, int contrast, int saturation) {
        BufferedImage out = Utils.deepCopy(src);
        int w = out.getWidth(), h = out.getHeight();
        int[] pixels = out.getRGB(0, 0, w, h, null, 0, w);

        float cScale = (float) Math.pow((100.0 + contrast) / 100.0, 2);
        int bOffset = brightness;

        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i] >>> 24) & 0xFF;
            int r = (pixels[i] >>> 16) & 0xFF;
            int g = (pixels[i] >>> 8) & 0xFF;
            int b = pixels[i] & 0xFF;

            // Apply brightness and contrast
            r = Utils.clamp((int) (((r - 128) * cScale) + 128 + bOffset));
            g = Utils.clamp((int) (((g - 128) * cScale) + 128 + bOffset));
            b = Utils.clamp((int) (((b - 128) * cScale) + 128 + bOffset));

            // Adjust saturation
            float[] hsb = Color.RGBtoHSB(r, g, b, null);
            float s = Math.max(0f, Math.min(1f, hsb[1] * ((100 + saturation) / 100f)));

            int rgb = Color.HSBtoRGB(hsb[0], s, hsb[2]);
            r = (rgb >> 16) & 0xFF;
            g = (rgb >> 8) & 0xFF;
            b = rgb & 0xFF;

            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        out.setRGB(0, 0, w, h, pixels, 0, w);
        return out;
    }
    private static BufferedImage convolve(BufferedImage src, float[] kernel) {
        Kernel k = new Kernel(3, 3, kernel);
        ConvolveOp op = new ConvolveOp(k, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage out = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB
        );
        op.filter(src, out);
        return out;
    }
}
