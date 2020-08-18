package eu.darkbot.kekawce.updater;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Changelog extends JPanel {
    public static int MAX_HEIGHT = 300;

    Changelog(List<Remote.Version> versions) {

        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 0.5; c.weighty = 0.5;

        JLabel label = null;
        JTextArea textArea = null;

        int texty = 0;
        int labely = 0;
        for (Remote.Version version : versions) {

            label = new JLabel(version.version);
            label.setFont(new Font("sans", Font.BOLD, 18));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0; c.gridy = labely++; texty++;
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.insets = new Insets(0, 0, 10, 0);
            contentPanel.add(label, c);

            for (Remote.Version.Message message : version.changelog) {

                label = new JTag(preprocessTag(message.title.text), getColor(message.title.color));
                label.setFont(new Font("sans", Font.BOLD, 16));
                label.setForeground(Color.WHITE);
                c.fill = GridBagConstraints.NONE;
                c.gridx = 0;
                c.gridy = labely++;
                c.anchor = GridBagConstraints.FIRST_LINE_END;
                c.insets = new Insets(0, 20, 0, 0);
                contentPanel.add(label, c);

                textArea = new JTextArea();
                textArea.setFont(new Font("sans", Font.PLAIN, 16));
                textArea.setOpaque(false);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setColumns(30);
                textArea.setText("• " + String.join("\n• ", message.body) + "\n");
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 1;
                c.gridy = texty++;
                c.insets = new Insets(0, 20, 0, 0);
                c.anchor = GridBagConstraints.CENTER;
                contentPanel.add(textArea, c);
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, MAX_HEIGHT));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(0);
                if (scrollPane.getHeight() > MAX_HEIGHT)
                    scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth(), MAX_HEIGHT));
            }
        });

        add(scrollPane);

    }

    private static String preprocessTag(String s) {
        return " " + s + " ";
    }

    private static Color getColor(String s) {
        switch (s) {
            case "blue": return new Color(34, 0, 224);
            case "purple": return new Color(137, 71, 235);
            case "green": return new Color(43, 174, 102);
            case "red": return new Color(237, 28, 36);
            case "gray":
            default: return new Color(134, 153, 172);
        }
    }
}