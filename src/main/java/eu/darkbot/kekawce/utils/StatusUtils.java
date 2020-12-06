package eu.darkbot.kekawce.utils;

public class StatusUtils {
    public static String status(String... arguments) {
        String version = "KEKW v%s" + (arguments.length > 0 ? " | " : "");
        return version + String.join(" | ", arguments);
    }
}
