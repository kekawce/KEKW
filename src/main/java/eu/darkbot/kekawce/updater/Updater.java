package eu.darkbot.kekawce.updater;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.kekawce.Version;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static com.github.manolo8.darkbot.Main.GSON;

public class Updater {

    // Edit github gist:      https://gist.github.com/
    // Permalink github gist: https://gist.github.com/atenni/5604615
    private static String GITHUB_GIST_URL = "https://gist.github.com/kekawce/0c7f520595aca02583a76dec53d3bb0b/raw/remote.json";

    private static Remote remote;

    private boolean checked = false; // FIXME ADD UPDATE CHECKER

    public static synchronized void checkUpdate(Main main, FeatureDefinition<?> feature) {
        if ((remote = getRemote()).download != null &&
                !remote.latest.equals(Version.getVersion(feature)))
            UpdateGui.show(main, remote);
    }

    private static Remote getRemote() {
        try (InputStreamReader in = new InputStreamReader(new URL(GITHUB_GIST_URL).openStream())) {
            return GSON.fromJson(in, Remote.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new Remote();
        }
    }
}
