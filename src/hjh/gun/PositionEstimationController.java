package hjh.gun;

import hjh.util.Helper;
import hjh.util.Vector2D;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.awt.geom.Point2D;
import java.io.PrintStream;

public class PositionEstimationController implements GunController {
    private static final double OPTIMAL_BULLET_TIME = 4.0;

    @Override
    public TargetingData getTargetingData(ScannedRobotEvent event, AdvancedRobot myRobot) {
        double distance = event.getDistance();
        double velocity = distance/OPTIMAL_BULLET_TIME;
        double power = Math.min(Helper.bulletPower(velocity), Math.pow(myRobot.getEnergy(), 0.5));
        double absBearing = event.getBearingRadians() + myRobot.getHeadingRadians();

        Point2D.Double enemyLocation = new Vector2D(distance, 0).rotateRadians(Helper.convertBearing(absBearing)).toPoint();
        Vector2D enemyVelocity = new Vector2D(event.getVelocity(), 0).rotateRadians(Helper.convertBearing(absBearing));
        Point2D.Double enemyEstimated = Vector2D.add(enemyLocation, enemyVelocity.multiply(OPTIMAL_BULLET_TIME));
        Vector2D targetVector = new Vector2D(enemyEstimated);

        //myRobot.out.println("debug: Enemy abs bearing = " + absBearing + "; " + Helper.getBearing(targetVector));
        //myRobot.out.println("debug: Enemy x = " + enemyLocation.getX() + " enemy y = " + enemyLocation.getY() + "deg; enemy velocity = " + enemyVelocity);
        //myRobot.out.println("debug: Target Vector = " + targetVector + "; target bearing = " + Helper.getBearing(targetVector)*Math.PI/2);
        return new TargetingData(power, Helper.getBearing(targetVector));
    }
}
