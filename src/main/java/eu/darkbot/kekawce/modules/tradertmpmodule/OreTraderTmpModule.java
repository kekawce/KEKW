package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.bases.BaseRefinery;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;
import com.github.manolo8.darkbot.core.objects.RefinementGui;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.TemporalModule;
import com.github.manolo8.darkbot.modules.utils.MapTraveler;
import com.github.manolo8.darkbot.modules.utils.PortalJumper;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.kekawce.utils.DefaultInstallable;
import eu.darkbot.kekawce.utils.StatusUtils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Feature(name = "Ore Trader", description = "When cargo is full travels to base to sell")
public class OreTraderTmpModule extends TemporalModule
        implements Behaviour, Configurable<OreTraderConfig> {

    private Main main;
    private Drive drive;
    private HeroManager hero;
    private StatsManager stats;
    private PortalJumper jumper;
    private Portal ggExitPortal;
    private OreTradeGui oreTrade;
    private RefinementGui refinement;
    private MapTraveler traveler;
    private List<Portal> portals;
    private List<BasePoint> bases;

    private OreTraderConfig config;

    private Iterator<OreTradeGui.Ore> ores;
    private long sellTime, sellUntil;
    private boolean hasAttemptedToSell;

    @Override
    public void install(Main main) {
        if (DefaultInstallable.cantInstall(main, this)) return;

        super.install(main);

        this.main = main;
        this.drive = main.hero.drive;
        this.hero = main.hero;
        this.stats = main.statsManager;
        this.jumper = new PortalJumper(main.hero);
        this.oreTrade = main.guiManager.oreTrade;
        this.refinement = main.guiManager.refinement;
        this.traveler = new MapTraveler(main);
        this.portals = main.mapManager.entities.portals;
        this.bases = main.mapManager.entities.basePoints;
    }

    @Override
    public void uninstall() {
        this.traveler.uninstall();
        this.traveler = null;
    }

    @Override
    public String status() {
        return StatusUtils.status("Ore Trader", "Selling", Maps.MAPS.get(config.SELL_MAP_INDEX) + " Station");
    }

    @Override
    public void setConfig(OreTraderConfig config) {
        this.config = config;
    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    @Override
    public void tickBehaviour() {
        if (!config.ENABLE_FEATURE) return;
        boolean hasTarget = !(this.hero.target == null || this.hero.target.removed);
        if (hasTarget && this.config.FINISH_TARGET_BEFORE_SELLING) return;

        if (this.stats.deposit >= this.stats.depositTotal && checkGG() && this.main.module != this)
            main.setModule(this);
    }

    @Override
    public void tickModule() {
        if (shouldGoBackEarly()) goBack();

        sellTick();

        if (!areSelectedResourcesSold()) return;
        if (oreTrade.visible) {
            sellTime = 0;
            oreTrade.showTrade(false, null);
        }
        else goBack();
    }

    @Override
    public void tick() {
    }

    @Override
    protected void goBack() {
        ores = null;
        ggExitPortal = null;

        super.goBack();
    }

    private boolean shouldGoBackEarly() {
        return !hasAttemptedToSell && (cargoHasDecreased() || isStuckInGG());
    }

    // to prevent special cases such as auto-refining/upgrading where resources will be used up
    private boolean cargoHasDecreased() {
        return stats.deposit < stats.depositTotal - 100;
    }

    // to prevent bug where bot will get stuck in GG due to jumping into wrong portal (most likely due to some client/server de-sync)
    private boolean isStuckInGG() {
        return hero.map.gg && !hero.map.name.equals("LoW") && ggExitPortal == null;
    }

    private void sellTick() {
        this.hero.setMode(this.config.SELL_CONFIG);

        if (this.hero.map.gg && this.ggExitPortal != null) {
            exitGG();
            return;
        }

        Map SELL_MAP;
        if (this.hero.map != (SELL_MAP = this.main.starManager.byName(Maps.MAPS.get(this.config.SELL_MAP_INDEX)))) {
            this.traveler.setTarget(SELL_MAP);
            this.traveler.tick();
        }
        else {
            this.bases.stream()
                    .filter(b -> b instanceof BaseRefinery)
                    .findFirst()
                    .ifPresent(this::travelToBaseAndSell);
        }
    }

    private void travelToBaseAndSell(BasePoint b) {
        if (b.locationInfo.distance(this.hero) > 200D ||
                (System.currentTimeMillis() - sellTime > 5 * Time.SECOND && sellTime != 0)) { // trade btn not appearing
            this.drive.move(b.locationInfo.now.x + ThreadLocalRandom.current().nextDouble(50D),
                    b.locationInfo.now.y + ThreadLocalRandom.current().nextDouble(50D));
            this.sellTime = 0;
        } else {
            if (this.sellTime == 0) this.sellTime = System.currentTimeMillis();
            if (oreTrade.showTrade(true, b)) {
                ores = config.ORES_TO_SELL.iterator();
                sellUntil = System.currentTimeMillis() + config.ADVANCED.SELL_WAIT;
            }

            if (sellUntil <= System.currentTimeMillis()) sellOres();
        }
    }

    private void sellOres() {
        if (sellUntil > System.currentTimeMillis()) return;
        sellUntil = System.currentTimeMillis() + config.ADVANCED.SELL_DELAY;

        if (ores.hasNext()) oreTrade.sellOre(ores.next());
        hasAttemptedToSell = true;
    }

    private boolean areSelectedResourcesSold() {
        return config.ORES_TO_SELL.stream()
                .filter(Objects::nonNull)
                .map(Enum::name)
                .map(RefinementGui.OreType::valueOf)
                .allMatch(ore -> ore == RefinementGui.OreType.PALLADIUM
                        ? refinement.get(ore).getAmount() < 15
                        : refinement.get(ore).getAmount() <= 0);
    }

    private void exitGG() {
        if (this.ggExitPortal.locationInfo.distance(main.hero) > 150D) {
            this.hero.drive.move(ggExitPortal);
            return;
        }

        jumper.jump(ggExitPortal);
    }

    private boolean checkGG() {
        return !hero.map.gg || (config.SELL_MAP_INDEX == Maps.MAPS.indexOf("LoW") && hero.map.name.equals("LoW")) || existsValidPortal();
    }

    private boolean existsValidPortal() {
        ggExitPortal = portals.stream()
                .filter(Objects::nonNull)
                .filter(p -> !p.target.gg)
                .min(Comparator.comparingDouble(p -> p.locationInfo.distance(main.hero)))
                .orElse(null);
        return ggExitPortal != null;
    }

}
