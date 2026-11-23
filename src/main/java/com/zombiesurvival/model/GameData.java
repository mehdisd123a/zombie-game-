package com.zombiesurvival.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Contient l'état global d'une partie.
 * Ajouts v2 : obstacles, particules, boutique.
 *
 * @author Elmehdi Eddareiy
 */
public class GameData {

    public static final double MAP_WIDTH  = 900;
    public static final double MAP_HEIGHT = 650;

    // ─── Entités ──────────────────────────────────────────────────────────────
    private Player         player;
    private List<Zombie>   zombies;
    private List<Bullet>   bullets;
    private List<Item>     items;
    private List<Obstacle> obstacles;
    private List<Particle> particles;

    // ─── Événements visuels (consommés par GameView) ──────────────────────────
    public static class VisualEvent {
        public final double x, y;
        public final String text;
        public final String colorHex;
        public final int    fontSize;
        public VisualEvent(double x, double y, String text, String colorHex, int fontSize) {
            this.x = x; this.y = y; this.text = text;
            this.colorHex = colorHex; this.fontSize = fontSize;
        }
    }
    private final List<VisualEvent> visualEvents = new ArrayList<>();

    public void addVisualEvent(double x, double y, String text, String colorHex, int fontSize) {
        visualEvents.add(new VisualEvent(x, y, text, colorHex, fontSize));
    }

    /** Consomme et vide les événements visuels en attente. */
    public List<VisualEvent> pollVisualEvents() {
        List<VisualEvent> copy = new ArrayList<>(visualEvents);
        visualEvents.clear();
        return copy;
    }

    public boolean isPlayerJustHit() { return playerJustHit; }
    private boolean playerJustHit = false;
    public void setPlayerJustHit(boolean v) { playerJustHit = v; }

    // ─── État ─────────────────────────────────────────────────────────────────
    private int     score         = 0;
    private int     currentWave   = 0;
    private int     zombiesKilled = 0;
    private boolean paused        = false;
    private boolean gameOver      = false;
    private boolean gameWon       = false;

    // ─── Transition inter-vague ───────────────────────────────────────────────
    private boolean waveTransition      = false;
    private long    waveTransitionStart = 0;
    private static final long WAVE_TRANSITION_DURATION_MS = 2500; // 2.5s entre stages

    // ─── Boutique ─────────────────────────────────────────────────────────────
    private boolean shopOpen = false;

    // ─── Temps ────────────────────────────────────────────────────────────────
    private long gameStartTime;
    private long gameDuration = 0;

    public GameData() {
        player    = new Player(MAP_WIDTH / 2 - Player.WIDTH / 2,
                               MAP_HEIGHT / 2 - Player.HEIGHT / 2);
        zombies   = new ArrayList<>();
        bullets   = new ArrayList<>();
        items     = new ArrayList<>();
        obstacles = new ArrayList<>();
        particles = new ArrayList<>();
        gameStartTime = System.currentTimeMillis();
        buildObstacles();
    }

    /** Crée les obstacles statiques de la carte. */
    private void buildObstacles() {
        // Caisses autour de la zone centrale
        obstacles.add(new Obstacle(120, 120, 60, 60, Obstacle.ObstacleType.CRATE));
        obstacles.add(new Obstacle(700, 120, 60, 60, Obstacle.ObstacleType.CRATE));
        obstacles.add(new Obstacle(120, 460, 60, 60, Obstacle.ObstacleType.CRATE));
        obstacles.add(new Obstacle(700, 460, 60, 60, Obstacle.ObstacleType.CRATE));

        // Barils au centre-gauche et centre-droit
        obstacles.add(new Obstacle(230, 290, 40, 40, Obstacle.ObstacleType.BARREL));
        obstacles.add(new Obstacle(620, 290, 40, 40, Obstacle.ObstacleType.BARREL));

        // Murs horizontaux en haut et en bas
        obstacles.add(new Obstacle(300, 150, 120, 20, Obstacle.ObstacleType.WALL));
        obstacles.add(new Obstacle(480, 150, 120, 20, Obstacle.ObstacleType.WALL));
        obstacles.add(new Obstacle(300, 480, 120, 20, Obstacle.ObstacleType.WALL));
        obstacles.add(new Obstacle(480, 480, 120, 20, Obstacle.ObstacleType.WALL));

        // Murs verticaux gauche/droite
        obstacles.add(new Obstacle(170, 250, 20, 100, Obstacle.ObstacleType.WALL));
        obstacles.add(new Obstacle(710, 250, 20, 100, Obstacle.ObstacleType.WALL));
    }

