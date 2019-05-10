package hjh;

import hjh.gun.GunControllerSelector;
import hjh.gun.TargetingData;
import hjh.movement.EnemyWave;
import hjh.util.Helper;
import hjh.util.Vector2D;
import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static hjh.util.Helper.*;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Greez - a robot by hjh
 */
public class Greez extends AdvancedRobot {
    public static int BINS = 47;
    public static double _surfStats[] = new double[BINS];
    public Point2D.Double _myLocation;     // our bot's location
    public Point2D.Double _enemyLocation;  // enemy bot's location

    public ArrayList<EnemyWave> _enemyWaves;
    public ArrayList<Integer> _surfDirections;
    public ArrayList<Double> _surfAbsBearings;

    public static double _oppEnergy = 100.0;

    private GunControllerSelector gunControllerSelector;
    private double firePower = 0.0;
    private GunTurnCompleteCondition gunCondition = null;

    /**
     * This is a rectangle that represents an 800x600 battle field,
     * used for a simple, iterative WallSmoothing method (by PEZ).
     * If you're not familiar with WallSmoothing, the wall stick indicates
     * the amount of space we try to always have on either end of the tank
     * (extending straight out the front or back) before touching a wall.
     */
    public static final Rectangle2D.Double _fieldRect
            = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);
    public static double WALL_STICK = 160;

    /**
     * run: Greez's default behavior
     */
    public void run() {

        setBodyColor(Color.getHSBColor(0.19444f, 0.50f, 0.54f));
        setRadarColor(Color.BLACK);
        setScanColor(Color.getHSBColor(0.19444f, 0.48f, 0.29f));

        gunControllerSelector = new GunControllerSelector(this);
        _enemyWaves = new ArrayList<>();
        _surfDirections = new ArrayList<>();
        _surfAbsBearings = new ArrayList<>();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        do {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        } while (true);
    }

    public void turnAndFire(TargetingData data) {
        if (data == null) {
            return;
        }

        if (gunCondition != null) {
            removeCustomEvent(gunCondition);
        }
        //out.println("debug: targeting " + data.bearing*180/Math.PI + " degrees");
        double turn = data.bearing - getGunHeadingRadians();
        while (turn > Math.PI) turn -= Math.PI * 2;
        while (turn < -Math.PI) turn += Math.PI * 2;
        //out.println("debug: turning " + turn*180/Math.PI + " degrees right");
        setTurnGunRightRadians(turn);
        if (getGunHeat() < 0.00001)
            addCustomEvent(gunCondition = new GunTurnCompleteCondition(this));
        firePower = data.bulletPower;
    }

    /**
     * On gun turned event
     *
     * @param event event
     */
    public void onCustomEvent(CustomEvent event) {
        if (event.getCondition() == gunCondition) {
            this.removeCustomEvent(gunCondition);
            gunCondition = null;
            gunControllerSelector.addBullet(fireBullet(firePower));
            //out.println("debug: firing bullet, gunHeading=" + getGunHeading());
            firePower = 0.0;
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        gunControllerSelector.onHitTarget(event);
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        gunControllerSelector.onMissed(event);
    }

    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {

        _myLocation = new Point2D.Double(getX(), getY());

        double lateralVelocity = getVelocity() * Math.sin(e.getBearingRadians());
        double absBearing = e.getBearingRadians() + getHeadingRadians();

        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing
                - getRadarHeadingRadians()) * 2);

        _surfDirections.add(0,
                (lateralVelocity >= 0) ? 1 : -1);
        _surfAbsBearings.add(0, absBearing + Math.PI);


        double bulletPower = _oppEnergy - e.getEnergy();
        if (bulletPower < 3.01 && bulletPower > 0.09
                && _surfDirections.size() > 2) {
            EnemyWave ew = new EnemyWave();
            ew.fireTime = getTime() - 1;
            ew.bulletVelocity = bulletVelocity(bulletPower);
            ew.distanceTraveled = bulletVelocity(bulletPower);
            ew.direction = _surfDirections.get(2);
            ew.directAngle = _surfAbsBearings.get(2);
            ew.fireLocation = (Point2D.Double) _enemyLocation.clone(); // last tick

            _enemyWaves.add(ew);
        }

        _oppEnergy = e.getEnergy();

        // update after EnemyWave detection, because that needs the previous
        // enemy location as the source of the wave
        _enemyLocation = project(_myLocation, absBearing, e.getDistance());

        updateWaves();
        doSurfing();

        //out.println("debug: gunCondition=" + gunCondition + " gunHeat="+getGunHeat());

        // gun code would go here...
        if (gunCondition == null/* && getGunHeat() < 0.00001*/)
            gunControllerSelector.controlGun(e);
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // If the _enemyWaves collection is empty, we must have missed the
        // detection of this wave somehow.
        if (!_enemyWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(
                    e.getBullet().getX(), e.getBullet().getY());
            EnemyWave hitWave = null;

            // look through the EnemyWaves, and find one that could've hit us.
            for (int x = 0; x < _enemyWaves.size(); x++) {
                EnemyWave ew = _enemyWaves.get(x);

                if (Math.abs(ew.distanceTraveled -
                        _myLocation.distance(ew.fireLocation)) < 50
                        && Math.abs(bulletVelocity(e.getBullet().getPower())
                        - ew.bulletVelocity) < 0.001) {
                    hitWave = ew;
                    break;
                }
            }

            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);

                // We can remove this wave now, of course.
                _enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
            }
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        for (EnemyWave enemyWave : _enemyWaves) {
            Point2D.Double waveLocation = Vector2D.add(enemyWave.fireLocation, Helper.getVector(Math.PI / 2 - enemyWave.directAngle, enemyWave.distanceTraveled));

            out.println("debug: direct angle = " + enemyWave.directAngle);
            g.setColor(Color.BLUE);
            g.drawArc((int) (enemyWave.fireLocation.getX() - enemyWave.distanceTraveled),
                    (int) (enemyWave.fireLocation.getY() - enemyWave.distanceTraveled),
                    (int) enemyWave.distanceTraveled * 2,
                    (int) enemyWave.distanceTraveled * 2,
                    0, 360);

            g.setColor(Color.CYAN);
            g.drawArc((int) (enemyWave.fireLocation.getX() - enemyWave.distanceTraveled),
                    (int) (enemyWave.fireLocation.getY() - enemyWave.distanceTraveled),
                    (int) enemyWave.distanceTraveled * 2,
                    (int) enemyWave.distanceTraveled * 2,
                    (int) (enemyWave.directAngle * 180 / Math.PI), -15*enemyWave.direction);

            g.setColor(Color.BLUE);
            g.drawLine((int) enemyWave.fireLocation.getX(), (int) enemyWave.fireLocation.getY(), (int) waveLocation.getX(), (int) waveLocation.getY());

            g.setColor(Color.RED);
            g.drawArc((int) (enemyWave.fireLocation.getX() - enemyWave.distanceTraveled),
                    (int) (enemyWave.fireLocation.getY() - enemyWave.distanceTraveled),
                    (int) enemyWave.distanceTraveled * 2,
                    (int) enemyWave.distanceTraveled * 2,
                    (int) (enemyWave.directAngle * 180 / Math.PI), enemyWave.direction*15);

        }

        //g.setColor(Color.CYAN);
        //g.drawArc(300, 300, 100, 100, 0, 30);

    }

    /**
     * onHitWall: What to do when you hit a wall
     */
    public void onHitWall(HitWallEvent e) {
        // Replace the next line with any behavior you would like
        back(20*Math.signum(Math.cos(e.getBearingRadians())));
    }

    public EnemyWave getClosestSurfableWave() {
        double closestDistance = 50000; // I juse use some very big number here
        EnemyWave surfWave = null;

        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave) _enemyWaves.get(x);
            double distance = _myLocation.distance(ew.fireLocation)
                    - ew.distanceTraveled;

            if (distance > ew.bulletVelocity && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
        }

        return surfWave;
    }

    public void updateWaves() {
        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave) _enemyWaves.get(x);

            ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
            if (ew.distanceTraveled >
                    _myLocation.distance(ew.fireLocation) + 50) {
                _enemyWaves.remove(x);
                x--;
            }
        }
    }

    // Given the EnemyWave that the bullet was on, and the point where we
    // were hit, update our stat array to reflect the danger in that area.
    public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
        int index = getFactorIndex(ew, targetLocation);

        for (int x = 0; x < BINS; x++) {
            // for the spot bin that we were hit on, add 1;
            // for the bins next to it, add 1 / 2;
            // the next one, add 1 / 5; and so on...
            _surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }

    public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
        Point2D.Double predictedPosition = (Point2D.Double) _myLocation.clone();
        double predictedVelocity = getVelocity();
        double predictedHeading = getHeadingRadians();
        double maxTurning, moveAngle, moveDir;

        int counter = 0; // number of ticks in the future
        boolean intercepted = false;

        do {    // the rest of these code comments are rozu's
            moveAngle =
                    wallSmoothing(predictedPosition, absoluteBearing(surfWave.fireLocation,
                            predictedPosition) + (direction * (Math.PI / 2)), direction)
                            - predictedHeading;
            moveDir = 1;

            if (Math.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            // maxTurning is built in like this, you can't turn more then this in one tick
            maxTurning = Math.PI / 720d * (40d - 3d * Math.abs(predictedVelocity));
            predictedHeading = Utils.normalRelativeAngle(predictedHeading
                    + limit(-maxTurning, moveAngle, maxTurning));

            // this one is nice ;). if predictedVelocity and moveDir have
            // different signs you want to breack down
            // otherwise you want to accelerate (look at the factor "2")
            predictedVelocity +=
                    (predictedVelocity * moveDir < 0 ? 2 * moveDir : moveDir);
            predictedVelocity = limit(-8, predictedVelocity, 8);

            // calculate the new predicted position
            predictedPosition = project(predictedPosition, predictedHeading,
                    predictedVelocity);

            counter++;

            if (predictedPosition.distance(surfWave.fireLocation) <
                    surfWave.distanceTraveled + (counter * surfWave.bulletVelocity)
                            + surfWave.bulletVelocity) {
                intercepted = true;
            }
        } while (!intercepted && counter < 500);

        return predictedPosition;
    }

    public double checkDanger(EnemyWave surfWave, int direction) {
        int index = getFactorIndex(surfWave,
                predictPosition(surfWave, direction));

        return _surfStats[index];
    }

    public void doSurfing() {
        EnemyWave surfWave = getClosestSurfableWave();

        if (surfWave == null) {
            return;
        }

        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        double goAngle = absoluteBearing(surfWave.fireLocation, _myLocation);
        if (dangerLeft < dangerRight) {
            goAngle = wallSmoothing(_myLocation, goAngle - (Math.PI / 2), -1);
        } else {
            goAngle = wallSmoothing(_myLocation, goAngle + (Math.PI / 2), 1);
        }

        setBackAsFront(this, goAngle);
    }
}
