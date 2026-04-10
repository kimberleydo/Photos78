package controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import model.UserManager;

/**
 * Controller for the admin subsystem.
 * Allows listing, creating, and deleting users.
 *
 * @author YourName
 */
public class AdminController {

    private UserManager userManager;

    /**
     * Injects the UserManager into this controller.
     * @param userManager the shared UserManager instance
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
}