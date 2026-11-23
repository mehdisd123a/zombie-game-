package com.zombiesurvival.model;

/**
 * Arme du joueur — v3 : munitions INFINIES, cadence augmentée.
 * Plus de rechargement, tir libre et rapide.
 *
 * @author Elmehdi Eddareiy
 */
public class Weapon {

    public enum WeaponType { PISTOL, RIFLE, SHOTGUN }

    private final WeaponType type;
    private final String     name;
    private final int        damage;
    private final int        pelletsPerShot;
    private final double     bulletSpeed;

    // Cadence de tir (délai entre deux balles)
    private long fireCooldownMs;
    private long lastShotTime = 0;

    // ── Fabriques ──────────────────────────────────────────────────────────────

    private Weapon(WeaponType type, String name, int damage,
                   long fireCooldownMs, int pelletsPerShot, double bulletSpeed) {
        this.type           = type;
        this.name           = name;
        this.damage         = damage;
        this.fireCooldownMs = fireCooldownMs;
        this.pelletsPerShot = pelletsPerShot;
        this.bulletSpeed    = bulletSpeed;
    }

    /** Pistolet : cadence rapide, dégâts moyens */
    public static Weapon pistol() {
        return new Weapon(WeaponType.PISTOL, "Pistol", 25, 120, 1, 11.0);
    }

    /** Rifle : très rapide, dégâts élevés */
    public static Weapon rifle() {
        return new Weapon(WeaponType.RIFLE, "Rifle", 20, 60, 1, 15.0);
    }

    /** Shotgun : 5 balles en éventail, cadence moyenne */
    public static Weapon shotgun() {
        return new Weapon(WeaponType.SHOTGUN, "Shotgun", 40, 400, 5, 10.0);
    }

    // ── Tir ────────────────────────────────────────────────────────────────────

    /** @return true si l'arme peut tirer maintenant */
    public boolean canShoot() {
        return System.currentTimeMillis() - lastShotTime >= fireCooldownMs;
    }

    /** Enregistre un tir. Munitions infinies — pas de décompte. */
    public void shoot() {
        lastShotTime = System.currentTimeMillis();
    }

    /** Accélère la cadence de tir (bonus de stage). */
    public void boostFireRate(double factor) {
        fireCooldownMs = Math.max(40, (long)(fireCooldownMs / factor));
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public WeaponType getType()          { return type; }
    public String     getName()          { return name; }
    public int        getDamage()        { return damage; }
    public int        getPelletsPerShot(){ return pelletsPerShot; }
    public double     getBulletSpeed()   { return bulletSpeed; }

    // isReloading et getAmmoInMag conservés pour compatibilité avec GameView
    public boolean    isReloading()      { return false; }
    public int        getAmmoInMag()     { return 999; }
    public int        getTotalAmmo()     { return 999; }
    public int        getMaxAmmo()       { return 999; }
    public long       getReloadDurationMs()  { return 0; }
    public long       getReloadStartTime()   { return 0; }
    public void       addAmmo(int n)         {}   // no-op
    public void       startReload()          {}   // no-op
    public void       updateReload()         {}   // no-op

    @Override
    public String toString() { return name + " (infini)"; }
}
