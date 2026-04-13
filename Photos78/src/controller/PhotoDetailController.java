package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Album;
import model.Photo;
import model.Tag;
import model.User;
import model.UserManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the photo detail view.
 * Shows full image, caption, date, and tags.
 *
 *
 */
public class PhotoDetailController {

    @FXML private ImageView        photoImageView;
    @FXML private Label            captionLabel;
    @FXML private Label            dateLabel;
    @FXML private ListView<String> tagListView;
    @FXML private Label            statusLabel;

    private UserManager userManager;
    private User        currentUser;
    private Album       currentAlbum;
    private Photo       currentPhoto;
    private Stage       stage;

    private static List<String> tagTypes =
        new ArrayList<>(List.of("person", "location"));

    private static final SimpleDateFormat SDF =
        new SimpleDateFormat("MMM dd, yyyy  hh:mm a");

    /**
     * Sets context for this controller.
     * @param um    the shared UserManager
     * @param user  the logged-in user
     * @param album the current album
     * @param photo the photo to display
     * @param stage the current stage
     */
    public void setContext(UserManager um, User user, Album album,
                           Photo photo, Stage stage) {
        this.userManager  = um;
        this.currentUser  = user;
        this.currentAlbum = album;
        this.currentPhoto = photo;
        this.stage        = stage;
    }

    /**
     * Refreshes the view with current photo data.
     */
    public void refresh() {
        try {
            Image img = new Image(
                new File(currentPhoto.getFilePath()).toURI().toString(),
                420, 360, true, true);
            photoImageView.setImage(img);
        } catch (Exception e) {
            photoImageView.setImage(null);
        }

        String cap = currentPhoto.getCaption();
        captionLabel.setText(cap.isEmpty() ? "(no caption)" : cap);
        dateLabel.setText("Taken: " +
            SDF.format(currentPhoto.getDateTaken().getTime()));

        ObservableList<String> tagStrings = FXCollections.observableArrayList();
        for (Tag t : currentPhoto.getTags()) {
            tagStrings.add(t.getName() + " = " + t.getValue());
        }
        tagListView.setItems(tagStrings);
        statusLabel.setText("");
    }

    /** Handles Add Tag button. */
    @FXML
    private void handleAddTag() {
        List<String> choices = new ArrayList<>(tagTypes);
        choices.add("+ Add new tag type...");

        ChoiceDialog<String> typeDialog =
            new ChoiceDialog<>(choices.get(0), choices);
        typeDialog.setTitle("Add Tag");
        typeDialog.setHeaderText("Select tag type:");

        Optional<String> typeResult = typeDialog.showAndWait();
        if (typeResult.isEmpty()) return;

        String tagType = typeResult.get();
        if (tagType.equals("+ Add new tag type...")) {
            TextInputDialog d = new TextInputDialog();
            d.setTitle("New Tag Type");
            d.setHeaderText("Enter new tag type name:");
            Optional<String> newType = d.showAndWait();
            if (newType.isEmpty() || newType.get().trim().isEmpty()) return;
            tagType = newType.get().trim().toLowerCase();
            if (!tagTypes.contains(tagType)) tagTypes.add(tagType);
        }

        TextInputDialog valueDialog = new TextInputDialog();
        valueDialog.setTitle("Add Tag");
        valueDialog.setHeaderText("Value for \"" + tagType + "\":");
        Optional<String> valueResult = valueDialog.showAndWait();
        if (valueResult.isEmpty() || valueResult.get().trim().isEmpty()) return;

        Tag tag = new Tag(tagType, valueResult.get().trim());
        if (currentPhoto.addTag(tag)) {
            refresh();
            setSuccess("Tag added.");
        } else {
            setError("Tag already exists.");
        }
    }

    /** Handles Delete Tag button. */
    @FXML
    private void handleDeleteTag() {
        int idx = tagListView.getSelectionModel().getSelectedIndex();
        if (idx < 0) { setError("Select a tag first."); return; }

        Tag tag = currentPhoto.getTags().get(idx);
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete tag \"" + tag + "\"?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            currentPhoto.removeTag(tag);
            refresh();
            setSuccess("Tag deleted.");
        }
    }

    /** Shows previous photo. */
    @FXML
    private void handlePrev() {
        List<Photo> photos = currentAlbum.getPhotos();
        int idx = photos.indexOf(currentPhoto);
        currentPhoto = photos.get((idx - 1 + photos.size()) % photos.size());
        refresh();
    }

    /** Shows next photo. */
    @FXML
    private void handleNext() {
        List<Photo> photos = currentAlbum.getPhotos();
        int idx = photos.indexOf(currentPhoto);
        currentPhoto = photos.get((idx + 1) % photos.size());
        refresh();
    }

    /** Returns to album view. */
    @FXML
    private void handleBack() {
        try {
            userManager.save();
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/albumView.fxml"));

            AlbumViewController controller = new AlbumViewController();
            controller.setContext(userManager, currentUser, currentAlbum);
            loader.setController(controller);

            stage.setScene(new Scene(loader.load(), 750, 550));
            stage.setTitle("Album – " + currentAlbum.getName());
            controller.refresh();
        } catch (Exception e) {
            setError("Error going back: " + e.getMessage());
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