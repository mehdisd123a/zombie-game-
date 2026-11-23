package com.zombiesurvival.view;

import com.zombiesurvival.model.GameData;
import com.zombiesurvival.service.ScoreService;
import com.zombiesurvival.service.Session;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Écran Game Over — v2 : affiche niveau, kills, record.
 *
 * @author Elmehdi Eddareiy
 */
public class GameOverView extends StackPane {

    private final Button btnRetry;
    private final Button btnMenu;
    private final boolean won;

    public GameOverView(GameData gameData, double width, double height, boolean won) {
        this.won = won;
        setPrefSize(width, height);

        // Fond dégradé : rouge sombre (défaite) ou vert (victoire)
        Rectangle bg = new Rectangle(width, height);
        bg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                won ? new Stop(0, Color.rgb(0, 30, 8))  : new Stop(0, Color.rgb(8,  0, 0)),
                won ? new Stop(1, Color.rgb(0, 60, 20))  : new Stop(1, Color.rgb(35, 0, 0))));

        // Titre
        Text titleText = new Text(won ? "VICTOIRE !" : "GAME OVER");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        titleText.setFill(won ? Color.rgb(50, 220, 80) : Color.rgb(220, 20, 20));

        // Stats
        int personalBest = new ScoreService().getPersonalBest();
        boolean newRecord = Session.getInstance().isLoggedIn()
                && gameData.getScore() > personalBest;

        Text scoreText  = stat("Score final",        String.valueOf(gameData.getScore()),       Color.rgb(255, 220, 50));
        Text levelText  = stat("Niveau atteint",     String.valueOf(gameData.getPlayer().getLevel()), Color.rgb(80, 150, 255));
        Text waveText   = stat("Vague atteinte",     String.valueOf(gameData.getCurrentWave()), Color.rgb(100, 180, 255));
        Text killsText  = stat("Zombies tues",       String.valueOf(gameData.getZombiesKilled()), Color.rgb(220, 80, 80));
        Text timeText   = stat("Duree",              gameData.getGameDuration() + "s",          Color.rgb(160, 160, 160));
        Text bestText   = stat("Record personnel",
                Session.getInstance().isLoggedIn()
                        ? (newRecord ? "NOUVEAU RECORD !" : String.valueOf(personalBest))
                        : "Non connecte",
                newRecord ? Color.rgb(50, 255, 100) : Color.rgb(180, 180, 180));

        String playerName = Session.getInstance().isLoggedIn()
                ? Session.getInstance().getDisplayName() : "Invite";
        Text playerText = stat("Joueur", playerName, Color.rgb(100, 170, 255));

        // Boutons
        btnRetry = btn("RECOMMENCER",    "#7a1a1a", "#cc2222");
        btnMenu  = btn("MENU PRINCIPAL", "#1a1a5a", "#2244aa");

        VBox box = new VBox(12,
                titleText,
                scoreText, levelText, waveText, killsText, timeText, bestText, playerText,
                btnRetry, btnMenu);
        box.setAlignment(Pos.CENTER);
        getChildren().addAll(bg, box);
    }

    private Text stat(String label, String value, Color color) {
        Text t = new Text(label + " : " + value);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        t.setFill(color);
        return t;
    }

    private Button btn(String text, String base, String hover) {
        Button b = new Button(text);
        b.setPrefWidth(240);
        b.setPrefHeight(42);
        String bs = "-fx-background-color:" + base + ";-fx-text-fill:white;" +
                    "-fx-font-size:14px;-fx-font-weight:bold;" +
                    "-fx-background-radius:6;-fx-cursor:hand;";
        String hs = "-fx-background-color:" + hover + ";-fx-text-fill:white;" +
                    "-fx-font-size:14px;-fx-font-weight:bold;" +
                    "-fx-background-radius:6;-fx-cursor:hand;";
        b.setStyle(bs);
        b.setOnMouseEntered(e -> b.setStyle(hs));
        b.setOnMouseExited(e  -> b.setStyle(bs));
        return b;
    }

    public Button getBtnRetry() { return btnRetry; }
    public Button getBtnMenu()  { return btnMenu; }
}
