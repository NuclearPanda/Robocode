package hjh.gun;

import hjh.util.Helper;
import hjh.util.Vector2D;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.awt.*;
import java.awt.geom.Point2D;

public class LongRangeController extends AbstractGunController {
    private static final double MAX_POWER = 3;
    private static final double MIN_POWER = 0.5;
    private static final double b_power_change = 0.666;

    private static final double CONSIDER_ACCELERATION = 0.9;

    private double b_power = 1.25;

    LongRangeController(EnemyDataProvider provider) {
        super(provider);
    }

    @Override
    public TargetingData getTargetingData(ScannedRobotEvent event, AdvancedRobot myRobot) {
        double distance = event.getDistance();
        if (distance < 200)
            return null;

        double power = Math.max(b_power * Math.exp(-Math.abs(event.getVelocity())), 0.3);
        double velocity = Helper.bulletVelocity(power);

        Vector2D acceleration = Vector2D.substract(provider.getEnemyVelocity(), provider.getOldEnemyVelocity()).multiply(1.0 / (myRobot.getTime() - provider.getOldTime())).multiply(CONSIDER_ACCELERATION);
        Vector2D enemyVector = new Vector2D(provider.getEnemyLocation());
        double acc = acceleration.getProjectedLength(enemyVector);

        double relVelocity = velocity - provider.getEnemyVelocity().getProjectedLength(enemyVector);
        double disc = relVelocity * relVelocity - 2 * acc * distance;

        double bullet_time;
        Point2D.Double enemyEstimated;
        if (disc < 0 || acc < 0.05) {
            //myRobot.out.println("debug: LR no solution with current enemy acceleration. acc = " + acc);
            bullet_time = distance / relVelocity;
            enemyEstimated = Vector2D.add(provider.getEnemyLocation(),
                    new Vector2D(provider.getEnemyVelocity()).multiply(bullet_time));
        } else {
            disc = Math.sqrt(disc);
            double t = (relVelocity - disc) / acc;
            if (t < 0) {
                t = (relVelocity + disc) / acc;
            }
            if (t < 0) {
                //if (myRobot.getGunHeat() < 0.001)
                //   myRobot.out.println("debug: LR no solution with current enemy acceleration. acc = " + acc + " bullet_time = " + t);
                bullet_time = distance / relVelocity;
                enemyEstimated = Vector2D.add(provider.getEnemyLocation(),
                        new Vector2D(provider.getEnemyVelocity()).multiply(bullet_time));
            } else {
                bullet_time = t;
                enemyEstimated = Vector2D.add(provider.getEnemyLocation(),
                        Vector2D.add(acceleration.multiply(bullet_time * bullet_time / 2),
                                new Vector2D(provider.getEnemyVelocity()).multiply(bullet_time)));
            }
        }


        Vector2D targetVector = new Vector2D(enemyEstimated);

        //myRobot.out.println("debug: my    x = " + myRobot.getX() + "; y = " + myRobot.getY());
        //if (myRobot.getGunHeat() < 0.001)
        //    myRobot.out.println("debug: LR b_power = " + b_power + " enemy velocity = " + event.getVelocity());
        //myRobot.out.println("debug: Enemy abs bearing = " + absBearing*180/Math.PI + "; " + Helper.getBearing(targetVector)*180/Math.PI);
        //myRobot.out.println("debug: LR Target Vector = " + targetVector + "; enemy velocity = " + provider.getEnemyVelocity());
        myRobot.setBulletColor(Color.CYAN);
        return new TargetingData(power, Helper.getBearing(targetVector));
    }

    @Override
    public void feedback(boolean hit) {
        b_power += hit ? b_power_change * 0.333 : -b_power_change;
        if (b_power < MIN_POWER)
            b_power = MIN_POWER;
        else if (b_power > MAX_POWER)
            b_power = MAX_POWER;
    }
}
