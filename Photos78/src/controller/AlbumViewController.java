package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Album;
import model.Photo;
import model.User;
import model.UserManager;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the album view.
 * Displays photo thumbnails and handles photo management operations.
 *
 * @author Mihail Bogdanoski, Kim Do
 */
public class AlbumViewController {

    @FXML private Label    albumTitleLabel;
    @FXML private TilePane photoTilePane;
    @FXML private Label    statusLabel;

    private UserManager userManager;
    private User        currentUser;
    private Album       currentAlbum;
    private Photo       selectedPhoto;  // currently selected photo

    /**
     * Puts in context into this controller.
     * @param um     the shared UserManager
     * @param user   the logged-in user
     * @param album  the album being viewed
     */
    public void setContext(UserManager um, User user, Album album) {
        this.userManager  = um;
        this.currentUser  = user;
        this.currentAlbum = album;
    }

    /**
     * Refreshes the tile pane with current album photos.
     */
    public void refresh() {
        albumTitleLabel.setText(currentAlbum.getName());
        photoTilePane.getChildren().clear();
        selectedPhoto = null;

        for (Photo photo : currentAlbum.getPhotos()) {
            photoTilePane.getChildren().add(buildThumbnail(photo));
        }
    }

    /**
     * Builds a thumbnail VBox for a given photo.
     * Clicking a thumbnail selects it.
     */
    private VBox buildThumbnail(Photo photo) {
        ImageView iv = new ImageView();
        iv.setFitWidth(110);
        iv.setFitHeight(110);
        iv.setPreserveRatio(true);

        try {
            Image img = new Image(
                new File(photo.getFilePath()).toURI().toString(),
                110, 110, true, true);
            iv.setImage(img);
        } catch (Exception e) {
            // Show placeholder if image fails to load
            iv.setStyle("-fx-background-color: #cccccc;");
        }

        Label caption = new Label(
            photo.getCaption().isEmpty() ? "(no caption)" : photo.getCaption());
        caption.setMaxWidth(110);
        caption.setStyle("-fx-font-size: 11px;");

        VBox box = new VBox(5, iv, caption);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 5; -fx-cursor: hand;");

        // Click to select
        box.setOnMouseClicked(e -> {
            // Reset all borders
            photoTilePane.getChildren().forEach(node ->
                node.setStyle("-fx-padding: 5; -fx-cursor: hand;"));
            // Highlight selected
            box.setStyle("-fx-padding: 5; -fx-cursor: hand; " +
                "-fx-border-color: #0096ff; -fx-border-width: 2;");
            selectedPhoto = photo;
            setSuccess("Selected: " + new File(photo.getFilePath()).getName());
        });

        return box;
    }

    /**
     * Works with Add Photo button.
     * Opens a FileChooser and adds the selected photo to the album.
     */
    @FXML
    private void handleAddPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select a Photo");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));

