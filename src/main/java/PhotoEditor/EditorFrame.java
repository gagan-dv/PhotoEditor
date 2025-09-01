package PhotoEditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Main window for the Mini Photoshop editor.
 * Handles menus, file operations, and the layout of editor components.
 */
class EditorFrame extends JFrame {

    // Core components
    private final CanvasPanel canvas = new CanvasPanel();
    private final HistoryManager history = new HistoryManager();
    private final StatusBar statusBar = new StatusBar();
    private final JFileChooser chooser = new JFileChooser();

    /**
     * Constructor initializes the editor UI.
     */
    EditorFrame() {
        super("Mini Photoshop – Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setLocationByPlatform(true);

        // Configure file chooser
        chooser.setFileFilter(
                new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "bmp", "gif")
        );

        // Layout
        setLayout(new BorderLayout());
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        add(new ToolPanel(canvas, history, this), BorderLayout.WEST);
        add(new PropertiesPanel(canvas, history), BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        // Link canvas and status bar
        canvas.setStatusBar(statusBar);

        // Build menu bar
        setJMenuBar(buildMenuBar());
    }

    /**
     * Opens an image selected by the user.
     */
    private void doOpen() {
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                BufferedImage img = ImageIO.read(file);

                if (img == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Unsupported image format.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                // Load into canvas and reset history
                canvas.setImage(img);
                history.clear();
                history.push(Utils.deepCopy(img));

                setTitle("Mini Photoshop – " + file.getName());

            } catch (Exception ex) {
                showError("Failed to open image: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Saves the current image to a file chosen by the user.
     */
    private void doSaveAs() {
        if (canvas.getImage() == null) {
            JOptionPane.showMessageDialog(this, "No image to save.");
            return;
        }

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                String ext = detectExtension(file.getName());

                // Save image
                ImageIO.write(canvas.getImage(), ext, file);
                setTitle("Mini Photoshop – " + file.getName());

            } catch (Exception ex) {
                showError("Failed to save image: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Build the application menu bar.
     */
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // --- File menu
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

        // --- Edit menu
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

        // Add menus to bar
        bar.add(fileMenu);
        bar.add(editMenu);

        return bar;
    }

    /**
     * Utility: Detect extension from filename (default: png).
     */
    private String detectExtension(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpg";
        if (lower.endsWith(".bmp")) return "bmp";
        if (lower.endsWith(".gif")) return "gif";
        return "png"; // default
    }

    /**
     * Utility: Show error dialog and log.
     */
    private void showError(String msg, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }


}
