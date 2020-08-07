package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;

import java.util.Arrays;
import java.util.List;

public class ZetaWaves extends OptionList<Integer> {
    public static final List<String> ZETA_WAVES = Arrays.asList("Infernal", "Scorcher", "Streuner", "Lordakia", "Saimon", "Sibelonit", "Kristallin", "1 Kristallin left");

    public Integer getValue(String text) {
        return ZETA_WAVES.indexOf(text);
    }
    public String getText(Integer value) {
        return (String)ZETA_WAVES.get(value);
    }
    public List<String> getOptions() {
        return ZETA_WAVES;
    }
}