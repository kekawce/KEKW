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

@SuppressWarnings("CanBeFinal")
public class ChrominFarmerConfig {

    @Option(value = "Enable feature", description = "check this to enable this feature/plugin")
    public boolean ENABLE_FEATURE = false;

    @Option(description = "click to show stats")
    @Editor(value = JStatsComponent.class, shared = true)
    public transient Lazy<String> STATUS_UPDATE = new Lazy.NoCache<>();
    public transient Map<String, Integer> STATS_INFO = new LinkedHashMap<String, Integer>() {{
        put("Lives Left", -2);
        put("Life Price", -2);
        put("Lives Bought", -2);
        put("Total Chromin", -2);
        put("Chromin Gained", -2);
        put("Chromin Per Hr", -2);
    }};
    public transient Lazy<String> STATS_INFO_UPDATE = new Lazy.NoCache<>();
    public final Object lock = new Object();

    @SuppressWarnings("DefaultAnnotationParam")
    @Option(value = "Buy lives", description = "how many additional lives to buy for each zeta gate")
    @Num(min = 0, max = 100, step = 1)
    public int BUY_LIVES = 0;

    @Option(value = "Cost to buy first life",
            description = "how much it costs to buy the first life\n" +
            "for normal players it should be 5000\n" +
            "for premium players it should be 4750\n" +
            "for players with premium and rebate it should be 3500")
    @Num(min = 1, max = 5000, step = 50)
    public int FIRST_LIFE_COST = 5000;

    @Option(value = "Suicide in radiation zone", description = "bot will suicide in radiation zone")
    public boolean SUICIDE_IN_RAD_ZONE = false;

    @Option(value = "Suicides on this wave", description = "bot will suicide either on the 1st or 2nd devourer wave")
    @Editor(JListField.class)
    @Options(Waves.class)
    public int ZETA_WAVE = 0;

    @Option(value = "Suicides on this sub-wave", description = "bot will suicide on this sub-wave")
    @Editor(JListField.class)
    @Options(SubWaves.class)
    public String ZETA_SUB_WAVE = "All npcs gone (only devourer left)";

    @Option(value = "Collect")
    public Collector COLLECTOR = new Collector();

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

        private final JLabel statusField;
        private final Map<String, JLabel> statsInfo = new LinkedHashMap<>();

        private final ChrominFarmerConfig config;

        public JStatsComponent(ChrominFarmerConfig config) {
            this.config = config;

            this.setOpaque(false);
            this.setPreferredSize(new Dimension(WIDTH,7 * HEIGHT));
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            statusField = new JLabel();
            statusField.setText("Click here to update stats");
            this.add(statusField);
            config.STATUS_UPDATE.add(info -> SwingUtilities.invokeLater(() -> statusField.setText("Status: " + info)));

            for (Map.Entry<String, Integer> e : config.STATS_INFO.entrySet()) {
                JLabel tmpLabel = new JLabel();
                String val = e.getValue() == -2 ? "-" : String.format("%,d", e.getValue());
                tmpLabel.setText(e.getKey() + ": " + val);
                statsInfo.put(e.getKey(), tmpLabel);

                config.STATS_INFO_UPDATE.add(this::onKeyReceived);

                this.add(tmpLabel);
            }
        }

        private void onKeyReceived(String key) {
            if (!config.STATS_INFO.containsKey(key)) return;

            synchronized (config.lock) {
                JLabel label = this.statsInfo.get(key);
                int value = config.STATS_INFO.get(key);
                String stringVal = String.format("%,d", value);
                SwingUtilities.invokeLater(() -> label.setText(key + ": " + stringVal));
            }
        }

        @Override
        public javax.swing.JComponent getComponent() {
            return this;
        }

        @Override
        public void edit(ConfigField configField) {
        }
    }

}
