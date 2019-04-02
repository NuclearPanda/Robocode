package hjh.movement;

import robocode.AdvancedRobot;
import robocode.util.Utils;

import java.awt.geom.Point2D;

import static hjh.Greez.WALL_STICK;
import static hjh.Greez._fieldRect;

public class EnemyWave {
    public Point2D.Double fireLocation;
    public long fireTime;
    public double bulletVelocity, directAngle, distanceTraveled;
    public int direction;

    public EnemyWave() { }
}

