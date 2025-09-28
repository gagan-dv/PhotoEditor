package PhotoEditor;

import java.awt.image.BufferedImage;
import java.util.Stack;

class HistoryManager {
    private final Stack<BufferedImage> undoStack = new Stack<>();
    private final Stack<BufferedImage> redoStack = new Stack<>();

    void push(BufferedImage img) {
        if (img != null) {
            undoStack.push(Utils.deepCopy(img));
            redoStack.clear();
        }
    }

    void undo(CanvasPanel canvas) {
        if (!undoStack.isEmpty() && canvas.getImage() != null) {
            redoStack.push(Utils.deepCopy(canvas.getImage()));
            canvas.setImage(Utils.deepCopy(undoStack.pop()));
        }
    }

    void redo(CanvasPanel canvas) {
        if (!redoStack.isEmpty() && canvas.getImage() != null) {
            undoStack.push(Utils.deepCopy(canvas.getImage()));
            canvas.setImage(Utils.deepCopy(redoStack.pop()));
        }
    }

    BufferedImage resetToFirst() {
        return undoStack.isEmpty() ? null : Utils.deepCopy(undoStack.firstElement());
    }

    void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
