package ui;

import data.DataStore;
import model.User;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class LoginWindow extends JFrame {
    private final DataStore dataStore;

    // Creates the login and signup window.
    public LoginWindow(DataStore dataStore) {
        this.dataStore = dataStore;
        setTitle("Accounting System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(430, 330);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("Sign Up", createSignupPanel());
        add(tabs);
    }

    // Builds the login form and its password-visibility control.
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox showPassword = new JCheckBox("Show password");
        JLabel errorLabel = new JLabel(" ");
        JButton loginButton = new JButton("Login");

        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(showPassword);
        form.add(loginButton);
        form.add(new JLabel(""));
        form.add(new JLabel(""));
        panel.add(form, BorderLayout.CENTER);
        panel.add(errorLabel, BorderLayout.SOUTH);

        showPassword.addActionListener(event -> {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
        });
        loginButton.addActionListener(event -> {
            User user = dataStore.checkLogin(usernameField.getText().trim(),
                    new String(passwordField.getPassword()));
            if (user == null) {
                errorLabel.setForeground(java.awt.Color.RED);
                errorLabel.setText("Incorrect username or password.");
            } else {
                dispose();
                new MainWindow(dataStore, user).setVisible(true);
            }
        });
        return panel;
    }

    // Builds and validates the signup form.
    private JPanel createSignupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(6, 2, 6, 6));
        JTextField fullNameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        JLabel errorLabel = new JLabel(" ");
        JButton signupButton = new JButton("Create Account");

        form.add(new JLabel("Full name:"));
        form.add(fullNameField);
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(new JLabel("Confirm password:"));
        form.add(confirmField);
        form.add(new JLabel(""));
        form.add(signupButton);
        form.add(new JLabel(""));
        form.add(new JLabel(""));
        panel.add(form, BorderLayout.CENTER);
        panel.add(errorLabel, BorderLayout.SOUTH);

        signupButton.addActionListener(event -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmation = new String(confirmField.getPassword());
            String message = validateSignup(fullName, username, password, confirmation);
            if (message != null) {
                errorLabel.setForeground(java.awt.Color.RED);
                errorLabel.setText(message);
                return;
            }
            try {
                dataStore.addUser(fullName, username, password);
                dispose();
                User user = dataStore.checkLogin(username, password);
                new MainWindow(dataStore, user).setVisible(true);
            } catch (Exception exception) {
                errorLabel.setForeground(java.awt.Color.RED);
                errorLabel.setText(exception.getMessage());
            }
        });
        return panel;
    }

    // Returns a friendly validation message or null when the signup fields are valid.
    private String validateSignup(String fullName, String username, String password,
                                  String confirmation) {
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmation.isEmpty()) {
            return "Please complete every field.";
        }
        if (dataStore.usernameExists(username)) {
            return "That username is already in use.";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        if (!password.equals(confirmation)) {
            return "Passwords do not match.";
        }
        return null;
    }
}
