package PhotoEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

class CanvasPanel extends JComponent {
    private BufferedImage image;
    private BufferedImage originalImage;
    private BufferedImage checkerboard;
    private double scale = 1.0;
    private Rectangle selection;
    private Point dragStart;
    private StatusBar statusBar;

    CanvasPanel() {
        setOpaque(true);
        setBackground(new Color(45, 45, 45));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                selection = new Rectangle(dragStart);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    selection = createRectangle(dragStart, e.getPoint());
                    repaint();
                    updateStatus(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
                updateStatus(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateStatus(e.getPoint());
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                adjustZoom(e.getWheelRotation() < 0 ? 1.1 : 1 / 1.1);
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    void setStatusBar(StatusBar sb) {
        this.statusBar = sb;
    }

    void setImage(BufferedImage img) {
        this.image = Utils.toARGB(img);
        this.originalImage = Utils.deepCopy(img);
        generateCheckerboard();
        resetView();
    }

    BufferedImage getImage() {
        return image;
    }

    void resetImage() {
        if (originalImage != null) {
            this.image = Utils.deepCopy(originalImage);
            resetView();
        }
    }

    void zoomToFit() {
        if (image == null || getParent() == null) return;
        Dimension vp = (getParent() instanceof JViewport)
                ? ((JViewport) getParent()).getExtentSize()
                : getSize();
        if (vp.width == 0 || vp.height == 0) return;
        double sx = vp.width / (double) image.getWidth();
        double sy = vp.height / (double) image.getHeight();
        adjustZoom(Math.min(sx, sy));
    }

    Rectangle getSelectionImageSpace() {
        if (selection == null || image == null) return null;
        Rectangle scaled = new Rectangle(
                (int) (selection.x / scale),
                (int) (selection.y / scale),
                (int) (selection.width / scale),
                (int) (selection.height / scale)
        );
        scaled = scaled.intersection(new Rectangle(0, 0, image.getWidth(), image.getHeight()));
        return (scaled.width > 0 && scaled.height > 0) ? scaled : null;
    }

    void clearSelection() {
        selection = null;
        repaint();
    }

    private void resetView() {
        selection = null;
        scale = 1.0;
        revalidate();
        repaint();
        updateStatus(null);
    }

    private void adjustZoom(double factor) {
        double newScale = Math.max(0.05, Math.min(10, scale * factor));
        if (Math.abs(newScale - scale) > 1e-6) {
            scale = newScale;
            revalidate();
            repaint();
            updateStatus(null);
        }
    }

    private void generateCheckerboard() {
        int size = 20;
        checkerboard = new BufferedImage(size * 2, size * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = checkerboard.createGraphics();
        Color c1 = new Color(200, 200, 200);
        Color c2 = new Color(150, 150, 150);
        g.setColor(c1);
        g.fillRect(0, 0, size, size);
        g.fillRect(size, size, size, size);
        g.setColor(c2);
        g.fillRect(size, 0, size, size);
        g.fillRect(0, size, size, size);
        g.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return image == null
                ? new Dimension(800, 600)
                : new Dimension(
                (int) Math.round(image.getWidth() * scale),
                (int) Math.round(image.getHeight() * scale)
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (checkerboard != null) {
            TexturePaint paint = new TexturePaint(checkerboard,
                    new Rectangle(0, 0, checkerboard.getWidth(), checkerboard.getHeight()));
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        if (image != null) {
            int iw = (int) Math.round(image.getWidth() * scale);
            int ih = (int) Math.round(image.getHeight() * scale);
            g2.drawImage(image, 0, 0, iw, ih, null);
        }

        if (selection != null && selection.width > 1 && selection.height > 1) {
            g2.setColor(new Color(0, 120, 215, 60));
            g2.fill(selection);
            g2.setColor(new Color(0, 120, 215));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(selection);
        }

        g2.dispose();
    }

    private Rectangle createRectangle(Point p1, Point p2) {
        int x = Math.min(p1.x, p2.x);
        int y = Math.min(p1.y, p2.y);
        int w = Math.abs(p1.x - p2.x);
        int h = Math.abs(p1.y - p2.y);
        return new Rectangle(x, y, w, h);
    }

    private void updateStatus(Point p) {
        if (statusBar != null && image != null) {
            if (p != null) {
                int ix = (int) (p.x / scale);
                int iy = (int) (p.y / scale);
                if (ix >= 0 && iy >= 0 && ix < image.getWidth() && iy < image.getHeight()) {
                    int rgb = image.getRGB(ix, iy);
                    Color c = new Color(rgb, true);
                    statusBar.setMessage(
                            String.format("x=%d y=%d  |  R=%d G=%d B=%d A=%d",
                                    ix, iy, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
                    return;
                }
            }
            statusBar.setMessage("Image size: " + image.getWidth() + "x" + image.getHeight());
        }
    }
}
