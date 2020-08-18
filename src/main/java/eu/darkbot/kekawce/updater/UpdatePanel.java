package eu.darkbot.kekawce.updater;

import javax.swing.*;
import java.awt.*;

public class UpdatePanel extends JPanel {

    UpdatePanel(String current, String latest) {
        // main panel containing 2 panels
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        // top panel containing 2 labels
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("New Update is Available");
        JLabel body = new JLabel("A new version of KEKW is available!");
        body.setForeground(new Color(body.getForeground().getRed(),
                body.getForeground().getGreen(),
                body.getForeground().getBlue(),200)); // alpha value bet [0,255] control opacity
        title.setFont(new Font("Mono", Font.BOLD, 22));
        body.setFont(new Font("Mono", Font.PLAIN, 22));

        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        body.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        infoPanel.add(title);
        infoPanel.add(Box.createRigidArea(new Dimension(20,20)));
        infoPanel.add(body);

        // bottom panel containing 2 more panels and a JLabel
        JPanel versionPanel = new JPanel(new FlowLayout());
        versionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // current version panel on left
        JPanel currVersionPanel = new JPanel();
        currVersionPanel.setLayout(new BoxLayout(currVersionPanel, BoxLayout.PAGE_AXIS));

        JLabel currVersionTitle = new JLabel("Current Version:");
        JLabel currVersionBody = new JLabel(current);
        currVersionTitle.setForeground(new Color(currVersionTitle.getForeground().getRed(),
                currVersionTitle.getForeground().getGreen(),
                currVersionTitle.getForeground().getBlue(),100));
        currVersionBody.setForeground(new Color(currVersionBody.getForeground().getRed(),
                currVersionBody.getForeground().getGreen(),
                currVersionBody.getForeground().getBlue(),100));
        currVersionTitle.setFont(new Font("Mono", Font.PLAIN, 14));
        currVersionBody.setFont(new Font("Mono", Font.BOLD, 32));
        currVersionTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        currVersionBody.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        currVersionPanel.add(currVersionTitle);
        currVersionPanel.add(currVersionBody);

        // arrow
        JLabel arrow = new JLabel("→");
        arrow.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        arrow.setFont(new Font("Mono", Font.PLAIN, 32));

        // new version panel on right
        JPanel newVersionPanel = new JPanel();
        newVersionPanel.setLayout(new BoxLayout(newVersionPanel, BoxLayout.PAGE_AXIS));

        JLabel newVersionTitle = new JLabel("New Version:");
        JLabel newVersionBody = new JLabel(latest);
        newVersionTitle.setFont(new Font("Mono", Font.PLAIN, 14));
        newVersionBody.setFont(new Font("Mono", Font.BOLD, 32));
        newVersionTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        newVersionBody.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        newVersionPanel.add(newVersionTitle); newVersionPanel.add(newVersionBody);

        versionPanel.add(currVersionPanel);
        versionPanel.add(Box.createRigidArea(new Dimension(30,30)));
        versionPanel.add(arrow);
        versionPanel.add(Box.createRigidArea(new Dimension(30,30)));
        versionPanel.add(newVersionPanel);

        contentPanel.add(infoPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(30,30)));
        contentPanel.add(versionPanel);

        setBorder(BorderFactory.createEmptyBorder(80, 100, 80, 100));
        add(contentPanel);
    }
}

