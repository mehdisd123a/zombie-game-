package com.zombiesurvival.view;

import com.zombiesurvival.model.LeaderboardEntry;
import com.zombiesurvival.service.DatabaseService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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

import java.util.List;

/**
 * Écran de classement : affiche les 10 meilleurs scores.
 *
 * @author Elmehdi Eddareiy
 */
public class LeaderboardView extends StackPane {

    private final Button btnBack;

    public LeaderboardView(double width, double height) {
        setPrefSize(width, height);

        // Fond
        Rectangle bg = new Rectangle(width, height);
        bg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(10, 5, 5)),
                new Stop(1, Color.rgb(30, 5, 5))));

        // Titre
        Text title = new Text("CLASSEMENT");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        title.setFill(Color.rgb(255, 200, 50));

        // Tableau
        TableView<LeaderboardEntry> table = buildTable();
        table.setMaxWidth(700);
        table.setPrefHeight(380);
        table.setStyle(
                "-fx-background-color: #1a1a1a;" +
                "-fx-text-fill: white;" +
                "-fx-table-cell-border-color: #333333;");

        // Charger les données
        List<LeaderboardEntry> data = DatabaseService.getInstance().getLeaderboard(10);
        table.setItems(FXCollections.observableArrayList(data));

        // Bouton retour
        btnBack = new Button("RETOUR");
        btnBack.setPrefWidth(200);
        btnBack.setPrefHeight(40);
        String bs = "-fx-background-color:#333333;-fx-text-fill:white;" +
                    "-fx-font-size:13px;-fx-font-weight:bold;" +
                    "-fx-background-radius:5;-fx-cursor:hand;";
        String hs = "-fx-background-color:#555555;-fx-text-fill:white;" +
                    "-fx-font-size:13px;-fx-font-weight:bold;" +
                    "-fx-background-radius:5;-fx-cursor:hand;";
        btnBack.setStyle(bs);
        btnBack.setOnMouseEntered(e -> btnBack.setStyle(hs));
        btnBack.setOnMouseExited(e -> btnBack.setStyle(bs));

        VBox box = new VBox(20, title, table, btnBack);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));

        getChildren().addAll(bg, box);
    }

    @SuppressWarnings("unchecked")
    private TableView<LeaderboardEntry> buildTable() {
        TableView<LeaderboardEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LeaderboardEntry, String> colUser = new TableColumn<>("Joueur");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUser.setStyle("-fx-text-fill: white; -fx-alignment: CENTER;");

        TableColumn<LeaderboardEntry, Integer> colScore = new TableColumn<>("Meilleur Score");
        colScore.setCellValueFactory(new PropertyValueFactory<>("bestScore"));
        colScore.setStyle("-fx-text-fill: white; -fx-alignment: CENTER;");

        TableColumn<LeaderboardEntry, Integer> colGames = new TableColumn<>("Parties");
        colGames.setCellValueFactory(new PropertyValueFactory<>("totalGames"));
        colGames.setStyle("-fx-text-fill: white; -fx-alignment: CENTER;");

        TableColumn<LeaderboardEntry, Integer> colKills = new TableColumn<>("Zombies Tués");
        colKills.setCellValueFactory(new PropertyValueFactory<>("totalZombiesKilled"));
        colKills.setStyle("-fx-text-fill: white; -fx-alignment: CENTER;");

        table.getColumns().addAll(colUser, colScore, colGames, colKills);

        // Style du tableau
        table.setStyle(
                "-fx-background-color: #0d0d0d;" +
                "-fx-border-color: #442200;" +
                "-fx-border-width: 2;" +
                "-fx-font-size: 14px;");

        return table;
    }

    public Button getBtnBack() { return btnBack; }
}
