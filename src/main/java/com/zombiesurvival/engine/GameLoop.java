package com.zombiesurvival.engine;

import com.zombiesurvival.model.*;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Boucle principale du jeu — v2.
 * Gère : armes multiples, obstacles, particules, XP.
 *
 * @author Elmehdi Eddareiy
 */
public class GameLoop {

    private AnimationTimer  timer;
    private final GameData  gameData;
    private final InputHandler inputHandler;
    private final WaveManager  waveManager;
    private final Runnable  renderCallback;
    private final Runnable  gameOverCallback;
    private final Runnable  pauseCallback;
    private boolean         running = false;

    public GameLoop(GameData gameData, InputHandler inputHandler,
                    WaveManager waveManager,
                    Runnable renderCallback,
                    Runnable gameOverCallback,
                    Runnable pauseCallback) {
        this.gameData        = gameData;
        this.inputHandler    = inputHandler;
        this.waveManager     = waveManager;
        this.renderCallback  = renderCallback;
        this.gameOverCallback = gameOverCallback;
        this.pauseCallback   = pauseCallback;

        this.timer = new AnimationTimer() {
            @Override public void handle(long now) { tick(); }
        };
    }

    public void start()  { running = true;  timer.start(); }
    public void stop()   { running = false; timer.stop();  }
    public boolean isRunning() { return running; }

    // ─── Tick ─────────────────────────────────────────────────────────────────

    private void tick() {
        if (!running) return;

        if (inputHandler.isEscPressed()) {
            inputHandler.clearFrameEvents();
            if (pauseCallback != null) pauseCallback.run();
            return;
        }
        if (gameData.isPaused()) {
            inputHandler.clearFrameEvents();
            renderCallback.run();
            return;
        }
        if (gameData.isGameOver() || gameData.isGameWon()) {
            timer.stop(); running = false;
            if (gameOverCallback != null) gameOverCallback.run();
            return;
        }

        update();
        renderCallback.run();
        inputHandler.clearFrameEvents();
    }
    // ─── Update ───────────────────────────────────────────────────────────────

    private void update() {
        gameData.updateDuration();
        Player player = gameData.getPlayer();

        // ── 1. Déplacement ────────────────────────────────────────────────────
        double dx = 0, dy = 0;
        if (inputHandler.isMovingUp())    dy -= 1;
        if (inputHandler.isMovingDown())  dy += 1;
        if (inputHandler.isMovingLeft())  dx -= 1;
        if (inputHandler.isMovingRight()) dx += 1;
        if (dx != 0 || dy != 0) {
            player.move(dx, dy);
        } else {
            player.stopMoving();
        }

        // Clamp
        player.setX(Math.max(0, Math.min(GameData.MAP_WIDTH  - Player.WIDTH,  player.getX())));
        player.setY(Math.max(0, Math.min(GameData.MAP_HEIGHT - Player.HEIGHT, player.getY())));

        // ── 2. Collisions Joueur ↔ Obstacles ─────────────────────────────────
        CollisionManager.resolvePlayerObstacleCollisions(gameData);

        // ── 3. Rotation vers souris ───────────────────────────────────────────
        player.setAngle(Math.atan2(
                inputHandler.getMouseY() - player.getCenterY(),
                inputHandler.getMouseX() - player.getCenterX()));

        // ── 4. Switch d'arme ──────────────────────────────────────────────────
        int sw = inputHandler.getWeaponSwitchIndex();
        if (sw >= 0) {
            player.switchWeapon(sw);
        } else if (sw == -2) { // scroll up → suivante
            player.switchWeapon((player.getCurrentWeaponIndex() + 1) % player.getWeapons().size());
        } else if (sw == -3) { // scroll down → précédente
            int prev = player.getCurrentWeaponIndex() - 1;
            player.switchWeapon(prev < 0 ? player.getWeapons().size() - 1 : prev);
        }

        // ── 5. Tir ────────────────────────────────────────────────────────────
        if (inputHandler.isShooting()) {
            Weapon weapon = player.getWeapon();
            if (weapon.canShoot()) {
                weapon.shoot();
                fireBullets(player, weapon);
            }
        }

        // ── 6. Rechargement ───────────────────────────────────────────────────
        if (inputHandler.isReloading()) player.getWeapon().startReload();
        player.update();

        // ── 6b. Boutique (H = soin, M = munitions) ────────────────────────────
        if (gameData.isShopOpen()) {
            if (inputHandler.isShopHeal() && gameData.getScore() >= 50) {
                gameData.addScore(-50);
                player.heal(50);
                gameData.addVisualEvent(player.getCenterX(), player.getY() - 20,
                        "+50 HP", "#44ff44", 16);
            }
            if (inputHandler.isShopAmmo() && gameData.getScore() >= 40) {
                gameData.addScore(-40);
                for (Weapon w : player.getWeapons()) w.addAmmo(30);
                gameData.addVisualEvent(player.getCenterX(), player.getY() - 20,
                        "+Balles", "#ffdd44", 14);
            }
        }

        // ── 7. Balles ─────────────────────────────────────────────────────────
        List<Bullet> bCopy = new ArrayList<>(gameData.getBullets());
        for (Bullet b : bCopy) b.move();
        CollisionManager.checkBulletObstacleCollisions(gameData);
        CollisionManager.checkBulletZombieCollisions(gameData);
        CollisionManager.cleanupBullets(gameData);

        // ── 8. Zombies ────────────────────────────────────────────────────────
        List<Zombie> zCopy = new ArrayList<>(gameData.getZombies());
        for (Zombie z : zCopy) {
            z.moveToward(player.getCenterX(), player.getCenterY());
            z.update();
        }
        CollisionManager.resolveZombieObstacleCollisions(gameData);
        CollisionManager.checkZombiePlayerCollisions(gameData);

        // ── 9. Items ──────────────────────────────────────────────────────────
        CollisionManager.checkPlayerItemCollisions(gameData);
        gameData.getItems().removeIf(Item::isExpired);

        // ── 10. Particules ────────────────────────────────────────────────────
        Iterator<Particle> pit = gameData.getParticles().iterator();
        while (pit.hasNext()) {
            Particle p = pit.next();
            p.update();
            if (p.isDead()) pit.remove();
        }

        // ── 11. Vagues ────────────────────────────────────────────────────────
        if (gameData.isWaveTransition()) {
            if (gameData.isWaveTransitionDone()) {
                gameData.setWaveTransition(false);
                gameData.setShopOpen(false);
                waveManager.startNextWave(gameData);
            }
        } else if (!waveManager.isWaveInProgress()) {
            waveManager.startNextWave(gameData);
        } else {
            waveManager.update(gameData);
        }
    }

