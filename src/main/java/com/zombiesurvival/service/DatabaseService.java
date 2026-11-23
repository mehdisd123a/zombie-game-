package com.zombiesurvival.service;

import com.zombiesurvival.model.LeaderboardEntry;
import com.zombiesurvival.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gère toutes les opérations d'accès à la base de données SQLite.
 * Utilise des PreparedStatement pour éviter les injections SQL.
 *
 * @author Elmehdi Eddareiy
 */
public class DatabaseService {

    // Chemin vers le fichier SQLite (créé dans le répertoire de travail)
    private static final String DB_URL = "jdbc:sqlite:zombie_survival.db";

    private static DatabaseService instance;

    private DatabaseService() {
        // Chargement explicite du driver SQLite
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite non trouvé : " + e.getMessage());
        }
        initDatabase();
    }

    /**
     * Singleton : retourne l'instance unique du service.
     */
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * Ouvre et retourne une connexion SQLite.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ─── Initialisation ───────────────────────────────────────────────────────

    /**
     * Crée les tables si elles n'existent pas encore.
     */
    public void initDatabase() {
        String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    username     TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    created_at   TEXT DEFAULT (datetime('now'))
                )""";

        String createGames = """
                CREATE TABLE IF NOT EXISTS games (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id        INTEGER,
                    score          INTEGER DEFAULT 0,
                    wave_reached   INTEGER DEFAULT 0,
                    zombies_killed INTEGER DEFAULT 0,
                    duration       INTEGER DEFAULT 0,
                    played_at      TEXT DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )""";

        String createStats = """
                CREATE TABLE IF NOT EXISTS user_statistics (
                    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id              INTEGER UNIQUE,
                    total_games          INTEGER DEFAULT 0,
                    total_zombies_killed INTEGER DEFAULT 0,
                    best_score           INTEGER DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )""";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createGames);
            stmt.execute(createStats);
            System.out.println("Base de données initialisée.");
        } catch (SQLException e) {
            System.err.println("Erreur init DB : " + e.getMessage());
        }
    }

    // ─── Utilisateurs ─────────────────────────────────────────────────────────

    /**
     * Crée un nouveau compte utilisateur.
     * @return true si la création a réussi, false si le nom existe déjà
     */
    public boolean createUser(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();

            // Créer les statistiques vides pour ce nouvel utilisateur
            int userId = getUserByUsername(username)
                    .map(User::getId).orElse(-1);
            if (userId != -1) {
                initUserStatistics(conn, userId);
            }
            return true;
        } catch (SQLException e) {
            // UNIQUE constraint failed → nom d'utilisateur déjà pris
            System.err.println("createUser erreur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Initialise une ligne de statistiques pour un nouvel utilisateur.
     */
    private void initUserStatistics(Connection conn, int userId) throws SQLException {
        String sql = "INSERT OR IGNORE INTO user_statistics (user_id) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Recherche un utilisateur par son nom.
     */
    public Optional<User> getUserByUsername(String username) {
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("getUserByUsername erreur : " + e.getMessage());
        }
        return Optional.empty();
    }

    // ─── Parties ──────────────────────────────────────────────────────────────

    /**
     * Enregistre les résultats d'une partie terminée.
     */
    public void saveGame(int userId, int score, int waveReached,
                         int zombiesKilled, long durationSeconds) {
        String sql = """
                INSERT INTO games (user_id, score, wave_reached, zombies_killed, duration)
                VALUES (?, ?, ?, ?, ?)""";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, score);
            ps.setInt(3, waveReached);
            ps.setInt(4, zombiesKilled);
            ps.setLong(5, durationSeconds);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("saveGame erreur : " + e.getMessage());
        }
    }

    // ─── Statistiques ─────────────────────────────────────────────────────────

    /**
     * Met à jour les statistiques globales de l'utilisateur.
     */
    public void updateStatistics(int userId, int score, int zombiesKilled) {
        // S'assurer que la ligne existe
        String insertIgnore = "INSERT OR IGNORE INTO user_statistics (user_id) VALUES (?)";
        String update = """
                UPDATE user_statistics
                SET total_games           = total_games + 1,
                    total_zombies_killed  = total_zombies_killed + ?,
                    best_score            = MAX(best_score, ?)
                WHERE user_id = ?""";
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(insertIgnore)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setInt(1, zombiesKilled);
                ps.setInt(2, score);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("updateStatistics erreur : " + e.getMessage());
        }
    }

    // ─── Classement ───────────────────────────────────────────────────────────

    /**
     * Retourne les N meilleurs scores.
     */
    public List<LeaderboardEntry> getLeaderboard(int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = """
                SELECT u.username,
                       s.best_score,
                       s.total_games,
                       s.total_zombies_killed
                FROM user_statistics s
                JOIN users u ON u.id = s.user_id
                ORDER BY s.best_score DESC
                LIMIT ?""";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entries.add(new LeaderboardEntry(
                        rs.getString("username"),
                        rs.getInt("best_score"),
                        rs.getInt("total_games"),
                        rs.getInt("total_zombies_killed")
                ));
            }
        } catch (SQLException e) {
            System.err.println("getLeaderboard erreur : " + e.getMessage());
        }
        return entries;
    }

    /**
     * Retourne le meilleur score d'un utilisateur.
     */
    public int getBestScore(int userId) {
        String sql = "SELECT best_score FROM user_statistics WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("best_score");
        } catch (SQLException e) {
            System.err.println("getBestScore erreur : " + e.getMessage());
        }
        return 0;
    }
}
