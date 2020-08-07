package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class Ores extends OptionList<OreTradeGui.Ore> {
    public static final List<OreTradeGui.Ore> ORES = Arrays.asList(OreTradeGui.Ore.values());
    private static final List<String> OPTIONS = EnumSet.allOf(OreTradeGui.Ore.class).stream()
            .map(e -> e.name().toLowerCase()).collect(Collectors.toList());

    @Override
    public OreTradeGui.Ore getValue(String text) { return ORES.get(OPTIONS.indexOf(text)); }
    @Override
    public String getText(OreTradeGui.Ore ore) { return ore.name().toLowerCase(); }
    @Override
    public List<String> getOptions() {
        return OPTIONS;
    }
}
