package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;

import java.util.Arrays;
import java.util.List;

public class Maps extends OptionList<Integer> {
    private static final List<String> OPTIONS = Arrays.asList("X-1", "X-8", "5-2", "LoW");

    @Override
    public Integer getValue(String s) {
        return OPTIONS.indexOf(s);
    }

    @Override
    public String getText(Integer value) {
        return OPTIONS.get(value);
    }

    @Override
    public List<String> getOptions() {
        return OPTIONS;
    }

    public static String getName(int ndx) {
        return OPTIONS.get(ndx);
    }
}
