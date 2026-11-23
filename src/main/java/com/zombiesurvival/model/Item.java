package com.zombiesurvival.model;

/**
 * Représente un objet bonus ramassable sur la carte.
 * Peut être une trousse de soins (HEALTH) ou des munitions (AMMO).
 *
 * @author Elmehdi Eddareiy
 */
public class Item {

    /**
     * Types d'objets disponibles.
     */
    public enum ItemType {
        HEALTH, // Restaure des points de vie
        AMMO    // Recharge des munitions
    }

    private double   x;
    private double   y;
    private ItemType type;
    private int      value;       // HP soignés ou balles ajoutées
    private boolean  collected = false;

    // Dimensions visuelles
    public static final double WIDTH  = 22;
    public static final double HEIGHT = 22;

    // Durée de vie de l'item sur la carte (10 secondes)
    private long spawnTime;
    private static final long LIFETIME_MS = 10_000;

    public Item(double x, double y, ItemType type) {
        this.x         = x;
        this.y         = y;
        this.type      = type;
        this.spawnTime = System.currentTimeMillis();

        // Valeur selon le type
        this.value = (type == ItemType.HEALTH) ? 25 : 15;
    }

    /**
     * @return true si l'item a expiré (trop longtemps sur la carte)
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > LIFETIME_MS;
    }

    /**
     * Collecte l'item : applique l'effet sur le joueur.
     * @param player joueur qui ramasse l'item
     */
    public void collect(Player player) {
        if (!collected) {
            collected = true;
            if (type == ItemType.HEALTH) {
                player.heal(value);
            } else {
                player.getWeapon().addAmmo(value);
            }
        }
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public double   getX()          { return x; }
    public double   getY()          { return y; }
    public double   getCenterX()    { return x + WIDTH  / 2; }
    public double   getCenterY()    { return y + HEIGHT / 2; }
    public ItemType getType()       { return type; }
    public int      getValue()      { return value; }
    public boolean  isCollected()   { return collected; }

    @Override
    public String toString() {
        return String.format("Item[%s, valeur=%d, x=%.1f, y=%.1f]",
                type, value, x, y);
    }
}
