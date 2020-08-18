package eu.darkbot.kekawce.updater;

import java.util.List;

class Remote {
    String latest, download;

    Remote(String latest, String download) {
        this.latest = latest;
        this.download = download;
    }

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
