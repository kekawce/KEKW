package eu.darkbot.kekawce.modules.chromintmpmodule;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyGate;
import com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyInfo;
import com.github.manolo8.darkbot.utils.http.Method;

public class BuyGalaxyLifeManager {

    private Main main;
    private GalaxyInfo galaxyInfo;

    public BuyGalaxyLifeManager(Main main) {
        this.main = main;
        this.galaxyInfo = new GalaxyInfo();
    }

    public void buyLivesForZeta(int numLives) {
        for (int i = 0; i < numLives; i++) {
            buyLifeForZeta();
            main.backpage.galaxyManager.updateGalaxyInfo(500);
        }
    }

    public void buyLifeForZeta() {
        try {
            main.backpage.getConnection("flashinput/galaxyGates.php", Method.GET, 1000)
                    .setRawParam("userID", main.hero.id)
                    .setRawParam("sid", main.statsManager.sid)
                    .setRawParam("gateID", GalaxyGate.ZETA.getId())
                    .setRawParam("action", "buyLife")
                    .closeInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
