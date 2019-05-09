package hjh.gun;

import hjh.Greez;
import hjh.util.Helper;
import robocode.*;

import java.util.*;

public class GunControllerSelector {
    private static final double HITRATE_CHANGE = 1.1;

    private GunController[] controllers = {
            new PositionEstimationController(),
            new DumbGunController()
    };
    private Map<GunController, Double> hitRates;
    private boolean missed = false;
    private AdvancedRobot myRobot;
    private List<Bullet> bullets = new ArrayList<>();

    public GunControllerSelector(AdvancedRobot myRobot) {
        this.myRobot = myRobot;
        hitRates = new HashMap<>();
        hitRates.put(controllers[0], 1.0);
        hitRates.put(controllers[1], 0.01);
    }

    public void controlGun(ScannedRobotEvent event) {
        if (missed)
            hitRates.put(controllers[0], hitRates.get(controllers[0]) / HITRATE_CHANGE);

        // sort by hit rates
        Arrays.sort(controllers, (o1, o2) -> (int) Math.ceil(hitRates.getOrDefault(o2, 0.0) - hitRates.getOrDefault(o1, 0.0)));
        TargetingData data = controllers[0].getTargetingData(event, myRobot);
        ((Greez) myRobot).turnAndFire(data);
        myRobot.out.println("debug: firing at " + data.bearing * 180 / Math.PI + "; power=" + data.bulletPower);
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
        missed = true;
    }

    public void onHitTarget() {
        hitRates.put(controllers[0], hitRates.get(controllers[0]) * HITRATE_CHANGE);
        missed = false;
    }


}
