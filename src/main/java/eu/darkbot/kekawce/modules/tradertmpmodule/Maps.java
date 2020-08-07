package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;

import java.util.Arrays;
import java.util.List;

public class Maps extends OptionList<Integer> {
    public static final List<String> MAPS = Arrays.asList("1-1", "2-1", "3-1", "5-2", "1-8", "2-8", "3-8", "LoW");

    public Integer getValue(String text) {
        return MAPS.indexOf(text);
    }
    public String getText(Integer value) {
        return (String)MAPS.get(value);
    }
    public List<String> getOptions() {
        return MAPS;
    }
}
