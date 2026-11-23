package com.zombiesurvival.model;

/**
 * Représente un projectile tiré par le joueur.
 * La vitesse est configurable (Rifle plus rapide que Pistol).
 *
 * @author Elmehdi Eddareiy
 */
public class Bullet {

    private double  x;
    private double  y;
    private double  dirX;
    private double  dirY;
    private double  speed;
    private int     damage;
    private boolean active = true;

    public static final double WIDTH  = 8;
    public static final double HEIGHT = 8;

    public Bullet(double startX, double startY,
                  double targetX, double targetY,
                  int damage, double speed) {
        this.x      = startX;
        this.y      = startY;
        this.damage = damage;
        this.speed  = speed;

        double dx  = targetX - startX;
        double dy  = targetY - startY;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len > 0) { this.dirX = dx / len; this.dirY = dy / len; }
        else         { this.dirX = 1;         this.dirY = 0; }
    }

    /** Constructeur simplifié avec direction pré-calculée (pour Shotgun). */
    public Bullet(double startX, double startY,
                  double dirX,   double dirY,
                  int damage,    double speed,
                  boolean normalized) {
        this.x      = startX;
        this.y      = startY;
        this.dirX   = dirX;
        this.dirY   = dirY;
        this.damage = damage;
        this.speed  = speed;
    }

    public void move() {
        x += dirX * speed;
        y += dirY * speed;
    }

    public boolean isOutOfBounds(double mapW, double mapH) {
        return x < -20 || y < -20 || x > mapW + 20 || y > mapH + 20;
    }

    public void deactivate() { active = false; }

    public double  getX()       { return x; }
    public double  getY()       { return y; }
    public double  getCenterX() { return x + WIDTH  / 2; }
    public double  getCenterY() { return y + HEIGHT / 2; }
    public int     getDamage()  { return damage; }
    public boolean isActive()   { return active; }
    public double  getSpeed()   { return speed; }
}
