package PhotoEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

/**
 * Canvas panel for displaying and interacting with images.
 * Supports zoom, selection, and status updates.
 */
class CanvasPanel extends JComponent {
    private BufferedImage image;
    private BufferedImage originalImage; // for reset
    private double scale = 1.0;
    private Rectangle selection;
    private Point dragStart;
    private StatusBar statusBar;

    CanvasPanel() {
        setOpaque(true);
        setBackground(new Color(45, 45, 45));

        // Mouse interaction
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
                    selection = rect(dragStart, e.getPoint());
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
                if (e.getWheelRotation() < 0) zoomIn();
                else zoomOut();
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    // --- Image handling ---
    void setStatusBar(StatusBar sb) {
        this.statusBar = sb;
    }

    void setImage(BufferedImage img) {
        this.image = Utils.toARGB(img);
        this.originalImage = Utils.deepCopy(img);
        revalidate();
        repaint();
        updateStatus(null);
    }

    BufferedImage getImage() {
        return image;
    }

    void resetImage() {
        if (originalImage != null) {
            this.image = Utils.deepCopy(originalImage);
            selection = null;
            scale = 1.0;
            revalidate();
            repaint();
            updateStatus(null);
        }
    }

    // --- Zoom ---
    void zoomIn() {
        setScale(scale * 1.1);
    }

    void zoomOut() {
        setScale(scale / 1.1);
    }

    void zoomToFit() {
        if (image == null || getParent() == null) return;
        Dimension vp = (getParent() instanceof JViewport)
                ? ((JViewport) getParent()).getExtentSize()
                : getSize();
        if (vp.width == 0 || vp.height == 0) return;
        double sx = vp.width / (double) image.getWidth();
        double sy = vp.height / (double) image.getHeight();
        setScale(Math.max(0.05, Math.min(sx, sy)));
    }

    private void setScale(double s) {
        s = Math.max(0.05, Math.min(10, s));
        if (Math.abs(s - scale) > 1e-6) {
            scale = s;
            revalidate();
            repaint();
            updateStatus(null);
        }
    }

    // --- Component sizing ---
    @Override
    public Dimension getPreferredSize() {
        return image == null
                ? new Dimension(800, 600)
                : new Dimension(
                (int) Math.round(image.getWidth() * scale),
                (int) Math.round(image.getHeight() * scale)
        );
    }

    // --- Painting ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        paintChecker(g2, getWidth(), getHeight());

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

    // --- Helpers ---
    private Rectangle rect(Point p1, Point p2) {
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

    private void paintChecker(Graphics2D g, int w, int h) {
        int size = 10;
        Color c1 = new Color(200, 200, 200);
        Color c2 = new Color(150, 150, 150);
        for (int y = 0; y < h; y += size) {
            for (int x = 0; x < w; x += size) {
                g.setColor(((x / size + y / size) % 2 == 0) ? c1 : c2);
                g.fillRect(x, y, size, size);
            }
        }
    }

    // Add to CanvasPanel.java
    Rectangle getSelectionImageSpace() {
        if (selection == null || image == null) return null;
        int x = (int) (selection.x / scale);
        int y = (int) (selection.y / scale);
        int w = (int) (selection.width / scale);
        int h = (int) (selection.height / scale);

        // Clamp to image bounds
        x = Math.max(0, x);
        y = Math.max(0, y);
        if (x + w > image.getWidth()) w = image.getWidth() - x;
        if (y + h > image.getHeight()) h = image.getHeight() - y;

        return (w > 0 && h > 0) ? new Rectangle(x, y, w, h) : null;
    }

    void clearSelection() {
        selection = null;
        repaint();
    }

}
