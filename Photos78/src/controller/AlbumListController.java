package controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Album;
import model.User;
import model.UserManager;

import java.text.SimpleDateFormat;
import java.util.Optional;

/**
 * Controller for the album list view.
 * Displays all albums for the logged-in user and
 * allows create, rename, delete, and open operations.
 *
 * @author Mihail Bogdanoski, Kim Do
 */
public class AlbumListController {

    @FXML private Label                  welcomeLabel;
    @FXML private TableView<Album>       albumTable;
    @FXML private TableColumn<Album, String>  nameCol;
    @FXML private TableColumn<Album, Integer> countCol;
    @FXML private TableColumn<Album, String>  dateRangeCol;
    @FXML private Label                  statusLabel;

    private UserManager            userManager;
    private User                   currentUser;
    private ObservableList<Album>  albumData;

    private static final SimpleDateFormat SDF =
        new SimpleDateFormat("MMM dd, yyyy");

    /**
     * Injects the UserManager and current user.
     * @param userManager the shared UserManager
     * @param user        the logged-in user
     */
    public void setUserManager(UserManager userManager, User user) {
        this.userManager = userManager;
        this.currentUser = user;
    }

    /**
     * Called automatically after FXML fields are injected.
     * Sets up table columns.
     */
    @FXML
    public void initialize() {
        albumData = FXCollections.observableArrayList();
        albumTable.setItems(albumData);

        // Name column
        nameCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getName()));

        // Photo count column
        countCol.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getSize()).asObject());

        // Date range column
        dateRangeCol.setCellValueFactory(data -> {
            Album a = data.getValue();
            if (a.getSize() == 0) {
                return new SimpleStringProperty("No photos");
            }
            String earliest = SDF.format(a.getEarliestDate().getTime());
            String latest   = SDF.format(a.getLatestDate().getTime());
            return new SimpleStringProperty(earliest + " – " + latest);
        });
    }

    /**
     * Refreshes the table and welcome label.
     * Must be called after setUserManager().
     */
    public void refresh() {
        welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        albumData.clear();
        albumData.addAll(currentUser.getAlbums());
    }

    /**
     * Works with Create Album button.
     */
    @FXML
    private void handleCreateAlbum() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Album");
        dialog.setHeaderText("Enter a name for the new album:");
        dialog.setContentText("Album name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            name = name.trim();
            if (name.isEmpty()) {
                setError("Album name cannot be empty.");
                return;
            }
            if (currentUser.hasAlbum(name)) {
                setError("Album \"" + name + "\" already exists.");
                return;
            }
            currentUser.addAlbum(new Album(name));
            refresh();
            setSuccess("Album \"" + name + "\" created.");
        });
    }

    /**
     * Works with Rename Album button.
     */
    @FXML
    private void handleRenameAlbum() {
        Album selected = albumTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setError("Select an album to rename."); return; }

        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Rename Album");
        dialog.setHeaderText("Enter a new name for \"" + selected.getName() + "\":");
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            name = name.trim();
            if (name.isEmpty()) { setError("Name cannot be empty."); return; }
            if (currentUser.hasAlbum(name)) {
                setError("Album \"" + name + "\" already exists.");
                return;
            }
            selected.setName(name);
            refresh();
            setSuccess("Album renamed to \"" + name + "\".");
        });
    }

    /**
     * Works with Delete Album button.
     */
    @FXML
    private void handleDeleteAlbum() {
        Album selected = albumTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setError("Select an album to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete album \"" + selected.getName() + "\"?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            currentUser.removeAlbum(selected.getName());
            refresh();
            setSuccess("Album deleted.");
        }
    }

    /**
     * Works with Open Album button — loads the album view.
     */
@FXML
private void handleOpenAlbum() {
    Album selected = albumTable.getSelectionModel().getSelectedItem();
    if (selected == null) { setError("Select an album to open."); return; }

    try {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/albumView.fxml"));
        Stage stage = (Stage) albumTable.getScene().getWindow();

        AlbumViewController controller = new AlbumViewController();
        controller.setContext(userManager, currentUser, selected);
        loader.setController(controller);

        stage.setScene(new Scene(loader.load(), 750, 550));
        stage.setTitle("Album – " + selected.getName());
        controller.refresh();
    } catch (Exception e) {
        setError("Error opening album: " + e.getMessage());
        e.printStackTrace();
    }
}

    /**
     * Works with Search button — loads the search view.
     */
    @FXML
    private void handleSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/search.fxml"));
            Stage stage = (Stage) albumTable.getScene().getWindow();

            SearchController controller = new SearchController();
            controller.setContext(userManager, currentUser);
            loader.setController(controller);

            stage.setScene(new Scene(loader.load(), 700, 500));
            stage.setTitle("Search Photos");
        } catch (Exception e) {
            setError("Error opening search.");
            e.printStackTrace();
        }
    }

    /**
     * Works with Logout — saves data and returns to login screen.
     */
    @FXML
    private void handleLogout() {
        try {
            userManager.save();

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) albumTable.getScene().getWindow();

            LoginController loginController = new LoginController();
            loginController.setUserManager(userManager);
            loader.setController(loginController);

            stage.setScene(new Scene(loader.load(), 400, 300));
            stage.setTitle("Photos");
        } catch (Exception e) {
            setError("Error logging out.");
            e.printStackTrace();
        }
    }

    // Helpers
    private void setError(String msg) {
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText(msg);
    }

    private void setSuccess(String msg) {
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText(msg);
    }
}
