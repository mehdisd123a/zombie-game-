package com.zombiesurvival.model;

/**
 * Représente une entrée dans le classement des meilleurs scores.
 *
 * @author Elmehdi Eddareiy
 */
public class LeaderboardEntry {

    private String username;
    private int    bestScore;
    private int    totalGames;
    private int    totalZombiesKilled;

    public LeaderboardEntry(String username, int bestScore,
                            int totalGames, int totalZombiesKilled) {
        this.username           = username;
        this.bestScore          = bestScore;
        this.totalGames         = totalGames;
        this.totalZombiesKilled = totalZombiesKilled;
    }

    public String getUsername()           { return username; }
    public int    getBestScore()          { return bestScore; }
    public int    getTotalGames()         { return totalGames; }
    public int    getTotalZombiesKilled() { return totalZombiesKilled; }

    @Override
    public String toString() {
        return String.format("LeaderboardEntry[user=%s, bestScore=%d, games=%d]",
                username, bestScore, totalGames);
    }
}
