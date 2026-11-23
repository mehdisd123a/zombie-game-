package com.zombiesurvival.service;

import com.zombiesurvival.model.GameData;

/**
 * Calcule et enregistre les scores en fin de partie.
 * Fait le lien entre GameData et DatabaseService.
 *
 * @author Elmehdi Eddareiy
 */
public class ScoreService {

    private final DatabaseService db      = DatabaseService.getInstance();
    private final Session         session = Session.getInstance();

    /**
     * Sauvegarde les résultats d'une partie si l'utilisateur est connecté.
     * Met à jour les statistiques globales.
     *
     * @param gameData état final de la partie
     */
    public void saveGameResult(GameData gameData) {
        if (!session.isLoggedIn()) {
            System.out.println("Mode guest : score non sauvegardé.");
            return;
        }

        int userId        = session.getCurrentUser().getId();
        int score         = gameData.getScore();
        int waveReached   = gameData.getCurrentWave();
        int zombiesKilled = gameData.getZombiesKilled();
        long duration     = gameData.getGameDuration();

        db.saveGame(userId, score, waveReached, zombiesKilled, duration);
        db.updateStatistics(userId, score, zombiesKilled);

        System.out.printf("Score sauvegardé : %d pts, vague %d, %d zombies tués%n",
                score, waveReached, zombiesKilled);
    }

    /**
     * @return le meilleur score de l'utilisateur connecté, ou 0 si guest
     */
    public int getPersonalBest() {
        if (!session.isLoggedIn()) return 0;
        return db.getBestScore(session.getCurrentUser().getId());
    }
}
