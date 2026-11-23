package com.zombiesurvival.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Joueur — v3 : vitesse augmentée, munitions infinies.
 *
 * @author Elmehdi Eddareiy
 */
public class Player {

    private double x, y;
    public static final double WIDTH  = 40;
    public static final double HEIGHT = 40;

    private int    hp;
    private int    maxHp;
    private double speed;
    private double angle;

    // Direction de déplacement courante (pour animation)
    private double moveX = 0;   // -1 gauche, +1 droite, 0 arrêt
    private double moveY = 0;   // -1 haut, +1 bas
    private boolean isMoving = false;

    // Compteur d'animation (pas de marche)
    private double walkCycle = 0;

    private final List<Weapon> weapons          = new ArrayList<>();
    private int                currentWeaponIndex = 0;

    // XP & Niveaux
    private int     xp            = 0;
    private int     level         = 1;
    private int     xpToNextLevel = 100;
    private boolean justLeveledUp = false;
    private long    levelUpTime;

    // Dégâts reçus
    private long    lastDamageTime  = 0;
    private static final long DAMAGE_COOLDOWN_MS = 400;
    private boolean flashRed        = false;
    private long    flashStartTime;

    private int kills = 0;

    public Player(double x, double y) {
        this.x     = x;
        this.y     = y;
        this.hp    = 100;
        this.maxHp = 100;
        this.speed = 5.5;        // ← vitesse augmentée (était 3.5)
        weapons.add(Weapon.pistol());
    }

    // ── Armes ──────────────────────────────────────────────────────────────────

    public Weapon getWeapon() { return weapons.get(currentWeaponIndex); }

    public void switchWeapon(int index) {
        if (index >= 0 && index < weapons.size()) currentWeaponIndex = index;
    }

    public void unlockWeapon(Weapon weapon) {
        for (Weapon w : weapons) if (w.getType() == weapon.getType()) return;
        weapons.add(weapon);
    }

    public List<Weapon> getWeapons()          { return weapons; }
    public int          getCurrentWeaponIndex(){ return currentWeaponIndex; }

    // ── XP / Niveau ────────────────────────────────────────────────────────────

    public void addXP(int amount) {
        xp += amount;
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            level++;
            xpToNextLevel = (int)(xpToNextLevel * 1.4);
            maxHp  += 15;
            hp      = Math.min(hp + 25, maxHp);
            speed  += 0.15;
            justLeveledUp = true;
            levelUpTime   = System.currentTimeMillis();
        }
    }

    public boolean consumeLevelUp() {
        if (justLeveledUp && System.currentTimeMillis() - levelUpTime > 2500)
            justLeveledUp = false;
        return justLeveledUp;
    }

    public int  getXp()           { return xp; }
    public int  getLevel()        { return level; }
    public int  getXpToNextLevel(){ return xpToNextLevel; }

    // ── Combat ─────────────────────────────────────────────────────────────────

    public void move(double dx, double dy) {
        // Mémoriser la direction pour l'animation
        this.moveX = dx;
        this.moveY = dy;
        this.isMoving = (dx != 0 || dy != 0);

        if (dx != 0 && dy != 0) {
            double len = Math.sqrt(dx * dx + dy * dy);
            dx = dx / len * speed;
            dy = dy / len * speed;
        } else {
            dx *= speed;
            dy *= speed;
        }
        x += dx;
        y += dy;

        // Avancer le cycle de marche
        if (isMoving) walkCycle += 0.18;
    }

    public void stopMoving() {
        this.moveX    = 0;
        this.moveY    = 0;
        this.isMoving = false;
    }

    public boolean takeDamage(int amount) {
        long now = System.currentTimeMillis();
        if (now - lastDamageTime < DAMAGE_COOLDOWN_MS) return false;
        lastDamageTime = now;
        hp = Math.max(0, hp - amount);
        flashRed      = true;
        flashStartTime = now;
        return true;
    }

    public void heal(int amount) { hp = Math.min(maxHp, hp + amount); }
    public boolean isAlive()     { return hp > 0; }
    public void incrementKills() { kills++; }

    public void update() {
        if (flashRed && System.currentTimeMillis() - flashStartTime > 180) flashRed = false;
        getWeapon().updateReload();
    }

    // ── Getters / Setters ───────────────────────────────────────────────────────

    public double  getX()          { return x; }
    public void    setX(double x)  { this.x = x; }
    public double  getY()          { return y; }
    public void    setY(double y)  { this.y = y; }
    public double  getCenterX()    { return x + WIDTH  / 2; }
    public double  getCenterY()    { return y + HEIGHT / 2; }
    public int     getHp()         { return hp; }
    public int     getMaxHp()      { return maxHp; }
    public double  getSpeed()      { return speed; }
    public void    setSpeed(double s){ speed = s; }
    public double  getAngle()      { return angle; }
    public void    setAngle(double a){ angle = a; }
    public boolean isFlashRed()    { return flashRed; }
    public int     getKills()      { return kills; }

    // Getters animation
    public double  getMoveX()      { return moveX; }
    public double  getMoveY()      { return moveY; }
    public boolean isMoving()      { return isMoving; }
    public double  getWalkCycle()  { return walkCycle; }
}
