package com.zombiesurvival.model;

/**
 * Représente un zombie ennemi.
 * Gère le déplacement vers le joueur, la santé et les dégâts.
 *
 * @author Elmehdi Eddareiy
 */
public class Zombie {

    // Position
    protected double x;
    protected double y;

    // Dimensions
    protected double width  = 35;
    protected double height = 35;

    // Statistiques
    protected double hp;
    protected double maxHp;
    protected double speed;
    protected int    damage;        // dégâts infligés au joueur par contact

    // Effet visuel : flash blanc quand touché
    private boolean flashHit       = false;
    private long    flashStartTime = 0;

    // Identifiant de type pour le rendu
    protected String type = "NORMAL";

    // Cooldown d'attaque (un zombie ne peut pas attaquer plusieurs fois par seconde)
    private long lastAttackTime = 0;
    public  static final long ATTACK_COOLDOWN_MS = 800;

    public Zombie(double x, double y) {
        this.x      = x;
        this.y      = y;
        this.hp     = 50;
        this.maxHp  = 50;
        this.speed  = 1.2;
        this.damage = 10;
    }

    /**
     * Déplace le zombie vers la position cible (joueur) à chaque frame.
     * Utilise un vecteur normalisé pour conserver une vitesse constante.
     *
     * @param targetX position X de la cible
     * @param targetY position Y de la cible
     */
    public void moveToward(double targetX, double targetY) {
        double dx  = targetX - (x + width  / 2);
        double dy  = targetY - (y + height / 2);
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            x += (dx / len) * speed;
            y += (dy / len) * speed;
        }
    }

    /**
     * Inflige des dégâts au zombie.
     * @param amount quantité de dégâts
     */
    public void takeDamage(double amount) {
        hp -= amount;
        flashHit       = true;
        flashStartTime = System.currentTimeMillis();
    }

    /**
     * @return true si le zombie est mort
     */
    public boolean isDead() {
        return hp <= 0;
    }

    /**
     * Met à jour l'état interne (flash, etc.)
     */
    public void update() {
        if (flashHit && System.currentTimeMillis() - flashStartTime > 150) {
            flashHit = false;
        }
    }

    /**
     * @return true si ce zombie peut attaquer maintenant (cooldown écoulé)
     */
    public boolean canAttack() {
        long now = System.currentTimeMillis();
        if (now - lastAttackTime >= ATTACK_COOLDOWN_MS) {
            lastAttackTime = now;
            return true;
        }
        return false;
    }

    // ─── Getters et Setters ───────────────────────────────────────────────────

    public double getX()        { return x; }
    public void   setX(double x){ this.x = x; }

    public double getY()        { return y; }
    public void   setY(double y){ this.y = y; }

    public double getCenterX()  { return x + width  / 2; }
    public double getCenterY()  { return y + height / 2; }

    public double getWidth()    { return width; }
    public double getHeight()   { return height; }

    public double getHp()       { return hp; }
    public double getMaxHp()    { return maxHp; }

    public double getSpeed()    { return speed; }
    public void   setSpeed(double speed) { this.speed = speed; }

    public int    getDamage()   { return damage; }

    public String getType()     { return type; }

    public boolean isFlashHit() { return flashHit; }

    @Override
    public String toString() {
        return String.format("Zombie[%s, x=%.1f, y=%.1f, hp=%.0f/%.0f]",
                type, x, y, hp, maxHp);
    }
}
