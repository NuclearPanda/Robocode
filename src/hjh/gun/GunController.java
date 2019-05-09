package hjh.gun;

import hjh.util.Vector2D;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.io.PrintStream;

public interface GunController {
    TargetingData getTargetingData(ScannedRobotEvent event, AdvancedRobot myRobot);

}
