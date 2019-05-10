package hjh.gun;

import hjh.util.Vector2D;

import java.awt.geom.Point2D;

/**
 * Our robot's location is (0, 0); Absolute enemy location is (our robot location) + getEnemyLocation()
 */
interface EnemyDataProvider {
    Point2D.Double getEnemyLocation();

    Vector2D getEnemyVelocity();

    double getAbsBearing();

    double getOldEnemyVelocityDouble();

    Vector2D getOldEnemyVelocity();

    long getOldTime();

}
