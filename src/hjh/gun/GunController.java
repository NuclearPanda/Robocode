package hjh.gun;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public interface GunController {
    TargetingData getTargetingData(ScannedRobotEvent event, AdvancedRobot myRobot);

    void feedback(boolean hit);

}
