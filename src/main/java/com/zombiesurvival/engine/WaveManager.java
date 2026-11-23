package com.zombiesurvival.engine;

import com.zombiesurvival.model.*;

import java.util.Random;

/**
 * Gestionnaire de STAGES — v3.
 *
 * Chaque stage a une difficulté et un objectif clairs :
 *
 *  Stage 1  — DEBUTANT     : 5 zombies lents.  Objectif : tous tuer.
 *  Stage 2  — NORMAL       : 10 zombies.        Zombies plus rapides.
 *  Stage 3  — RAPIDE       : 15 zombies rapides. Rifle débloqué.
 *  Stage 4  — INTENSE      : 20 zombies + spéciaux.
 *  Stage 5  — FRENETIQUE   : 25 zombies très rapides.  Shotgun débloqué.
 *  Stage 6  — EXTREME      : 30 zombies + spéciaux fréquents.
 *  Stage 7  — CAUCHEMAR    : 35 zombies ultra-rapides.
 *  Stage 8  — ENFER        : 40 zombies + spéciaux partout.
 *  Stage 9  — APOCALYPSE   : 45 zombies, quasi-spéciaux.
 *  Stage 10 — BOSS FINAL   : 1 Boss massif.
 *
 * Entre chaque stage : 3 secondes de transition avec le nom du prochain stage.
 *
 * @author Elmehdi Eddareiy
 */
public class WaveManager {

    // Noms et descriptions des stages
    private static final String[] STAGE_NAMES = {
        "",                // index 0 inutilisé
        "DEBUTANT",        // 1
        "NORMAL",          // 2
        "RAPIDE",          // 3
        "INTENSE",         // 4
        "FRENETIQUE",      // 5
        "EXTREME",         // 6
        "CAUCHEMAR",       // 7
        "ENFER",           // 8
        "APOCALYPSE",      // 9
        "BOSS FINAL"       // 10
    };

    private static final String[] STAGE_DESC = {
        "",
        "5 zombies lents — Prends ton temps",
        "10 zombies — Ca commence !",
        "15 zombies rapides — Reste mobile !",
        "20 zombies + speciaux — Attention !",
        "25 zombies tres rapides — Ne t arrete pas !",
        "30 zombies — Infernal !",
        "35 zombies ultra-rapides — Cauchemar !",
        "40 zombies — L enfer est la !",
        "45 zombies quasi-speciaux — Apocalypse !",
        "LE BOSS FINAL — Bonne chance !"
    };

    private final Random random = new Random();

    private int     zombiesRemainingToSpawn = 0;
    private long    lastSpawnTime           = 0;
    private long    spawnIntervalMs         = 1000;
    private boolean waveInProgress          = false;

    // ── Démarrer un stage ─────────────────────────────────────────────────────

    public void startNextWave(GameData data) {
        int stage = data.getCurrentWave() + 1;
        data.setCurrentWave(stage);
        data.setWaveTransition(false);

        // Déblocage automatique d'armes
        if (stage == 3) data.getPlayer().unlockWeapon(Weapon.rifle());
        if (stage == 5) data.getPlayer().unlockWeapon(Weapon.shotgun());

        // Paramètres du stage
        StageConfig cfg = getStageConfig(stage);
        zombiesRemainingToSpawn = cfg.zombieCount;
        spawnIntervalMs         = cfg.spawnInterval;
        waveInProgress          = true;
        lastSpawnTime           = System.currentTimeMillis();

        System.out.printf("Stage %d [%s] — %d zombies, spawn toutes les %dms%n",
                stage, STAGE_NAMES[Math.min(stage, 10)], cfg.zombieCount, cfg.spawnInterval);
    }

