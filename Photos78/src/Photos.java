import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.UserManager;

/**
 * Main entry point for the Photos application.
 * Loads user data and shows the login screen.
 *
 * @author YourName
 */
public class Photos extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Load saved data (or create defaults on first run)
        UserManager userManager = UserManager.load();

        // Load login FXML
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/login.fxml"));

        // Manually set controller so we can pass userManager to it
        LoginController loginController = new LoginController();
        loginController.setUserManager(userManager);
        loader.setController(loginController);

        Scene scene = new Scene(loader.load(), 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Photos");
        primaryStage.setResizable(false);

        // Save on close (safe quit)
        primaryStage.setOnCloseRequest(e -> {
            try { userManager.save(); }
            catch (Exception ex) { ex.printStackTrace(); }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}