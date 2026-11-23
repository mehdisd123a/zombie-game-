package com.zombiesurvival.model;

/**
 * Boss : ennemi de fin de niveau, apparaît à la vague 10.
 * Très résistant, grand et inflige beaucoup de dégâts.
 * Charge vers le joueur par intermittence.
 *
 * @author Elmehdi Eddareiy
 */
public class Boss extends ZombieSpecial {

    private int     phase           = 1;   // Phase 1 : marche, Phase 2 : charge
    private boolean charging        = false;
    private long    chargeStartTime = 0;
    private static final long CHARGE_DURATION_MS = 800;
    private static final long CHARGE_COOLDOWN_MS = 3000;
    private long    lastChargeTime  = 0;
    private double  chargeSpeedMultiplier = 3.5;

    public Boss(double x, double y) {
        super(x, y);
        this.hp     = 500;
        this.maxHp  = 500;
        this.speed  = 1.0;
        this.damage = 30;
        this.width  = 80;
        this.height = 80;
        this.type   = "BOSS";
    }

    /**
     * Override : le boss charge vers le joueur périodiquement.
     */
    @Override
    public void moveToward(double targetX, double targetY) {
        long now = System.currentTimeMillis();

        // Détecter transition vers phase 2 (moins de 50% HP)
        if (hp < maxHp * 0.5) {
            phase = 2;
        }

        // Gérer la charge
        if (charging) {
            if (now - chargeStartTime > CHARGE_DURATION_MS) {
                charging = false;
                lastChargeTime = now;
            }
            // Pendant la charge, vitesse multipliée
            double oldSpeed = this.speed;
            this.speed = oldSpeed * chargeSpeedMultiplier;
            super.moveToward(targetX, targetY);
            this.speed = oldSpeed;
        } else {
            // Déclencher une nouvelle charge si le cooldown est écoulé
            if (now - lastChargeTime > CHARGE_COOLDOWN_MS) {
                charging       = true;
                chargeStartTime = now;
            }
            super.moveToward(targetX, targetY);
        }

        // Phase 2 : dégâts plus élevés
        if (phase == 2) {
            this.damage = 45;
        }
    }

    public boolean isCharging() { return charging; }
    public int     getPhase()   { return phase; }

    @Override
    public String toString() {
        return String.format("Boss[phase=%d, x=%.1f, y=%.1f, hp=%.0f/%.0f, charging=%b]",
                phase, x, y, hp, maxHp, charging);
    }
}
