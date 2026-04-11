package controller;

import model.User;
import model.UserManager;

public class SearchController {
    private UserManager userManager;
    private User currentUser;

    public void setContext(UserManager um, User u) {
        this.userManager = um;
        this.currentUser = u;
    }
}