    // ─── Tir automatique vers le zombie le plus proche ───────────────────────

    private void fireBullets(Player player, Weapon weapon) {
        double bx = player.getCenterX() - Bullet.WIDTH  / 2;
        double by = player.getCenterY() - Bullet.HEIGHT / 2;

        // Trouver le zombie le plus proche
        double tx, ty;
        Zombie nearest = findNearestZombie(player);

        if (nearest != null) {
            // Tirer vers le zombie le plus proche
            tx = nearest.getCenterX();
            ty = nearest.getCenterY();
        } else {
            // Pas de zombie : tirer dans la direction du déplacement
            // ou vers la droite par défaut
            double angle = player.getAngle();
            tx = bx + Math.cos(angle) * 200;
            ty = by + Math.sin(angle) * 200;
        }

        // Mettre à jour l'angle du joueur vers la cible
        player.setAngle(Math.atan2(ty - by, tx - bx));

        int    pellets = weapon.getPelletsPerShot();
        double speed   = weapon.getBulletSpeed();

        if (pellets == 1) {
            gameData.getBullets().add(new Bullet(bx, by, tx, ty,
                    weapon.getDamage(), speed));
        } else {
            // Shotgun : 5 balles en éventail autour de la cible
            double baseAngle   = Math.atan2(ty - by, tx - bx);
            double spreadAngle = Math.toRadians(25);
            for (int i = 0; i < pellets; i++) {
                double angle = baseAngle - spreadAngle / 2
                        + (spreadAngle / (pellets - 1)) * i;
                gameData.getBullets().add(new Bullet(bx, by,
                        Math.cos(angle), Math.sin(angle),
                        weapon.getDamage(), speed, true));
            }
        }
    }

    /** Retourne le zombie le plus proche du joueur. */
    private Zombie findNearestZombie(Player player) {
        Zombie nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Zombie z : gameData.getZombies()) {
            double dx   = z.getCenterX() - player.getCenterX();
            double dy   = z.getCenterY() - player.getCenterY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < minDist) {
                minDist = dist;
                nearest = z;
            }
        }
        return nearest;
    }
}
