package hjh.gun;

import hjh.util.Helper;
import hjh.util.Vector2D;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.awt.*;
import java.awt.geom.Point2D;

public class PositionEstimationController extends AbstractGunController {
    private static final double MAX_BULLET_TIME = 30.0;
    private static final double MIN_BULLET_TIME = 15.0;
    private static final double bullet_time_change = 0.7;
    private double bullet_time = (MAX_BULLET_TIME + MIN_BULLET_TIME) / 2;

    private static final double CONSIDER_ACCELERATION = 0.85;

    PositionEstimationController(EnemyDataProvider provider) {
        super(provider);
    }

    @Override
    public TargetingData getTargetingData(ScannedRobotEvent event, AdvancedRobot myRobot) {
        //double absBearing = event.getBearingRadians() + myRobot.getHeadingRadians();

        /*Point2D.Double enemyLocation = new Vector2D(event.getDistance(), 0).rotateRadians(Helper.convertBearing(absBearing)).toPoint();
        Vector2D enemyVelocity = new Vector2D(event.getVelocity(), 0).rotateRadians(Helper.convertBearing(event.getHeadingRadians()));*/
        Point2D.Double enemyEstimated = Vector2D.add(provider.getEnemyLocation(), new Vector2D(provider.getEnemyVelocity()).multiply(bullet_time));

        Vector2D acceleration = Vector2D.substract(provider.getEnemyVelocity(), provider.getOldEnemyVelocity()).multiply(1.0 / (myRobot.getTime() - provider.getOldTime())).multiply(CONSIDER_ACCELERATION);
        Vector2D enemyVector = new Vector2D(provider.getEnemyLocation());
        double acc = acceleration.getProjectedLength(enemyVector);

        if (acc > 0.05) {
            enemyEstimated = Vector2D.add(provider.getEnemyLocation(),
                    Vector2D.add(acceleration.multiply(bullet_time * bullet_time / 2),
                            new Vector2D(provider.getEnemyVelocity()).multiply(bullet_time)));
        }

        Vector2D targetVector = new Vector2D(enemyEstimated);

        double distance = targetVector.getLength();
        double velocity = distance / bullet_time;
        double power = Math.min(Helper.bulletPower(velocity), Math.pow(myRobot.getEnergy(), 0.7) / 5);

        if (power < 0.1) {
            if (myRobot.getGunHeat() < 0.001)
                myRobot.out.println("debug: target too far, distance = " + distance + ", bullet_time = " + bullet_time);
            return null;
        }

        //myRobot.out.println("debug: my    x = " + myRobot.getX() + "; y = " + myRobot.getY());
        if (myRobot.getGunHeat() < 0.001)
            myRobot.out.println("debug: Enemy location = " + provider.getEnemyLocation() + "; enemy velocity = " + provider.getEnemyVelocity());
        //myRobot.out.println("debug: Enemy abs bearing = " + absBearing*180/Math.PI + "; " + Helper.getBearing(targetVector)*180/Math.PI);
        if (myRobot.getGunHeat() < 0.001)
            myRobot.out.println("debug: Target Vector = " + targetVector + "; target bearing = " + Helper.getBearing(targetVector) * 180 / Math.PI);

        myRobot.setBulletColor(Color.RED);
        return new TargetingData(power, Helper.getBearing(targetVector));
    }

    @Override
    public void feedback(boolean hit) {
        bullet_time += hit ? bullet_time_change : -bullet_time_change * 0.667;
        if (bullet_time < MIN_BULLET_TIME)
            bullet_time = MIN_BULLET_TIME;
        else if (bullet_time > MAX_BULLET_TIME)
            bullet_time = MAX_BULLET_TIME;
    }
}
