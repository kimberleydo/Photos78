package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * shows a user of the Photos application.
 * Each user has a unique username
 * 
 * @author Mihail Bogdanoski, Kim Do
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String      username;
    private List<Album> albums;

    /**
     * makes a new user with the given username
     * @param username the unique username
     */
    public User(String username) {
        this.username = username;
        this.albums   = new ArrayList<>();
    }

    public String      getUsername() { return username; }
    public List<Album> getAlbums()   { return albums; }

    /**
     * Adds an album to their collection.
     * @param album the album to add
     */
    public void addAlbum(Album album) {
        albums.add(album);
    }

    /**
     * Removes an album by name.
     * @param name the album name to remove
     * @return true if removed, false if not found
     */
    public boolean removeAlbum(String name) {
        return albums.removeIf(a -> a.getName().equals(name));
    }

    /**
     * Finds an album by name.
     * @param name the album name to search 
     * @return the Album if found, null otherwise
     */
    public Album getAlbum(String name) {
        for (Album a : albums) {
            if (a.getName().equals(name)) return a;
        }
        return null;
    }

    /**
     * Checks if the user already has an album
     * @param name album name to check
     */
    public boolean hasAlbum(String name) {
        return getAlbum(name) != null;
    }

    @Override
    public String toString() {
        return username;
    }
}