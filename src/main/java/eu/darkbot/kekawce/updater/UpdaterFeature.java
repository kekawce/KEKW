package eu.darkbot.kekawce.updater;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.Popups;
import eu.darkbot.kekawce.DefaultInstallable;
import eu.darkbot.kekawce.ImageUtils;
import eu.darkbot.kekawce.Version;

import javax.swing.*;
import java.awt.*;

@Feature(name = "KEKW - Check for updates", description = "Check for updates for KEKW plugin here", enabledByDefault = true)
public class UpdaterFeature implements DefaultInstallable, Task, Configurable<UpdaterFeature.Config> {

    private enum UpdateStatus {
        UP_TO_DATE("You're up to date", ImageUtils.CHECKMARK_IMG),
        LOADING(" Loading ... ", ImageUtils.LOADING_GIF),
        OUT_OF_DATE("You're out of date!", ImageUtils.WARNING_IMG),
        ERROR("Connection error", ImageUtils.NO_CONNECTION_IMG);

        private String text;
        private ImageIcon icon;

        UpdateStatus(String text, ImageIcon icon) {
            this.text = text;
            this.icon = icon;
        }
    }

    private static Main main;
    private static UpdateStatus updateStatus;
    private static FeatureDefinition<?> feature;

    @Override
    public void install(Main main) {
        if (!DefaultInstallable.Install.install(main, DefaultInstallable.super::install))
            return;

        this.main = main;
        this.feature = main.featureRegistry.getFeatureDefinition(this);
        this.updateStatus = getUpdateStatus();
    }

    @Override
    public void setConfig(Config config) {
    }

    @Override
    public void tick() { }

    private static UpdateStatus getUpdateStatus() {
        try {
            if (!ImageUtils.areAllImagesLoaded())
                return UpdateStatus.ERROR;
            return Version.isLatestVersion() ? UpdateStatus.UP_TO_DATE : UpdateStatus.OUT_OF_DATE;
        } catch (Exception e) {
            return UpdateStatus.ERROR;
        }
    }

    public static class Config {
        @Option()
        @Editor(value = Config.Panel.class, shared = true)
        public transient Lazy<ImageIcon> ICON = new Lazy.NoCache<>();
        public transient Lazy<String> STATUS = new Lazy.NoCache<>();

        public static class Panel extends JPanel implements OptionEditor {

            private JLabel icon, status;

            public Panel(Config config) {
                super(new GridBagLayout());

                GridBagConstraints c = new GridBagConstraints();
                c.weightx = 0.5; c.weighty = 0.5;

                // image label
                icon = new JLabel(updateStatus.icon);
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 0; c.gridy = 0;
                add(icon, c);

                // status label + last checked label
                String status = "<html><span style=\"font-weight:bold;font-size:22px;\">" +
                        updateStatus.text + "<br></span>" +
                        "<span style=\"font-size:18px;\">" +
                        (updateStatus == UpdateStatus.ERROR
                            ? "Check that you have internet connection<br> and reload plugins"
                            : "Last checked: " + Updater.formatter.format(Updater.lastChecked)) +
                        "</span></html>";
                this.status = new JLabel(status);
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 1; c.gridy = 0;
                add(this.status, c);

                // check update button
                JButton button = new JButton("Check for updates");

                button.addActionListener(l -> {

                    updateStatus = UpdateStatus.LOADING;
                    config.ICON.send(updateStatus.icon);
                    config.STATUS.send("<html><span style=\"font-weight:bold;font-size:32px;\">" + updateStatus.text + "</span></html>");

                    Updater.setRemote(null);
                    Updater.checkUpdate(main, feature, true);
                    if (Updater.showErrorMessage() || !ImageUtils.areAllImagesLoaded()) {
                        update(config, UpdateStatus.ERROR,
                                UpdateStatus.ERROR.text,
                                "Check that you have internet connection<br> and reload plugins");
                        return;
                    }

                    if (UpdateGui.SUCCESSFULLY_INSTALLED) {
                        config.STATUS.send("<html><span style=\"font-weight:bold;font-size:22px;\">Successfully installed<br></span>" +
                                "<span style=\"font-size:18px;\">You can close this window now</span></html>");
                    } else if (Version.isLatestVersion()) {
                        update(config, UpdateStatus.UP_TO_DATE);
                        Popups.showMessageSync("KEKW",
                                new JOptionPane("You already have the latest version",
                                        JOptionPane.INFORMATION_MESSAGE),
                                jDialog -> ImageUtils.setIconImage(jDialog, true));
                    } else {
                        update(config, UpdateStatus.OUT_OF_DATE);
                    }
                });

                c.fill = GridBagConstraints.NONE;
                c.gridx = 0; c.gridy = 1;
                c.anchor = GridBagConstraints.LINE_START;
                add(button, c);

                button = new JButton("See release notes");

                button.addActionListener(l -> {
                    if (Updater.showErrorMessage()) return;
                    Changelog.showChangelog();
                });

                c.fill = GridBagConstraints.NONE;
                c.gridx = 1; c.gridy = 1;
                c.anchor = GridBagConstraints.LINE_START;
                add(button, c);

                config.ICON.add(img -> SwingUtilities.invokeLater(() -> this.icon.setIcon(img)));
                config.STATUS.add(info -> SwingUtilities.invokeLater(() -> this.status.setText(info)));
            }

            @Override
            public JComponent getComponent() {
                return this;
            }

            @Override
            public void edit(ConfigField configField) { }

            private void update(Config config, UpdateStatus status) {
                update(config, status, status.text, "Last checked: " + Updater.formatter.format(Updater.lastChecked));
            }

            private void update(Config config, UpdateStatus status, String topMessage, String botMessage) {
                updateStatus = status;
                config.ICON.send(updateStatus.icon);
                config.STATUS.send("<html><span style=\"font-weight:bold;font-size:22px;\">" + topMessage + "<br></span>" +
                        "<span style=\"font-size:18px;\">" + botMessage + "</span></html>");
            }
        }
    }
}
