package com.zombiesurvival.controller;

import com.zombiesurvival.model.User;
import com.zombiesurvival.service.DatabaseService;
import com.zombiesurvival.service.Session;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Gère la logique d'authentification : connexion et création de compte.
 * Hash les mots de passe avec SHA-256 + sel fixe.
 *
 * @author Elmehdi Eddareiy
 */
public class LoginController {

    private static final String SALT = "ZombieSurvival_SALT_2026";

    private final DatabaseService db      = DatabaseService.getInstance();
    private final Session         session = Session.getInstance();

    public enum LoginResult {
        SUCCESS,
        INVALID_CREDENTIALS,
        USER_NOT_FOUND,
        USERNAME_TAKEN,
        INVALID_INPUT,
        ERROR
    }

    /**
     * Tente de connecter un utilisateur.
     *
     * @param username nom d'utilisateur
     * @param password mot de passe en clair
     * @return résultat de la tentative
     */
    public LoginResult login(String username, String password) {
        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            return LoginResult.INVALID_INPUT;
        }

        Optional<User> userOpt = db.getUserByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return LoginResult.USER_NOT_FOUND;
        }

        User user = userOpt.get();
        String hashInput = hashPassword(password);

        if (hashInput.equals(user.getPasswordHash())) {
            session.login(user);
            return LoginResult.SUCCESS;
        } else {
            return LoginResult.INVALID_CREDENTIALS;
        }
    }

    /**
     * Crée un nouveau compte utilisateur.
     *
     * @param username nom d'utilisateur souhaité
     * @param password mot de passe en clair
     * @return résultat de la tentative
     */
    public LoginResult register(String username, String password) {
        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            return LoginResult.INVALID_INPUT;
        }

        String trimmedUsername = username.trim();

        // Validation : 3-20 caractères, alphanumérique + underscore
        if (!trimmedUsername.matches("[a-zA-Z0-9_]{3,20}")) {
            return LoginResult.INVALID_INPUT;
        }

        // Vérifier si le nom est déjà pris
        if (db.getUserByUsername(trimmedUsername).isPresent()) {
            return LoginResult.USERNAME_TAKEN;
        }

        String passwordHash = hashPassword(password);
        boolean created = db.createUser(trimmedUsername, passwordHash);

        if (created) {
            // Auto-login après inscription
            return login(trimmedUsername, password);
        }
        return LoginResult.ERROR;
    }

    /**
     * Déconnecte l'utilisateur courant.
     */
    public void logout() {
        session.logout();
    }

    /**
     * Hash un mot de passe avec SHA-256 + sel.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String salted    = SALT + password;
            byte[] hashBytes = md.digest(salted.getBytes(StandardCharsets.UTF_8));
            // Convertir en chaîne hexadécimale
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 non disponible", e);
        }
    }

    /**
     * Message d'erreur lisible selon le résultat.
     */
    public static String getErrorMessage(LoginResult result) {
        return switch (result) {
            case SUCCESS             -> "Connexion réussie !";
            case INVALID_CREDENTIALS -> "Mot de passe incorrect.";
            case USER_NOT_FOUND      -> "Nom d'utilisateur introuvable.";
            case USERNAME_TAKEN      -> "Ce nom d'utilisateur est déjà pris.";
            case INVALID_INPUT       -> "Entrée invalide. Le nom doit faire 3-20 caractères (lettres, chiffres, _).";
            case ERROR               -> "Une erreur est survenue. Réessayez.";
        };
    }
}
