package eu.darkbot.kekawce.updater;

import java.util.List;

class Remote {
    String latest, download = null;
    List<Version> versions;

    Remote() {}
    Remote(String latest, String download, List<Version> versions) {
        this.latest = latest;
        this.download = download;
        this.versions = versions;
    }

    // https://gitversion.readthedocs.io/en/latest/input/docs/reference/intro-to-semver/
    // version: {major}.{minor}.{patch}-{tag}+{buildmetadata}
    static class Version {
        String version;
        List<Message> changelog;

        Version(String version, List<Message> changelog) {
            this.version = version;
            this.changelog = changelog;
        }

        static class Message {
            Title title;
            List<String> body;

            Message(Title title, List<String> body) {
                this.title = title;
                this.body = body;
            }

            static class Title {
                String color, text;

                Title(String color, String text) {
                    this.color = color;
                    this.text = text;
                }
            }
        }
    }
}
