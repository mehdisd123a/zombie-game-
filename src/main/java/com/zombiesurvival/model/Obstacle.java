package com.zombiesurvival.model;

/**
 * Représente un obstacle statique sur la carte (mur, caisse, etc.).
 * Bloque le déplacement des zombies et du joueur.
 *
 * @author Elmehdi Eddareiy
 */
public class Obstacle {

    public enum ObstacleType { CRATE, WALL, BARREL }

    private final double       x, y, width, height;
    private final ObstacleType type;

    public Obstacle(double x, double y, double width, double height, ObstacleType type) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.type   = type;
    }

    public double       getX()      { return x; }
    public double       getY()      { return y; }
    public double       getWidth()  { return width; }
    public double       getHeight() { return height; }
    public ObstacleType getType()   { return type; }

    /** Vérification AABB simple. */
    public boolean collidesWith(double ox, double oy, double ow, double oh) {
        return ox < x + width  && ox + ow > x &&
               oy < y + height && oy + oh > y;
    }
}
