package com.zombiesurvival.view;

import com.zombiesurvival.service.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Menu principal — design amélioré v2.
 * Fond animé, logo stylisé, bouton "Comment jouer".
 *
 * @author Elmehdi Eddareiy
 */
public class MenuView extends StackPane {

    private final Button btnPlay;
    private final Button btnHowToPlay;
    private final Button btnLogin;
    private final Button btnLeaderboard;
    private final Button btnExit;

    private Label lblUser;

    public MenuView(double width, double height) {
        setPrefSize(width, height);

        // ── Fond dessiné ──────────────────────────────────────────────────────
        Canvas bgCanvas = new Canvas(width, height);
        drawBackground(bgCanvas.getGraphicsContext2D(), width, height);

        // ── Info utilisateur (haut droit) ─────────────────────────────────────
        lblUser = buildUserLabel();
        StackPane.setAlignment(lblUser, javafx.geometry.Pos.TOP_RIGHT);
        lblUser.setPadding(new Insets(12, 16, 0, 0));

        // ── Colonne de boutons (centrée en bas) ───────────────────────────────
        btnPlay        = menuBtn("JOUER",           "#aa0000", "#dd2222", "▶");
        btnHowToPlay   = menuBtn("COMMENT JOUER",   "#005577", "#0077aa", "?");
        btnLogin       = menuBtn("CONNEXION",        "#1a3a6b", "#2255aa", "");
        btnLeaderboard = menuBtn("CLASSEMENT",       "#334400", "#557700", "");
        btnExit        = menuBtn("QUITTER",          "#2a2a2a", "#444444", "");

        updateLoginButton();

        VBox buttonsBox = new VBox(10,
                btnPlay, btnHowToPlay, btnLogin, btnLeaderboard, btnExit);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(0, 0, 50, 0));
        // Décaler les boutons vers le bas
        StackPane.setAlignment(buttonsBox, Pos.BOTTOM_CENTER);

