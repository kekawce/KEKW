package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.bases.BaseRefinery;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;
import com.github.manolo8.darkbot.core.objects.RefinementGui;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.TemporalModule;
import com.github.manolo8.darkbot.modules.utils.MapTraveler;
import com.github.manolo8.darkbot.modules.utils.PortalJumper;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.kekawce.utils.Captcha;
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
    private long sellTime, sellBtnTime = Long.MAX_VALUE, sellUntil;
    private boolean hasAttemptedToSell, hasClickedTradeBtn;

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
        String state, map = getTargetMap().name;
        if (hero.map.id != config.SELL_MAP_ID)
            state = I18n.get("module.map_travel.status.no_next", map);
        else if (bases.stream().filter(b -> b instanceof BaseRefinery).anyMatch(b -> hero.locationInfo.distance(b) > 300D))
            state = "Travelling to station";
        else state = "Selling";
        return StatusUtils.status("Ore Trader", state, map + " Station");
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
        if (!config.ENABLE_FEATURE || config.ORES_TO_SELL.isEmpty()) return;
        if (Captcha.exists(main.mapManager.entities.boxes)) return;

        boolean hasTarget = !(this.hero.target == null || this.hero.target.removed);
        if (hasTarget && this.config.FINISH_TARGET_BEFORE_SELLING) return;

        if (this.stats.deposit >= this.stats.depositTotal && checkGG() && this.main.module != this)
            main.setModule(this);
        Maps.ID = main.hero.playerInfo.factionId;
    }

    @Override
    public void tickModule() {
        if (shouldGoBackEarly()) goBack();

        sellTick();

        if (!areSelectedResourcesSold() && !oreSellBtnsAreBugged()) return;
        if (oreTrade.visible) {
            sellTime = 0;
            sellBtnTime = Long.MAX_VALUE;
            hasClickedTradeBtn = false;
            System.out.println("closing trade");
            oreTrade.showTrade(false, null);
        }
        else goBack();
    }

    @Override
    public void tick() {
    }

    @Override
    public void tickStopped() {
        Maps.ID = main.hero.playerInfo.factionId;
    }

    @Override
    protected void goBack() {
        ggExitPortal = null;
        hasAttemptedToSell = false;

        super.goBack();
    }

    // bug that causes you to be unable to sell ores
    private boolean oreSellBtnsAreBugged() {
        return stats.deposit >= stats.depositTotal && oreTrade.visible &&
                sellBtnTime <= System.currentTimeMillis();
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

        if (this.hero.map.id != config.SELL_MAP_ID) {
            this.traveler.setTarget(getTargetMap());
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
        if (!oreTrade.visible && oreTrade.isAnimationDone() && // can't move while trade window is open or ores won't be sold (some weird DO bug)
                ((drive.movingTo().distance(b) > 200D) ||
                (System.currentTimeMillis() - sellTime > 5 * Time.SECOND && sellTime != 0))) { // trade btn not appearing
            double angle = ThreadLocalRandom.current().nextDouble(2 * Math.PI);
            double distance = 100 + ThreadLocalRandom.current().nextDouble(100);
            drive.move(Location.of(b.locationInfo.now, angle, distance));
            System.out.println("moving");
            this.sellTime = 0;
        } else {
            if (this.sellTime == 0) this.sellTime = System.currentTimeMillis();
            if (!hasClickedTradeBtn && !hero.locationInfo.isMoving() && oreTrade.showTrade(true, b)) {
                System.out.println("opening trade");
                hasClickedTradeBtn = true;
                sellTime = Long.MAX_VALUE;
                sellBtnTime = System.currentTimeMillis() + (long) config.ADVANCED.SELL_DELAY * config.ORES_TO_SELL.size() + config.ADVANCED.SELL_WAIT;
                sellUntil = System.currentTimeMillis() + config.ADVANCED.SELL_WAIT;
            }

            sellOres();
        }
    }

    private void sellOres() {
        if (!oreTrade.visible || !oreTrade.isAnimationDone()) return;
        if (sellUntil > System.currentTimeMillis()) return;
        sellUntil = System.currentTimeMillis() + config.ADVANCED.SELL_DELAY;

        if (ores == null || !ores.hasNext()) ores = config.ORES_TO_SELL.iterator();
        if (!ores.hasNext()) return;

        OreTradeGui.Ore ore = ores.next();
        if (ore == null) return; // can occur due to GSON not finding a value (from name change in enum in darkbot)
        oreTrade.sellOre(ore);
        System.out.println("selling: " + ore);

        hasAttemptedToSell = true;
    }

    private boolean areSelectedResourcesSold() {
        return config.ORES_TO_SELL.stream()
                .filter(Objects::nonNull)
                .map(Enum::name)
                .map(RefinementGui.OreType::valueOf)
                .allMatch(ore -> ore == RefinementGui.OreType.PALLADIUM
                        ? !hero.map.name.equals("5-2") || refinement.get(ore).getAmount() < 15
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
        return !hero.map.gg || (config.SELL_MAP_ID == 200 && hero.map.name.equals("LoW")) || existsValidPortal();
    }

    private boolean existsValidPortal() {
        ggExitPortal = portals.stream()
                .filter(Objects::nonNull)
                .filter(p -> !p.target.gg)
                .min(Comparator.comparingDouble(p -> p.locationInfo.distance(main.hero)))
                .orElse(null);
        return ggExitPortal != null;
    }

    private Map getTargetMap() {
        return StarManager.getInstance().byId(config.SELL_MAP_ID);
    }

}
