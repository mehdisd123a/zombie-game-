package com.zombiesurvival;

import com.zombiesurvival.controller.MenuController;
import com.zombiesurvival.model.GameData;
import com.zombiesurvival.service.DatabaseService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Point d'entrée de l'application Zombie Survival.
 * Initialise JavaFX, la base de données et affiche le menu principal.
 *
 * @author Elmehdi Eddareiy
 * @version 1.0
 * @since Novembre 2026
 *
 * Projet réalisé par :
 *   - Elmehdi Eddareiy  (développeur)
 *   - Omar Samma        (rapport)
 *   - Erami Amin        (présentation)
 */
public class Main extends Application {

    private static final double WIDTH  = GameData.MAP_WIDTH;   // 900
    private static final double HEIGHT = GameData.MAP_HEIGHT;  // 650

    @Override
    public void start(Stage primaryStage) {
        // ── Initialiser la base de données ───────────────────────────────────
        DatabaseService.getInstance().initDatabase();

        // ── Conteneur racine ─────────────────────────────────────────────────
        StackPane root = new StackPane();

        // ── Scène principale ─────────────────────────────────────────────────
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // ── Configurer la fenêtre ─────────────────────────────────────────────
        primaryStage.setTitle("Zombie Survival");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // ── Démarrer au menu principal ────────────────────────────────────────
        MenuController menuController = new MenuController(root, WIDTH, HEIGHT);
        menuController.showMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
