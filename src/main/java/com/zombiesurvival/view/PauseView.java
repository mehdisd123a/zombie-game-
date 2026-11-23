package com.zombiesurvival.view;

import com.zombiesurvival.model.GameData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * Écran de pause — design amélioré v2.
 * Résumé des commandes affiché pendant la pause.
 *
 * @author Elmehdi Eddareiy
 */
public class PauseView extends StackPane {

    private final Button btnResume;
    private final Button btnRestart;
    private final Button btnMenu;

    public PauseView(double width, double height) {
        setPrefSize(width, height);

        // ── Fond flou semi-transparent ────────────────────────────────────────
        Canvas overlay = new Canvas(width, height);
        drawOverlay(overlay.getGraphicsContext2D(), width, height);

        // ── Panneau central ───────────────────────────────────────────────────
        VBox panel = new VBox(0);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setMaxWidth(500);
        panel.setStyle(
                "-fx-background-color: rgba(5,8,5,0.95);" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: rgba(80,0,0,0.8);" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.8),20,0,0,4);");
        panel.setPadding(new Insets(30, 40, 30, 40));

        // ── Titre PAUSE ───────────────────────────────────────────────────────
        Text titleText = new Text("PAUSE");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 52));
        titleText.setFill(Color.rgb(255, 200, 50));

        Text subTitle = new Text("Partie en attente...");
        subTitle.setFont(Font.font("Arial", 13));
        subTitle.setFill(Color.rgb(120, 120, 120));

        // ── Rappel des commandes rapides ──────────────────────────────────────
        Canvas cmdCanvas = new Canvas(420, 90);
        drawQuickCommands(cmdCanvas.getGraphicsContext2D());

        // ── Séparateur ────────────────────────────────────────────────────────
        Region sep1 = new Region();
        sep1.setPrefHeight(16);

        // ── Boutons ───────────────────────────────────────────────────────────
        btnResume  = pauseBtn("REPRENDRE",          "#155215", "#1e7a1e");
        btnRestart = pauseBtn("RECOMMENCER",        "#6a1010", "#aa2020");
        btnMenu    = pauseBtn("RETOUR AU MENU",     "#1a1a40", "#2a2a7a");

        VBox buttonsBox = new VBox(10, btnResume, btnRestart, btnMenu);
        buttonsBox.setAlignment(Pos.CENTER);

        // ESC hint
        Text escHint = new Text("[ESC]  pour reprendre");
        escHint.setFont(Font.font("Arial", 12));
        escHint.setFill(Color.rgb(80, 80, 80));

        panel.getChildren().addAll(
                titleText, subTitle,
                new Region() {{ setPrefHeight(16); }},
                cmdCanvas,
                sep1,
                buttonsBox,
                new Region() {{ setPrefHeight(10); }},
                escHint);

        getChildren().addAll(overlay, panel);
    }

    private void drawOverlay(GraphicsContext gc, double w, double h) {
        // Fond noir semi-transparent
        gc.setFill(Color.rgb(0, 0, 0, 0.78));
        gc.fillRect(0, 0, w, h);

        // Lignes diagonales légères pour texture
        gc.setStroke(Color.rgb(10, 20, 10, 0.3));
        gc.setLineWidth(1);
        for (double x = -h; x < w + h; x += 30) {
            gc.strokeLine(x, 0, x + h, h);
        }
    }

    private void drawQuickCommands(GraphicsContext gc) {
        double w = 420, h = 90;
        gc.setFill(Color.rgb(15, 20, 15, 0.8));
        gc.fillRoundRect(0, 0, w, h, 8, 8);
        gc.setStroke(Color.rgb(50, 70, 50));
        gc.setLineWidth(1);
        gc.strokeRoundRect(0, 0, w, h, 8, 8);

        // Titre
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setFill(Color.rgb(255, 200, 50));
        gc.fillText("RAPPEL DES COMMANDES", w / 2, 18);

        // Commandes en 2 lignes x 4 colonnes
        String[][] cmds = {
                {"Z/W/S/Q", "Bouger"},
                {"Souris",  "Viser"},
                {"Clic",    "Tirer"},
                {"R",       "Recharger"},
                {"1/2/3",   "Armes"},
                {"Molette", "Switch arme"},
                {"ESC",     "Pause"},
                {"H/M",     "Boutique"}
        };

        int cols = 4;
        double colW = w / cols;
        for (int i = 0; i < cmds.length; i++) {
            int col = i % cols;
            int row = i / cols;
            double cx = col * colW + colW / 2;
            double cy = 38 + row * 26;

            // Touche
            gc.setFill(Color.rgb(50, 50, 10, 0.8));
            gc.fillRoundRect(cx - 24, cy - 12, 48, 14, 3, 3);
            gc.setStroke(Color.rgb(100, 90, 20));
            gc.setLineWidth(0.5);
            gc.strokeRoundRect(cx - 24, cy - 12, 48, 14, 3, 3);
            gc.setFill(Color.rgb(255, 220, 80));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.fillText(cmds[i][0], cx, cy - 1);

            // Description
            gc.setFill(Color.rgb(170, 185, 170));
            gc.setFont(Font.font("Arial", 10));
            gc.fillText(cmds[i][1], cx, cy + 11);
        }
    }

    private Button pauseBtn(String label, String base, String hover) {
        Button b = new Button(label);
        b.setPrefWidth(300);
        b.setPrefHeight(44);
        String bs = String.format(
                "-fx-background-color:%s;-fx-text-fill:white;" +
                "-fx-font-size:14px;-fx-font-weight:bold;" +
                "-fx-background-radius:7;-fx-cursor:hand;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.5),4,0,0,1);",
                base);
        String hs = String.format(
                "-fx-background-color:%s;-fx-text-fill:white;" +
                "-fx-font-size:14px;-fx-font-weight:bold;" +
                "-fx-background-radius:7;-fx-cursor:hand;", hover);
        b.setStyle(bs);
        b.setOnMouseEntered(e -> b.setStyle(hs));
        b.setOnMouseExited(e  -> b.setStyle(bs));
        return b;
    }

    public Button getBtnResume()  { return btnResume; }
    public Button getBtnRestart() { return btnRestart; }
    public Button getBtnMenu()    { return btnMenu; }
}
