package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all users for the Photos application
 * Handles loading from and saving to disk
 * 
 * @author Mihail Bogdanoski, Kim Do
 */
public class UserManager implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "data/users.dat";

    private List<User> users;

    /**
     * Constructs a UserManager with empty user list.
     */
    public UserManager() {
        this.users = new ArrayList<>();
    }

    public List<User> getUsers() { return users; }

    /**
     * Finds a user by username.
     * @param username the username to search
     * @return User if found, null otherwise
     */
    public User getUser(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) return u;
        }
        return null;
    }

    /**
     * Adds a new user if username is not taken.
     * @param username the new username
     * @return true if added, false if duplicate
     */
    public boolean addUser(String username) {
        if (getUser(username) != null) return false;
        users.add(new User(username));
        return true;
    }

    /**
     * Deletes a user by username.
     * @param username the username to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteUser(String username) {
        return users.removeIf(u -> u.getUsername().equals(username));
    }

    /**
     * Saves all users to disk 
     * @throws IOException if saving fails
     */
    public void save() throws IOException {
        // Make sure the data directory exists
        new File("data").mkdirs();
        ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(DATA_FILE));
        oos.writeObject(users);
        oos.close();
    }

    /**
     * Loads all users from disk
     * If there is no save file exists, returns to a fresh UserManager
     * @return the loaded (or new) UserManager
     */
    @SuppressWarnings("unchecked")
    public static UserManager load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(DATA_FILE));
            List<User> users = (List<User>) ois.readObject();
            ois.close();
            UserManager um = new UserManager();
            um.users = users;
            return um;
        } catch (Exception e) {
            return createDefault();
        }
    }

    /**
     * Creates a default UserManager
     * Called on first run when no save file exists.
     */
    private static UserManager createDefault() {
        UserManager um = new UserManager();
        User stock = new User("stock");
        Album stockAlbum = new Album("stock");

        // Load stock photos 
        File dataDir = new File("data");
        if (dataDir.exists()) {
            for (File f : dataDir.listFiles()) {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".jpg") || name.endsWith(".jpeg")
                 || name.endsWith(".png") || name.endsWith(".gif")
                 || name.endsWith(".bmp")) {
                    stockAlbum.addPhoto(new Photo(f.getAbsolutePath()));
                }
            }
        }

        stock.addAlbum(stockAlbum);
        um.users.add(stock);
        return um;
    }
}