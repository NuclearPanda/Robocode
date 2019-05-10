package hjh.gun;

import hjh.util.Helper;
import hjh.util.Vector2D;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.awt.*;
import java.awt.geom.Point2D;

public class RandomizingController extends AbstractGunController {
    private static final double MAX_BULLET_TIME = 40.0;
    private static final double MIN_BULLET_TIME = 20.0;
    private static final double bullet_time_change = 2.5;
    private double bullet_time = (MAX_BULLET_TIME + MIN_BULLET_TIME) / 2;

    private static final double MAX_VARIATION = 2;
    private static final double MIN_VARIATION = 0.5;
    private static final double variation_change = 0.2;
    private double variation = (MAX_VARIATION + MIN_VARIATION) / 2;

    RandomizingController(EnemyDataProvider provider) {
        super(provider);
    }

    private int select(int max) {
        return (int) (Math.random() * max);
    }

    @Override
    public TargetingData getTargetingData(ScannedRobotEvent event, AdvancedRobot myRobot) {
        //double absBearing = event.getBearingRadians() + myRobot.getHeadingRadians();

        /*Point2D.Double enemyLocation = new Vector2D(event.getDistance(), 0).rotateRadians(Helper.convertBearing(absBearing)).toPoint();
        Vector2D enemyVelocity = new Vector2D(event.getVelocity(), 0).rotateRadians(Helper.convertBearing(event.getHeadingRadians()));*/

        if (myRobot.getGunHeat() > 0.0001)
            return null;

        Vector2D enemyVelocity = Helper.getVector(Helper.convertBearing(event.getHeadingRadians()), 4 * Math.signum(event.getVelocity())).multiply(variation);

        Point2D.Double enemyEstimated;

        int mode = select(300);
        if (mode < 30) {
            enemyEstimated = provider.getEnemyLocation();
        } else if (mode < 52) {
            enemyEstimated = Vector2D.add(provider.getEnemyLocation(), new Vector2D(enemyVelocity).multiply(0.5 * bullet_time));
        } else if (mode < 74) {
            enemyEstimated = Vector2D.add(provider.getEnemyLocation(), new Vector2D(enemyVelocity).multiply(-0.666 * 0.5 * bullet_time));
        } else if (mode < 87) {
            enemyEstimated = Vector2D.add(provider.getEnemyLocation(), new Vector2D(enemyVelocity).multiply(bullet_time));
        } else if (mode < 100) {
            enemyEstimated = Vector2D.add(provider.getEnemyLocation(), new Vector2D(enemyVelocity).multiply(-0.666 * bullet_time));
        } else {
            return null;
        }


        Vector2D targetVector = new Vector2D(enemyEstimated);

        double distance = targetVector.getLength();
        double velocity = distance / bullet_time;
        double power = Math.min(Helper.bulletPower(velocity), Math.pow(myRobot.getEnergy(), 0.7) / 5);

        if (power < 0.1) {
            /*if (myRobot.getGunHeat() < 0.001)
                myRobot.out.println("debug: RC target too far, distance = " + distance + ", bullet_time = " + bullet_time);*/
            return null;
        }

        //myRobot.out.println("debug: my    x = " + myRobot.getX() + "; y = " + myRobot.getY());
        /*if (myRobot.getGunHeat() < 0.001)
            myRobot.out.println("debug: RC Enemy location = " + provider.getEnemyLocation() + "; enemy velocity = " + provider.getEnemyVelocity());
        //myRobot.out.println("debug: Enemy abs bearing = " + absBearing*180/Math.PI + "; " + Helper.getBearing(targetVector)*180/Math.PI);
        if (myRobot.getGunHeat() < 0.001)
            myRobot.out.println("debug: RC Target Vector = " + targetVector + "; target bearing = " + Helper.getBearing(targetVector) * 180 / Math.PI);*/
        myRobot.setBulletColor(Color.YELLOW);
        return new TargetingData(power, Helper.getBearing(targetVector));
    }

    @Override
    public void feedback(boolean hit) {
        bullet_time += hit ? bullet_time_change : -bullet_time_change * 0.667;
        if (bullet_time < MIN_BULLET_TIME)
            bullet_time = MIN_BULLET_TIME;
        else if (bullet_time > MAX_BULLET_TIME)
            bullet_time = MAX_BULLET_TIME;

        variation += hit ? variation_change * 0.75 : -variation_change;
        if (variation < MIN_VARIATION)
            variation = MIN_VARIATION;
        else if (variation > MAX_VARIATION)
            variation = MAX_VARIATION;
    }
}
