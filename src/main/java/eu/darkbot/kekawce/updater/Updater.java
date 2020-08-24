package eu.darkbot.kekawce.updater;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.utils.Popups;
import eu.darkbot.kekawce.ImageUtils;
import eu.darkbot.kekawce.Version;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.manolo8.darkbot.Main.GSON;

public class Updater {

    static SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM, hh:mm a");
    static Date lastChecked;

    static boolean HAS_ERROR = false;

    // Edit github gist:      https://gist.github.com/
    // Permalink github gist: https://gist.github.com/atenni/5604615
    private static String GITHUB_GIST_URL = "https://gist.github.com/kekawce/0c7f520595aca02583a76dec53d3bb0b/raw/remote.json";
    // for testing uses
    //private static String GITHUB_GIST_URL = "https://gist.github.com/kekawce/0c7f520595aca02583a76dec53d3bb0b/raw/remotedevelopment.json";

    static Remote remote;

    public static synchronized void checkUpdate(Main main, FeatureDefinition<?> feature, boolean show) {
        lastChecked = new Date();

        if (remote == null && (remote = getRemote()).download != null &&
                feature != null && !remote.latest.equals(Version.getVersion(feature))) {
            if (show) UpdateGui.show(main, remote);
        }
    }

    static Remote getRemote() {
        try (InputStreamReader in = new InputStreamReader(new URL(GITHUB_GIST_URL).openStream())) {
            HAS_ERROR = false;
            return GSON.fromJson(in, Remote.class);
        } catch (IOException e) {
            HAS_ERROR = true;
            e.printStackTrace();
            return new Remote();
        }
    }

    static boolean showErrorMessage() {
        if (!HAS_ERROR) return false;

        Popups.showMessageSync("KEKW",
                new JOptionPane("No internet",
                        JOptionPane.ERROR_MESSAGE),
                jDialog -> ImageUtils.setIconImage(jDialog, true));
        return true;
    }

    static void setRemote(Remote r) {
        remote = r;
    }

    public static String getLatestVersion() {
        return remote.latest;
    }
}
