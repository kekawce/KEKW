package eu.darkbot.kekawce.updater;

import java.util.List;

class Remote {
    String latest, download = null;
    List<Version> versions;

    // https://gitversion.readthedocs.io/en/latest/input/docs/reference/intro-to-semver/
    // version: {major}.{minor}.{patch}-{tag}+{buildmetadata}
    static class Version {
        String version;
        List<Message> changelog;

        static class Message {
            Title title;
            List<String> body;

            static class Title {
                String color, text;

                // blue   Hex: #FF2200E0
                // purple Hex: #FF8947EB
                // green  Hex: #FF2BAE66
                // red    Hex: #FFED1C24
                // gray   Hex: #FF8699AC
            }
        }
    }
}
