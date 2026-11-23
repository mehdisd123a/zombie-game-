package com.zombiesurvival.model;

/**
 * Particule visuelle pour les effets de mort et d'impact.
 * Disparaît progressivement après son délai de vie.
 *
 * @author Elmehdi Eddareiy
 */
public class Particle {

    private double x, y;
    private double vx, vy;    // vélocité
    private double life;       // vie restante (0.0 → 1.0)
    private double decay;      // vitesse de disparition par frame
    private double size;
    private int    r, g, b;   // couleur RGB

    public Particle(double x, double y, double vx, double vy,
                    double size, int r, int g, int b) {
        this.x     = x;
        this.y     = y;
        this.vx    = vx;
        this.vy    = vy;
        this.size  = size;
        this.r     = r;
        this.g     = g;
        this.b     = b;
        this.life  = 1.0;
        this.decay = 0.04 + Math.random() * 0.04; // disparaît en ~15-25 frames
    }

    /** Met à jour la particule (physique + vie). */
    public void update() {
        x    += vx;
        y    += vy;
        vx   *= 0.92; // friction
        vy   *= 0.92;
        vy   += 0.15; // gravité légère
        life -= decay;
        size *= 0.97; // rétrécit
    }

    public boolean isDead()  { return life <= 0 || size < 0.5; }
    public double  getX()    { return x; }
    public double  getY()    { return y; }
    public double  getLife() { return life; }
    public double  getSize() { return size; }
    public int     getR()    { return r; }
    public int     getG()    { return g; }
    public int     getB()    { return b; }
}
