package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;

import java.util.Arrays;
import java.util.List;

public class ZetaMacroWave extends OptionList<Integer> {
    public static final List<String> ZETA_WAVES = Arrays.asList("1st Devourer", "2nd Devourer");
    public static int INDEX = 1;
    public static List<String> WAVES = getUpdatedWaves();

    public Integer getValue(String text) {
        return INDEX = ZETA_WAVES.indexOf(text);
    }
    public String getText(Integer value) {
        return (String)ZETA_WAVES.get(value);
    }
    public List<String> getOptions() {
        return ZETA_WAVES;
    }

    private static List<String> getUpdatedWaves() {
        return INDEX == 0 ? ZetaMicroWaves.ZETA_WAVE_1 : ZetaMicroWaves.ZETA_WAVE_2;
    }
}
