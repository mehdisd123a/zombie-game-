package com.zombiesurvival.view;

import com.zombiesurvival.model.*;
import com.zombiesurvival.service.Session;
import com.zombiesurvival.engine.WaveManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Rendu graphique du gameplay — v3 "clair et lisible".
 *
 * Principes de lisibilité appliqués :
 *  - Fond clair (vert herbe) pour que les entités ressortent
 *  - Joueur : cercle BLEU VIFÉ avec nom "VOUS"
 *  - Zombie normal : carré VERT FONCÉ avec croix rouge
 *  - Zombie spécial : carré VIOLET avec éclairs
 *  - Boss : carré ROUGE avec label BOSS clignotant
 *  - Items : TRÈS grands avec label texte
 *  - HUD simplifié : 3 zones claires, police plus grande
 *  - Flèches sur les bords pour les zombies hors-écran
 *  - Textes flottants "+10", "TOUCHE!" etc.
 *  - Flash rouge plein écran quand le joueur est touché
 *
 * @author Elmehdi Eddareiy
 */
public class GameView extends Canvas {

    private final GraphicsContext gc;

    // Flottants texte pour feedback (kill, hit, levelup)
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private long lastPlayerHitTime = 0;

    public GameView(double width, double height) {
        super(width, height);
        this.gc = getGraphicsContext2D();
    }

    // ─── Classe interne : texte flottant ──────────────────────────────────────
    private static class FloatingText {
        double x, y, vy = -1.2;
        double life = 1.0;
        String text;
        Color  color;
        int    size;
        FloatingText(double x, double y, String text, Color color, int size) {
            this.x = x; this.y = y; this.text = text; this.color = color; this.size = size;
        }
        void update() { y += vy; life -= 0.022; }
        boolean isDead() { return life <= 0; }
    }

    /** Ajoute un texte flottant (kill score, heal, etc.). */
    public void addFloatingText(double x, double y, String text, Color color, int size) {
        floatingTexts.add(new FloatingText(x, y, text, color, size));
    }

    /** Notifie que le joueur vient d'être touché (flash rouge). */
    public void notifyPlayerHit() { lastPlayerHitTime = System.currentTimeMillis(); }

    // ─── Rendu principal ──────────────────────────────────────────────────────

    public void render(GameData data) {
        gc.clearRect(0, 0, getWidth(), getHeight());

        drawBackground();
        drawObstacles(data);
        drawItems(data);
        drawBullets(data);
        drawParticles(data);
        drawZombies(data);
        drawPlayer(data.getPlayer());
        drawZombieArrows(data);         // flèches pour zombies hors-écran
        drawFloatingTexts();
        drawPlayerHitFlash();           // flash rouge si touché
        drawHUD(data);
        if (data.isWaveTransition()) drawShopOverlay(data);
        drawMiniMap(data);
    }

    // ─── Fond de carte ────────────────────────────────────────────────────────
    // Fond CLAIR pour que les entités ressortent
    private void drawBackground() {
        double w = getWidth(), h = getHeight();

        // Sol vert herbe clair
        gc.setFill(Color.rgb(55, 100, 55));
        gc.fillRect(0, 0, w, h);

        // Carreaux légèrement alternés pour donner une texture "gazon"
        for (int row = 0; row < (int)(h / 60) + 1; row++) {
            for (int col = 0; col < (int)(w / 60) + 1; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(Color.rgb(50, 95, 50));
                    gc.fillRect(col * 60, row * 60, 60, 60);
                }
            }
        }

