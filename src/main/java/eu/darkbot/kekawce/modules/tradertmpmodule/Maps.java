package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.core.manager.StarManager;

import java.util.Arrays;
import java.util.List;

public class Maps extends StarManager.MapList {
    private static final List<String> OPTIONS = Arrays.asList("1-1", "2-1", "3-1", "5-2", "1-8", "2-8", "3-8", "LoW");

    @Override
    public List<String> getOptions() {
        return OPTIONS;
    }
}
