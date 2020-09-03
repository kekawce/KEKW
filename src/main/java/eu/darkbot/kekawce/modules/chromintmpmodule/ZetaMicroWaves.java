package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;

import java.util.Arrays;
import java.util.List;

public class ZetaMicroWaves extends OptionList<String> {
    static final List<String> ZETA_WAVE_1 = Arrays.asList("Infernal", "All npcs gone (only devourer left)");
    static final List<String> ZETA_WAVE_2 = Arrays.asList(
            "Infernal",
            "Scorcher",
            "Streuner",
            "Lordakia",
            "Saimon",
            "Sibelonit",
            "Kristallin",
            "All npcs gone (only devourer left)");

    public static List<String> ZETA_WAVES = ZetaMacroWave.INDEX == 0 ? ZETA_WAVE_1 : ZETA_WAVE_2;

    @Override
    public String getValue(String text) {
        System.out.println("getValue: " + text);
        return text;
    }

    @Override
    public String getText(String value) {
        System.out.println("getText: " + value);
        return ZETA_WAVES.contains(value) ? value : null;
    }
    @Override
    public List<String> getOptions() {
        System.out.println("getOptions");
        return ZETA_WAVES = ZetaMacroWave.INDEX == 0 ? ZETA_WAVE_1 : ZETA_WAVE_2;
    }

//    public Integer getValue(String text) {
//        return ZETA_WAVES.indexOf(text);
//    }
//    public String getText(Integer value) {
//        return (String)ZETA_WAVES.get(value);
//    }
//    public List<String> getOptions() {
//        return ZETA_WAVES;
//    }
}