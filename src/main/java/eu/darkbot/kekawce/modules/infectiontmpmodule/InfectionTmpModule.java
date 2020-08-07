package eu.darkbot.kekawce.modules.infectiontmpmodule;

import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Mine;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.bases.BaseRefinery;
import com.github.manolo8.darkbot.core.entities.bases.BaseStation;
import com.github.manolo8.darkbot.core.entities.bases.BaseTurret;
import com.github.manolo8.darkbot.core.entities.bases.QuestGiver;
import com.github.manolo8.darkbot.core.itf.*;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.modules.TemporalModule;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.Feature;

import eu.darkbot.kekawce.VerifierChecker;

import java.util.Comparator;
import java.util.function.Consumer;

@Feature(name = "Auto Infection", description = "drops infection mine when you are not infected")
public class InfectionTmpModule extends TemporalModule implements Behaviour, Configurable<InfectionConfig> {

    private static final int INFECT_MINE_ID = 17;
    private static final int INFECT_MINE_EFFECT = 85;

    private Main main;
    private Module mainModule;
    private InfectionConfig config;

    private long waitTime;
    private long activeTime;
    private boolean moved;

    private Consumer<Map> onMapChange = (map) -> {
        this.waitTime = 0;
    };

    @Override
    public void install(Main main) {
        if (!VerifierChecker.getAuthApi().requireDonor()) return;

        super.install(main);
        this.main = main;
        this.moved = false;

        if (!(main.module instanceof TemporalModule)) this.mainModule = main.module;

        removeListener();
        main.mapManager.mapChange.add(this.onMapChange);
    }

    @Override
    public void uninstall() {
        this.removeListener();
    }

    @Override
    public void setConfig(InfectionConfig config) {
        this.config = config;
    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    @Override
    public String status() {
        long activeFor = System.currentTimeMillis() - this.activeTime;
        return (isSafe() ? "Infecting..." : "Not safe aborting infection") + activeFor + "ms";
    }

    @Override
    public void tickBehaviour() {
        if (!canInfect() || System.currentTimeMillis() - this.activeTime < 60_000L) return;

        if (waitTime == 0) waitTime = System.currentTimeMillis();
        if (this.main.module != this && System.currentTimeMillis() - waitTime > 15_000) {
            this.activeTime = 0;
            main.setModule(this);
        }
    }

    @Override
    public void tickModule() {
        if (activeTime == 0) activeTime = System.currentTimeMillis();

        infect();

        if (isInfected() || isUnderAttack() || !isSafe()
                || System.currentTimeMillis() - activeTime > 30_000L) {
            this.moved = false;
            goBack();
        }
    }

    @Override
    public void tick() { }

    private void infect() {

        if (!this.moved) {
            this.moved = true;
            this.main.hero.drive.stop(true);
        }

        this.main.hero.setMode(config.INFECT_CONFIG);

        if (!this.main.hero.drive.isMoving()) {
            Main.API.keyboardClick(this.config.INFECT_KEY);
        }

        Mine infectionMine = this.main.mapManager.entities.mines.stream().filter(m -> m.typeId == INFECT_MINE_ID).findFirst().orElse(null);
        if (infectionMine != null) {
            this.main.hero.drive.move(infectionMine);
        }
    }

    private boolean canInfect() {
        return canLayMines() && !isInfected() && !isUnderAttack() && onWorkingMap() && isSafe();
    }

    private boolean canLayMines() {
        return !this.main.hero.map.gg && !isInDemiZone();
    }

    private boolean onWorkingMap() {
        return this.main.config.GENERAL.WORKING_MAP == this.main.hero.map.id;
    }

    private boolean isInfected() {
        return this.main.hero.hasEffect(INFECT_MINE_EFFECT);
    }

    private boolean isUnderAttack() {
        return this.main.mapManager.entities.ships.stream().anyMatch(s -> s.playerInfo.isEnemy() && s.isAttacking(this.main.hero));
    }

    private boolean isInDemiZone() {
        Portal closestPort = this.main.mapManager.entities.portals.stream()
                .min(Comparator.comparingDouble(s -> this.main.hero.locationInfo.now.distance(s)))
                .orElse(null);

        BasePoint closestBase = this.main.mapManager.entities.basePoints.stream()
                .min(Comparator.comparingDouble(s -> this.main.hero.locationInfo.now.distance(s)))
                .orElse(null);

        boolean isOnPort = closestPort != null && this.main.hero.locationInfo.distance(closestPort) < radius(closestPort);
        boolean isOnBase = closestBase != null && this.main.hero.locationInfo.distance(closestBase) < radius(closestBase);

        return isOnBase || isOnPort;
    }

    private double diameter(Entity entity) {
        if (this.main.hero.map.id == 92) { // 5-2 base
            if (entity instanceof BaseStation) return 4000;
            if (entity instanceof BaseRefinery) return 3500;
        }
        if (entity instanceof Portal) return 2700;
        if (entity instanceof QuestGiver) return 2000;
        if (entity instanceof BaseTurret) return 1500; // x-8
        return 2500;
    }

    private double radius(Entity entity) {
        return diameter(entity) / 2;
    }

    private boolean isSafe() {
        return hasEnoughHp() && npcsInRangeLessThanX();
    }

    private boolean hasEnoughHp() {
        return this.main.hero.health.hpPercent() > this.config.MIN_HP;
    }

    private boolean npcsInRangeLessThanX() {
        return this.main.mapManager.entities.npcs.size() < this.config.MAX_NPCS_IN_VISION;
    }

    public void removeListener() {
        this.main.mapManager.mapChange.remove2(onMapChange);
    }

    @Override
    protected void goBack() {
        if (this.mainModule != null) this.main.setModule(this.mainModule);
    }
}
