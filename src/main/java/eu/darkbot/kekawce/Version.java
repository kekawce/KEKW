package eu.darkbot.kekawce;

import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;

public class Version {
    public static String VERSION;

    public static <T> String getVersion(FeatureRegistry featureRegistry, T feature) {
        return VERSION = featureRegistry
                .getFeatureDefinition(feature)
                .getPlugin()
                .getDefinition()
                .version.toString();
    }
}
