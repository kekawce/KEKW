package eu.darkbot.kekawce;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Installable;
import eu.darkbot.kekawce.updater.Updater;

import java.util.function.Consumer;

public interface DefaultInstallable extends Installable {
    @Override
    default void install(Main main) throws AuthenticationException {
        if (!VerifierChecker.getAuthApi().requireDonor()) throw new AuthenticationException();
        Updater.checkUpdate(main, main.featureRegistry.getFeatureDefinition(this), !main.isRunning());
    }

    class Install {
        public static boolean install(Main main, Consumer<Main> install) {
            try {
                install.accept(main);
                return true;
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    class AuthenticationException extends RuntimeException { }
}