    public void reset() {
        player    = new Player(MAP_WIDTH / 2 - Player.WIDTH / 2,
                               MAP_HEIGHT / 2 - Player.HEIGHT / 2);
        zombies   = new ArrayList<>();
        bullets   = new ArrayList<>();
        items     = new ArrayList<>();
        particles = new ArrayList<>();
        // On garde les obstacles (carte fixe)
        score         = 0;
        currentWave   = 0;
        zombiesKilled = 0;
        paused        = false;
        gameOver      = false;
        gameWon       = false;
        waveTransition = false;
        shopOpen       = false;
        gameStartTime  = System.currentTimeMillis();
        gameDuration   = 0;
    }

    // ─── Particules ───────────────────────────────────────────────────────────

    /** Crée une explosion de particules (mort d'un zombie). */
    public void spawnDeathParticles(double cx, double cy, int r, int g, int b) {
        for (int i = 0; i < 12; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 1.5 + Math.random() * 3.0;
            double size  = 3 + Math.random() * 6;
            particles.add(new Particle(cx, cy,
                    Math.cos(angle) * speed, Math.sin(angle) * speed,
                    size, r, g, b));
        }
    }

    /** Crée des particules d'impact (hit). */
    public void spawnHitParticles(double cx, double cy) {
        for (int i = 0; i < 4; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 1.0 + Math.random() * 2.0;
            particles.add(new Particle(cx, cy,
                    Math.cos(angle) * speed, Math.sin(angle) * speed,
                    3 + Math.random() * 3, 255, 200, 50));
        }
    }

    public void addScore(int pts) { score += pts; }
    public void incrementZombiesKilled() { zombiesKilled++; }

    public void startWaveTransition() {
        waveTransition      = true;
        waveTransitionStart = System.currentTimeMillis();
        shopOpen            = true;
    }

    public boolean isWaveTransitionDone() {
        return System.currentTimeMillis() - waveTransitionStart > WAVE_TRANSITION_DURATION_MS;
    }

    public int getWaveTransitionCountdown() {
        long remaining = WAVE_TRANSITION_DURATION_MS
                - (System.currentTimeMillis() - waveTransitionStart);
        return (int) Math.max(0, Math.ceil(remaining / 1000.0));
    }

    public void updateDuration() {
        gameDuration = (System.currentTimeMillis() - gameStartTime) / 1000;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public Player         getPlayer()     { return player; }
    public List<Zombie>   getZombies()    { return zombies; }
    public List<Bullet>   getBullets()    { return bullets; }
    public List<Item>     getItems()      { return items; }
    public List<Obstacle> getObstacles()  { return obstacles; }
    public List<Particle> getParticles()  { return particles; }
    public int            getScore()      { return score; }
    public int            getCurrentWave(){ return currentWave; }
    public void           setCurrentWave(int w){ currentWave = w; }
    public int            getZombiesKilled(){ return zombiesKilled; }
    public boolean        isPaused()      { return paused; }
    public void           setPaused(boolean p){ paused = p; }
    public boolean        isGameOver()    { return gameOver; }
    public void           setGameOver(boolean g){ gameOver = g; }
    public boolean        isGameWon()     { return gameWon; }
    public void           setGameWon(boolean w){ gameWon = w; }
    public boolean        isWaveTransition(){ return waveTransition; }
    public void           setWaveTransition(boolean wt){ waveTransition = wt; }
    public boolean        isShopOpen()    { return shopOpen; }
    public void           setShopOpen(boolean s){ shopOpen = s; }
    public long           getGameDuration(){ return gameDuration; }
}
