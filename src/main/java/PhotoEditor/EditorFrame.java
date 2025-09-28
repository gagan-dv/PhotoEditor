package PhotoEditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

class EditorFrame extends JFrame {
    private final CanvasPanel canvas = new CanvasPanel();
    private final HistoryManager history = new HistoryManager();
    private final StatusBar statusBar = new StatusBar();
    private final JFileChooser chooser = new JFileChooser();

    EditorFrame() {
        super("Mini Photoshop – Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setLocationByPlatform(true);

        chooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files", "png", "jpg", "jpeg", "bmp", "gif"));

        setLayout(new BorderLayout());
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        add(new ToolPanel(canvas, history, this), BorderLayout.WEST);
        add(new PropertiesPanel(canvas, history), BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        canvas.setStatusBar(statusBar);
        setJMenuBar(buildMenuBar());
    }

    private void doOpen() {
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                BufferedImage img = ImageIO.read(file);
                if (img == null) {
                    JOptionPane.showMessageDialog(this, "Unsupported image format.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                canvas.setImage(img);
                history.clear();
                history.push(Utils.deepCopy(img));
                setTitle("Mini Photoshop – " + file.getName());
            } catch (Exception ex) {
                showError("Failed to open image: " + ex.getMessage(), ex);
            }
        }
    }

    private void doSaveAs() {
        if (canvas.getImage() == null) {
            JOptionPane.showMessageDialog(this, "No image to save.");
            return;
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                String ext = detectExtension(file.getName());
                ImageIO.write(canvas.getImage(), ext, file);
                setTitle("Mini Photoshop – " + file.getName());
            } catch (Exception ex) {
                showError("Failed to save image: " + ex.getMessage(), ex);
            }
        }
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        JMenuItem open = new JMenuItem("Open…");
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        open.addActionListener(e -> doOpen());

        JMenuItem saveAs = new JMenuItem("Save As…");
        saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveAs.addActionListener(e -> doSaveAs());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());

        fileMenu.add(open);
        fileMenu.add(saveAs);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem undo = new JMenuItem("Undo");
        undo.addActionListener(e -> history.undo(canvas));

        JMenuItem redo = new JMenuItem("Redo");
        redo.addActionListener(e -> history.redo(canvas));

        JMenuItem reset = new JMenuItem("Reset");
        reset.addActionListener(e -> {
            BufferedImage base = history.resetToFirst();
            if (base != null) canvas.setImage(base);
        });

        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.addSeparator();
        editMenu.add(reset);

        bar.add(fileMenu);
        bar.add(editMenu);
        return bar;
    }

    private String detectExtension(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpg";
        if (lower.endsWith(".bmp")) return "bmp";
        if (lower.endsWith(".gif")) return "gif";
        return "png";
    }

    private void showError(String msg, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
