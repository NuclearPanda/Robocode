package hjh.gun;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.awt.*;

public class DumbGunController implements GunController {
    private double chance;

    DumbGunController(double chance) {
        this.chance = chance;
    }

    @Override
    public TargetingData getTargetingData(ScannedRobotEvent event, AdvancedRobot myRobot) {
        if (myRobot.getGunHeat() < 0.001 && Math.random() < chance) {
            myRobot.out.println("debug: using dumb controller.");
            myRobot.setBulletColor(Color.ORANGE);
            return new TargetingData(Math.min(Math.log10(myRobot.getEnergy()), 250 / event.getDistance()), myRobot.getHeadingRadians() + event.getBearingRadians());
        }
        return null;
    }

    @Override
    public void feedback(boolean hit) {

    }


}
