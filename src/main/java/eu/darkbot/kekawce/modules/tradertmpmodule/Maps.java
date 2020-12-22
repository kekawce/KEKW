package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.core.manager.StarManager;

import java.util.Arrays;
import java.util.List;

public class Maps extends StarManager.MapList {
    public static final List<String> MAPS_MMO = Arrays.asList("1-1", "5-2", "1-8", "LoW");
    public static final List<String> MAPS_EIC = Arrays.asList("2-1", "5-2", "2-8", "LoW");
    public static final List<String> MAPS_VRU = Arrays.asList("3-1", "5-2", "3-8", "LoW");
    public static final List<String> MAPS = Arrays.asList("1-1", "2-1", "3-1", "5-2", "1-8", "2-8", "3-8", "LoW");
    public static int ID;

    @Override
    public List<String> getOptions() {
        switch (ID){
            case 1:
                return MAPS_MMO;
            case 2:
                return MAPS_EIC;
            case 3:
                return MAPS_VRU;
            default: return MAPS;
        }
    }
}
