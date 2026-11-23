package com.zombiesurvival.view;

import com.zombiesurvival.model.GameData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * Écran "Comment jouer" — guide complet du jeu.
 * Présente les commandes, règles, vagues, armes et conseils.
 *
 * @author Elmehdi Eddareiy
 */
public class HowToPlayView extends StackPane {

    private final Button btnBack;

    public HowToPlayView(double width, double height, Runnable onBack) {
        setPrefSize(width, height);

        // ── Fond animé ────────────────────────────────────────────────────────
        Canvas bgCanvas = new Canvas(width, height);
        drawBackground(bgCanvas.getGraphicsContext2D(), width, height);

        // ── Contenu scrollable ────────────────────────────────────────────────
        VBox content = new VBox(22);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30, 50, 30, 50));
        content.setMaxWidth(820);

        // Titre principal
        content.getChildren().add(makeTitle("COMMENT JOUER", 42, "#ff4444"));
        content.getChildren().add(makeDivider(width - 100));

        // ── Section 1 : Objectif ─────────────────────────────────────────────
        content.getChildren().add(makeSection("OBJECTIF",
                "Survivez face a des vagues de zombies de plus en plus nombreuses " +
                "et rapides. Eliminez les zombies, ramassez les ressources et " +
                "resistez jusqu'a la vague 10 pour affronter le BOSS final : " +
                "vainquez-le et la partie est GAGNEE !"));

        // ── Section 2 : Commandes ────────────────────────────────────────────
        content.getChildren().add(makeSectionTitle("COMMANDES"));

        HBox commandsGrid = new HBox(20);
        commandsGrid.setAlignment(Pos.CENTER);

        VBox moveBox = makeCommandBox("DEPLACEMENT",
                new String[][]{
                        {"Fleche Haut",   "Avancer"},
                        {"Fleche Bas",    "Reculer"},
                        {"Fleche Gauche", "Gauche"},
                        {"Fleche Droite", "Droite"}
                }, "#2255aa");

        VBox actionBox = makeCommandBox("ACTIONS",
                new String[][]{
                        {"Souris", "Viser"},
                        {"Espace", "Tirer"},
                        {"ESC",    "Pause"}
                }, "#cc2200");

        VBox weaponBox = makeCommandBox("ARMES",
                new String[][]{
                        {"[1]",     "Pistol (depart)"},
                        {"[2]",     "Rifle (vague 3)"},
                        {"[3]",     "Shotgun (vague 5)"},
                        {"Molette", "Changer d'arme"}
                }, "#227722");

        commandsGrid.getChildren().addAll(moveBox, actionBox, weaponBox);
        content.getChildren().add(commandsGrid);
        content.getChildren().add(makeDivider(width - 100));

        // ── Section 3 : Les Armes ────────────────────────────────────────────
        content.getChildren().add(makeSectionTitle("LES ARMES"));

        HBox weaponsRow = new HBox(16);
        weaponsRow.setAlignment(Pos.CENTER);
        weaponsRow.getChildren().addAll(
                makeWeaponCard("PISTOL",   "Depart",  "Degats : 25\nMunitions : Infinies\nCadence : Rapide\nVitesse balle : Normale\nIdeal pour debuter",      "#4488ff"),
                makeWeaponCard("RIFLE",    "Vague 3", "Degats : 20\nMunitions : Infinies\nCadence : Tres rapide\nVitesse balle : Haute\nParfait pour vagues denses", "#44cc44"),
                makeWeaponCard("SHOTGUN",  "Vague 5", "Degats : 40 x5\nMunitions : Infinies\nCadence : Lente\nPortee : Courte\nDevastateur de pres",            "#ff8844")
        );
        content.getChildren().add(weaponsRow);
        content.getChildren().add(makeDivider(width - 100));

        // ── Section 4 : Les Ennemis ──────────────────────────────────────────
        content.getChildren().add(makeSectionTitle("LES ENNEMIS"));

        HBox enemiesRow = new HBox(16);
        enemiesRow.setAlignment(Pos.CENTER);
        enemiesRow.getChildren().addAll(
                makeEnemyCard("ZOMBIE",         "#4a9643", "Lent, 50 PV\nDegats : 10\n+10 pts\nApparait des vague 1"),
                makeEnemyCard("ZOMBIE SPECIAL", "#8028bb", "Rapide, 80 PV\nDegats : 15\n+30 pts\nApparait vague 4+"),
                makeEnemyCard("BOSS",           "#cc1111", "500 PV, charge vite\nDegats : 30 (45 en phase 2)\n+500 pts\nVague 10 seulement\nVaincre = VICTOIRE !")
        );
        content.getChildren().add(enemiesRow);
        content.getChildren().add(makeDivider(width - 100));

        // ── Section 5 : Progression ──────────────────────────────────────────
        content.getChildren().add(makeSectionTitle("PROGRESSION DES VAGUES"));

        VBox wavesBox = makeWavesTable();
        content.getChildren().add(wavesBox);
        content.getChildren().add(makeDivider(width - 100));

        // ── Section 6 : Items ────────────────────────────────────────────────
        content.getChildren().add(makeSectionTitle("OBJETS RAMASSABLES"));

        HBox itemsRow = new HBox(20);
        itemsRow.setAlignment(Pos.CENTER);
        itemsRow.getChildren().addAll(
                makeItemCard("TROUSSE DE SOINS", "#cc2222",
                        "Restaure 25 PV.\nApparait a 30% quand\nun zombie meurt.\nDisparait apres 10 s."),
                makeItemCard("MUNITIONS", "#cc9900",
                        "Vos armes ont des\nmunitions infinies :\nce bonus n'a aucun\neffet.")
        );
        content.getChildren().add(itemsRow);
        content.getChildren().add(makeDivider(width - 100));

        // ── Section 7 : Boutique ─────────────────────────────────────────────
        content.getChildren().add(makeSectionTitle("BOUTIQUE INTER-VAGUE"));
        content.getChildren().add(makeSection(
                "Entre chaque vague, une boutique s'ouvre pendant 2,5 secondes.\n" +
                "Depensez vos points :\n\n" +
                "  [H]  Trousse de soins (+50 HP)   — 50 pts\n" +
                "  [M]  Munitions (+30 balles)      — 40 pts (munitions infinies : sans effet)\n\n" +
                "Les armes (Rifle, Shotgun) se debloquent automatiquement aux vagues 3 et 5."));
        content.getChildren().add(makeDivider(width - 100));

        // ── Section 8 : Score ────────────────────────────────────────────────
        content.getChildren().add(makeSectionTitle("SYSTEME DE SCORE"));
        content.getChildren().add(makeSection(
                "  Zombie normal tue       : +10 pts\n" +
                "  Zombie special tue      : +30 pts\n" +
                "  Boss tue                : +500 pts\n" +
                "  Vague completee         : +150 x numero de vague\n\n" +
                "Le score est sauvegarde apres chaque partie si vous etes connecte.\n" +
                "Consultez le classement pour voir les meilleurs joueurs !"));
        content.getChildren().add(makeDivider(width - 100));

        // ── Section 9 : XP & Niveaux ─────────────────────────────────────────
        content.getChildren().add(makeSectionTitle("XP ET NIVEAUX"));
        content.getChildren().add(makeSection(
                "Gagnez de l'XP en tuant des zombies et en completant des vagues.\n" +
                "Chaque niveau gagne vous rapporte :\n\n" +
                "  +15 HP maximum\n" +
                "  +25 HP de soin immediat\n" +
                "  +0,15 de vitesse de deplacement\n\n" +
                "La barre bleue sous votre barre de vie indique votre progression XP."));
        content.getChildren().add(makeDivider(width - 100));

        // ── Section 10 : Conseils ────────────────────────────────────────────
        content.getChildren().add(makeSectionTitle("CONSEILS STRATEGIQUES"));

        VBox tipsBox = new VBox(10);
        tipsBox.setAlignment(Pos.CENTER_LEFT);
        tipsBox.setMaxWidth(700);
        String[] tips = {
                "Gardez toujours une distance entre vous et les zombies.",
                "Munitions infinies : tirez librement, pas besoin de recharger.",
                "Utilisez les obstacles (caisses, murs) comme protection.",
                "Le Rifle est ideal contre les groupes de zombies en ligne.",
                "Le Shotgun est tres efficace quand un zombie est proche.",
                "Surveillez la mini-map en haut a droite pour anticiper les spawns.",
                "Achetez le soin ([H]) en boutique si vous etes en dessous de 50 HP.",
                "Le Boss charge : restez en mouvement et evitez ses charges !",
                "Les items disparaissent apres 10 secondes, ramassez-les vite !",
                "Vainquez le Boss de la vague 10 pour gagner la partie !"
        };
        for (int i = 0; i < tips.length; i++) {
            Text tip = new Text("  " + (i + 1) + ".  " + tips[i]);
            tip.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            tip.setFill(Color.rgb(200, 220, 200));
            tipsBox.getChildren().add(tip);
        }
        content.getChildren().add(tipsBox);

        // ── Bouton retour ─────────────────────────────────────────────────────
        content.getChildren().add(makeDivider(width - 100));
        btnBack = makeBtn("RETOUR AU MENU", "#cc0000", "#ff3333");
        btnBack.setOnAction(e -> { if (onBack != null) onBack.run(); });
        content.getChildren().add(btnBack);
        content.getChildren().add(new Region() {{ setPrefHeight(20); }});

        // ── ScrollPane ────────────────────────────────────────────────────────
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background: transparent; " +
                "-fx-background-color: transparent; " +
                "-fx-padding: 0;");
        scroll.setPrefSize(width, height);

        getChildren().addAll(bgCanvas, scroll);
    }

    // ─── Helpers visuels ──────────────────────────────────────────────────────

    private void drawBackground(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.rgb(8, 12, 8));
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.rgb(20, 35, 20));
        gc.setLineWidth(0.5);
        for (double x = 0; x < w; x += 50) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y < h; y += 50) gc.strokeLine(0, y, w, y);
        gc.setStroke(Color.rgb(60, 0, 0));
        gc.setLineWidth(2);
        gc.strokeRect(1, 1, w - 2, h - 2);
    }

    private Text makeTitle(String text, int size, String hexColor) {
        Text t = new Text(text);
        t.setFont(Font.font("Arial", FontWeight.BOLD, size));
        t.setFill(Color.web(hexColor));
        return t;
    }

    private Text makeSectionTitle(String text) {
        Text t = new Text(text);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        t.setFill(Color.rgb(255, 180, 50));
        return t;
    }

    private Rectangle makeDivider(double w) {
        Rectangle r = new Rectangle(Math.min(w, 800), 1);
        r.setFill(Color.rgb(60, 80, 60, 0.6));
        return r;
    }

    private Text makeSection(String text) {
        Text t = new Text(text);
        t.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        t.setFill(Color.rgb(190, 210, 190));
        t.setWrappingWidth(760);
        return t;
    }

    private javafx.scene.Node makeSection(String title, String text) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.TOP_LEFT);
        Text titleText = makeSectionTitle(title);
        Text bodyText  = makeSection(text);
        box.getChildren().addAll(titleText, bodyText);
        return box;
    }

    private VBox makeCommandBox(String title, String[][] rows, String accent) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: rgba(10,10,20,0.8); " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: " + accent + "88; " +
                     "-fx-border-radius: 8; " +
                     "-fx-border-width: 1;");
        box.setPrefWidth(230);

        Text titleText = new Text(title);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleText.setFill(Color.web(accent));
        box.getChildren().add(titleText);

        for (String[] row : rows) {
            HBox rowBox = new HBox(8);
            rowBox.setAlignment(Pos.CENTER_LEFT);

            // Touche
            Text keyText = new Text(row[0]);
            keyText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            keyText.setFill(Color.rgb(255, 220, 100));
            StackPane keyBox = new StackPane(keyText);
            keyBox.setPadding(new Insets(2, 7, 2, 7));
            keyBox.setStyle("-fx-background-color: rgba(60,60,0,0.7); " +
                            "-fx-background-radius: 4; " +
                            "-fx-border-color: #888800; " +
                            "-fx-border-radius: 4; " +
                            "-fx-border-width: 1;");
            keyBox.setMinWidth(70);

            // Description
            Text descText = new Text(row[1]);
            descText.setFont(Font.font("Arial", 12));
            descText.setFill(Color.rgb(200, 200, 200));

            rowBox.getChildren().addAll(keyBox, descText);
            box.getChildren().add(rowBox);
        }
        return box;
    }

    private VBox makeWeaponCard(String name, String unlock, String desc, String color) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(14));
        box.setPrefWidth(230);
        box.setStyle("-fx-background-color: rgba(8,8,15,0.9); " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: " + color + "99; " +
                     "-fx-border-radius: 8; " +
                     "-fx-border-width: 1.5;");

        // Icône arme dessinée
        Canvas icon = new Canvas(60, 20);
        drawWeaponIcon(icon.getGraphicsContext2D(), name, Color.web(color));
        box.getChildren().add(icon);

        Text nameText = new Text(name);
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        nameText.setFill(Color.web(color));

        Text unlockText = new Text("Disponible : " + unlock);
        unlockText.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
        unlockText.setFill(Color.rgb(150, 150, 180));

        Text descText = new Text(desc);
        descText.setFont(Font.font("Arial", 12));
        descText.setFill(Color.rgb(200, 210, 200));

        box.getChildren().addAll(nameText, unlockText, descText);
        return box;
    }

    private void drawWeaponIcon(GraphicsContext gc, String type, Color color) {
        gc.setFill(color);
        switch (type) {
            case "PISTOL" -> {
                gc.fillRoundRect(5,  7, 25, 7, 3, 3);
                gc.fillRect(28, 6, 15, 4);
                gc.fillRect(10, 14, 8, 5);
            }
            case "RIFLE" -> {
                gc.fillRoundRect(2,  7, 15, 7, 2, 2);
                gc.fillRect(16, 8, 35, 3);
                gc.fillRect(6, 14, 7,  5);
            }
            case "SHOTGUN" -> {
                gc.fillRoundRect(5, 6, 20, 9, 3, 3);
                gc.fillRect(23, 8, 25, 5);
                gc.fillRect(9, 15, 10, 4);
            }
        }
    }

    private VBox makeEnemyCard(String name, String hexColor, String desc) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(14));
        box.setPrefWidth(230);
        box.setStyle("-fx-background-color: rgba(8,8,8,0.9); " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: " + hexColor + "88; " +
                     "-fx-border-radius: 8; " +
                     "-fx-border-width: 1.5;");

        // Représentation visuelle du zombie
        Canvas zombieCanvas = new Canvas(50, 50);
        drawZombieIcon(zombieCanvas.getGraphicsContext2D(), name, hexColor);
        box.getChildren().add(zombieCanvas);

        Text nameText = new Text(name);
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameText.setFill(Color.web(hexColor));

        Text descText = new Text(desc);
        descText.setFont(Font.font("Arial", 12));
        descText.setFill(Color.rgb(200, 200, 200));

        box.getChildren().addAll(nameText, descText);
        return box;
    }

    private void drawZombieIcon(GraphicsContext gc, String type, String hexColor) {
        Color c = Color.web(hexColor);
        double s = type.equals("BOSS") ? 44 : 32;
        double off = type.equals("BOSS") ? 3 : 9;
        gc.setFill(c);
        gc.fillRoundRect(off, off, s, s, 6, 6);
        gc.setFill(Color.rgb(255, 50, 50));
        gc.fillOval(off + s * 0.2, off + s * 0.2, s * 0.18, s * 0.18);
        gc.fillOval(off + s * 0.6, off + s * 0.2, s * 0.18, s * 0.18);
    }

    private VBox makeWavesTable() {
        VBox box = new VBox(0);
        box.setMaxWidth(700);
        box.setAlignment(Pos.CENTER_LEFT);

        String[][] waves = {
                {"1",  "5 zombies lents",             "Vague d'entrainement"},
                {"2",  "10 zombies",                  "Plus nombreux"},
                {"3",  "15 zombies + RIFLE",          "Zombies plus rapides"},
                {"4",  "20 zombies + speciaux (10%)", "Premiers zombies speciaux"},
                {"5",  "25 zombies + SHOTGUN",        "Speciaux 15%"},
                {"6",  "30 zombies",                  "Speciaux 20%"},
                {"7",  "35 zombies",                  "Speciaux 25%"},
                {"8",  "40 zombies",                  "Speciaux 30%"},
                {"9",  "45 zombies",                  "Speciaux 35% — avant le boss"},
                {"10", "1 BOSS",                      "Vaincre le boss = VICTOIRE !"}
        };

        // En-tête
        HBox header = makeTableRow("Vague", "Contenu", "Description", true);
        box.getChildren().add(header);

        for (int i = 0; i < waves.length; i++) {
            boolean isBoss = waves[i][0].equals("10");
            HBox row = makeTableRow(waves[i][0], waves[i][1], waves[i][2], false);
            if (isBoss) {
                row.setStyle("-fx-background-color: rgba(100,0,0,0.5);");
            } else if (i % 2 == 0) {
                row.setStyle("-fx-background-color: rgba(20,30,20,0.6);");
            } else {
                row.setStyle("-fx-background-color: rgba(10,15,10,0.4);");
            }
            box.getChildren().add(row);
        }
        return box;
    }

    private HBox makeTableRow(String col1, String col2, String col3, boolean isHeader) {
        HBox row = new HBox(0);
        row.setPrefWidth(700);

        Text t1 = cell(col1, 60,  isHeader);
        Text t2 = cell(col2, 300, isHeader);
        Text t3 = cell(col3, 340, isHeader);

        if (col1.equals("10")) {
            t1.setFill(Color.rgb(255, 80, 80));
            t2.setFill(Color.rgb(255, 120, 120));
            t3.setFill(Color.rgb(255, 150, 150));
        }

        row.getChildren().addAll(wrapCell(t1, 60), wrapCell(t2, 300), wrapCell(t3, 340));
        return row;
    }

    private StackPane wrapCell(Text t, double width) {
        StackPane cell = new StackPane(t);
        cell.setPrefWidth(width);
        cell.setMinWidth(width);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(5, 8, 5, 8));
        return cell;
    }

    private Text cell(String text, double width, boolean isHeader) {
        Text t = new Text(text);
        t.setFont(Font.font("Arial",
                isHeader ? FontWeight.BOLD : FontWeight.NORMAL,
                isHeader ? 13 : 12));
        t.setFill(isHeader ? Color.rgb(255, 200, 50) : Color.rgb(200, 215, 200));
        t.setWrappingWidth(width - 16);
        return t;
    }

    private VBox makeItemCard(String name, String hexColor, String desc) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(14));
        box.setPrefWidth(320);
        box.setStyle("-fx-background-color: rgba(8,8,8,0.9); " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: " + hexColor + "99; " +
                     "-fx-border-radius: 8; " +
                     "-fx-border-width: 1.5;");

        Text nameText = new Text(name);
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameText.setFill(Color.web(hexColor));

        Text descText = new Text(desc);
        descText.setFont(Font.font("Arial", 13));
        descText.setFill(Color.rgb(200, 210, 200));

        box.getChildren().addAll(nameText, descText);
        return box;
    }

    private Button makeBtn(String label, String base, String hover) {
        Button b = new Button(label);
        b.setPrefWidth(260);
        b.setPrefHeight(44);
        String bs = "-fx-background-color:" + base + ";-fx-text-fill:white;" +
                    "-fx-font-size:15px;-fx-font-weight:bold;" +
                    "-fx-background-radius:7;-fx-cursor:hand;";
        String hs = "-fx-background-color:" + hover + ";-fx-text-fill:white;" +
                    "-fx-font-size:15px;-fx-font-weight:bold;" +
                    "-fx-background-radius:7;-fx-cursor:hand;";
        b.setStyle(bs);
        b.setOnMouseEntered(e -> b.setStyle(hs));
        b.setOnMouseExited(e  -> b.setStyle(bs));
        return b;
    }

    public Button getBtnBack() { return btnBack; }
}
