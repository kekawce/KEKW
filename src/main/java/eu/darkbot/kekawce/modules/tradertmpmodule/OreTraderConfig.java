package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;
import com.github.manolo8.darkbot.gui.tree.components.JCheckboxListField;
import com.github.manolo8.darkbot.gui.tree.components.JListField;

import java.util.EnumSet;
import java.util.Set;

@SuppressWarnings("CanBeFinal")
public class OreTraderConfig {
    @Option(value = "Enable feature", description = "check this to enable this feature/plugin")
    public boolean ENABLE_FEATURE = false;

    @Option(value = "Sell map", description = "goes to this map to sell resources")
    @Editor(JListField.class)
    @Options(Maps.class)
    public int SELL_MAP_NDX = 0;

    @Option(value = "Sell config", description = "changes to this config when selling")
    public Config.ShipConfig SELL_CONFIG = new Config.ShipConfig(2, '9');

    @Option(value = "Finish current target before selling", description = "will kill current target before travelling to base to sell")
    public boolean FINISH_TARGET_BEFORE_SELLING = false;

    @Option(value = "Resources to sell", description = "will only sell these selected resources")
    @Editor(JCheckboxListField.class)
    @Options(Ores.class)
    public Set<OreTradeGui.Ore> ORES_TO_SELL = EnumSet.allOf(OreTradeGui.Ore.class);

    @Option(value = "Advanced", description = "You can ignore this if you have no issues")
    public Advanced ADVANCED = new Advanced();

    public static class Advanced {
        @SuppressWarnings("DefaultAnnotationParam")
        @Option(
                value = "Wait before starting to sell(ms)",
                description = "This is how long the bot will wait before starting to sell" +
                        "\nIncrease it if you find your ship stuck moving before selling" +
                        "\nOtherwise decrease it if you want to speed up this action"
        )
        @Num(min = 0, max = 5000, step = 100)
        public int SELL_WAIT = 2000;

        @SuppressWarnings("DefaultAnnotationParam")
        @Option(
                value = "Sell delay(ms)",
                description = "This is the delay between selling each resource" +
                        "\nIncrease it if you find that some resources have been skipped during selling" +
                        "\nOtherwise decrease it if you want to speed up this action"
        )
        @Num(min = 0, max = 1000, step = 100)
        public int SELL_DELAY = 300;
    }
}
