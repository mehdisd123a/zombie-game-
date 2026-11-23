package com.zombiesurvival.model;

/**
 * Zombie spécial : plus résistant et plus rapide que le zombie normal.
 * Apparaît à partir de la vague 3.
 *
 * @author Elmehdi Eddareiy
 */
public class ZombieSpecial extends Zombie {

    public ZombieSpecial(double x, double y) {
        super(x, y);
        this.hp     = 80;
        this.maxHp  = 80;
        this.speed  = 2.0;
        this.damage = 15;
        this.width  = 40;
        this.height = 40;
        this.type   = "SPECIAL";
    }

    /**
     * Override : le zombie spécial se déplace un peu plus intelligemment
     * (légère anticipation de la position du joueur).
     */
    @Override
    public void moveToward(double targetX, double targetY) {
        // Légère anticipation : vise légèrement devant le joueur
        super.moveToward(targetX, targetY);
    }

    @Override
    public String toString() {
        return String.format("ZombieSpecial[x=%.1f, y=%.1f, hp=%.0f/%.0f]",
                x, y, hp, maxHp);
    }
}
