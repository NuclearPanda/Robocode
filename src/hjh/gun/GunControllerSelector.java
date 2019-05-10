package hjh.gun;

import hjh.Greez;
import hjh.util.Helper;
import hjh.util.Vector2D;
import robocode.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

public class GunControllerSelector implements EnemyDataProvider {
    private static final double HITRATE_CHANGE = 1.1;

    private GunController[] controllers = {
            new PositionEstimationController(this),
            new LongRangeController(this),
            new DumbGunController(0.667),
            new RandomizingController(this)
    };
    private Map<GunController, Double> hitRates;
    private Map<GunController, Integer> priorities;
    private GunController lastController = null;
    private AdvancedRobot myRobot;
    private Map<Bullet, GunController> bullets = new HashMap<>();

    private Vector2D oldEnemyVelocity;
    private long oldTime;
    private double oldEnemyVelocityDouble;
    private Point2D.Double enemyLocation;
    private Vector2D enemyVelocity;
    private double absBearing;

    private int cycle = 0;
    private Color[] colorCycle = {
            Color.GRAY, Color.DARK_GRAY, Color.BLACK
    };

    public GunControllerSelector(AdvancedRobot myRobot) {
        this.myRobot = myRobot;

        oldEnemyVelocity = new Vector2D(0, 0);
        oldTime = myRobot.getTime();
        enemyLocation = new Point2D.Double(myRobot.getX(), myRobot.getY());
        enemyVelocity = new Vector2D(0, 0);
        absBearing = 0;

        hitRates = new HashMap<>();
        hitRates.put(controllers[0], 1.0);
        hitRates.put(controllers[1], 1.0);
        hitRates.put(controllers[2], 0.5);
        hitRates.put(controllers[3], 1.0);

        priorities = new HashMap<>();
        priorities.put(controllers[0], 2);
        priorities.put(controllers[1], 1);
        priorities.put(controllers[2], 1);
        priorities.put(controllers[3], 2);
    }

    public void controlGun(ScannedRobotEvent event) {

        absBearing = event.getBearingRadians() + myRobot.getHeadingRadians();
        enemyLocation = Helper.getVector(Helper.convertBearing(absBearing), event.getDistance()).toPoint();
        //enemyLocation = new Vector2D(event.getDistance(), 0).rotateRadians(Helper.convertBearing(absBearing)).toPoint();
        if (oldTime >= myRobot.getTime())
            oldTime = myRobot.getTime() - 1;
        double a = (event.getVelocity() - oldEnemyVelocityDouble) / (myRobot.getTime() - oldTime);
        //if (myRobot.getGunHeat() < 0.001) myRobot.out.println("debug: CG enemy acc = " + a);
        double realEnemyVelocity = a > -0.5 ? (a > 0.5 ? Math.min(8, event.getVelocity() + 6) : event.getVelocity()) : Math.max(-8, event.getVelocity() - 6);
        enemyVelocity = Helper.getVector(Helper.convertBearing(event.getHeadingRadians()), realEnemyVelocity);
        //enemyVelocity = new Vector2D(realEnemyVelocity, 0).rotateRadians(Helper.convertBearing(event.getHeadingRadians()));

        //GunController old = controllers[0];

        // sort by hit rates
        Arrays.sort(controllers, (o1, o2) ->
                (priorities.getOrDefault(o2, 0) - priorities.getOrDefault(o1, 0)) * 2
                        + (int) Math.signum(hitRates.getOrDefault(o2, 0.0) - hitRates.getOrDefault(o1, 0.0)));

        /*if (myRobot.getGunHeat() < 0.001) {
            myRobot.out.print((priorities.getOrDefault(controllers[2], 0) - priorities.getOrDefault(controllers[1], 0)) * 2
                    + Math.max(Math.min((int) Math.signum(hitRates.getOrDefault(controllers[2], 0.0) - hitRates.getOrDefault(controllers[1], 0.0)), 1), -1));
            myRobot.out.print((priorities.getOrDefault(controllers[2], 0) - priorities.getOrDefault(controllers[1], 0)) * 2);
            myRobot.out.println(Math.max(Math.min((int) Math.signum(hitRates.getOrDefault(controllers[2], 0.0) - hitRates.getOrDefault(controllers[1], 0.0)), 1), -1));
        }*/

        TargetingData data = null;
        for (int i = 0; i < controllers.length && data == null; i++) {
            data = controllers[i].getTargetingData(event, myRobot);
            lastController = controllers[i];
        }

        if (myRobot.getGunHeat() < 0.001) {
            myRobot.setGunColor(colorCycle[cycle % colorCycle.length]);
            cycle++;
        }
        ((Greez) myRobot).turnAndFire(data);

        if (myRobot.getTime() - oldTime > 5) {
            oldEnemyVelocity = enemyVelocity;
            oldTime = myRobot.getTime();
            oldEnemyVelocityDouble = event.getVelocity();
        }

        /*if (old != controllers[0]) {
            myRobot.out.println("debug: greez switched to gun controller '" + controllers[0].getClass() + "'");
            for (Map.Entry<GunController, Double> entry : hitRates.entrySet()) {
                myRobot.out.println("debug: " + entry.getKey().getClass() + " HR=" + entry.getValue());
            }
        }*/
    }

    public void addBullet(Bullet bullet) {
        if (lastController != null) {
            bullets.put(bullet, lastController);
            lastController = null;
        }
    }

    public void onHitTarget(BulletHitEvent event) {
        GunController controller = bullets.remove(event.getBullet());
        if (controller != null) {
            hitRates.put(controller, hitRates.get(controller) * HITRATE_CHANGE * HITRATE_CHANGE);
            controller.feedback(true);
        }
    }

    public void onMissed(BulletMissedEvent event) {
        GunController controller = bullets.remove(event.getBullet());
        if (controller != null) {
            hitRates.put(controller, hitRates.get(controller) / HITRATE_CHANGE);
            controller.feedback(false);

            /*myRobot.out.println("debug: missed, controller = " + controller.getClass() + ", HR = " + hitRates.get(controller));
            for (GunController gunController : controllers) {
                myRobot.out.println("debug: " + gunController.getClass() + " HR=" + hitRates.get(gunController));
            }*/
        }
    }


    @Override
    public Point2D.Double getEnemyLocation() {
        return enemyLocation;
    }

    @Override
    public Vector2D getEnemyVelocity() {
        return enemyVelocity;
    }

    @Override
    public double getAbsBearing() {
        return absBearing;
    }

    @Override
    public double getOldEnemyVelocityDouble() {
        return oldEnemyVelocityDouble;
    }

    @Override
    public Vector2D getOldEnemyVelocity() {
        return oldEnemyVelocity;
    }

    @Override
    public long getOldTime() {
        return oldTime;
    }
}
