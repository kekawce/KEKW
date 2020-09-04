package eu.darkbot.kekawce.updater;

import java.util.List;

class Remote {
    String latest, download = null;
    List<Version> versions;

    // https://gitversion.readthedocs.io/en/latest/input/docs/reference/intro-to-semver/
    // version: {major}.{minor}.{patch}-{tag}+{buildmetadata}
    // TODO: Remember to update version in plugin.json every new update
    static class Version {
        String version;
        List<Message> changelog;

        static class Message {
            Title title;
            List<String> body;

            static class Title {
                String color, text;

                // blue   Hex: #2200E0
                // purple Hex: #8947EB
                // green  Hex: #2BAE66
                // red    Hex: #ED1C24
                // gray   Hex: #8699AC
            }
        }
    }
}
