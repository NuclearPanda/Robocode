package hjh.movement;

import hjh.util.Vector2D;

import java.awt.geom.*;

public abstract class MovementType {
    abstract public Point2D evadeBullet(Point2D bulletPos, Vector2D velocity, Point2D myPos, Vector2D myVelocity);
}
