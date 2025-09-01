package PhotoEditor;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for adjusting brightness, contrast, and saturation.
 */
class PropertiesPanel extends JPanel {

    private final JSlider brightnessSlider = createSlider(-100, 100, 0, "Brightness");
    private final JSlider contrastSlider   = createSlider(-100, 100, 0, "Contrast");
    private final JSlider saturationSlider = createSlider(-100, 100, 0, "Saturation");

    PropertiesPanel(CanvasPanel canvas, HistoryManager history) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // --- Sliders panel ---
        JPanel sliders = new JPanel(new GridLayout(0, 1, 6, 6));
        sliders.add(brightnessSlider);
        sliders.add(contrastSlider);
        sliders.add(saturationSlider);
        add(sliders, BorderLayout.CENTER);

        // --- Buttons panel ---
        JButton applyButton = new JButton("Apply");
        JButton resetButton = new JButton("Reset sliders");

        JPanel actions = new JPanel(new GridLayout(1, 0, 6, 6));
        actions.add(applyButton);
        actions.add(resetButton);
        add(actions, BorderLayout.SOUTH);

        // --- Event listeners ---
        applyButton.addActionListener(e -> applyAdjustments(canvas, history));
        resetButton.addActionListener(e -> resetSliders());
    }

    // --- Helpers ---
    private void applyAdjustments(CanvasPanel canvas, HistoryManager history) {
        if (canvas.getImage() == null) return;
        history.push(Utils.deepCopy(canvas.getImage()));
        canvas.setImage(Filters.adjust(
                canvas.getImage(),
                brightnessSlider.getValue(),
                contrastSlider.getValue(),
                saturationSlider.getValue()
        ));
    }

    private void resetSliders() {
        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);
        saturationSlider.setValue(0);
    }

    private static JSlider createSlider(int min, int max, int initial, String title) {
        JSlider slider = new JSlider(min, max, initial);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing((max - min) / 4);
        slider.setBorder(BorderFactory.createTitledBorder(title));
        return slider;
    }
}
