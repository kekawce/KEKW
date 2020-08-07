package eu.darkbot.kekawce.modules.tradertmpmodule;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.bases.BaseRefinery;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StatsManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.objects.OreTradeGui;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.TemporalModule;
import com.github.manolo8.darkbot.modules.utils.MapTraveler;
import com.github.manolo8.darkbot.modules.utils.PortalJumper;

import eu.darkbot.kekawce.VerifierChecker;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Feature(name = "Ore Trader", description = "When cargo is full travels to base to sell")
public class OreTraderTmpModule extends TemporalModule implements Behaviour, Configurable<OreTraderConfig> {

    private Main main;
    private Drive drive;
    private HeroManager hero;
    private Module mainModule;
    private StatsManager stats;
    private PortalJumper jumper;
    private Portal ggExitPortal;
    private OreTradeGui oreTrade;
    private MapTraveler traveler;
    private List<Portal> portals;
    private List<BasePoint> bases;

    private OreTraderConfig config;

    private long sellTime;

    @Override
    public void install(Main main) {
        if (!VerifierChecker.getAuthApi().requireDonor()) return;

        super.install(main);

        this.main = main;
        this.drive = main.hero.drive;
        this.hero = main.hero;
        this.stats = main.statsManager;
        this.jumper = new PortalJumper(main.hero);
        this.oreTrade = main.guiManager.oreTrade;
        this.traveler = new MapTraveler(main);
        this.portals = main.mapManager.entities.portals;
        this.bases = main.mapManager.entities.basePoints;

        if (!(main.module instanceof TemporalModule)) this.mainModule = main.module;
    }

    @Override
    public void uninstall() {
        this.traveler.uninstall();
        this.traveler = null;
    }

    @Override
    public String status() {
        long sellTimer = (System.currentTimeMillis() - this.sellTime);
        return "CargoSeller | Selling | "
                + Maps.MAPS.get(config.SELL_MAP_INDEX) + " Station | "
                + (sellTimer <= 5000L ? sellTimer + "ms" : "");
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
        boolean hasTarget = !(this.hero.target == null || this.hero.target.removed);
        if (hasTarget && this.config.FINISH_TARGET_BEFORE_SELLING) return;

        if (this.stats.deposit >= this.stats.depositTotal && checkGG()
                && this.main.module != this)
            main.setModule(this);
    }

    @Override
    public void tickModule() {
        sell();

        // assumes resources has been sold when cargo < total cargo space
        // only checks if cargo is almost full to prevent bot from goingBack()
        // when autoupgrade cpu in use
        if ((this.stats.deposit < this.stats.depositTotal - 100 && System.currentTimeMillis() - this.sellTime > 1500L)
                || (System.currentTimeMillis() - this.sellTime > 5000L && this.sellTime != 0)) { // stuck on trade window
            if (this.oreTrade.visible) {
                this.sellTime = 0;
                this.oreTrade.showTrade(false, null);
            }
            else {
                this.ggExitPortal = null;
                goBack();
            }
        }
    }

    @Override
    public void tick() { }

    private void sell() {
        this.hero.setMode(this.config.SELL_CONFIG);

        if (this.hero.map.gg && !this.hero.map.name.equals("LoW") && this.ggExitPortal != null) {
            exitGG();
            return;
        }

        Map SELL_MAP;
        if (this.hero.map != (SELL_MAP = this.main.starManager.byName(Maps.MAPS.get(this.config.SELL_MAP_INDEX)))) {
            this.traveler.setTarget(SELL_MAP);
            this.traveler.tick();
        }
        else {
            this.bases.stream().filter(b -> b instanceof BaseRefinery).findFirst()
                    .ifPresent((b) -> {
                        if (b.locationInfo.distance(this.hero) > 250.0D) {
                            this.drive.move(b);
                        }
                        else if (this.oreTrade.showTrade(true, b)) {
                            if (this.sellTime == 0) this.sellTime = System.currentTimeMillis();

                            Set<OreTradeGui.Ore> ores = config.TOGGLE;
                            for (OreTradeGui.Ore ore : ores) {
                                this.oreTrade.sellOre(ore);
                            }
                        }
                    });
        }
    }

    private void exitGG() {
        if (this.ggExitPortal.locationInfo.distance(main.hero) > 150.0D) {
            this.hero.drive.move(ggExitPortal);
            return;
        }

        jumper.jump(ggExitPortal);
    }

    private boolean checkGG() {
        return this.hero.map.gg && !this.hero.map.name.equals("LoW") ? existsValidPortal() : true;
    }

    private boolean existsValidPortal() {
        ggExitPortal = portals.stream()
                .filter(p -> !(p.target != null && p.target.gg))
                .min(Comparator.comparingDouble(p -> p.locationInfo.distance(main.hero)))
                .orElse(null);
        return ggExitPortal != null;
    }

    @Override
    protected void goBack() {
        if (this.mainModule != null) this.main.setModule(this.mainModule);
    }
}
