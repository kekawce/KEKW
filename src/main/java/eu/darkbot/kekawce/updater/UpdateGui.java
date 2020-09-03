package eu.darkbot.kekawce.updater;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.http.Http;
import eu.darkbot.kekawce.ImageUtils;
import eu.darkbot.kekawce.Version;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UpdateGui {

    public static boolean SUCCESSFULLY_INSTALLED = false;

    static void show(Main main, Remote remote) {

        JPanel content = new UpdatePanel(Version.VERSION, remote.latest);

        JButton updateButton = new JButton("Install Update"),
                skipButton = new JButton("Skip This Version"),
                changelogButton = new JButton("See What's New");

        updateButton.addActionListener(l -> {
            try (InputStream in = Http.create(remote.download).getInputStream()) {
                if (Files.notExists(PluginHandler.PLUGIN_UPDATE_PATH))
                    Files.createDirectory(PluginHandler.PLUGIN_UPDATE_PATH);

                Files.copy(in, PluginHandler.PLUGIN_UPDATE_PATH.resolve("KEKW.jar"), StandardCopyOption.REPLACE_EXISTING);

                Popups.showMessageSync("KEKW",
                        new JOptionPane("Update completed",
                                JOptionPane.INFORMATION_MESSAGE),
                                jDialog -> ImageUtils.setIconImage(jDialog, true));
                SUCCESSFULLY_INSTALLED = true;
                main.pluginHandler.updatePlugins();
            } catch (IOException e) {
                e.printStackTrace();
                Popups.showMessageSync("KEKW",
                        new JOptionPane("Update failed",
                                JOptionPane.ERROR_MESSAGE),
                                jDialog -> ImageUtils.setIconImage(jDialog, true));
            }
            SwingUtilities.getWindowAncestor(updateButton).setVisible(false);
        });

        skipButton.addActionListener(l -> SwingUtilities.getWindowAncestor(skipButton).setVisible(false));

        changelogButton.addActionListener(l -> Changelog.showChangelog());

        JOptionPane pane = new JOptionPane(content,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{changelogButton, skipButton, updateButton}, updateButton);
        ((Container)pane.getComponent(1)).setLayout(new GridLayout(1, 3));

        Popups.showMessageSync("Update Available", pane, jDialog -> ImageUtils.setIconImage(jDialog, true));
    }
}
