package hjh.util;


import java.awt.geom.Point2D;

public class Vector2D {
    private double x;
    private double y;

    Vector2D(Point2D a, Point2D b) {
        x = a.getX() - b.getX();
        y = a.getY() - b.getY();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLength() {
        return Math.sqrt(x*x + y*y);
    }

    public double getLengthSq() {
        return x*x + y*y;
    }

    public double dot(Vector2D other) {
        return x*other.getX() + y*other.getY();
    }

    public double angle(Vector2D other) {
        return Math.acos(dot(other)/ getLength()/other.getLength());
    }
}
