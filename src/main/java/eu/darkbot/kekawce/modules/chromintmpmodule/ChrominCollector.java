package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.factory.EntityFactory;
import com.github.manolo8.darkbot.modules.CollectorModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class ChrominCollector extends CollectorModule {

    private static final String CHROMIN_BOX = "CHROMIN_BOX";
    private final Object lock = new Object();

    private final Main main;
    private final HeroManager hero;
    private final ChrominFarmerConfig config;

    private long waitingForBoxUntil;

    private final List<Box> boxes;
    public List<Box> chrominBoxes;

    private final Consumer<Entity> filterBoxes;
    private final Consumer<Map> onMapChange = (map) -> this.chrominBoxes = new ArrayList<>();

    public ChrominCollector(Main main, ChrominFarmerConfig config) {
        super.install(main);

        this.main = main;
        this.hero = main.hero;
        this.boxes = main.mapManager.entities.boxes;
        this.config = config;

        this.waitingForBoxUntil = -1;

        this.chrominBoxes = new ArrayList<>();

        this.filterBoxes = entity -> {
            synchronized (lock) {
                Box box = (Box) entity;

                if (box.type.contains(CHROMIN_BOX)) {
                    String hash = Main.API.readMemoryString(box.address, 160);

                    if (chrominBoxes.stream().noneMatch(cbox -> cbox.getMetadata("BOX_HASH").equals(hash))) {
                        box.setMetadata("BOX_HASH", hash);
                        chrominBoxes.add(box);
                    } else { // anyMatch(), there is match/duplicate box found
                        // replace old box with new box to reset removed variable value or else it will always be true when
                        // the box becomes out of sight
                        Box oldBox = chrominBoxes.stream()
                                .filter(cbox -> cbox.getMetadata("BOX_HASH").equals(hash))
                                .findFirst()
                                .orElse(null);
                        box.setMetadata("BOX_HASH", hash);
                        //noinspection ResultOfMethodCallIgnored
                        Collections.replaceAll(chrominBoxes, oldBox, box);
                    }
                }
            }
        };

        main.mapManager.mapChange.add(this.onMapChange);
    }

    @Override
    public void uninstall() {
        super.uninstall();
        this.main.mapManager.entities.entityRegistry.remove(EntityFactory.BOX, filterBoxes);
        this.main.mapManager.mapChange.remove2(onMapChange);
    }

    @Override
    public void tick() {
        synchronized (lock) {
            this.chrominBoxes.removeIf(box -> this.hero.locationInfo.distance(box) < 700.0D && box.removed);
        }
        this.main.mapManager.entities.entityRegistry.add(EntityFactory.BOX, filterBoxes);
    }

    public boolean hasBoxes() {
        Box oldCurr = current;
        if (isNotWaiting()) findBox();
        if (current == null || oldCurr == null || current != oldCurr) waitingForBoxUntil = -1;

        Box curr = current != null ? current : this.chrominBoxes.stream()
                .min(Comparator.comparingDouble(box -> this.hero.locationInfo.distance(box)))
                .orElse(null);
        return curr != null;
    }

    public void collectBox() {
        if (current == null) {
            this.chrominBoxes.stream()
                    .min(Comparator.comparingDouble(box -> this.hero.locationInfo.distance(box)))
                    .ifPresent(box -> moveTowardsBox(box.locationInfo.now));
            return;
        }
        if (config.COLLECTOR.PET_BOX_COLLECTING_ONLY && current.type.contains(CHROMIN_BOX)) {
            moveTowardsBox(current.locationInfo.now);

            if (this.hero.locationInfo.distance(current) < 450.0D) {
                boolean petStuckOnCargo = this.main.statsManager.deposit >= this.main.statsManager.depositTotal &&
                        this.boxes.stream().anyMatch(box -> box.type.equals("FROM_SHIP") || box.type.equals("CANDY_CARGO"));
                if (waitingForBoxUntil == -1 || petStuckOnCargo) waitingForBoxUntil = System.currentTimeMillis() + 10_000;
                else if (System.currentTimeMillis() > waitingForBoxUntil) tryCollectNearestBox();
            } else waitingForBoxUntil = -1;
        } else tryCollectNearestBox();
    }

    private void moveTowardsBox(Location box) {
        box = Location.of(box, box.angle(main.hero.locationInfo.now) - 0.3, 200);
        if (main.hero.drive.movingTo().distance(box) > 300) main.hero.drive.move(box);
    }

    @Override
    public String toString() {
        sortBoxesByClosestDistance();
        StringBuilder locInfo = new StringBuilder();

        for (final Box box : chrominBoxes)
            locInfo.append("(").append(box.locationInfo.now.toString()).append(") | ");

        return locInfo.toString();
    }

    private void sortBoxesByClosestDistance() {
        chrominBoxes.sort((b1, b2) -> {
            double distToB1 = hero.locationInfo.distance(b1);
            double distToB2 = hero.locationInfo.distance(b2);

            return Double.compare(distToB1, distToB2);
        });
    }

}