        Stage stage = (Stage) photoTilePane.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file == null) return;

        Photo photo = new Photo(file.getAbsolutePath());
        boolean added = currentAlbum.addPhoto(photo);
        if (added) {
            refresh();
            setSuccess("Photo added.");
        } else {
            setError("Photo is already in this album.");
        }
    }

    /**
     * Works with Remove Photo button.
     */
    @FXML
    private void handleRemovePhoto() {
        if (selectedPhoto == null) { setError("Select a photo first."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Remove this photo from the album?",
            ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            currentAlbum.removePhoto(selectedPhoto);
            selectedPhoto = null;
            refresh();
            setSuccess("Photo removed.");
        }
    }

    /**
     * Works with Caption Photo button.
     */
    @FXML
    private void handleCaptionPhoto() {
        if (selectedPhoto == null) { setError("Select a photo first."); return; }

        TextInputDialog dialog = new TextInputDialog(selectedPhoto.getCaption());
        dialog.setTitle("Caption Photo");
        dialog.setHeaderText("Enter a caption:");
        dialog.setContentText("Caption:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(caption -> {
            selectedPhoto.setCaption(caption.trim());
            refresh();
            setSuccess("Caption updated.");
        });
    }

    /**
     * Works with Display Photo button — opens the photo detail view.
     */
    @FXML
    private void handleDisplayPhoto() {
        if (selectedPhoto == null) { setError("Select a photo first."); return; }

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/photoDetail.fxml"));
            Stage stage = (Stage) photoTilePane.getScene().getWindow();

            PhotoDetailController controller = new PhotoDetailController();
            controller.setContext(userManager, currentUser, currentAlbum,
                                selectedPhoto, stage);
            loader.setController(controller);

            stage.setScene(new Scene(loader.load(), 700, 500));
            stage.setTitle("Photo Detail");
            controller.refresh();
        } catch (Exception e) {
            setError("Error opening photo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Works with Add Tag — opens photo detail to add tag.
     */
    @FXML
    private void handleAddTag() {
        if (selectedPhoto == null) { setError("Select a photo first."); return; }
        handleDisplayPhoto(); // tags are managed in the detail view
    }

    /**
     * Works with Remove Tag — opens photo detail to remove tag.
     */
    @FXML
    private void handleRemoveTag() {
        if (selectedPhoto == null) { setError("Select a photo first."); return; }
        handleDisplayPhoto();
    }

    /**
     * Works with Copy Photo to another album.
     */
    @FXML
    private void handleCopyPhoto() {
        if (selectedPhoto == null) { setError("Select a photo first."); return; }

        List<Album> otherAlbums = currentUser.getAlbums();
        if (otherAlbums.size() <= 1) {
            setError("No other albums to copy to.");
            return;
        }

        // Build a list of other album names
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        for (Album a : otherAlbums) {
            if (!a.getName().equals(currentAlbum.getName())) {
                dialog.getItems().add(a.getName());
            }
        }
        dialog.setSelectedItem(dialog.getItems().get(0));
        dialog.setTitle("Copy Photo");
        dialog.setHeaderText("Copy photo to which album?");
        dialog.setContentText("Album:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(albumName -> {
            Album target = currentUser.getAlbum(albumName);
            boolean copied = target.addPhoto(selectedPhoto);
            if (copied) setSuccess("Photo copied to \"" + albumName + "\".");
            else setError("Photo already exists in \"" + albumName + "\".");
        });
    }

    /**
     * Works with Move Photo — copies then removes from source album.
     */
    @FXML
    private void handleMovePhoto() {
        if (selectedPhoto == null) { setError("Select a photo first."); return; }

        List<Album> otherAlbums = currentUser.getAlbums();
        if (otherAlbums.size() <= 1) {
            setError("No other albums to move to.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        for (Album a : otherAlbums) {
            if (!a.getName().equals(currentAlbum.getName())) {
                dialog.getItems().add(a.getName());
            }
        }
        dialog.setSelectedItem(dialog.getItems().get(0));
        dialog.setTitle("Move Photo");
        dialog.setHeaderText("Move photo to which album?");
        dialog.setContentText("Album:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(albumName -> {
            Album target = currentUser.getAlbum(albumName);
            boolean copied = target.addPhoto(selectedPhoto);
            if (copied) {
                currentAlbum.removePhoto(selectedPhoto);
                selectedPhoto = null;
                refresh();
                setSuccess("Photo moved to \"" + albumName + "\".");
            } else {
                setError("Photo already exists in \"" + albumName + "\".");
            }
        });
    }

    /**
     * Works with Prev button — selects the previous photo in the album.
     */
    @FXML
    private void handlePrev() {
        List<Photo> photos = currentAlbum.getPhotos();
        if (photos.isEmpty()) { setError("No photos in album."); return; }
        int idx = selectedPhoto == null ? 0 : photos.indexOf(selectedPhoto);
        idx = (idx - 1 + photos.size()) % photos.size();
        selectedPhoto = photos.get(idx);
        highlightPhoto(idx);
    }

    /**
     * Works with Next button — selects the next photo in the album.
     */
    @FXML
    private void handleNext() {
        List<Photo> photos = currentAlbum.getPhotos();
        if (photos.isEmpty()) { setError("No photos in album."); return; }
        int idx = selectedPhoto == null ? 0 : photos.indexOf(selectedPhoto);
        idx = (idx + 1) % photos.size();
        selectedPhoto = photos.get(idx);
        highlightPhoto(idx);
    }

    /**
     * Highlights the thumbnail at the given index.
     */
    private void highlightPhoto(int idx) {
        photoTilePane.getChildren().forEach(node ->
            node.setStyle("-fx-padding: 5; -fx-cursor: hand;"));
        if (idx < photoTilePane.getChildren().size()) {
            photoTilePane.getChildren().get(idx).setStyle(
                "-fx-padding: 5; -fx-cursor: hand; " +
                "-fx-border-color: #0096ff; -fx-border-width: 2;");
        }
        setSuccess("Photo " + (idx + 1) + " of " +
            currentAlbum.getPhotos().size());
    }

    /**
     * Works with Back button — returns to album list.
     */
    @FXML
    private void handleBack() {
        try {
            userManager.save();

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/albumList.fxml"));
            Stage stage = (Stage) photoTilePane.getScene().getWindow();

            AlbumListController controller = new AlbumListController();
            controller.setUserManager(userManager, currentUser);
            loader.setController(controller);

            stage.setScene(new Scene(loader.load(), 700, 500));
            stage.setTitle("Photos – " + currentUser.getUsername());
            controller.refresh();
        } catch (Exception e) {
            setError("Error going back.");
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
