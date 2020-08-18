package eu.darkbot.kekawce.updater;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.http.Http;
import eu.darkbot.kekawce.Version;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class UpdateGui {
    static void show(Main main, Remote remote) {
        List<Remote.Version> versions = remote.versions;

        JPanel content = new UpdatePanel(Version.VERSION, remote.latest);

        JButton updateButton = new JButton("Install Update"),
                skipButton = new JButton("Skip This Version"),
                changelogButton = new JButton("See What's New");

        Image icon = getIconImage();
        updateButton.addActionListener(l -> {
            try (InputStream in = Http.create(remote.download).getInputStream()) {
                if (Files.notExists(PluginHandler.PLUGIN_UPDATE_PATH))
                    Files.createDirectory(PluginHandler.PLUGIN_UPDATE_PATH);

                Files.copy(in, PluginHandler.PLUGIN_UPDATE_PATH.resolve("KEKW.jar"), StandardCopyOption.REPLACE_EXISTING);

                Popups.showMessageSync("KEKW",
                        new JOptionPane("Update completed",
                                JOptionPane.INFORMATION_MESSAGE),
                                jDialog -> {
                                    if (icon == null) return;

                                    SwingUtilities.invokeLater(() -> jDialog.setIconImage(icon));
                                });
                main.pluginHandler.updatePlugins();
            } catch (IOException e) {
                e.printStackTrace();
                Popups.showMessageSync("KEKW",
                        new JOptionPane("Update failed",
                                JOptionPane.ERROR_MESSAGE),
                                jDialog -> {
                                    if (icon == null) return;

                                    SwingUtilities.invokeLater(() -> jDialog.setIconImage(icon));
                                });
            }
            SwingUtilities.getWindowAncestor(updateButton).setVisible(false);
        });

        skipButton.addActionListener(l -> SwingUtilities.getWindowAncestor(skipButton).setVisible(false));

        changelogButton.addActionListener(l -> {
            JOptionPane changelogPane = new JOptionPane(new Changelog(versions), JOptionPane.PLAIN_MESSAGE);
            Popups.showMessageSync("Release Notes", changelogPane, jDialog -> SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (jDialog.getHeight() > Changelog.MAX_HEIGHT)
                        jDialog.setSize(new Dimension(jDialog.getWidth(), Changelog.MAX_HEIGHT + 100));

                    jDialog.setLocationRelativeTo(null);

                    if (icon != null) jDialog.setIconImage(icon);
                }
            }));
        });

        JOptionPane pane = new JOptionPane(content,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{changelogButton, skipButton, updateButton}, updateButton);

        Popups.showMessageSync("Update Available", pane, jDialog -> {
            if (icon == null) return;

            SwingUtilities.invokeLater(() -> jDialog.setIconImage(icon));
        });
    }

    private static Image getIconImage() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://cdn.discordapp.com/emojis/641661470156914698.png?v=1").openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            return ImageIO.read(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