        getChildren().addAll(bgCanvas, buttonsBox, lblUser);
    }

    private void drawBackground(GraphicsContext gc, double w, double h) {
        // Fond noir profond
        gc.setFill(Color.rgb(5, 5, 5));
        gc.fillRect(0, 0, w, h);

        // Lignes de grille très discrètes
        gc.setStroke(Color.rgb(15, 25, 15, 0.8));
        gc.setLineWidth(0.5);
        for (double x = 0; x < w; x += 50) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y < h; y += 50) gc.strokeLine(0, y, w, y);

        // Vignette (coins sombres)
        for (int i = 0; i < 80; i++) {
            double alpha = (80 - i) / 160.0;
            gc.setFill(Color.rgb(0, 0, 0, alpha));
            gc.fillRect(0,     0,     i * 2, h);
            gc.fillRect(w - i * 2, 0, i * 2, h);
        }

        // Lueur rouge centrale
        gc.setFill(new LinearGradient(0.5, 0, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(80, 0, 0, 0.5)),
                new Stop(1, Color.rgb(0,  0, 0, 0))));
        gc.fillOval(w / 2 - 250, -80, 500, 400);

        // ── Logo "ZOMBIE" ─────────────────────────────────────────────────────
        // Ombre du texte
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 90));
        gc.fillText("ZOMBIE", w / 2 + 4, 160);

        // Texte principal avec dégradé simulé (deux passes)
        gc.setFill(Color.rgb(180, 10, 10));
        gc.fillText("ZOMBIE", w / 2, 155);
        gc.setFill(Color.rgb(220, 30, 30, 0.6));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 90));
        gc.fillText("ZOMBIE", w / 2, 155);

        // Traits décoratifs autour du mot ZOMBIE
        gc.setStroke(Color.rgb(140, 0, 0, 0.6));
        gc.setLineWidth(1.5);
        gc.strokeLine(w / 2 - 240, 170, w / 2 - 60, 170);
        gc.strokeLine(w / 2 +  60, 170, w / 2 + 240, 170);

        // ── Logo "SURVIVAL" ───────────────────────────────────────────────────
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 46));
        gc.fillText("SURVIVAL", w / 2 + 2, 218);
        gc.setFill(Color.rgb(230, 230, 230));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 46));
        gc.fillText("SURVIVAL", w / 2, 215);

        // Sous-titre
        gc.setFont(Font.font("Arial", FontPosture.ITALIC, 15));
        gc.setFill(Color.rgb(130, 130, 130));
        gc.fillText("Survivez le plus longtemps possible...", w / 2, 248);

        // ── Crédits ───────────────────────────────────────────────────────────
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        gc.setFill(Color.rgb(55, 55, 55));
        gc.fillText("Elmehdi Eddareiy  |  Omar Samma  |  Erami Amin  --  EMSI 2026", w / 2, h - 10);

        // Bordure rouge
        gc.setStroke(Color.rgb(80, 0, 0, 0.5));
        gc.setLineWidth(2);
        gc.strokeRect(1, 1, w - 2, h - 2);
    }

    private Button menuBtn(String label, String base, String hover, String prefix) {
        String fullLabel = prefix.isEmpty() ? label : prefix + "  " + label;
        Button btn = new Button(fullLabel);
        btn.setPrefWidth(300);
        btn.setPrefHeight(46);
        String bs = String.format(
                "-fx-background-color:%s;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:15px;-fx-font-weight:bold;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.6),6,0,0,2);",
                base);
        String hs = String.format(
                "-fx-background-color:%s;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:15px;-fx-font-weight:bold;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;" +
                "-fx-effect:dropshadow(gaussian,rgba(200,0,0,0.4),10,0,0,2);",
                hover);
        btn.setStyle(bs);
        btn.setOnMouseEntered(e -> btn.setStyle(hs));
        btn.setOnMouseExited(e  -> btn.setStyle(bs));
        return btn;
    }

    private Label buildUserLabel() {
        Session session = Session.getInstance();
        String text;
        String color;
        if (session.isLoggedIn()) {
            text  = "Connecte : " + session.getDisplayName();
            color = "#44aaff";
        } else {
            text  = "Mode invite";
            color = "#777777";
        }
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill:" + color + ";" +
                     "-fx-font-size:12px;-fx-font-weight:bold;" +
                     "-fx-background-color:rgba(0,0,0,0.5);" +
                     "-fx-background-radius:5;" +
                     "-fx-padding:4 10 4 10;");
        return lbl;
    }

    public void updateLoginButton() {
        Session session = Session.getInstance();
        if (session.isLoggedIn()) {
            btnLogin.setText("DECONNEXION  (" + session.getDisplayName() + ")");
            lblUser.setText("Connecte : " + session.getDisplayName());
            lblUser.setStyle("-fx-text-fill:#44aaff;-fx-font-size:12px;-fx-font-weight:bold;" +
                             "-fx-background-color:rgba(0,0,0,0.5);-fx-background-radius:5;" +
                             "-fx-padding:4 10 4 10;");
        } else {
            btnLogin.setText("CONNEXION");
            lblUser.setText("Mode invite");
            lblUser.setStyle("-fx-text-fill:#777777;-fx-font-size:12px;-fx-font-weight:bold;" +
                             "-fx-background-color:rgba(0,0,0,0.5);-fx-background-radius:5;" +
                             "-fx-padding:4 10 4 10;");
        }
    }

    // ─── Accesseurs ───────────────────────────────────────────────────────────
    public Button getBtnPlay()        { return btnPlay; }
    public Button getBtnHowToPlay()   { return btnHowToPlay; }
    public Button getBtnLogin()       { return btnLogin; }
    public Button getBtnLeaderboard() { return btnLeaderboard; }
    public Button getBtnExit()        { return btnExit; }
}
