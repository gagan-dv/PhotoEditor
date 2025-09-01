package PhotoEditor;

import javax.swing.*;

public class PhotoEditorApp{
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new EditorFrame().setVisible(true);
        });
    }
}
