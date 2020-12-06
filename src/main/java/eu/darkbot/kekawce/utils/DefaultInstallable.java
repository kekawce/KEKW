package eu.darkbot.kekawce.utils;

import com.github.manolo8.darkbot.Main;

import java.util.Arrays;

public class DefaultInstallable {
    private static String VERSION = null;

    public static <T> boolean cantInstall(Main main, T feature) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), feature.getClass().getSigners()))
            return true;
        if (!VerifierChecker.getAuthApi().requireDonor()) return true;

        if (VERSION != null) return false;
        VERSION = main.featureRegistry
                .getFeatureDefinition(feature)
                .getPlugin()
                .getDefinition()
                .version.toString();
        return false;
    }

}
