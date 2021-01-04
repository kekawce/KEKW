package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;

import java.util.Arrays;
import java.util.List;

public class SubWaves extends OptionList<String> {
    static final List<String> ZETA_SUB_WAVES_1 = Arrays.asList("Infernal", "All npcs gone (only devourer left)");
    static final List<String> ZETA_SUB_WAVES_2 = Arrays.asList(
            "Infernal",
            "Scorcher",
            "Streuner",
            "Lordakia",
            "Saimon",
            "Sibelonit",
            "Kristallin",
            "All npcs gone (only devourer left)");

    @Override
    public String getValue(String text) {
        return text;
    }

    @Override
    public String getText(String value) {
        return Waves.SUB_WAVES.contains(value) ? value : null;
    }

    @Override
    public List<String> getOptions() {
        return Waves.SUB_WAVES;
    }
}