        // Lignes de grille très discrètes pour s'orienter
        gc.setStroke(Color.rgb(40, 80, 40, 0.5));
        gc.setLineWidth(0.5);
        for (double x = 0; x < w; x += 60) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y < h; y += 60) gc.strokeLine(0, y, w, y);

        // Bordure de la zone de jeu — rouge sang pour montrer les limites
        gc.setStroke(Color.rgb(180, 20, 20));
        gc.setLineWidth(4);
        gc.strokeRect(2, 2, w - 4, h - 4);

        // Coins de la carte - marqueurs visuels
        gc.setFill(Color.rgb(180, 20, 20, 0.4));
        gc.fillRect(2, 2, 20, 20);
        gc.fillRect(w - 22, 2, 20, 20);
        gc.fillRect(2, h - 22, 20, 20);
        gc.fillRect(w - 22, h - 22, 20, 20);
    }

    // ─── Obstacles ────────────────────────────────────────────────────────────
    // Obstacles bien contrastés sur le fond vert
    private void drawObstacles(GameData data) {
        for (Obstacle o : data.getObstacles()) {
            double x = o.getX(), y = o.getY(), w = o.getWidth(), h = o.getHeight();

            // Ombre portée (donne de la profondeur)
            gc.setFill(Color.rgb(0, 0, 0, 0.3));
            gc.fillRoundRect(x + 4, y + 4, w, h, 6, 6);

            switch (o.getType()) {
                case CRATE -> {
                    // Caisse en bois : marron clair bien visible
                    gc.setFill(Color.rgb(160, 110, 50));
                    gc.fillRect(x, y, w, h);
                    gc.setFill(Color.rgb(190, 140, 70));
                    gc.fillRect(x + 3, y + 3, w - 6, h * 0.4);
                    gc.setStroke(Color.rgb(100, 65, 20));
                    gc.setLineWidth(2.5);
                    gc.strokeRect(x, y, w, h);
                    // Planches
                    gc.setStroke(Color.rgb(100, 65, 20));
                    gc.setLineWidth(1.5);
                    gc.strokeLine(x + w / 2, y, x + w / 2, y + h);
                    gc.strokeLine(x, y + h / 2, x + w, y + h / 2);
                    // Label
                    gc.setFill(Color.rgb(90, 55, 15));
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.fillText("CAISSE", x + w / 2, y + h / 2 + 3);
                }
                case WALL -> {
                    // Mur gris béton bien visible
                    gc.setFill(Color.rgb(130, 130, 145));
                    gc.fillRect(x, y, w, h);
                    gc.setFill(Color.rgb(150, 150, 165));
                    // Motif briques
                    int bH = 10, bW = 20;
                    for (int row = 0; row * bH < h; row++) {
                        int offset = (row % 2 == 0) ? 0 : bW / 2;
                        for (int col = -1; col * bW < w + bW; col++) {
                            int bx = (int)(x + col * bW + offset);
                            int by = (int)(y + row * bH);
                            gc.setFill(Color.rgb(135 + (col + row) % 3 * 5,
                                               135 + (col + row) % 2 * 5, 148));
                            gc.fillRect(bx + 1, by + 1, bW - 2, bH - 2);
                        }
                    }
                    gc.setStroke(Color.rgb(90, 90, 100));
                    gc.setLineWidth(2);
                    gc.strokeRect(x, y, w, h);
                }
                case BARREL -> {
                    // Baril rouge orange bien visible
                    gc.setFill(Color.rgb(190, 60, 20));
                    gc.fillOval(x, y, w, h);
                    gc.setFill(Color.rgb(220, 80, 30));
                    gc.fillOval(x + 5, y + 4, w - 10, h * 0.35);
                    gc.setStroke(Color.rgb(120, 35, 10));
                    gc.setLineWidth(2.5);
                    gc.strokeOval(x, y, w, h);
                    // Cerclages
                    gc.setStroke(Color.rgb(60, 60, 60));
                    gc.setLineWidth(2);
                    gc.strokeOval(x + 3, y + h * 0.25, w - 6, h * 0.15);
                    gc.strokeOval(x + 3, y + h * 0.60, w - 6, h * 0.15);
                    // Label DANGER
                    gc.setFill(Color.rgb(255, 220, 0));
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.fillText("!", x + w / 2, y + h / 2 + 3);
                }
            }
        }
    }

    // ─── Joueur ───────────────────────────────────────────────────────────────
    // BLEU VIF, grand, avec label "VOUS" — impossible à confondre
    private void drawPlayer(Player player) {
        double cx  = player.getCenterX();
        double cy  = player.getCenterY();
        boolean hit     = player.isFlashRed();
        boolean moving  = player.isMoving();
        double  walk    = player.getWalkCycle(); // cycle continu 0→∞
        double  swingL  = moving ? Math.sin(walk)      * 7.0 : 0; // jambe gauche
        double  swingR  = moving ? Math.sin(walk + Math.PI) * 7.0 : 0; // jambe droite (opposé)
        double  armSwing= moving ? Math.sin(walk + Math.PI) * 5.0 : 0; // bras gauche

        // Direction principale pour retourner le sprite gauche/droite
        double moveX = player.getMoveX();
        boolean facingLeft = moveX < 0;
        // Si pas de mouvement horizontal, garder l'orientation selon l'angle
        if (moveX == 0) {
            facingLeft = Math.cos(player.getAngle()) < 0;
        }
        int flip = facingLeft ? -1 : 1; // miroir horizontal

        // ── Ombre au sol ──────────────────────────────────────────────────────
        gc.setFill(Color.rgb(0, 0, 0, 0.20));
        gc.fillOval(cx - 15, cy + 16, 30, 7);

        gc.save();
        gc.translate(cx, cy);
        gc.scale(flip, 1); // retourner selon la direction

        // ── Couleurs ──────────────────────────────────────────────────────────
        Color skinColor  = hit ? Color.rgb(255, 160, 160) : Color.rgb(255, 210, 160);
        Color pantsColor = hit ? Color.rgb(255,  80,  80) : Color.rgb( 40,  65, 150);
        Color shirtColor = hit ? Color.rgb(255,  80,  80) : Color.rgb( 25, 110, 210);
        Color shoeColor  = Color.rgb(35, 20, 8);
        Color hairColor  = Color.rgb(55, 32, 8);
        Color capColor   = Color.rgb(55, 80, 35);

        // ══ JAMBES ════════════════════════════════════════════════════════════

        // Jambe GAUCHE (swing avant/arrière)
        gc.save();
        gc.translate(-5, 10);
        gc.rotate(swingL);
        gc.setFill(pantsColor);
        gc.fillRoundRect(-4, 0, 8, 14, 3, 3);  // cuisse
        gc.save();
        gc.translate(0, 12);
        gc.rotate(-swingL * 0.4); // genou légèrement fléchi
        gc.fillRoundRect(-4, 0, 8, 10, 3, 3);  // mollet
        gc.setFill(shoeColor);
        gc.fillRoundRect(-5, 9, 11, 5, 3, 3);  // chaussure
        gc.restore();
        gc.restore();

        // Jambe DROITE (swing opposé)
        gc.save();
        gc.translate(5, 10);
        gc.rotate(swingR);
        gc.setFill(pantsColor);
        gc.fillRoundRect(-4, 0, 8, 14, 3, 3);
        gc.save();
        gc.translate(0, 12);
        gc.rotate(-swingR * 0.4);
        gc.fillRoundRect(-4, 0, 8, 10, 3, 3);
        gc.setFill(shoeColor);
        gc.fillRoundRect(-5, 9, 11, 5, 3, 3);
        gc.restore();
        gc.restore();

        // ══ TORSE ════════════════════════════════════════════════════════════
        gc.setFill(shirtColor);
        gc.fillRoundRect(-10, -8, 20, 14, 5, 5);
        // Poche
        gc.setFill(Color.rgb(15, 80, 175));
        gc.fillRect(-8, -6, 5, 4);
        // Col
        gc.setFill(skinColor);
        gc.fillRect(-3, -9, 6, 3);

        // ══ BRA GAUCHE (swing avant/arrière, opposé jambe gauche) ════════════
        gc.save();
        gc.translate(-13, -5);
        gc.rotate(-armSwing); // bras gauche swing avant quand jambe droite avance
        gc.setFill(shirtColor);
        gc.fillRoundRect(-3, 0, 6, 13, 3, 3);
        gc.setFill(skinColor);
        gc.fillOval(-3, 12, 7, 7); // main
        gc.restore();

        // ══ BRA DROIT + ARME ════════════════════════════════════════════════
        gc.save();
        gc.translate(13, -5);
        gc.rotate(armSwing); // bras droit swing opposé
        gc.setFill(shirtColor);
        gc.fillRoundRect(-3, 0, 6, 11, 3, 3);
        gc.setFill(skinColor);
        gc.fillOval(-3, 10, 7, 7);

        // Arme tenue dans la main droite
        Color gunColor;
        double gunLen, gunH;
        switch (player.getWeapon().getType()) {
            case RIFLE   -> { gunColor = Color.rgb(160, 140, 30); gunLen = 28; gunH = 4; }
            case SHOTGUN -> { gunColor = Color.rgb(120, 65,  12); gunLen = 20; gunH = 7; }
            default      -> { gunColor = Color.rgb(55,  55,  55); gunLen = 18; gunH = 4; }
        }
        gc.setFill(gunColor);
        gc.fillRoundRect(3, 12, (int) gunLen, (int) gunH, 2, 2);
        // Canon
        gc.setFill(Color.rgb(200, 190, 40));
        gc.fillOval((int)(3 + gunLen - 2), 12, 5, (int) gunH + 1);
        gc.restore();

        // ══ COU ══════════════════════════════════════════════════════════════
        gc.setFill(skinColor);
        gc.fillRect(-4, -15, 8, 8);

        // ══ TÊTE ═════════════════════════════════════════════════════════════
        // Légère oscillation de la tête pendant la marche
        double headBob = moving ? Math.sin(walk * 2) * 1.5 : 0;

        gc.save();
        gc.translate(0, headBob);

        // Visage
        gc.setFill(skinColor);
        gc.fillOval(-11, -28, 22, 22);
        gc.setStroke(Color.rgb(190, 150, 110));
        gc.setLineWidth(1);
        gc.strokeOval(-11, -28, 22, 22);

        // Cheveux (dessous de la casquette)
        gc.setFill(hairColor);
        gc.fillOval(-11, -28, 22, 10);

        // Oreilles
        gc.setFill(skinColor);
        gc.fillOval(-13, -22, 5, 7);
        gc.fillOval(8,   -22, 5, 7);

        // ── Yeux ─────────────────────────────────────────────────────────────
        gc.setFill(Color.WHITE);
        gc.fillOval(-7, -22, 5, 5);
        gc.fillOval(2,  -22, 5, 5);
        gc.setFill(Color.rgb(50, 90, 200));
        gc.fillOval(-6, -21, 3, 3);
        gc.fillOval(3,  -21, 3, 3);
        gc.setFill(Color.BLACK);
        gc.fillOval(-5.5, -20.5, 1.5, 1.5);
        gc.fillOval(3.5,  -20.5, 1.5, 1.5);

        // Sourcils
        gc.setStroke(hairColor);
        gc.setLineWidth(1.5);
        gc.strokeLine(-7, -24, -2, -23);
        gc.strokeLine(2,  -23,  7, -24);

        // Bouche
        gc.setStroke(Color.rgb(180, 80, 60));
        gc.setLineWidth(1.2);
        gc.strokeArc(-4, -17, 8, 5, 200, 140,
                javafx.scene.shape.ArcType.OPEN);

        // ── Casquette militaire ───────────────────────────────────────────────
        gc.setFill(capColor);
        gc.fillOval(-11, -31, 22, 10);   // calotte
        gc.fillRect(-11, -26, 22,  4);   // bord
        gc.setFill(Color.rgb(40, 60, 22));
        gc.fillRect(-13, -23, 26,  3);   // visière
        // Insigne (petit cercle)
        gc.setFill(Color.rgb(220, 180, 50));
        gc.fillOval(-2, -29, 4, 4);

        gc.restore(); // fin headBob

        gc.restore(); // fin flip

        // ══ LABEL NOM ════════════════════════════════════════════════════════
        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRoundRect(cx - 28, player.getY() - 44, 56, 15, 5, 5);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("VOUS", cx, player.getY() - 32);

        gc.setFill(Color.rgb(255, 220, 50));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText("Niv." + player.getLevel(), cx, player.getY() - 46);
    }

    // ─── Zombies ──────────────────────────────────────────────────────────────
    // Chaque type a une forme ET une couleur distinctes
    private void drawZombies(GameData data) {
        List<Zombie> copy = new ArrayList<>(data.getZombies());
        for (Zombie z : copy) drawZombie(z);
    }

    private void drawZombie(Zombie z) {
        double x = z.getX(), y = z.getY(), w = z.getWidth(), h = z.getHeight();

        // Flash blanc quand touché → très visible
        if (z.isFlashHit()) {
            gc.setFill(Color.rgb(255, 255, 255, 0.8));
            gc.fillRoundRect(x - 4, y - 4, w + 8, h + 8, 10, 10);
        }

        // Ombre portée
        gc.setFill(Color.rgb(0, 0, 0, 0.35));
        gc.fillRoundRect(x + 4, y + 4, w, h, 8, 8);

        if (z instanceof Boss) {
            drawBoss((Boss) z, x, y, w, h);
        } else if (z instanceof ZombieSpecial) {
            drawZombieSpecial(z, x, y, w, h);
        } else {
            drawZombieNormal(z, x, y, w, h);
        }

        // Barre de vie : toujours affichée, bien lisible
        drawHealthBar(x, y - 10, w, z.getHp(), z.getMaxHp(), 7);
    }

    private void drawZombieNormal(Zombie z, double x, double y, double w, double h) {
        // Forme : carré vert foncé avec visage
        Color fill = z.isFlashHit() ? Color.WHITE : Color.rgb(50, 160, 50);
        gc.setFill(fill);
        gc.fillRoundRect(x, y, w, h, 8, 8);
        gc.setStroke(Color.rgb(20, 100, 20));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, w, h, 8, 8);

        // Yeux rouges (signature du zombie)
        gc.setFill(Color.rgb(255, 30, 30));
        gc.fillOval(x + w * 0.18, y + h * 0.22, w * 0.22, h * 0.22);
        gc.fillOval(x + w * 0.60, y + h * 0.22, w * 0.22, h * 0.22);
        // Pupilles noires
        gc.setFill(Color.BLACK);
        gc.fillOval(x + w * 0.23, y + h * 0.28, w * 0.11, h * 0.11);
        gc.fillOval(x + w * 0.65, y + h * 0.28, w * 0.11, h * 0.11);
        // Bouche menaçante
        gc.setStroke(Color.rgb(180, 10, 10));
        gc.setLineWidth(2);
        gc.strokeArc(x + w * 0.2, y + h * 0.58, w * 0.6, h * 0.22,
                     200, 140, javafx.scene.shape.ArcType.OPEN);

        // Label type
        gc.setFill(Color.rgb(20, 80, 20));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("ZOMBIE", x + w / 2, y + h + 10);
    }

    private void drawZombieSpecial(Zombie z, double x, double y, double w, double h) {
        // Forme : losange VIOLET — différent du zombie normal
        double cx = x + w / 2, cy = y + h / 2;
        double[] px = {cx, cx + w / 2 + 2, cx, cx - w / 2 - 2};
        double[] py = {cy - h / 2 - 2, cy, cy + h / 2 + 2, cy};

        Color fill = z.isFlashHit() ? Color.WHITE : Color.rgb(150, 50, 220);
        gc.setFill(fill);
        gc.fillPolygon(px, py, 4);
        gc.setStroke(Color.rgb(220, 120, 255));
        gc.setLineWidth(2.5);
        gc.strokePolygon(px, py, 4);

        // Yeux blancs (différents du zombie normal)
        gc.setFill(Color.rgb(255, 255, 100));
        gc.fillOval(cx - 9, cy - 7, 8, 8);
        gc.fillOval(cx + 1,  cy - 7, 8, 8);

        // Label
        gc.setFill(Color.rgb(220, 150, 255));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("RAPIDE", cx, y + h + 14);
    }

    private void drawBoss(Boss boss, double x, double y, double w, double h) {
        // Forme : grand carré ROUGE avec animation clignotante
        long now = System.currentTimeMillis();
        boolean blink = (now / 400) % 2 == 0;

        // Halo pulsant rouge
        double pulse = 0.3 + 0.2 * Math.sin(now / 200.0);
        gc.setFill(Color.rgb(255, 0, 0, pulse));
        gc.fillRect(x - 8, y - 8, w + 16, h + 16);

        // Corps rouge
        Color bodyColor = boss.isCharging()
                ? Color.rgb(255, 120, 0)   // orange si charge
                : (blink ? Color.rgb(220, 20, 20) : Color.rgb(190, 10, 10));
        gc.setFill(bodyColor);
        gc.fillRect(x, y, w, h);

        // Motif intérieur
        gc.setFill(Color.rgb(140, 0, 0));
        gc.fillRect(x + 8, y + 8, w - 16, h - 16);

        // Yeux jaunes GRANDS
        gc.setFill(Color.rgb(255, 220, 0));
        gc.fillOval(x + 12, y + 12, 18, 18);
        gc.fillOval(x + w - 30, y + 12, 18, 18);
        gc.setFill(Color.BLACK);
        gc.fillOval(x + 17, y + 17, 8, 8);
        gc.fillOval(x + w - 25, y + 17, 8, 8);

        gc.setStroke(Color.rgb(255, 60, 60));
        gc.setLineWidth(3.5);
        gc.strokeRect(x, y, w, h);

        // Label BOSS clignotant grand et lisible
        gc.setFill(Color.rgb(255, 220, 0));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(boss.isCharging() ? "CHARGE !!!" : "BOSS", x + w / 2, y - 14);

        if (boss.getPhase() == 2) {
            gc.setFill(Color.rgb(255, 100, 100));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gc.fillText("PHASE 2", x + w / 2, y - 28);
        }

        // Barre de vie du boss : GRANDE et bien visible
        double ratio = boss.getHp() / boss.getMaxHp();
        gc.setFill(Color.rgb(50, 0, 0));
        gc.fillRoundRect(x - 10, y + h + 5, w + 20, 12, 4, 4);
        gc.setFill(ratio > 0.5 ? Color.rgb(50, 220, 50)
                 : ratio > 0.25 ? Color.rgb(255, 180, 0)
                 : Color.rgb(220, 20, 20));
        gc.fillRoundRect(x - 10, y + h + 5, (w + 20) * ratio, 12, 4, 4);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x - 10, y + h + 5, w + 20, 12, 4, 4);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.fillText((int) boss.getHp() + "/" + (int) boss.getMaxHp(),
                     x + w / 2, y + h + 15);
    }

    // ─── Barre de vie entité ──────────────────────────────────────────────────
    private void drawHealthBar(double x, double y, double width,
                               double hp, double maxHp, double barHeight) {
        double ratio = hp / maxHp;
        gc.setFill(Color.rgb(40, 0, 0));
        gc.fillRoundRect(x, y, width, barHeight, 3, 3);
        gc.setFill(ratio > 0.6 ? Color.rgb(30, 200, 30)
                 : ratio > 0.3 ? Color.rgb(240, 170, 0)
                 : Color.rgb(220, 20, 20));
        gc.fillRoundRect(x, y, width * ratio, barHeight, 3, 3);
        gc.setStroke(Color.rgb(0, 0, 0, 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, width, barHeight, 3, 3);
    }

    // ─── Balles ───────────────────────────────────────────────────────────────
    // Balles JAUNES VIFES bien visibles sur fond vert
    private void drawBullets(GameData data) {
        List<Bullet> copy = new ArrayList<>(data.getBullets());
        for (Bullet b : copy) {
            if (!b.isActive()) continue;
            // Halo
            gc.setFill(Color.rgb(255, 240, 100, 0.5));
            gc.fillOval(b.getX() - 3, b.getY() - 3, Bullet.WIDTH + 6, Bullet.HEIGHT + 6);
            // Corps
            gc.setFill(Color.rgb(255, 230, 50));
            gc.fillOval(b.getX(), b.getY(), Bullet.WIDTH, Bullet.HEIGHT);
        }
    }

    // ─── Particules ───────────────────────────────────────────────────────────
    private void drawParticles(GameData data) {
        List<Particle> copy = new ArrayList<>(data.getParticles());
        for (Particle p : copy) {
            if (p.isDead()) continue;
            gc.setFill(Color.rgb(p.getR(), p.getG(), p.getB(), p.getLife()));
            double s = p.getSize();
            gc.fillOval(p.getX() - s / 2, p.getY() - s / 2, s, s);
        }
    }

    // ─── Items ────────────────────────────────────────────────────────────────
    // Items GRANDS avec label texte clair — impossibles à rater
    private void drawItems(GameData data) {
        List<Item> copy = new ArrayList<>(data.getItems());
        for (Item item : copy) {
            if (item.isCollected()) continue;
            drawItem(item);
        }
    }

    private void drawItem(Item item) {
        double x = item.getX(), y = item.getY();
        double s = Item.WIDTH;
        long now = System.currentTimeMillis();
        double pulse = 0.5 + 0.5 * Math.sin(now / 350.0);
        double bounce = Math.sin(now / 500.0) * 3; // rebond vertical

        double drawY = y + bounce;

        if (item.getType() == Item.ItemType.HEALTH) {
            // Trousse de soins : fond vert clair avec croix rouge
            // Halo vert pulsant
            gc.setFill(Color.rgb(50, 220, 50, 0.4 * pulse));
            gc.fillOval(x - 8, drawY - 8, s + 16, s + 16);

            gc.setFill(Color.rgb(240, 240, 240));
            gc.fillRoundRect(x, drawY, s, s, 5, 5);
            gc.setStroke(Color.rgb(30, 180, 30));
            gc.setLineWidth(2.5);
            gc.strokeRoundRect(x, drawY, s, s, 5, 5);

            // Croix rouge épaisse
            gc.setFill(Color.rgb(220, 20, 20));
            gc.fillRect(x + s * 0.35, drawY + s * 0.08, s * 0.3, s * 0.84);
            gc.fillRect(x + s * 0.08, drawY + s * 0.35, s * 0.84, s * 0.3);

            // Label
            gc.setFill(Color.rgb(255, 255, 255));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("+25 HP", x + s / 2, drawY + s + 12);

        } else {
            // Munitions : fond jaune foncé avec icône balle
            gc.setFill(Color.rgb(220, 180, 0, 0.4 * pulse));
            gc.fillOval(x - 8, drawY - 8, s + 16, s + 16);

            gc.setFill(Color.rgb(60, 50, 10));
            gc.fillRoundRect(x, drawY, s, s, 5, 5);
            gc.setStroke(Color.rgb(220, 180, 0));
            gc.setLineWidth(2.5);
            gc.strokeRoundRect(x, drawY, s, s, 5, 5);

            // 3 petites balles dessinées
            gc.setFill(Color.rgb(255, 210, 50));
            gc.fillOval(x + s * 0.15, drawY + s * 0.2, s * 0.2, s * 0.5);
            gc.fillOval(x + s * 0.40, drawY + s * 0.15, s * 0.2, s * 0.5);
            gc.fillOval(x + s * 0.65, drawY + s * 0.2, s * 0.2, s * 0.5);

            // Label
            gc.setFill(Color.rgb(255, 220, 50));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("+15 Balles", x + s / 2, drawY + s + 12);
        }
    }

    // ─── Flèches vers zombies hors-écran ─────────────────────────────────────
    // Triangles rouges sur les bords indiquant où sont les zombies
    private void drawZombieArrows(GameData data) {
        double W = getWidth(), H = getHeight();
        double margin = 20;

        List<Zombie> copy = new ArrayList<>(data.getZombies());
        for (Zombie z : copy) {
            double zx = z.getCenterX(), zy = z.getCenterY();
            // Seulement pour les zombies HORS de l'écran visible
            if (zx < 0 || zx > W || zy < 0 || zy > H) {
                // Direction du joueur vers ce zombie
                double angle = Math.atan2(zy - H / 2, zx - W / 2);
                // Position sur le bord
                double arrowX = W / 2 + Math.cos(angle) * Math.min(W / 2 - margin, H / 2 - margin);
                double arrowY = H / 2 + Math.sin(angle) * Math.min(H / 2 - margin, W / 2 - margin);

                // Clipper sur les bords
                arrowX = Math.max(margin, Math.min(W - margin, arrowX));
                arrowY = Math.max(margin + 56, Math.min(H - margin - 52, arrowY));

                drawArrow(arrowX, arrowY, angle, z instanceof Boss
                        ? Color.rgb(255, 50, 50)
                        : Color.rgb(255, 180, 50));
            }
        }
    }

    private void drawArrow(double x, double y, double angle, Color color) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(angle));
        // Triangle flèche
        gc.setFill(color);
        double[] px = {12, -6, -6};
        double[] py = {0,  -6,  6};
        gc.fillPolygon(px, py, 3);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokePolygon(px, py, 3);
        gc.restore();
    }

    // ─── Textes flottants ─────────────────────────────────────────────────────
    private void drawFloatingTexts() {
        List<FloatingText> copy = new ArrayList<>(floatingTexts);
        for (FloatingText ft : copy) {
            ft.update();
            if (ft.isDead()) { floatingTexts.remove(ft); continue; }
            gc.setGlobalAlpha(ft.life);
            gc.setFill(ft.color);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, ft.size));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(ft.text, ft.x, ft.y);
        }
        gc.setGlobalAlpha(1.0);
    }

    // ─── Flash rouge quand le joueur est touché ───────────────────────────────
    private void drawPlayerHitFlash() {
        long elapsed = System.currentTimeMillis() - lastPlayerHitTime;
        if (elapsed < 300) {
            double alpha = 0.35 * (1.0 - elapsed / 300.0);
            gc.setFill(Color.rgb(255, 0, 0, alpha));
            gc.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // ─── HUD simplifié ────────────────────────────────────────────────────────
    // 3 zones claires : gauche (vie), centre (vague/score), droite (arme)
    private void drawHUD(GameData data) {
        Player p = data.getPlayer();
        Weapon w = p.getWeapon();
        double W = getWidth(), H = getHeight();

        // ── Bande HUD haut ────────────────────────────────────────────────────
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, W, 60);
        gc.setStroke(Color.rgb(100, 100, 100, 0.5));
        gc.setLineWidth(1);
        gc.strokeLine(0, 60, W, 60);

        // ── Zone gauche : VIE ─────────────────────────────────────────────────
        double hpRatio = (double) p.getHp() / p.getMaxHp();
        // Icône cœur simplifié
        gc.setFill(Color.rgb(220, 30, 30));
        gc.fillOval(10, 10, 14, 14);
        gc.fillOval(20, 10, 14, 14);
        gc.fillPolygon(new double[]{10, 34, 22}, new double[]{18, 18, 32}, 3);

        // Label HP
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(p.getHp() + " / " + p.getMaxHp(), 38, 24);

        // Barre HP grande et lisible
        Color hpColor = hpRatio > 0.5 ? Color.rgb(50, 220, 50)
                      : hpRatio > 0.25 ? Color.rgb(255, 180, 0)
                      : Color.rgb(220, 30, 30);
        gc.setFill(Color.rgb(40, 40, 40));
        gc.fillRoundRect(10, 30, 180, 20, 8, 8);
        gc.setFill(hpColor);
        gc.fillRoundRect(10, 30, 180 * hpRatio, 20, 8, 8);
        gc.setStroke(Color.rgb(200, 200, 200, 0.6));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(10, 30, 180, 20, 8, 8);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("VIE", 15, 44);

        // Barre XP sous la vie
        double xpRatio = (double) p.getXp() / p.getXpToNextLevel();
        gc.setFill(Color.rgb(20, 20, 60));
        gc.fillRoundRect(10, 52, 180, 6, 3, 3);
        gc.setFill(Color.rgb(100, 120, 255));
        gc.fillRoundRect(10, 52, 180 * xpRatio, 6, 3, 3);
        gc.setFill(Color.rgb(150, 170, 255));
        gc.setFont(Font.font("Arial", 9));
        gc.fillText("XP  Niv." + p.getLevel(), 15, 61);

        // ── Zone centre : VAGUE et SCORE ─────────────────────────────────────
        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.rgb(255, 90, 90));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("STAGE " + data.getCurrentWave()
                + "  —  " + WaveManager.getStageName(data.getCurrentWave()),
                W / 2, 22);

        gc.setFill(Color.rgb(255, 220, 50));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        gc.fillText("SCORE : " + data.getScore(), W / 2, 42);

        // Compteur zombies actifs (aide le joueur à savoir combien restent)
        int aliveCount = data.getZombies().size();
        gc.setFill(aliveCount > 0
                ? Color.rgb(255, 120, 50)
                : Color.rgb(100, 220, 100));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText(aliveCount > 0
                ? aliveCount + " zombie(s) restant(s)"
                : "Stage termine !",
                W / 2, 60);

        // ── Zone droite : ARME ───────────────────────────────────────────────
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.rgb(200, 200, 200));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText(w.getName(), W - 10, 20);

        // Munitions : grands et lisibles
        if (w.isReloading()) {
            long elapsed  = System.currentTimeMillis() - w.getReloadStartTime();
            double reload = Math.min(1.0, (double) elapsed / w.getReloadDurationMs());
            gc.setFill(Color.rgb(255, 165, 0));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("RECHARGEMENT...", W - 10, 38);
            gc.setFill(Color.rgb(40, 40, 40));
            gc.fillRoundRect(W - 190, 42, 180, 10, 4, 4);
            gc.setFill(Color.rgb(255, 165, 0));
            gc.fillRoundRect(W - 190, 42, 180 * reload, 10, 4, 4);
        } else {
            gc.setFill(w.getAmmoInMag() > 3
                    ? Color.rgb(220, 220, 50)
                    : Color.rgb(255, 60, 60));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            gc.fillText(w.getAmmoInMag() + "", W - 10, 42);
            gc.setFill(Color.rgb(150, 150, 150));
            gc.setFont(Font.font("Arial", 13));
            gc.fillText("/ " + w.getTotalAmmo() + " restantes", W - 10, 58);
        }

        // ── Bande HUD bas : slots armes ───────────────────────────────────────
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, H - 58, W, 58);
        gc.setStroke(Color.rgb(100, 100, 100, 0.5));
        gc.setLineWidth(1);
        gc.strokeLine(0, H - 58, W, H - 58);
        drawWeaponSlots(data);

        // ── Conseil contextuel ────────────────────────────────────────────────
        drawContextualTip(data, p, w);

        // ── Banner level up ───────────────────────────────────────────────────
        if (p.consumeLevelUp()) {
            gc.setFill(Color.rgb(60, 80, 220, 0.9));
            gc.fillRoundRect(W / 2 - 160, H / 2 - 35, 320, 70, 14, 14);
            gc.setStroke(Color.rgb(150, 180, 255));
            gc.setLineWidth(2);
            gc.strokeRoundRect(W / 2 - 160, H / 2 - 35, 320, 70, 14, 14);
            gc.setFill(Color.rgb(255, 220, 50));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("NIVEAU " + p.getLevel() + " !", W / 2, H / 2 + 5);
            gc.setFill(Color.rgb(180, 200, 255));
            gc.setFont(Font.font("Arial", 13));
            gc.fillText("+10 HP max  |  +vitesse", W / 2, H / 2 + 24);
        }
    }

    // Conseil contextuel : affiché seulement quand c'est pertinent
    private void drawContextualTip(GameData data, Player p, Weapon w) {
        double W = getWidth(), H = getHeight();
        String tip = null;

        if (w.getAmmoInMag() == 0 && !w.isReloading()) {
            tip = "Appuyez sur [R] pour recharger !";
        } else if ((double) p.getHp() / p.getMaxHp() < 0.25) {
            tip = "Vie critique ! Cherchez une trousse de soins.";
        } else if (data.getCurrentWave() == 1 && data.getZombiesKilled() == 0) {
            tip = "Utilisez les FLECHES pour bouger et ESPACE pour tirer !";
        }

        if (tip != null) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRoundRect(W / 2 - 220, H - 100, 440, 28, 10, 10);
            gc.setFill(Color.rgb(255, 220, 100));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText(tip, W / 2, H - 81);
        }

        // Rappel commandes bas-droite (permanent)
        gc.setFill(Color.rgb(100, 120, 100));
        gc.setFont(Font.font("Arial", 10));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("[Fleches] Bouger   [ESPACE] Tirer   [ESC] Pause   [1/2/3] Armes",
                W - 8, H - 62);
    }

    // ─── Slots armes (HUD bas) ────────────────────────────────────────────────
    private void drawWeaponSlots(GameData data) {
        Player p = data.getPlayer();
        double W = getWidth(), H = getHeight();
        double slotW = 110, slotH = 50;
        double totalW = 3 * slotW + 2 * 8;
        double startX = W / 2 - totalW / 2;

        String[] labels = {"[1] Pistol", "[2] Rifle", "[3] Shotgun"};
        Color[]  accents = {
                Color.rgb(100, 160, 255),
                Color.rgb(100, 220, 100),
                Color.rgb(255, 150, 60)
        };

        for (int i = 0; i < 3; i++) {
            double sx = startX + i * (slotW + 8);
            double sy = H - 55;
            boolean owned    = i < p.getWeapons().size();
            boolean selected = p.getCurrentWeaponIndex() == i && owned;

            // Fond slot
            gc.setFill(selected
                    ? Color.rgb(30, 40, 70, 0.95)
                    : Color.rgb(10, 10, 20, 0.8));
            gc.fillRoundRect(sx, sy, slotW, slotH, 8, 8);

            // Bordure colorée si sélectionné
            gc.setStroke(selected ? accents[i] : Color.rgb(60, 60, 80));
            gc.setLineWidth(selected ? 2.5 : 1);
            gc.strokeRoundRect(sx, sy, slotW, slotH, 8, 8);

            gc.setTextAlign(TextAlignment.CENTER);
            double cx = sx + slotW / 2;

            if (owned) {
                Weapon wSlot = p.getWeapons().get(i);
                // Nom de l'arme
                gc.setFill(selected ? accents[i] : Color.rgb(180, 180, 180));
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                gc.fillText(labels[i], cx, sy + 16);

                // Munitions ou rechargement
                if (wSlot.isReloading()) {
                    long el = System.currentTimeMillis() - wSlot.getReloadStartTime();
                    double ratio = Math.min(1.0, (double) el / wSlot.getReloadDurationMs());
                    gc.setFill(Color.rgb(255, 165, 0, 0.8));
                    gc.fillRoundRect(sx + 6, sy + 32, (slotW - 12) * ratio, 8, 3, 3);
                    gc.setFill(Color.rgb(255, 165, 0));
                    gc.setFont(Font.font("Arial", 10));
                    gc.fillText("Rechargement...", cx, sy + 30);
                } else {
                    Color ammoColor = wSlot.getAmmoInMag() > 3
                            ? Color.rgb(220, 220, 100)
                            : Color.rgb(255, 80, 80);
                    gc.setFill(ammoColor);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    gc.fillText(wSlot.getAmmoInMag() + "/" + wSlot.getTotalAmmo(), cx, sy + 38);
                }
            } else {
                // Arme non débloquée
                gc.setFill(Color.rgb(80, 80, 80));
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                gc.fillText(labels[i], cx, sy + 18);
                gc.setFill(Color.rgb(60, 60, 60));
                gc.setFont(Font.font("Arial", 10));
                gc.fillText("Non debloquee", cx, sy + 34);
            }
        }
    }

    // ─── Overlay boutique ─────────────────────────────────────────────────────
    private void drawShopOverlay(GameData data) {
        double W = getWidth(), H = getHeight();
        int    stage     = data.getCurrentWave();
        int    nextStage = stage + 1;
        int    countdown = data.getWaveTransitionCountdown();

        // ── Fond semi-transparent ─────────────────────────────────────────────
        gc.setFill(Color.rgb(0, 0, 0, 0.82));
        gc.fillRect(0, 0, W, H);

        gc.setTextAlign(TextAlignment.CENTER);

        // ── "STAGE X TERMINE !" ───────────────────────────────────────────────
        gc.setFill(Color.rgb(255, 220, 50));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 44));
        gc.fillText("STAGE " + stage + " TERMINE !", W / 2, H / 2 - 110);

        gc.setFill(Color.rgb(100, 255, 120));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("+" + (150 * stage) + " pts  |  +" + (60 * stage) + " XP", W / 2, H / 2 - 70);

        // ── Séparateur ────────────────────────────────────────────────────────
        gc.setStroke(Color.rgb(100, 100, 100, 0.5));
        gc.setLineWidth(1);
        gc.strokeLine(W / 2 - 250, H / 2 - 48, W / 2 + 250, H / 2 - 48);

        // ── Prochain stage ────────────────────────────────────────────────────
        if (nextStage <= 10) {
            // Couleur selon la difficulté
            Color stageColor = switch (nextStage) {
                case 1, 2  -> Color.rgb(100, 220, 100);
                case 3, 4  -> Color.rgb(220, 220, 50);
                case 5, 6  -> Color.rgb(255, 150, 30);
                case 7, 8  -> Color.rgb(255, 80, 80);
                case 9, 10 -> Color.rgb(220, 30, 30);
                default    -> Color.WHITE;
            };

            gc.setFill(Color.rgb(180, 180, 180));
            gc.setFont(Font.font("Arial", 16));
            gc.fillText("PROCHAIN STAGE :", W / 2, H / 2 - 20);

            gc.setFill(stageColor);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            gc.fillText("STAGE " + nextStage + " — " + WaveManager.getStageName(nextStage),
                    W / 2, H / 2 + 22);

            gc.setFill(Color.rgb(200, 200, 200));
            gc.setFont(Font.font("Arial", 15));
            gc.fillText(WaveManager.getStageDesc(nextStage), W / 2, H / 2 + 55);

            // Barre de progression stages (1 à 10)
            double barW = 500, barH = 12;
            double barX = W / 2 - barW / 2;
            double barY = H / 2 + 80;
            gc.setFill(Color.rgb(40, 40, 40));
            gc.fillRoundRect(barX, barY, barW, barH, 5, 5);
            gc.setFill(stageColor);
            gc.fillRoundRect(barX, barY, barW * (stage / 10.0), barH, 5, 5);
            gc.setStroke(Color.rgb(100, 100, 100));
            gc.setLineWidth(1);
            gc.strokeRoundRect(barX, barY, barW, barH, 5, 5);
            // Numéros de stage
            for (int i = 1; i <= 10; i++) {
                double tx = barX + barW * (i / 10.0) - barW / 20.0;
                gc.setFill(i <= stage ? stageColor : Color.rgb(80, 80, 80));
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                gc.fillText(String.valueOf(i), tx, barY + 25);
            }
        } else {
            gc.setFill(Color.rgb(255, 220, 50));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
            gc.fillText("TU AS SURVIE A TOUS LES STAGES !", W / 2, H / 2 + 20);
        }

        // ── Bouton boutique rapide ────────────────────────────────────────────
        double panW = 580, panH = 70;
        double panX = W / 2 - panW / 2, panY = H / 2 + 108;
        gc.setFill(Color.rgb(10, 15, 10, 0.9));
        gc.fillRoundRect(panX, panY, panW, panH, 10, 10);
        gc.setStroke(Color.rgb(60, 120, 60));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(panX, panY, panW, panH, 10, 10);

        gc.setFill(Color.rgb(150, 200, 150));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("BOUTIQUE RAPIDE", W / 2, panY + 18);

        gc.setFill(Color.rgb(200, 200, 200));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("[2] Rifle 150pts   [3] Shotgun 200pts   [H] +50HP 50pts   [M] +Balles 40pts",
                W / 2, panY + 40);

        gc.setFill(Color.rgb(100, 180, 100));
        gc.setFont(Font.font("Arial", 11));
        gc.fillText("Score : " + data.getScore() + " pts", W / 2, panY + 58);

        // ── Compte à rebours ──────────────────────────────────────────────────
        gc.setFill(countdown <= 1 ? Color.rgb(255, 100, 100) : Color.rgb(220, 220, 220));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("Debut dans " + countdown + "s...", W / 2, H / 2 + 200);
    }

    // ─── Mini-map ─────────────────────────────────────────────────────────────
    private void drawMiniMap(GameData data) {
        double W = getWidth(), H = getHeight();
        double mmW = 115, mmH = 82, mmX = W - mmW - 10, mmY = 68;
        double scaleX = mmW / GameData.MAP_WIDTH, scaleY = mmH / GameData.MAP_HEIGHT;

        // Fond de la mini-map
        gc.setFill(Color.rgb(20, 35, 20, 0.85));
        gc.fillRoundRect(mmX - 2, mmY - 2, mmW + 4, mmH + 4, 6, 6);
        gc.setFill(Color.rgb(40, 70, 40, 0.9));
        gc.fillRoundRect(mmX, mmY, mmW, mmH, 5, 5);
        gc.setStroke(Color.rgb(80, 140, 80, 0.8));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(mmX, mmY, mmW, mmH, 5, 5);

        // Obstacles (gris foncé)
        gc.setFill(Color.rgb(100, 80, 60));
        for (Obstacle o : data.getObstacles()) {
            gc.fillRect(mmX + o.getX() * scaleX, mmY + o.getY() * scaleY,
                    Math.max(2, o.getWidth() * scaleX),
                    Math.max(2, o.getHeight() * scaleY));
        }

        // Zombies (rouge, avec taille selon type)
        for (Zombie z : new ArrayList<>(data.getZombies())) {
            if (z instanceof Boss) {
                gc.setFill(Color.rgb(255, 50, 50));
                gc.fillRect(mmX + z.getCenterX() * scaleX - 3,
                            mmY + z.getCenterY() * scaleY - 3, 6, 6);
            } else {
                gc.setFill(z instanceof ZombieSpecial
                        ? Color.rgb(200, 80, 255)
                        : Color.rgb(220, 60, 60));
                gc.fillOval(mmX + z.getCenterX() * scaleX - 2,
                            mmY + z.getCenterY() * scaleY - 2, 4, 4);
            }
        }

        // Items (vert = soin, jaune = ammo)
        for (Item item : new ArrayList<>(data.getItems())) {
            gc.setFill(item.getType() == Item.ItemType.HEALTH
                    ? Color.rgb(50, 255, 80)
                    : Color.rgb(255, 210, 50));
            gc.fillOval(mmX + item.getCenterX() * scaleX - 2,
                        mmY + item.getCenterY() * scaleY - 2, 4, 4);
        }

        // Joueur (grand point bleu vif)
        Player pl = data.getPlayer();
        gc.setFill(Color.rgb(50, 160, 255));
        gc.fillOval(mmX + pl.getCenterX() * scaleX - 4,
                    mmY + pl.getCenterY() * scaleY - 4, 8, 8);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeOval(mmX + pl.getCenterX() * scaleX - 4,
                      mmY + pl.getCenterY() * scaleY - 4, 8, 8);

        // Légende
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        gc.setFill(Color.rgb(50, 160, 255));
        gc.fillText("● Vous", mmX + 2, mmY + mmH + 11);
        gc.setFill(Color.rgb(220, 60, 60));
        gc.fillText("● Zombie", mmX + 40, mmY + mmH + 11);
        gc.setFill(Color.rgb(50, 220, 80));
        gc.fillText("● Soin", mmX + 90, mmY + mmH + 11);
    }
}
