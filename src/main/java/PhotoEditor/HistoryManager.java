package PhotoEditor;

import java.awt.image.BufferedImage;
import java.util.Stack;

/**
 * Manages undo/redo history for image edits.
 */
class HistoryManager {
    private final Stack<BufferedImage> undoStack = new Stack<>();
    private final Stack<BufferedImage> redoStack = new Stack<>();

    /**
     * Save a new state into history (push current image before modification).
     */
    void push(BufferedImage img) {
        if (img != null) {
            undoStack.push(Utils.deepCopy(img));
            redoStack.clear();
        }
    }

    /**
     * Undo last action and apply to canvas.
     */
    void undo(CanvasPanel canvas) {
        if (!undoStack.isEmpty() && canvas.getImage() != null) {
            redoStack.push(Utils.deepCopy(canvas.getImage()));
            BufferedImage prev = undoStack.pop();
            canvas.setImage(Utils.deepCopy(prev));
        }
    }

    /**
     * Redo last undone action and apply to canvas.
     */
    void redo(CanvasPanel canvas) {
        if (!redoStack.isEmpty() && canvas.getImage() != null) {
            undoStack.push(Utils.deepCopy(canvas.getImage()));
            BufferedImage next = redoStack.pop();
            canvas.setImage(Utils.deepCopy(next));
        }
    }

    /**
     * Reset to the very first image (initial state).
     */
    BufferedImage resetToFirst() {
        if (undoStack.isEmpty()) return null;
        return Utils.deepCopy(undoStack.firstElement());
    }

    /**
     * Clears history.
     */
    void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
