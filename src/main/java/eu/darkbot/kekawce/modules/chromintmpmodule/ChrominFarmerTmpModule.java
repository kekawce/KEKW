package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.galaxy.Gate;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.facades.ChrominProxy;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.TemporalModule;

import eu.darkbot.kekawce.DefaultInstallable;
import eu.darkbot.kekawce.Version;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// FIXME failed to update
// FIXME incorrect suiciding
@Feature(name = "Zeta Chromin Farmer alpha 1", description = "suicides on last wave in zeta for more chromin")
public class ChrominFarmerTmpModule extends TemporalModule implements DefaultInstallable, Behaviour, Task, Configurable<ChrominFarmerConfig> {

    private enum ChrominFarmerState {
        COLLECTING,
        SUICIDING,
        WAITING;

        @Override
        public String toString() {
            return this.name().charAt(0) + this.name().substring(1).toLowerCase();
        }
    }
    private ChrominFarmerTmpModule.ChrominFarmerState chrominFarmerState;

    private ChrominProxy chrominEvent;
    private double totalAmt = -1.0D, earnedAmt;

    static final int ZETA_ID = 6; // id from com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyGate
    private static final int ZETA_FIRST_MAP_ID = 71; // name: "GG ζ 1", first map in zeta
    private static final int ZETA_LAST_MAP_ID = 73; // name: "GG ζ 3", last map in zeta

    private Main main;
    private HeroManager hero;
    private Module mainModule;
    private ChrominFarmerConfig config;
    private ChrominCollector collector;
    private List<Npc> npcs;

    private BuyGalaxyLifeManager buyLifeManager;
    private int livesBought;

    private boolean hasBeenUpdated;
    private boolean hasBeenUpdatedOnEmptyMap;

    private long lastStatsCheck;
    //private long lastLocStatsCheck;
    private boolean isLastStatsInitialized;
    //private boolean isLastLocStatsInitialized;
    private SimpleDateFormat formatter;

    private boolean hasSeenLastSubWave;

    @Override
    public void install(Main main) {
        if (!DefaultInstallable.Install.install(main, DefaultInstallable.super::install))
            return;

        super.install(main);

        this.chrominFarmerState = ChrominFarmerState.WAITING;
        this.chrominEvent = main.facadeManager.chrominEvent;
        this.buyLifeManager = new BuyGalaxyLifeManager(main);

        this.main = main;
        this.hero = main.hero;
        this.npcs = main.mapManager.entities.npcs;

        this.hasBeenUpdated = false;
        this.hasBeenUpdatedOnEmptyMap = false;

        this.formatter = new SimpleDateFormat("HH:mm:ss");

        if (!(main.module instanceof TemporalModule)) this.mainModule = main.module;
    }

    @Override
    public void uninstall() {
        this.collector.uninstall();
    }

    @Override
    public void setConfig(ChrominFarmerConfig config) {
        this.config = config;
        this.collector = new ChrominCollector(this.main, config);
    }

