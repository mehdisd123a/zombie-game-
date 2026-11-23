package com.zombiesurvival.engine;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.HashSet;
import java.util.Set;

/**
 * Gère tous les événements clavier et souris.
 *
 * Compatible AZERTY et QWERTY :
 *
 *  AZERTY  → position physique → JavaFX KeyCode
 *  ──────────────────────────────────────────────
 *  Z (haut)   → position W → KeyCode.W   (ou Z)
 *  Q (gauche) → position A → KeyCode.A   (ou Q)
 *  S (bas)    → position S → KeyCode.S
 *  D (droite) → position D → KeyCode.D
 *  R          → position R → KeyCode.R
 *  E          → position E → KeyCode.E
 *
 *  JavaFX sur Windows utilise le code de position physique,
 *  donc un AZERTY envoie W quand on appuie sur Z, A quand on appuie sur Q.
 *  On écoute les DEUX pour être compatible avec les deux layouts.
 *
 * @author Elmehdi Eddareiy
 */
public class InputHandler {

    private final Set<KeyCode> keysPressed = new HashSet<>();

    private double  mouseX = 0, mouseY = 0;
    private boolean mousePressed    = false;
    private boolean spacePressed    = false;
    private boolean reloadPressed   = false;
    private boolean escPressed      = false;
    private boolean pickupPressed   = false;
    private boolean shopHealPressed = false;
    private boolean shopAmmoPressed = false;

    private int weaponSwitchIndex = -1;

    public void attachToScene(Scene scene) {

        scene.setOnKeyPressed(event -> {
            keysPressed.add(event.getCode());

            switch (event.getCode()) {
                case R      -> reloadPressed     = true;
                case E      -> pickupPressed     = true;
                case ESCAPE -> escPressed        = true;
                case SPACE  -> spacePressed      = true;
                case H      -> shopHealPressed   = true;
                case M      -> shopAmmoPressed   = true;
                case DIGIT1, NUMPAD1 -> weaponSwitchIndex = 0;
                case DIGIT2, NUMPAD2 -> weaponSwitchIndex = 1;
                case DIGIT3, NUMPAD3 -> weaponSwitchIndex = 2;
                default -> {}
            }
        });

        scene.setOnKeyReleased(event -> {
            keysPressed.remove(event.getCode());
            if (event.getCode() == KeyCode.SPACE) spacePressed = false;
        });

        scene.setOnMouseMoved(e    -> { mouseX = e.getX(); mouseY = e.getY(); });
        scene.setOnMouseDragged(e  -> { mouseX = e.getX(); mouseY = e.getY(); });

        scene.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) mousePressed = true;
        });
        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) mousePressed = false;
        });

        // Molette : changer d'arme
        scene.setOnScroll(e -> {
            if (e.getDeltaY() > 0) weaponSwitchIndex = -2;
            else                   weaponSwitchIndex = -3;
        });
    }

    public void clearFrameEvents() {
        reloadPressed     = false;
        escPressed        = false;
        pickupPressed     = false;
        shopHealPressed   = false;
        shopAmmoPressed   = false;
        weaponSwitchIndex = -1;
    }

    // ─── Déplacement ──────────────────────────────────────────────────────────
    //
    // On écoute les deux codes pour chaque direction :
    //   Haut   : Z (AZERTY label) ou W (position physique QWERTY / ce que JavaFX renvoie sur AZERTY)
    //   Gauche : Q (AZERTY label) ou A (position physique QWERTY / ce que JavaFX renvoie sur AZERTY)
    //   Bas    : S
    //   Droite : D
    //   + Flèches directionnelles comme alternative

    // ─── Déplacement — FLÈCHES UNIQUEMENT ────────────────────────────────────
    public boolean isMovingUp()    { return keysPressed.contains(KeyCode.UP); }
    public boolean isMovingDown()  { return keysPressed.contains(KeyCode.DOWN); }
    public boolean isMovingLeft()  { return keysPressed.contains(KeyCode.LEFT); }
    public boolean isMovingRight() { return keysPressed.contains(KeyCode.RIGHT); }

    // ─── Actions ──────────────────────────────────────────────────────────────
    public boolean isShooting()           { return spacePressed; }
    public boolean isReloading()          { return reloadPressed; }
    public boolean isEscPressed()         { return escPressed; }
    public boolean isPickingUp()          { return pickupPressed; }
    public boolean isShopHeal()           { return shopHealPressed; }
    public boolean isShopAmmo()           { return shopAmmoPressed; }
    public int     getWeaponSwitchIndex() { return weaponSwitchIndex; }

    // ─── Souris ───────────────────────────────────────────────────────────────

    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }
}
