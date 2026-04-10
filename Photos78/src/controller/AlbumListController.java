package controller;

import javafx.fxml.FXML;
import model.User;
import model.UserManager;

/**
 * Controller for the album list view.
 * Displays all albums belonging to the logged-in user.
 *
 * @author YourName
 */
public class AlbumListController {

    private UserManager userManager;
    private User        currentUser;

    /**
     * Injects the UserManager and current user into this controller.
     * @param userManager the shared UserManager instance
     * @param user        the currently logged-in user
     */
    public void setUserManager(UserManager userManager, User user) {
        this.userManager = userManager;
        this.currentUser = user;
    }
}