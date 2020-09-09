package eu.darkbot.kekawce;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.kekawce.updater.Updater;

public class Version {
    public static String VERSION;

    public static String getVersion(FeatureDefinition<?> feature) {
        return VERSION = feature
                .getPlugin()
                .getDefinition()
                .version.toString();
    }

    public static boolean isLatestVersion() {
        return VERSION.equals(Updater.getLatestVersion());
    }

    public static String fullname() {
        return "KEKW v" + VERSION;
    }
}
