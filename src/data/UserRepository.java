package data;

import model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

class UserRepository {
    private final ArrayList<User> users = new ArrayList<>();
    private final File usersFile = new File("users.txt");

    UserRepository() throws IOException {
        loadUsers();
    }

    boolean usernameExists(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    User checkLogin(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)
                    && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    void addUser(String fullName, String username, String password) {
        users.add(new User(fullName, username, password));
    }

    private void loadUsers() throws IOException {
        if (!usersFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                users.add(new User(parts[0], parts[1], parts[2]));
            }
        }
    }

    void saveUsers() throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(usersFile)))) {
            for (User user : users) {
                writer.println(user.getFullName() + "|" + user.getUsername() + "|"
                        + user.getPassword());
            }
        }
    }
}
