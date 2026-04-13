package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Album;
import model.Photo;
import model.User;
import model.UserManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the search view.
 * Searches photos by date range or by tag (AND/OR).
 *
 * @author YourName
 */
public class SearchController {

    @FXML private RadioButton      dateRadio;
    @FXML private RadioButton      tagRadio;
    @FXML private VBox             datePanel;
    @FXML private VBox             tagPanel;

    @FXML private javafx.scene.control.TextField fromDateField;
    @FXML private javafx.scene.control.TextField toDateField;

    @FXML private javafx.scene.control.TextField tag1TypeField;
    @FXML private javafx.scene.control.TextField tag1ValueField;
    @FXML private RadioButton                    andRadio;
    @FXML private RadioButton                    orRadio;
    @FXML private javafx.scene.control.TextField tag2TypeField;
    @FXML private javafx.scene.control.TextField tag2ValueField;

    @FXML private ListView<String> resultsListView;
    @FXML private Label            statusLabel;

    private UserManager userManager;
    private User        currentUser;
    private List<Photo> searchResults = new ArrayList<>();

    private static final SimpleDateFormat SDF =
        new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Sets context for this controller.
     * @param um   the shared UserManager
     * @param user the logged-in user
     */
    public void setContext(UserManager um, User user) {
        this.userManager = um;
        this.currentUser = user;
    }

    /**
     * Sets up toggle groups on load.
     */
    @FXML
    public void initialize() {
        ToggleGroup modeGroup = new ToggleGroup();
        dateRadio.setToggleGroup(modeGroup);
        tagRadio.setToggleGroup(modeGroup);
        dateRadio.setSelected(true);
        tagPanel.setVisible(false);
        tagPanel.setManaged(false);

        ToggleGroup conjGroup = new ToggleGroup();
        andRadio.setToggleGroup(conjGroup);
        orRadio.setToggleGroup(conjGroup);
        andRadio.setSelected(true);
    }

    /**
     * Switches between date and tag search panels.
     */
    @FXML
    private void handleModeSwitch() {
        boolean dateMode = dateRadio.isSelected();
        datePanel.setVisible(dateMode);
        datePanel.setManaged(dateMode);
        tagPanel.setVisible(!dateMode);
        tagPanel.setManaged(!dateMode);
        resultsListView.setItems(FXCollections.observableArrayList());
        searchResults.clear();
        statusLabel.setText("");
    }

    /**
     * Runs the search based on selected mode.
     */
    @FXML
    private void handleSearch() {
        searchResults.clear();

        if (dateRadio.isSelected()) {
            searchByDate();
        } else {
            searchByTag();
        }

        ObservableList<String> display = FXCollections.observableArrayList();
        for (Photo p : searchResults) {
            String name    = new java.io.File(p.getFilePath()).getName();
            String caption = p.getCaption().isEmpty() ? "" : " – " + p.getCaption();
            display.add(name + caption);
        }
        resultsListView.setItems(display);

        if (searchResults.isEmpty()) {
            setError("No photos found.");
        } else {
            setSuccess(searchResults.size() + " photo(s) found.");
        }
    }

    /**
     * Searches all albums for photos within the date range.
     */
    private void searchByDate() {
        String fromStr = fromDateField.getText().trim();
        String toStr   = toDateField.getText().trim();

        if (fromStr.isEmpty() || toStr.isEmpty()) {
            setError("Enter both From and To dates.");
            return;
        }

        try {
            // Accept both M/D/YYYY and MM/DD/YYYY
            SimpleDateFormat lenient = new SimpleDateFormat("M/d/yyyy");
            lenient.setLenient(false);

            Calendar from = Calendar.getInstance();
            Calendar to   = Calendar.getInstance();
            from.setTime(lenient.parse(fromStr));
            to.setTime(lenient.parse(toStr));
            from.set(Calendar.MILLISECOND, 0);
            to.set(Calendar.MILLISECOND, 0);

            // Set to end of day for "to" date so it's inclusive
            to.set(Calendar.HOUR_OF_DAY, 23);
            to.set(Calendar.MINUTE, 59);
            to.set(Calendar.SECOND, 59);

            List<Photo> seen = new ArrayList<>();
            for (Album album : currentUser.getAlbums()) {
                for (Photo photo : album.getPhotos()) {
                    if (seen.contains(photo)) continue;
                    seen.add(photo);
                    Calendar d = photo.getDateTaken();
                    if (!d.before(from) && !d.after(to)) {
                        searchResults.add(photo);
                    }
                }
            }
        } catch (Exception e) {
            setError("Invalid date format. Use MM/DD/YYYY.");
        }
    }

    /**
     * looks at all albums for photos matching tag criteria.
     */
    private void searchByTag() {
        String t1Name  = tag1TypeField.getText().trim().toLowerCase();
        String t1Value = tag1ValueField.getText().trim().toLowerCase();

        if (t1Name.isEmpty() || t1Value.isEmpty()) {
            setError("Enter at least Tag 1 type and value.");
            return;
        }

        String  t2Name     = tag2TypeField.getText().trim().toLowerCase();
        String  t2Value    = tag2ValueField.getText().trim().toLowerCase();
        boolean hasTwoTags = !t2Name.isEmpty() && !t2Value.isEmpty();
        boolean isAnd      = andRadio.isSelected();

        List<Photo> seen = new ArrayList<>();
        for (Album album : currentUser.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                if (seen.contains(photo)) continue;
                seen.add(photo);

                boolean match1 = photo.hasTag(t1Name, t1Value);

                if (!hasTwoTags) {
                    if (match1) searchResults.add(photo);
                } else {
                    boolean match2 = photo.hasTag(t2Name, t2Value);
                    if (isAnd  && match1 && match2) searchResults.add(photo);
                    if (!isAnd && (match1 || match2)) searchResults.add(photo);
                }
            }
        }
    }

    /**
     * makes a new album from the current search results.
     */
    @FXML
    private void handleCreateAlbumFromResults() {
        if (searchResults.isEmpty()) {
            setError("No results to save. Run a search first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Album from Results");
        dialog.setHeaderText("Enter a name for the new album:");
        dialog.setContentText("Album name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            name = name.trim();
            if (name.isEmpty()) { setError("Name cannot be empty."); return; }
            if (currentUser.hasAlbum(name)) {
                setError("Album \"" + name + "\" already exists.");
                return;
            }
            Album newAlbum = new Album(name);
            for (Photo p : searchResults) newAlbum.addPhoto(p);
            currentUser.addAlbum(newAlbum);
            setSuccess("Album \"" + name + "\" created with " +
                searchResults.size() + " photo(s).");
        });
    }

    /**
     * Returns to the album list view.
     */
    @FXML
    private void handleBack() {
        try {
            userManager.save();
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/albumList.fxml"));
            Stage stage = (Stage) resultsListView.getScene().getWindow();

            AlbumListController controller = new AlbumListController();
            controller.setUserManager(userManager, currentUser);
            loader.setController(controller);

            stage.setScene(new Scene(loader.load(), 700, 500));
            stage.setTitle("Photos – " + currentUser.getUsername());
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
