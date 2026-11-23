package com.zombiesurvival.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
 * Écran de connexion et d'inscription.
 *
 * @author Elmehdi Eddareiy
 */
public class LoginView extends StackPane {

    private final TextField     tfUsername;
    private final PasswordField pfPassword;
    private final Button        btnLogin;
    private final Button        btnRegister;
    private final Button        btnBack;
    private final Label         lblError;
    private final Label         lblSuccess;

    public LoginView(double width, double height) {
        setPrefSize(width, height);

        // Fond
        Rectangle bg = new Rectangle(width, height);
        bg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(5, 5, 20)),
                new Stop(1, Color.rgb(20, 5, 30))));

        // Titre
        Text title = new Text("CONNEXION");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        title.setFill(Color.rgb(100, 150, 255));

        // Champs
        tfUsername = new TextField();
        tfUsername.setPromptText("Nom d'utilisateur");
        tfUsername.setMaxWidth(320);
        styleField(tfUsername);

        pfPassword = new PasswordField();
        pfPassword.setPromptText("Mot de passe");
        pfPassword.setMaxWidth(320);
        styleField(pfPassword);

        // Boutons
        btnLogin    = createBtn("SE CONNECTER",   "#1a4a1a", "#2a7a2a");
        btnRegister = createBtn("CREER UN COMPTE", "#1a1a4a", "#2a2a7a");
        btnBack     = createBtn("RETOUR",           "#333333", "#555555");

        // Labels message
        lblError = new Label("");
        lblError.setTextFill(Color.rgb(255, 80, 80));
        lblError.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        lblSuccess = new Label("");
        lblSuccess.setTextFill(Color.rgb(80, 255, 80));
        lblSuccess.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        VBox box = new VBox(14,
                title,
                tfUsername, pfPassword,
                btnLogin, btnRegister,
                lblError, lblSuccess,
                btnBack);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(360);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: rgba(0,0,0,0.6);" +
                     "-fx-background-radius: 12;");

        getChildren().addAll(bg, box);
    }

    private void styleField(TextField field) {
        field.setStyle(
                "-fx-background-color: #1a1a2a;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #666688;" +
                "-fx-border-color: #444466;" +
                "-fx-border-radius: 4;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 8;" +
                "-fx-font-size: 14px;");
    }

    private Button createBtn(String text, String base, String hover) {
        Button btn = new Button(text);
        btn.setMaxWidth(320);
        btn.setPrefHeight(42);
        String bs = String.format(
                "-fx-background-color:%s;-fx-text-fill:white;" +
                "-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-background-radius:5;-fx-cursor:hand;", base);
        String hs = String.format(
                "-fx-background-color:%s;-fx-text-fill:white;" +
                "-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-background-radius:5;-fx-cursor:hand;", hover);
        btn.setStyle(bs);
        btn.setOnMouseEntered(e -> btn.setStyle(hs));
        btn.setOnMouseExited(e -> btn.setStyle(bs));
        return btn;
    }

    public void showError(String msg) {
        lblError.setText(msg);
        lblSuccess.setText("");
    }

    public void showSuccess(String msg) {
        lblSuccess.setText(msg);
        lblError.setText("");
    }

    public void clearFields() {
        tfUsername.clear();
        pfPassword.clear();
        lblError.setText("");
        lblSuccess.setText("");
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String    getUsername()       { return tfUsername.getText(); }
    public String    getPassword()       { return pfPassword.getText(); }
    public Button    getBtnLogin()       { return btnLogin; }
    public Button    getBtnRegister()    { return btnRegister; }
    public Button    getBtnBack()        { return btnBack; }
}
