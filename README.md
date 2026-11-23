# Zombie Survival

Jeu vidéo 2D action/survie en **Java 21 + JavaFX + SQLite**.

> Projet de groupe — EMSI 2026  
> **Elmehdi Eddareiy** (développeur) · **Omar Samma** (rapport) · **Erami Amin** (présentation)

---

## Lancer le jeu

```bash
mvn javafx:run
```

---

## Commandes

| Touche | Action |
|--------|--------|
| Z / W  | Avancer |
| S      | Reculer |
| Q / A  | Gauche |
| D      | Droite |
| Souris | Viser |
| Clic gauche | Tirer |
| R      | Recharger |
| ESC    | Pause |
| 1      | Pistol |
| 2      | Rifle (vague 3) |
| 3      | Shotgun (vague 6) |
| Molette | Changer d'arme |
| H      | Acheter soin (boutique) |
| M      | Acheter munitions (boutique) |

---

## Fonctionnalités v2

- **3 armes** : Pistol / Rifle / Shotgun avec stats différentes
- **Boutique inter-vague** : acheter armes, soins et munitions
- **Système XP/Niveaux** : gagnez des niveaux et améliorez vos stats
- **Obstacles** : caisses, murs et barils sur la carte
- **Particules** : effets visuels à chaque mort et impact
- **Mini-map** : aperçu de la position des zombies
- **Écran "Comment jouer"** : guide complet avec commandes, armes, ennemis
- **Design amélioré** : menus, pause et HUD redessinés

---

## Vagues

| Vague | Contenu |
|-------|---------|
| 1 | 5 zombies lents |
| 3 | Rifle débloqué |
| 5 | Zombies spéciaux (20%) |
| 6 | Shotgun débloqué |
| 10 | BOSS final |

---

## Score

- Zombie tué : +10 pts
- Zombie spécial : +30 pts
- Boss : +500 pts
- Vague complétée : +100 × numéro de vague

---

## Architecture

```
src/main/java/com/zombiesurvival/
├── Main.java
├── model/     Player, Zombie, ZombieSpecial, Boss, Bullet, Weapon, Item, GameData, Obstacle, Particle, User, LeaderboardEntry
├── engine/    GameLoop, InputHandler, CollisionManager, WaveManager
├── controller/ GameController, MenuController, LoginController
├── service/   DatabaseService, ScoreService, Session
└── view/      GameView, MenuView, PauseView, GameOverView, LoginView, LeaderboardView, HowToPlayView
```
