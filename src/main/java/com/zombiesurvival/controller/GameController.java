package com.zombiesurvival.controller;

import com.zombiesurvival.engine.GameLoop;
import com.zombiesurvival.engine.InputHandler;
import com.zombiesurvival.engine.WaveManager;
import com.zombiesurvival.model.GameData;
import com.zombiesurvival.service.ScoreService;
import com.zombiesurvival.view.GameOverView;
import com.zombiesurvival.view.GameView;
import com.zombiesurvival.view.PauseView;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;

/**
 * Orchestre une partie complète.
 * v2 : gère la boutique inter-vague et le switch d'armes.
 *
 * @author Elmehdi Eddareiy
 */
public class GameController {

    private final StackPane      root;
    private final MenuController menuController;

    private GameData     gameData;
    private GameView     gameView;
    private GameLoop     gameLoop;
    private InputHandler inputHandler;
    private WaveManager  waveManager;
    private ScoreService scoreService;

    private PauseView    pauseView;
    private boolean      pauseShown = false;

    // Listener de boutique — géré dans GameLoop via InputHandler
    public GameController(StackPane root, MenuController menuController) {
        this.root           = root;
        this.menuController = menuController;
        this.scoreService   = new ScoreService();
    }

    // ─── Démarrer / Redémarrer ────────────────────────────────────────────────

    public void startGame() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();

        gameData     = new GameData();
        waveManager  = new WaveManager();
        inputHandler = new InputHandler();
        gameView     = new GameView(GameData.MAP_WIDTH, GameData.MAP_HEIGHT);

        // Attacher InputHandler dès que la scène est disponible
        gameView.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                // Attacher l'InputHandler (gère flèches, espace, ESC, 1/2/3)
                inputHandler.attachToScene(newS);
            }
        });

        gameLoop = new GameLoop(
                gameData, inputHandler, waveManager,
                () -> {
                    // Consommer les événements visuels avant le rendu
                    for (GameData.VisualEvent ev : gameData.pollVisualEvents()) {
                        gameView.addFloatingText(ev.x, ev.y, ev.text,
                                javafx.scene.paint.Color.web(ev.colorHex), ev.fontSize);
                    }
                    if (gameData.isPlayerJustHit()) {
                        gameView.notifyPlayerHit();
                        gameData.setPlayerJustHit(false);
                    }
                    gameView.render(gameData);
                },
                this::onGameOver,
                this::onPause);

        root.getChildren().setAll(gameView);
        gameLoop.start();
    }

    // ─── Game Over ────────────────────────────────────────────────────────────

    private void onGameOver() {
        scoreService.saveGameResult(gameData);
        Platform.runLater(() -> {
            GameOverView gov = new GameOverView(gameData, GameData.MAP_WIDTH, GameData.MAP_HEIGHT, gameData.isGameWon());
            gov.getBtnRetry().setOnAction(e -> startGame());
            gov.getBtnMenu() .setOnAction(e -> menuController.showMenu());
            root.getChildren().setAll(gov);
        });
    }

    // ─── Pause ────────────────────────────────────────────────────────────────

    private void onPause() {
        Platform.runLater(() -> {
            if (!pauseShown) {
                gameData.setPaused(true);
                pauseShown = true;
                pauseView  = new PauseView(GameData.MAP_WIDTH, GameData.MAP_HEIGHT);
                pauseView.getBtnResume() .setOnAction(e -> resumeGame());
                pauseView.getBtnRestart().setOnAction(e -> startGame());
                pauseView.getBtnMenu()   .setOnAction(e -> {
                    gameLoop.stop();
                    menuController.showMenu();
                });
                root.getChildren().add(pauseView);
            } else {
                resumeGame();
            }
        });
    }

    private void resumeGame() {
        Platform.runLater(() -> {
            gameData.setPaused(false);
            pauseShown = false;
            root.getChildren().remove(pauseView);
            pauseView = null;
        });
    }
}
