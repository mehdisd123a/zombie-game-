package com.zombiesurvival.engine;

import com.zombiesurvival.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Détecte et gère toutes les collisions entre les entités du jeu.
 * v2 : obstacles, particules de mort.
 *
 * @author Elmehdi Eddareiy
 */
public class CollisionManager {

    private static final Random random = new Random();

    public static boolean checkAABB(double x1, double y1, double w1, double h1,
                                    double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 &&
               y1 < y2 + h2 && y1 + h1 > y2;
    }

    // ─── Zombie ↔ Joueur ──────────────────────────────────────────────────────
    public static void checkZombiePlayerCollisions(GameData data) {
        Player p = data.getPlayer();
        for (Zombie z : data.getZombies()) {
            if (checkAABB(z.getX(), z.getY(), z.getWidth(), z.getHeight(),
                          p.getX(), p.getY(), Player.WIDTH, Player.HEIGHT)) {
                if (z.canAttack()) {
                    boolean hit = p.takeDamage(z.getDamage());
                    if (hit) {
                        data.setPlayerJustHit(true);
                        data.addVisualEvent(p.getCenterX(), p.getY() - 20,
                                "-" + z.getDamage(), "#ff4444", 16);
                    }
                }
            }
        }
        if (!p.isAlive()) data.setGameOver(true);
    }

    // ─── Balle ↔ Zombie ───────────────────────────────────────────────────────
    public static void checkBulletZombieCollisions(GameData data) {
        List<Zombie> toRemove = new ArrayList<>();

        for (Bullet b : data.getBullets()) {
            if (!b.isActive()) continue;
            for (Zombie z : data.getZombies()) {
                if (toRemove.contains(z)) continue;
                if (checkAABB(b.getX(), b.getY(), Bullet.WIDTH, Bullet.HEIGHT,
                              z.getX(), z.getY(), z.getWidth(), z.getHeight())) {

                    // Particules d'impact
                    data.spawnHitParticles(b.getCenterX(), b.getCenterY());

                    z.takeDamage(b.getDamage());
                    b.deactivate();

                    if (z.isDead()) {
                        toRemove.add(z);
                        data.incrementZombiesKilled();
                        data.getPlayer().incrementKills();

                        // Score + XP + texte flottant
                        int pts, xp;
                        String color;
                        if (z instanceof Boss)               { pts = 500; xp = 200; color = "#ff4444"; }
                        else if (z instanceof ZombieSpecial) { pts = 30;  xp = 30;  color = "#dd88ff"; }
                        else                                 { pts = 10;  xp = 10;  color = "#ffdd44"; }
                        data.addScore(pts);
                        data.getPlayer().addXP(xp);
                        // Texte flottant "+10 pts"
                        data.addVisualEvent(z.getCenterX(), z.getY() - 10,
                                "+" + pts + " pts", color, 14);

                        // Particules de mort colorées selon le type
                        if (z instanceof Boss)
                            data.spawnDeathParticles(z.getCenterX(), z.getCenterY(), 220, 20, 20);
                        else if (z instanceof ZombieSpecial)
                            data.spawnDeathParticles(z.getCenterX(), z.getCenterY(), 160, 50, 220);
                        else
                            data.spawnDeathParticles(z.getCenterX(), z.getCenterY(), 60, 160, 50);

                        // Drop item (30%)
                        if (random.nextDouble() < 0.30) {
                            Item.ItemType dropType = random.nextBoolean()
                                    ? Item.ItemType.HEALTH : Item.ItemType.AMMO;
                            data.getItems().add(new Item(
                                    z.getCenterX() - Item.WIDTH  / 2,
                                    z.getCenterY() - Item.HEIGHT / 2,
                                    dropType));
                        }
                    }
                    break;
                }
            }
        }
        data.getZombies().removeAll(toRemove);
    }

