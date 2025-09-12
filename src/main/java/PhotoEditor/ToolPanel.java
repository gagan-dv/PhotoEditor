package PhotoEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


class ToolPanel extends JPanel {

    ToolPanel(CanvasPanel canvas, HistoryManager history, EditorFrame frame) {
        setLayout(new GridLayout(0, 1, 6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // --- Editing tools ---
        add(createButton("Crop", () -> cropImage(canvas, history)));
        add(createButton("Rotate 90°", () -> apply(canvas, history, img -> Filters.rotate(img, Math.PI / 2))));
        add(createButton("Flip H", () -> apply(canvas, history, Filters::flipHorizontal)));
        add(createButton("Flip V", () -> apply(canvas, history, Filters::flipVertical)));
        add(createButton("Grayscale", () -> apply(canvas, history, Filters::toGrayscale)));
        add(createButton("Sepia", () -> apply(canvas, history, Filters::toSepia)));
        add(createButton("Blur", () -> apply(canvas, history, Filters::blur3x3)));
        add(createButton("Sharpen", () -> apply(canvas, history, Filters::sharpen3x3)));
        add(createButton("Reset", () -> resetImage(canvas, history)));
    }


    private JButton createButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void cropImage(CanvasPanel canvas, HistoryManager history) {
        Rectangle selection = canvas.getSelectionImageSpace();
        if (selection == null) {
            JOptionPane.showMessageDialog(this, "Drag to select an area first.");
            return;
        }
        if (canvas.getImage() == null) return;

        history.push(Utils.deepCopy(canvas.getImage()));
        BufferedImage cropped = canvas.getImage().getSubimage(
                selection.x, selection.y, selection.width, selection.height
        );
        canvas.setImage(Utils.toARGB(cropped));
        canvas.clearSelection();
        canvas.zoomToFit();
    }

    private void resetImage(CanvasPanel canvas, HistoryManager history) {
        BufferedImage base = history.resetToFirst();
        if (base != null) {
            canvas.setImage(base);
        }
    }

    private void apply(CanvasPanel canvas, HistoryManager history, Filters.Op operation) {
        if (canvas.getImage() == null) return;
        history.push(Utils.deepCopy(canvas.getImage()));
        canvas.setImage(operation.apply(canvas.getImage()));
    }
}
