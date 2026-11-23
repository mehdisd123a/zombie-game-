package com.zombiesurvival.service;

import com.zombiesurvival.model.User;

/**
 * Singleton qui maintient l'état de la session courante.
 * Contient l'utilisateur connecté (ou null si mode guest).
 *
 * @author Elmehdi Eddareiy
 */
public class Session {

    private static Session instance;

    private User    currentUser = null;  // null = mode guest
    private boolean loggedIn    = false;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /**
     * Connecte un utilisateur.
     */
    public void login(User user) {
        this.currentUser = user;
        this.loggedIn    = true;
        System.out.println("Connecté en tant que : " + user.getUsername());
    }

    /**
     * Déconnecte l'utilisateur courant.
     */
    public void logout() {
        this.currentUser = null;
        this.loggedIn    = false;
        System.out.println("Déconnecté.");
    }

    public boolean isLoggedIn()      { return loggedIn; }
    public User    getCurrentUser()  { return currentUser; }

    /**
     * @return le nom à afficher (pseudo si connecté, "Guest" sinon)
     */
    public String getDisplayName() {
        return loggedIn ? currentUser.getUsername() : "Guest";
    }
}