    // ─── Joueur ↔ Items ───────────────────────────────────────────────────────
    public static void checkPlayerItemCollisions(GameData data) {
        Player p = data.getPlayer();
        List<Item> toRemove = new ArrayList<>();
        for (Item item : data.getItems()) {
            if (item.isCollected() || item.isExpired()) { toRemove.add(item); continue; }
            if (checkAABB(p.getX(), p.getY(), Player.WIDTH, Player.HEIGHT,
                          item.getX(), item.getY(), Item.WIDTH, Item.HEIGHT)) {
                item.collect(p);
                toRemove.add(item);
            }
        }
        data.getItems().removeAll(toRemove);
    }

    // ─── Balle ↔ Obstacles ────────────────────────────────────────────────────
    public static void checkBulletObstacleCollisions(GameData data) {
        for (Bullet b : data.getBullets()) {
            if (!b.isActive()) continue;
            for (Obstacle o : data.getObstacles()) {
                if (checkAABB(b.getX(), b.getY(), Bullet.WIDTH, Bullet.HEIGHT,
                              o.getX(), o.getY(), o.getWidth(), o.getHeight())) {
                    data.spawnHitParticles(b.getCenterX(), b.getCenterY());
                    b.deactivate();
                    break;
                }
            }
        }
    }

    // ─── Zombie ↔ Obstacles (répulsion) ──────────────────────────────────────
    public static void resolveZombieObstacleCollisions(GameData data) {
        for (Zombie z : data.getZombies()) {
            for (Obstacle o : data.getObstacles()) {
                if (checkAABB(z.getX(), z.getY(), z.getWidth(), z.getHeight(),
                              o.getX(), o.getY(), o.getWidth(), o.getHeight())) {
                    // Repousser le zombie hors de l'obstacle
                    double overlapX = Math.min(z.getX() + z.getWidth(),  o.getX() + o.getWidth())
                                    - Math.max(z.getX(), o.getX());
                    double overlapY = Math.min(z.getY() + z.getHeight(), o.getY() + o.getHeight())
                                    - Math.max(z.getY(), o.getY());
                    if (overlapX < overlapY) {
                        double dir = z.getCenterX() < o.getX() + o.getWidth() / 2 ? -1 : 1;
                        z.setX(z.getX() + dir * overlapX);
                    } else {
                        double dir = z.getCenterY() < o.getY() + o.getHeight() / 2 ? -1 : 1;
                        z.setY(z.getY() + dir * overlapY);
                    }
                }
            }
        }
    }

    // ─── Joueur ↔ Obstacles ───────────────────────────────────────────────────
    public static void resolvePlayerObstacleCollisions(GameData data) {
        Player p = data.getPlayer();
        for (Obstacle o : data.getObstacles()) {
            if (checkAABB(p.getX(), p.getY(), Player.WIDTH, Player.HEIGHT,
                          o.getX(), o.getY(), o.getWidth(), o.getHeight())) {
                double overlapX = Math.min(p.getX() + Player.WIDTH,  o.getX() + o.getWidth())
                                - Math.max(p.getX(), o.getX());
                double overlapY = Math.min(p.getY() + Player.HEIGHT, o.getY() + o.getHeight())
                                - Math.max(p.getY(), o.getY());
                if (overlapX < overlapY) {
                    double dir = p.getCenterX() < o.getX() + o.getWidth() / 2 ? -1 : 1;
                    p.setX(p.getX() + dir * overlapX);
                } else {
                    double dir = p.getCenterY() < o.getY() + o.getHeight() / 2 ? -1 : 1;
                    p.setY(p.getY() + dir * overlapY);
                }
            }
        }
    }

    // ─── Nettoyage balles hors carte ──────────────────────────────────────────
    public static void cleanupBullets(GameData data) {
        Iterator<Bullet> it = data.getBullets().iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            if (!b.isActive() || b.isOutOfBounds(GameData.MAP_WIDTH, GameData.MAP_HEIGHT)) it.remove();
        }
    }
}
