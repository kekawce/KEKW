package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.core.manager.StarManager;

import java.util.Arrays;
import java.util.List;

public class Maps extends StarManager.MapList {
    public static final List<String> OPTIONS_MMO = Arrays.asList("1-1", "5-2", "1-8", "LoW");
    public static final List<String> OPTIONS_EIC = Arrays.asList("2-1", "5-2", "2-8", "LoW");
    public static final List<String> OPTIONS_VRU = Arrays.asList("3-1", "5-2", "3-8", "LoW");
    public static int ID;

    @Override
    public List<String> getOptions() {
        if (ID == 1) return OPTIONS_MMO;
        else if (ID == 2) return OPTIONS_EIC;
        else if (ID == 3) return OPTIONS_VRU;
        return null;
    }
}
