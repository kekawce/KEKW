package eu.darkbot.kekawce;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Installable;
import eu.darkbot.kekawce.updater.Updater;

public interface DefaultInstallable extends Installable {
    @Override
    default void install(Main main) {
        if (!main.isRunning())
            Updater.checkUpdate(main, main.featureRegistry.getFeatureDefinition(this));
    }
}