    @Override
    public String status() {
        Gate gate = main.backpage.galaxyManager.getGalaxyInfo().getGate(Integer.valueOf(ZETA_ID));
        return String.format("KEKW %s | Chromin Farmer | %s | %s",
                Version.VERSION,
                this.chrominFarmerState.toString(),
                (gate.getCurrentWave() >= 26 ? "2nd devourer" : "1st devourer"));
    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    @Override
    public void tickTask() {
        if (!config.ENABLE_FEATURE) return;
        _updateGalaxyInfo();
        buyLivesForZeta();
        updateStats();
        //updateLocationStats();
    }

    @Override
    public void tickBehaviour() {
        if (!config.ENABLE_FEATURE) return;
        this.collector.tick();

        if (!canStartChrominFarmingModule()) return;

        if (this.main.module != this) main.setModule(this);
    }

    @Override
    public void tickModule() {
        chrominFarmerTick();
    }

    @Override
    public void tick() { }

    private void chrominFarmerTick() {
        this.chrominFarmerState = getChrominFarmerState();

        switch (this.chrominFarmerState) {
            case COLLECTING:
                if (this.hero.target != null && this.hero.isAttacking(hero.target))
                    Main.API.keyboardClick(this.config.COLLECTOR.AMMO_KEY);
                this.hero.setMode(this.config.COLLECTOR.COLLECT_CONFIG);
                this.collector.collectBox();
                break;
            case SUICIDING:
                if (this.hero.target != null && this.hero.isAttacking(hero.target))
                    Main.API.keyboardClick(this.config.COLLECTOR.AMMO_KEY);
                this.hero.setMode(this.config.COLLECTOR.SUICIDE_CONFIG);
                this.hero.drive.move(this.npcs.get(0));
                break;
            case WAITING:
                this.hasBeenUpdated = false;
                this.hasSeenLastSubWave = false;
                this.hasBeenUpdatedOnEmptyMap = false;
                goBack();
                break;
        }
    }

    private ChrominFarmerTmpModule.ChrominFarmerState getChrominFarmerState() {
        if (canStartChrominFarmingModule()) {
            return this.collector.hasBoxes()
                    ? ChrominFarmerState.COLLECTING
                    : ChrominFarmerState.SUICIDING;
        }
        return ChrominFarmerState.WAITING;
    }

    private boolean canStartChrominFarmingModule() {
        Gate gate = main.backpage.galaxyManager.getGalaxyInfo().getGate(Integer.valueOf(ZETA_ID));
        if (gate.getLivesLeft() <= 1 || this.hero.map.id != ZETA_LAST_MAP_ID) return false;
        if (gate == null || this.hero == null || this.npcs == null) return false;

        System.out.println("currentWave: " + gate.getCurrentWave());

        if (this.config.ZETA_WAVE == 0 && !(24 <= gate.getCurrentWave())) return false;
        if (this.config.ZETA_WAVE == 1 && !(26 <= gate.getCurrentWave())) return false;

        String subwave = Waves.SUB_WAVES.contains(this.config.ZETA_SUB_WAVE) ? config.ZETA_SUB_WAVE : null;
        boolean isInLastWaveButSettingsDontWantLastWave = 26 <= gate.getCurrentWave() && config.ZETA_WAVE == 0;

        System.out.format("1st cond: %s %s %s\n", (subwave == null), subwave.equals("All npcs gone (only devourer left)"), isInLastWaveButSettingsDontWantLastWave);

        if (subwave == null || subwave.equals("All npcs gone (only devourer left)") || isInLastWaveButSettingsDontWantLastWave) {
            this.hasSeenLastSubWave = hasSeenLastSubWave ? true :
                    24 <= gate.getCurrentWave() && gate.getCurrentWave() < 26
                    ? this.npcs.stream().anyMatch(npc -> npc.playerInfo.username.contains("Infernal"))
                    : this.npcs.stream().anyMatch(npc -> npc.playerInfo.username.contains("Kristallin"));

            System.out.println("hasSeenLastSubWave: " + hasSeenLastSubWave);
            System.out.println("npcsize: " + npcs.size());

            return hasSeenLastSubWave && npcs.size() == 1;
        } else {
            boolean canSeeNpc = this.npcs.stream().anyMatch(npc -> npc.playerInfo.username.contains(subwave));
            return canSeeNpc;
        }
    }

    private void updateGalaxyInfo() {
        if (isOnHomeMap()) {
            if (!this.hasBeenUpdated && isZetaPortalOnMap()) {
                this.hasBeenUpdated = true;
                _updateGalaxyInfo();
            }
            else if (!this.hasBeenUpdatedOnEmptyMap && !isZetaPortalOnMap()) {
                this.hasBeenUpdatedOnEmptyMap = true;
                _updateGalaxyInfo();
            }
        }
    }

    private void _updateGalaxyInfo() {
        this.main.backpage.galaxyManager.updateGalaxyInfo(500);
    }

    private boolean isOnHomeMap() {
        return Arrays.stream(StarManager.HOME_MAPS).anyMatch(this.hero.map.name::equals);
    }

    private boolean isZetaPortalOnMap() {
        return this.main.mapManager.entities.portals.stream().anyMatch(p -> p.target != null && p.target.id == ZETA_FIRST_MAP_ID);
    }

    private void buyLivesForZeta() {
        Gate gate = main.backpage.galaxyManager.getGalaxyInfo().getGate(Integer.valueOf(ZETA_ID));
        if (gate.getLivesLeft() == -1) return;

        this.livesBought = (int)(Math.log(gate.getLifePrice() / config.FIRST_LIFE_COST) / Math.log(2));

        if (this.livesBought >= this.config.BUY_LIVES || this.config.BUY_LIVES == 0) return;

        setStatsStatus("Buying Li" + (this.config.BUY_LIVES == 1 ? "fe" : "ves"));
        int numLivesToBuy = this.config.BUY_LIVES - livesBought;
        buyLifeManager.buyLivesForZeta(numLivesToBuy);
    }

    private void updateStats() {
        //updateGalaxyInfo();

        if (!isLastStatsInitialized) setStatsStatus("Initializing...");

        Gate gate = main.backpage.galaxyManager.getGalaxyInfo().getGate(Integer.valueOf(ZETA_ID));
        if (gate == null) return;

        if (lastStatsCheck == 0) lastStatsCheck = System.currentTimeMillis();

        if ((System.currentTimeMillis() - lastStatsCheck) > 1_000) {
            this.isLastStatsInitialized = true;
            this.lastStatsCheck = 0;

            setStatsStatus(this.chrominFarmerState.toString());
            updateStats("Lives Left", gate.getLivesLeft());
            updateStats("Life Price", gate.getLifePrice());
            updateStats("Lives Bought", this.livesBought);

            if (this.chrominEvent.address == 0) return;

            updateChromin(this.chrominEvent.currAmt);

            updateStats("Total Chromin", (int)(this.totalAmt));
            updateStats("Chromin Gained", (int)(this.earnedAmt));
            updateStats("Chromin Per Hr", (int)(this.earnedAmt / (main.statsManager.runningTime() / 3_600_000.0D)));
        }
    }

    public void updateChromin(double currAmt) {
        if (this.totalAmt == -1.0D) {
            this.totalAmt = currAmt;
            return;
        }
        if (currAmt <= 0.0D) return;

        double diff = currAmt - this.totalAmt;
        if (diff > 0.0D) earnedAmt += diff;
        this.totalAmt = currAmt;
    }

//    private void updateLocationStats() {
//        if (!this.config.LOCATION_INFO.SHOW_STATS) {
//            setCurrent("Disabled", this.config.LOCATION_INFO.STATUS_UPDATE);
//            setCurrent("", this.config.LOCATION_INFO.HERO_POS_UPDATE);
//            setCurrent("", this.config.LOCATION_INFO.LOCATION_UPDATE);
//            return;
//        }
//        if (!this.isLastLocStatsInitialized) setLocationStatus("Initializing...");
//
//        if (lastLocStatsCheck == 0) lastLocStatsCheck = System.currentTimeMillis();
//        if ((System.currentTimeMillis() - lastLocStatsCheck) > 1_000) { // updates every sec
//            this.isLastLocStatsInitialized = true;
//            this.lastLocStatsCheck = 0;
//
//            setLocationStatus(this.collector.getNumOfBoxes() + " box(es) seen");
//            setCurrent("(" + this.hero.locationInfo.now.toString() + ")", this.config.LOCATION_INFO.HERO_POS_UPDATE);
//            setCurrent(this.collector.toString(), this.config.LOCATION_INFO.LOCATION_UPDATE);
//        }
//    }
//
//    private synchronized void setLocationStatus(String status) {
//        this.config.LOCATION_INFO.STATUS_UPDATE.send("[" + this.formatter.format(new Date()) + "] " + status);
//    }

    private synchronized void setStatsStatus(String status) {
        this.config.STATUS_UPDATE.send("[" + this.formatter.format(new Date()) + "] " + status);
    }

    private synchronized void setCurrent(String current, Lazy<String> toUpdate) {
        toUpdate.send(current);
    }

    private void updateStats(String key, Integer value) {
        synchronized (this.config.lock) {
            this.config.STATS_INFO.put(key, value);
            this.config.STATS_INFO_UPDATE.send(key);
        }
    }

    @Override
    protected void goBack() {
        if (this.mainModule != null) this.main.setModule(this.mainModule);
    }
}
