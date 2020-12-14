package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.galaxy.Gate;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.facades.ChrominProxy;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.TemporalModule;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.kekawce.utils.DefaultInstallable;
import eu.darkbot.kekawce.utils.StatusUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Feature(name = "Zeta Chromin Farmer", description = "suicides on last wave in zeta for more chromin")
public class ChrominFarmerTmpModule extends TemporalModule
        implements Behaviour, Task, Configurable<ChrominFarmerConfig> {

    private static final Pattern LIVES_PATTERN = Pattern.compile("\\{(\\d+)}");
    private final Consumer<String> livesListener = this::onLogReceived;

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
    private double totalAmt = -1D, earnedAmt;

    static final int ZETA_ID = 6; // id from com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyGate
    private static final int ZETA_LAST_MAP_ID = 73; // name: "GG ζ 3", last map in zeta

    private Main main;
    private HeroManager hero;
    private ChrominFarmerConfig config;
    private ChrominCollector collector;
    private List<Npc> npcs;
    private Gate gate;

    private BuyGalaxyLifeManager buyLifeManager;
    private int livesBought;

    private long lastStatsCheck;
    private boolean isLastStatsInitialized;
    private SimpleDateFormat formatter;

    private boolean canSuicideInRadZone;
    private boolean hasSeenLastSubWave;
    private int currWave = -1;
    private int currLives = -1;

    @Override
    public void install(Main main) {
        if (DefaultInstallable.cantInstall(main, this)) return;

        super.install(main);

        this.chrominFarmerState = ChrominFarmerState.WAITING;
        this.chrominEvent = main.facadeManager.chrominEvent;
        this.buyLifeManager = new BuyGalaxyLifeManager(main);

        this.main = main;
        this.hero = main.hero;
        this.npcs = main.mapManager.entities.npcs;
        this.gate = main.backpage.galaxyManager.getGalaxyInfo().getGate(ZETA_ID);

        this.formatter = new SimpleDateFormat("HH:mm:ss");
        main.facadeManager.log.logs.add(livesListener);
    }

    @Override
    public void uninstall() {
        main.facadeManager.log.logs.remove2(livesListener);
        this.collector.uninstall();
    }

    private void onLogReceived(String log) {
        Matcher m = LIVES_PATTERN.matcher(log);
        if (m.find()) currLives = Integer.parseInt(m.group(1));
    }

    @Override
    public void setConfig(ChrominFarmerConfig config) {
        this.config = config;
        this.collector = new ChrominCollector(this.main, config);
    }

    @Override
    public String status() {
        return StatusUtils.status("Chromin Farmer", chrominFarmerState.toString(),
                (currWave >= 26 ? "2nd devourer" : "1st devourer"));
    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    @Override
    public void tickTask() {
        if (!config.ENABLE_FEATURE) return;
        this.main.backpage.galaxyManager.updateGalaxyInfos(500);
        buyLivesForZeta();
        updateStats();
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
    public void tick() {
    }

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

                Npc devourer = npcs.stream()
                        .filter(npc -> npc.playerInfo.username.contains("Devourer"))
                        .findFirst().orElseGet(() -> npcs.get(0));
                if (!canSuicideInRadZone) canSuicideInRadZone = config.SUICIDE_IN_RAD_ZONE || devourerIsBugged(devourer);
                if (canSuicideInRadZone) hero.drive.clickCenter(true, getClosestRadZone());
                else hero.drive.move(devourer);
                break;
            case WAITING:
                hasSeenLastSubWave = canSuicideInRadZone = false;
                goBack();
                break;
        }
    }

    private boolean devourerIsBugged(Npc devourer) {
        return hero.locationInfo.distance(devourer) < 200D && !hero.health.hpDecreasedIn(Time.MINUTE);
    }

    private Location getClosestRadZone() {
        double width = MapManager.internalWidth, height = MapManager.internalHeight;
        Location currLoc = hero.locationInfo.now;
        double percentX = currLoc.x / width, percentY = currLoc.y / height;

        if (Math.abs(percentX - 0.5) > Math.abs(percentY - 0.5)) {
            int sign = percentX < 0.5 ? -1 : 1;
            return new Location(currLoc.x + sign * 100, currLoc.y);
        }
        int sign = percentY < 0.5 ? -1 : 1;
        return new Location(currLoc.x, currLoc.y + sign * 100);
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
        if (isNotInitialized()) return false;
        if (currLives == -1) {
            currLives = gate.getLivesLeft();
            return false;
        }
        if (currLives <= 1 || this.hero.map.id != ZETA_LAST_MAP_ID) return false;

        currWave = getWave();
        if (currWave == -1) return false;
        if (config.ZETA_WAVE == 0 && !(24 <= currWave)) return false;
        if (config.ZETA_WAVE == 1 && !(26 <= currWave)) return false;

        String subwave = Waves.SUB_WAVES.contains(this.config.ZETA_SUB_WAVE) ? config.ZETA_SUB_WAVE : null;
        boolean failsafe = 26 <= currWave && config.ZETA_WAVE == 0;

        if (subwave == null || subwave.equals("All npcs gone (only devourer left)") || failsafe) {
            this.hasSeenLastSubWave = hasSeenLastSubWave ||
                    (24 <= currWave && currWave < 26
                    ? this.npcs.stream().anyMatch(npc -> npc.playerInfo.username.contains("Infernal"))
                    : this.npcs.stream().anyMatch(npc -> npc.playerInfo.username.contains("Kristallin")));

            return hasSeenLastSubWave && npcs.size() == 1;
        } else {
            return this.npcs.stream().anyMatch(npc -> npc.playerInfo.username.contains(subwave));
        }
    }

    private int getWave() {
        return npcs.stream()
                .filter(npc -> npc.playerInfo.username.contains("Devourer"))
                .findFirst()
                .map(npc -> {
                    String name = npc.playerInfo.username;
                    return Integer.parseInt(name.substring(name.length() - 2));
                })
                .orElse(-1);
    }

    private boolean isNotInitialized() {
        if (gate != null) return false;
        gate = main.backpage.galaxyManager.getGalaxyInfo().getGate(ZETA_ID);
        return true;
    }

    private void buyLivesForZeta() {
        if (isNotInitialized()) return;
        if (gate.getLivesLeft() == -1) return;

        this.livesBought = (int)(Math.log((float)gate.getLifePrice() / config.FIRST_LIFE_COST) / Math.log(2));
        if (livesBought < 0) livesBought = 0;

        if (this.livesBought >= this.config.BUY_LIVES) return;

        setStatsStatus("Buying Li" + (this.config.BUY_LIVES == 1 ? "fe" : "ves"));
        int numLivesToBuy = this.config.BUY_LIVES - livesBought;
        buyLifeManager.buyLivesForZeta(numLivesToBuy);
    }

    private void updateStats() {
        if (!isLastStatsInitialized) setStatsStatus("Initializing...");
        if (isNotInitialized()) return;

        if (lastStatsCheck == 0) lastStatsCheck = System.currentTimeMillis();

        if ((System.currentTimeMillis() - lastStatsCheck) > Time.SECOND) {
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
            updateStats("Chromin Per Hr", (int)(this.earnedAmt / (main.statsManager.runningTime() / (double)Time.HOUR)));
        }
    }

    public void updateChromin(double currAmt) {
        if (this.totalAmt == -1D) {
            this.totalAmt = currAmt;
            return;
        }
        if (currAmt <= 0) return;

        double diff = currAmt - this.totalAmt;
        if (diff > 0) earnedAmt += diff;
        this.totalAmt = currAmt;
    }

    private synchronized void setStatsStatus(String status) {
        this.config.STATUS_UPDATE.send("[" + this.formatter.format(new Date()) + "] " + status);
    }

    private void updateStats(String key, Integer value) {
        synchronized (config.lock) {
            this.config.STATS_INFO.put(key, value);
            this.config.STATS_INFO_UPDATE.send(key);
        }
    }

}
