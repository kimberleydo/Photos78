package controller;

import model.Album;
import model.User;
import model.UserManager;

public class AlbumViewController {
    private UserManager userManager;
    private User currentUser;
    private Album currentAlbum;

    public void setContext(UserManager um, User u, Album a) {
        this.userManager  = um;
        this.currentUser  = u;
        this.currentAlbum = a;
    }

    public void refresh() {}
}