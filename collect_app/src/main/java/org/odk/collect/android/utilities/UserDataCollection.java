package org.odk.collect.android.utilities;

/**
 * Created by sabbir on 10/27/16.
 */


public class UserDataCollection {
    private String username = "";
    private String password = "";

    public void resetAll() {
        username = "";
        password = "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}