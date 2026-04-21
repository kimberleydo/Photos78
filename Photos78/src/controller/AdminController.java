package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import model.User;
import model.UserManager;

import java.util.Optional;

/**
 * Used for the admin panel.
 * Allows listing, creating, and deleting users.
 * Admin cannot manage albums.
 *
 * @author Mihail Bogdanoski, Kim Do
 */
public class AdminController {

    @FXML private ListView<String> userListView;
    @FXML private Label            statusLabel;

    private UserManager userManager;

    /**
     * puts in the UserManager.
     * @param um the shared UserManager
     */
    public void setUserManager(UserManager um) {
        this.userManager = um;
    }

    /**
     * Refreshes the user list display.
     */
    public void refresh() {
        ObservableList<String> names = FXCollections.observableArrayList();
        for (User u : userManager.getUsers()) {
            names.add(u.getUsername());
        }
        userListView.setItems(names);
        statusLabel.setText("");
    }

    /**
     * works with Create User button.
     * Prompts for a username and creates a new user if valid.
     */
    @FXML
    private void handleCreateUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create User");
        dialog.setHeaderText("Enter a username for the new user:");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            username = username.trim();

            if (username.isEmpty()) {
                setError("Username cannot be empty.");
                return;
            }
            if (username.equals("admin")) {
                setError("Cannot create a user named 'admin'.");
                return;
            }
            if (userManager.getUser(username) != null) {
                setError("User \"" + username + "\" already exists.");
                return;
            }

            userManager.addUser(username);
            refresh();
            setSuccess("User \"" + username + "\" created.");
        });
    }

    /**
     * works with Delete User button.
     * Removes the selected user after confirmation.
     */
    @FXML
    private void handleDeleteUser() {
        String selected = userListView.getSelectionModel().getSelectedItem();
        if (selected == null) { setError("Select a user to delete."); return; }

        if (selected.equals("stock")) {
            setError("Cannot delete the stock user.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete user \"" + selected + "\" and all their albums?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            userManager.deleteUser(selected);
            refresh();
            setSuccess("User \"" + selected + "\" deleted.");
        }
    }

    /**
     * works with Logout — saves data and returns to login screen.
     */
    @FXML
    private void handleLogout() {
        try {
            userManager.save();

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) userListView.getScene().getWindow();

            LoginController loginController = new LoginController();
            loginController.setUserManager(userManager);
            loader.setController(loginController);

            stage.setScene(new Scene(loader.load(), 400, 300));
            stage.setTitle("Photos");
        } catch (Exception e) {
            setError("Error logging out: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setError(String msg) {
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText(msg);
    }

    private void setSuccess(String msg) {
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText(msg);
    }
}
