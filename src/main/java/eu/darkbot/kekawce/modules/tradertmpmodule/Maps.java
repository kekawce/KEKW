package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Map;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Maps extends OptionList<Map> {
    private static final List<String> OPTIONS = Arrays.asList("1-1", "2-1", "3-1", "5-2", "1-8", "2-8", "3-8", "LoW");
    public static final List<Map> MAPS = OPTIONS.stream().map(m -> StarManager.getInstance().byName(m)).collect(Collectors.toList());

    @Override
    public Map getValue(String s) {
        return getMap(s);
    }

    @Override
    public String getText(Map map) {
        return map.name;
    }

    @Override
    public List<String> getOptions() {
        return OPTIONS;
    }

    public static Map getMap(String name) {
        return MAPS.stream().filter(m -> m.name.equals(name)).findFirst().orElse(null);
    }
}
