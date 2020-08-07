package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;
import com.github.manolo8.darkbot.gui.tree.components.JCheckboxListField;
import com.github.manolo8.darkbot.gui.tree.components.JListField;

import java.util.EnumSet;
import java.util.Set;

public class OreTraderConfig {
    @Option(value = "Sell map", description = "goes to this map to sell resources")
    @Editor(JListField.class)
    @Options(Maps.class)
    public int SELL_MAP_INDEX;

    @Option(value = "Sell config", description = "changes to this config when selling")
    public Config.ShipConfig SELL_CONFIG;

    @Option(value = "Finish current target before selling", description = "will kill current target before travelling to base to sell")
    public boolean FINISH_TARGET_BEFORE_SELLING;

    @Option(value = "Resources to sell", description = "will only sell these selected resources")
    @Editor(JCheckboxListField.class)
    @Options(Ores.class)
    public Set<OreTradeGui.Ore> TOGGLE;

    public OreTraderConfig() {
        this.SELL_MAP_INDEX = 0;
        this.SELL_CONFIG    = new Config.ShipConfig(2, '9');
        this.FINISH_TARGET_BEFORE_SELLING = false;
        this.TOGGLE         = EnumSet.of(
                OreTradeGui.Ore.PROMETIUM,
                OreTradeGui.Ore.ENDRIUM,
                OreTradeGui.Ore.TERBIUM,
                OreTradeGui.Ore.PROMETID,
                OreTradeGui.Ore.DURANIUM,
                OreTradeGui.Ore.PROMERIUM,
                OreTradeGui.Ore.SEPROM,
                OreTradeGui.Ore.PALLADIUM,
                OreTradeGui.Ore.OSMIUM);
    }
}
