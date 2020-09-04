package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.tree.components.JListField;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChrominFarmerConfig {

    @Option(value = "Enable feature", description = "check this to enable this feature/plugin")
    public boolean ENABLE_FEATURE = false;

    @Option(value = "", description = "click to show stats")
    @Editor(value = JStatsComponent.class, shared = true)
    public transient Lazy<String> STATUS_UPDATE;
    public transient Map<String, Integer> STATS_INFO;
    public transient Lazy<String> STATS_INFO_UPDATE;
    public final Object lock = new Object();

    @Option(value = "Buy lives", description = "how many additional lives to buy for each zeta gate")
    @Num(min = 0, max = 100, step = 1)
    public int BUY_LIVES;

    @Option(value = "Suicides on this wave", description = "bot will suicide either on the 1st or 2nd devourer wave")
    @Editor(JListField.class)
    @Options(Waves.class)
    public int ZETA_WAVE = 0;

    @Option(value = "Suicides on this sub-wave", description = "bot will suicide on this sub-wave")
    @Editor(JListField.class)
    @Options(SubWaves.class)
    public String ZETA_SUB_WAVE = "All npcs gone (only devourer left)";

    @Option(value = "Collect", description = "")
    public Collector COLLECTOR;

//    @Option(value = "Location Info.", description = "")
//    public ChrominFarmerConfig.LocationInfo LOCATION_INFO = new ChrominFarmerConfig.LocationInfo();;

    public ChrominFarmerConfig() {
        this.BUY_LIVES = 0;
        this.COLLECTOR = new ChrominFarmerConfig.Collector();
        this.STATUS_UPDATE = new Lazy.NoCache();
        this.STATS_INFO = new LinkedHashMap<String, Integer>() {{
            put("Lives Left", -2);
            put("Life Price", -2);
            put("Lives Bought", -2);
            put("Total Chromin", -2);
            put("Chromin Gained", -2);
            put("Chromin Per Hr", -2);
        }};
        this.STATS_INFO_UPDATE = new Lazy.NoCache();
    }

    public static class Collector {
        @Option(value = "Use the pet to collect chromin boxes", description = "Will wait for pet to pick up any chromin box")
        public boolean PET_BOX_COLLECTING_ONLY;

        @Option(value = "Ammo key", description = "Presses this key to stop shooting when collecting")
        public Character AMMO_KEY;

        @Option(value = "Collecting config", description = "Will change to this config when collecting boxes")
        public Config.ShipConfig COLLECT_CONFIG;

        @Option(value = "Suiciding config", description = "Will change to this config when suiciding")
        public Config.ShipConfig SUICIDE_CONFIG;

        public Collector() {
            this.PET_BOX_COLLECTING_ONLY = true;
            this.AMMO_KEY = '1';
            this.COLLECT_CONFIG = new Config.ShipConfig(2, '9');
            this.SUICIDE_CONFIG = new Config.ShipConfig(2, '9');
        }
    }

    public static class JStatsComponent extends JPanel implements OptionEditor {
        private static final int WIDTH = 210;
        private static final int HEIGHT = 16; // height for 1 line of text

        private JLabel statusField;
        private Map<String, JLabel> statsInfo;

        public JStatsComponent(ChrominFarmerConfig config) {
            this.setOpaque(false);
            this.setPreferredSize(new Dimension(WIDTH,7 * HEIGHT));
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            statusField = new JLabel();
            statusField.setText("Click here to update stats");
            this.add(statusField);
            config.STATUS_UPDATE.add(info -> SwingUtilities.invokeLater(() -> statusField.setText("Status: " + info)));

            statsInfo = new LinkedHashMap();
            for (Map.Entry<String, Integer> e : config.STATS_INFO.entrySet()) {
                JLabel tmpLabel = new JLabel();
                String val = e.getValue() == -2 ? "-" : String.format("%,d", e.getValue());
                tmpLabel.setText(e.getKey() + ": " + val);
                statsInfo.put(e.getKey(), tmpLabel);

                config.STATS_INFO_UPDATE.add(key -> {
                    if (!config.STATS_INFO.containsKey(key)) return;

                    synchronized (config.lock) {
                        JLabel label = this.statsInfo.get(key);
                        int value = config.STATS_INFO.get(key);
                        String stringVal = String.format("%,d", value);
                        SwingUtilities.invokeLater(() -> label.setText(key + ": " + stringVal));
                    }
                });

                this.add(tmpLabel);
            }
        }

        @Override
        public javax.swing.JComponent getComponent() {
            return this;
        }

        @Override
        public void edit(ConfigField configField) { }
    }

    public static class LocationInfo {
        @Option(value = "Enable location info stats", description = "shows the location of your ship and all chromin boxes")
        public boolean SHOW_STATS;

        @Option(value = "", description = "click to show location stats")
        @Editor(value = LocationInfo.JLocationStatsComponent.class, shared = true)
        public transient Lazy<String> STATUS_UPDATE;
        public transient Lazy<String> HERO_POS_UPDATE;
        public transient Lazy<String> LOCATION_UPDATE;

        public LocationInfo() {
            this.SHOW_STATS = false;
            this.STATUS_UPDATE = new Lazy.NoCache();
            this.HERO_POS_UPDATE = new Lazy.NoCache();
            this.LOCATION_UPDATE = new Lazy.NoCache();
        }

        public static class JLocationStatsComponent extends JPanel implements OptionEditor {
            private static final int WIDTH = 310;
            private static final int HEIGHT = 16; // height for 1 line of text

            public JLocationStatsComponent(ChrominFarmerConfig.LocationInfo config) {
                this.setOpaque(false);
                this.setPreferredSize(new Dimension(WIDTH,5 * HEIGHT));
                this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                JLabel statusField = new JLabel();
                statusField.setText("Click here to update stats");
                statusField.setAlignmentX(Component.LEFT_ALIGNMENT);
                this.add(statusField);
                config.STATUS_UPDATE.add(info -> SwingUtilities.invokeLater(() -> statusField.setText("Status: " + info)));

                JLabel heroPos = new JLabel();
                heroPos.setText("Current hero pos:   -");
                heroPos.setAlignmentX(Component.LEFT_ALIGNMENT);
                this.add(heroPos);
                config.HERO_POS_UPDATE.add(info -> SwingUtilities.invokeLater(() -> heroPos.setText("Current hero pos: " + info)));

                JTextArea boxLocations = new JTextArea();
                boxLocations.setLineWrap(true);
                boxLocations.setWrapStyleWord(true);
                boxLocations.setEditable(false);
                boxLocations.setOpaque(false);
                boxLocations.setText("Box Locations:   -");
                boxLocations.setAlignmentX(Component.LEFT_ALIGNMENT);
                this.add(new JScrollPane(boxLocations));
                config.LOCATION_UPDATE.add(info -> SwingUtilities.invokeLater(() -> boxLocations.setText("Box Location(s): " + info)));
            }

            @Override
            public JComponent getComponent() { return this; }

            @Override
            public void edit(ConfigField configField) { }
        }
    }
}
