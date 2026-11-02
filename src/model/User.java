package model;

public class User {
    private final String fullName;
    private final String username;
    // Kept plain text for this simple example; a real application should hash passwords.
    private final String password;

    // Creates a user with the supplied login details.
    public User(String fullName, String username, String password) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
    }

    // Returns the user's full name.
    public String getFullName() {
        return fullName;
    }

    // Returns the user's username.
    public String getUsername() {
        return username;
    }

    // Returns the user's password.
    public String getPassword() {
        return password;
    }
}