    public void update(GameData data) {
        if (!waveInProgress) return;

        long now = System.currentTimeMillis();

        // Spawn progressif
        if (zombiesRemainingToSpawn > 0 && (now - lastSpawnTime) >= spawnIntervalMs) {
            spawnZombie(data);
            zombiesRemainingToSpawn--;
            lastSpawnTime = now;
        }

        // Stage terminé quand tous les zombies sont morts
        if (zombiesRemainingToSpawn <= 0 && data.getZombies().isEmpty()) {
            waveInProgress = false;

            int stage  = data.getCurrentWave();
            int bonus  = 150 * stage;
            data.addScore(bonus);
            data.getPlayer().addXP(60 * stage);
            data.addVisualEvent(GameData.MAP_WIDTH / 2,
                    GameData.MAP_HEIGHT / 2 - 60,
                    "STAGE " + stage + " TERMINE ! +" + bonus + " pts",
                    "#ffdd44", 18);

            // Vague 10 = BOSS FINAL : la partie est gagnée (si le joueur est vivant).
            if (stage >= 10 && data.getPlayer().isAlive()) {
                data.setGameWon(true);
                return;
            }
            data.startWaveTransition();
        }
    }

    // ── Configuration par stage ───────────────────────────────────────────────

    private static class StageConfig {
        int  zombieCount;
        long spawnInterval;  // ms entre chaque zombie
        StageConfig(int count, long interval) {
            zombieCount   = count;
            spawnInterval = interval;
        }
    }

    private StageConfig getStageConfig(int stage) {
        return switch (Math.min(stage, 10)) {
            case 1  -> new StageConfig(5,  1200);  // lent, espacé
            case 2  -> new StageConfig(10, 1000);
            case 3  -> new StageConfig(15,  800);  // Rifle dispo
            case 4  -> new StageConfig(20,  700);
            case 5  -> new StageConfig(25,  600);  // Shotgun dispo
            case 6  -> new StageConfig(30,  500);
            case 7  -> new StageConfig(35,  400);
            case 8  -> new StageConfig(40,  350);
            case 9  -> new StageConfig(45,  300);
            case 10 -> new StageConfig(1,    50);  // Boss
            default -> new StageConfig(50,  250);  // stages > 10
        };
    }

    // ── Spawn d'un zombie ─────────────────────────────────────────────────────

    private void spawnZombie(GameData data) {
        int    stage     = data.getCurrentWave();
        // Vitesse augmente progressivement avec le stage
        double baseSpeed = 1.0 + stage * 0.15;   // lent au début, croissance douce
        double[] pos     = randomBorderPosition();

        Zombie zombie;

        if (stage == 10) {
            zombie = new Boss(pos[0], pos[1]);
        } else {
            // Probabilité de zombie spécial croissante avec le stage
            double specialChance = Math.min(0.05 * (stage - 2), 0.60);
            if (stage >= 4 && random.nextDouble() < specialChance) {
                zombie = new ZombieSpecial(pos[0], pos[1]);
                zombie.setSpeed(baseSpeed + 0.4);   // spécial légèrement plus rapide
            } else {
                zombie = new Zombie(pos[0], pos[1]);
                zombie.setSpeed(Math.min(baseSpeed, 3.5)); // max 3.5 même en fin de jeu
            }
        }

        data.getZombies().add(zombie);
    }

    private double[] randomBorderPosition() {
        int    border = random.nextInt(4);
        double m = 25;
        double w = GameData.MAP_WIDTH, h = GameData.MAP_HEIGHT;
        if      (border == 0) return new double[]{ random.nextDouble() * w, -m };
        else if (border == 1) return new double[]{ random.nextDouble() * w, h + m };
        else if (border == 2) return new double[]{ -m, random.nextDouble() * h };
        else                  return new double[]{ w + m, random.nextDouble() * h };
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean isWaveInProgress()  { return waveInProgress; }

    public static String getStageName(int stage) {
        if (stage < 1 || stage > STAGE_NAMES.length - 1) return "STAGE " + stage;
        return STAGE_NAMES[stage];
    }

    public static String getStageDesc(int stage) {
        if (stage < 1 || stage > STAGE_DESC.length - 1) return "";
        return STAGE_DESC[stage];
    }
}
