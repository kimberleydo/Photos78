package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import model.UserManager;

/**
 * Controller for the login
 * Handles user authentication and routing
 *
 * @author Mihail Bogdanoski, Kim Do
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private Label     errorLabel;

    // Shared UserManager across everything
    private UserManager userManager;

    /**
     * Called by Photos.java to UserManager.
     * @param userManager the UserManager instance
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Log In button click.
     * Routes to admin
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username.");
            return;
        }

        if (username.equals("admin")) {
            loadAdminView();
        } else {
            User user = userManager.getUser(username);
            if (user == null) {
                errorLabel.setText("User \"" + username + "\" not found.");
            } else {
                loadAlbumListView(user);
            }
        }
    }

    /**
     * Loads the admin view.
     */
    private void loadAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/admin.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();

            AdminController controller = new AdminController();
            controller.setUserManager(userManager);
            loader.setController(controller);

            stage.setScene(new Scene(loader.load(), 600, 400));
            stage.setTitle("Admin Panel");
        } catch (Exception e) {
            errorLabel.setText("Error loading admin view.");
            e.printStackTrace();
        }
    }

    /**
     * Loads the album list view for user.
     * @param user the logged-in user
     */
    private void loadAlbumListView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/albumList.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();

            AlbumListController controller = new AlbumListController();
            controller.setUserManager(userManager, user);
            loader.setController(controller);

            stage.setScene(new Scene(loader.load(), 700, 500));
            stage.setTitle("Photos – " + user.getUsername());
        } catch (Exception e) {
            errorLabel.setText("Error loading user view.");
            e.printStackTrace();
        }
    }
}