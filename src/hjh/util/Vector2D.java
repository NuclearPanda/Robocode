package hjh.util;


import java.awt.geom.Point2D;

public class Vector2D {
    private double x;
    private double y;

    public Vector2D(Point2D a, Point2D b) {
        x = a.getX() - b.getX();
        y = a.getY() - b.getY();
    }

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Point2D p) {
        this.x = p.getX();
        this.y = p.getY();
    }

    public Vector2D(Vector2D other) {
        this.x = other.getX();
        this.y = other.getY();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public double getLengthSq() {
        return x * x + y * y;
    }

    public double dot(Vector2D other) {
        return x * other.getX() + y * other.getY();
    }

    public double angle(Vector2D other) {
        return Math.acos(dot(other) / getLength() / other.getLength());
    }

    public Vector2D multiply(double k) {
        x *= k;
        y *= k;
        return this;
    }

    public static Point2D.Double add(Point2D.Double origin, Vector2D vector) {
        return new Point2D.Double(origin.getX() + vector.getX(), origin.getY() + vector.getY());
    }

    public static Vector2D add(Vector2D a, Vector2D b) {
        return new Vector2D(a.x + b.x, a.y + b.y);
    }

    public static Vector2D substract(Vector2D a, Vector2D b) {
        return new Vector2D(a.x - b.x, a.y - b.y);
    }

    public Vector2D rotateRadians(double angle) {
        angle -= getAngle();
        double length = getLength();
        x = Math.cos(angle) * length;
        y = Math.sin(angle) * length;
        return this;
    }

    public double getAngle() {
        return Math.atan2(y, x);
    }

    public Vector2D projectedTo(Vector2D direction) {
        return Helper.getVector(direction.getAngle(), getProjectedLength(direction));
    }

    public double getProjectedLength(Vector2D direction) {
        return getLength() * Math.cos(direction.getAngle() - getAngle());
    }

    public Point2D.Double toPoint() {
        return new Point2D.Double(x, y);
    }

    @Override
    public String toString() {
        return "Vector2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
