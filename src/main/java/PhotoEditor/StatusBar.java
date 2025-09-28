package PhotoEditor;

import javax.swing.*;
import java.awt.*;

class StatusBar extends JPanel {
    private final JLabel zoomLabel = new JLabel("100%");
    private final JLabel sizeLabel = new JLabel("–");
    private final JLabel cursorLabel = new JLabel("–");

    StatusBar() {
        setLayout(new GridLayout(1, 0));
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        add(createField("Zoom:", zoomLabel));
        add(createField("Image:", sizeLabel));
        add(createField("Cursor:", cursorLabel));
    }

    private JPanel createField(String name, JLabel value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.add(new JLabel(name));
        panel.add(value);
        return panel;
    }

    void setMessage(String text) {
        cursorLabel.setText(text);
    }

    void update(String zoomText, String sizeText, String cursorText) {
        zoomLabel.setText(zoomText);
        sizeLabel.setText(sizeText);
        cursorLabel.setText(cursorText);
    }

    void setZoom(String zoomText) { zoomLabel.setText(zoomText); }
    void setSize(String sizeText) { sizeLabel.setText(sizeText); }
    void setCursor(String cursorText) { cursorLabel.setText(cursorText); }
}
