package eu.darkbot.kekawce.updater;

import javax.swing.*;
import java.awt.*;

public class JTag extends JLabel {
    private Color color;

    public JTag() {
        this("", Color.BLACK);
    }

    public JTag(String text) {
        this(text, Color.BLACK);
    }

    public JTag(String text, Color color) {
        super(text);
        this.color = color;
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setColor(color);

        int arcw = 10, arch = 10;
        g2d.drawRoundRect(0, 0, getWidth(), getHeight(), arcw, arch);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arcw, arch);

        super.paintComponent(g2d);
        g2d.dispose();
    }
}