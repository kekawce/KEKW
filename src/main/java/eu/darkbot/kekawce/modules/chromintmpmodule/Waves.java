package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;

import java.util.Arrays;
import java.util.List;

public class Waves extends OptionList<Integer> {
    public static final List<String> ZETA_WAVES = Arrays.asList("1st Devourer", "2nd Devourer");
    public static List<String> SUB_WAVES = getUpdatedWaves();
    private static int INDEX = 0;

    @Override
    public Integer getValue(String text) {
        INDEX = ZETA_WAVES.indexOf(text);
        SUB_WAVES = getUpdatedWaves();
        return INDEX;
    }

    @Override
    public String getText(Integer value) {
        SUB_WAVES = getUpdatedWaves();
        return ZETA_WAVES.get(value);
    }

    @Override
    public List<String> getOptions() {
        SUB_WAVES = getUpdatedWaves();
        return ZETA_WAVES;
    }

    private static List<String> getUpdatedWaves() {
        return INDEX == 0 ? SubWaves.ZETA_SUB_WAVES_1 : SubWaves.ZETA_SUB_WAVES_2;
    }
}
