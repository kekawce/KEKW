package eu.darkbot.kekawce.modules.infectiontmpmodule;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.gui.tree.components.JPercentField;

public class InfectionConfig {
    @Option(value = "Enable feature", description = "check this to enable this feature/plugin")
    public boolean ENABLE_FEATURE = false;

    @Option("Infect mine key")
    public Character INFECT_KEY;

    @Option(value = "Infection config", description = "uses this config when infecting self")
    public Config.ShipConfig INFECT_CONFIG;

    @Option(value = "Only infect when hp greater than", description = "won't infect if hp less than this amount")
    @Editor(JPercentField.class)
    public double MIN_HP;

    @Option(value = "Only infect when npcs in sight less than", description = "won't infect if npcs in vision exceed this amount")
    @Num(min = 0, max = 1000, step = 1)
    public int MAX_NPCS_IN_VISION;

    public InfectionConfig() {
        this.INFECT_KEY         = '2';
        this.INFECT_CONFIG      = new Config.ShipConfig(2, '9');
        this.MIN_HP             = 0.4;
        this.MAX_NPCS_IN_VISION = 10;
    }
}
