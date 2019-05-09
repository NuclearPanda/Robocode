package hjh.gun;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class DumbGunController implements GunController {
    @Override
    public TargetingData getTargetingData(ScannedRobotEvent event, AdvancedRobot myRobot) {
        return new TargetingData(Math.log10(myRobot.getEnergy()), myRobot.getHeadingRadians() + event.getBearingRadians());
    }
}
