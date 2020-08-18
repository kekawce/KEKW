package eu.darkbot.kekawce;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;

public class Version {
    public static String VERSION;

    public static String getVersion(FeatureDefinition<?> feature) {

        return VERSION = feature
                .getPlugin()
                .getDefinition()
                .version.toString();
    }
}
