-- ============================================================
-- Zombie Survival — Script de création de la base de données
-- Base : SQLite (compatible MySQL avec légères adaptations)
-- Auteur : Elmehdi Eddareiy
-- Date   : Novembre 2026
-- ============================================================

-- Supprimer les tables existantes (pour reset complet)
DROP TABLE IF EXISTS user_statistics;
DROP TABLE IF EXISTS games;
DROP TABLE IF EXISTS users;

-- ── Table des utilisateurs ────────────────────────────────────────────────────
CREATE TABLE users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT    UNIQUE NOT NULL,
    password_hash TEXT    NOT NULL,                    -- SHA-256 + sel
    created_at    TEXT    DEFAULT (datetime('now'))
);

-- ── Table des parties jouées ──────────────────────────────────────────────────
CREATE TABLE games (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER NOT NULL,
    score          INTEGER DEFAULT 0,
    wave_reached   INTEGER DEFAULT 0,
    zombies_killed INTEGER DEFAULT 0,
    duration       INTEGER DEFAULT 0,                 -- en secondes
    played_at      TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ── Table des statistiques globales ──────────────────────────────────────────
CREATE TABLE user_statistics (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id              INTEGER UNIQUE NOT NULL,
    total_games          INTEGER DEFAULT 0,
    total_zombies_killed INTEGER DEFAULT 0,
    best_score           INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ── Index pour optimiser les requêtes ────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_games_user_id    ON games(user_id);
CREATE INDEX IF NOT EXISTS idx_stats_best_score ON user_statistics(best_score DESC);

-- ── Données de test (optionnel) ───────────────────────────────────────────────
-- INSERT INTO users (username, password_hash) VALUES ('elmehdi', '<hash>');
-- INSERT INTO user_statistics (user_id) VALUES (1);
