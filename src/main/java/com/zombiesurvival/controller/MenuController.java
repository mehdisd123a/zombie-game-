package com.zombiesurvival.controller;

import com.zombiesurvival.service.Session;
import com.zombiesurvival.view.*;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;

/**
 * Gère la navigation entre tous les écrans de l'application.
 * Menu → Jeu / Comment jouer / Login / Leaderboard
 *
 * @author Elmehdi Eddareiy
 */
public class MenuController {

    private final StackPane      root;
    private final double         width;
    private final double         height;

    private MenuView             menuView;
    private GameController       gameController;
    private final LoginController loginController;

    public MenuController(StackPane root, double width, double height) {
        this.root            = root;
        this.width           = width;
        this.height          = height;
        this.loginController = new LoginController();
        this.gameController  = new GameController(root, this);
    }

    // ─── Menu principal ───────────────────────────────────────────────────────

    public void showMenu() {
        Platform.runLater(() -> {
            menuView = new MenuView(width, height);

            menuView.getBtnPlay()       .setOnAction(e -> gameController.startGame());
            menuView.getBtnHowToPlay()  .setOnAction(e -> showHowToPlay());
            menuView.getBtnLeaderboard().setOnAction(e -> showLeaderboard());
            menuView.getBtnExit()       .setOnAction(e -> Platform.exit());

            menuView.getBtnLogin().setOnAction(e -> {
                if (Session.getInstance().isLoggedIn()) {
                    loginController.logout();
                    menuView.updateLoginButton();
                } else {
                    showLogin();
                }
            });

            root.getChildren().setAll(menuView);
        });
    }

    // ─── Guide "Comment jouer" ────────────────────────────────────────────────

    private void showHowToPlay() {
        Platform.runLater(() -> {
            HowToPlayView htpView = new HowToPlayView(width, height, this::showMenu);
            root.getChildren().setAll(htpView);
        });
    }

    // ─── Connexion ────────────────────────────────────────────────────────────

    private void showLogin() {
        Platform.runLater(() -> {
            LoginView loginView = new LoginView(width, height);

            loginView.getBtnLogin().setOnAction(e -> {
                LoginController.LoginResult result =
                        loginController.login(loginView.getUsername(), loginView.getPassword());
                if (result == LoginController.LoginResult.SUCCESS) {
                    loginView.showSuccess("Bienvenue, " +
                            Session.getInstance().getDisplayName() + " !");
                    // Retour au menu après 1.2 secondes
                    new Thread(() -> {
                        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                        Platform.runLater(this::showMenu);
                    }).start();
                } else {
                    loginView.showError(LoginController.getErrorMessage(result));
                }
            });

            loginView.getBtnRegister().setOnAction(e -> {
                LoginController.LoginResult result =
                        loginController.register(loginView.getUsername(), loginView.getPassword());
                if (result == LoginController.LoginResult.SUCCESS) {
                    loginView.showSuccess("Compte cree ! Bienvenue, " +
                            Session.getInstance().getDisplayName() + " !");
                    new Thread(() -> {
                        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                        Platform.runLater(this::showMenu);
                    }).start();
                } else {
                    loginView.showError(LoginController.getErrorMessage(result));
                }
            });

            loginView.getBtnBack().setOnAction(e -> showMenu());

            root.getChildren().setAll(loginView);
        });
    }

    // ─── Classement ───────────────────────────────────────────────────────────

    private void showLeaderboard() {
        Platform.runLater(() -> {
            LeaderboardView lbView = new LeaderboardView(width, height);
            lbView.getBtnBack().setOnAction(e -> showMenu());
            root.getChildren().setAll(lbView);
        });
    }
}
