package ui;

import data.DataStore;
import model.User;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;

public class MainWindow extends JFrame {
    private final DataStore dataStore;
    private final User user;
    private final JLabel statusLabel;
    private final NewTransactionTab newTransactionTab;
    private final TransactionsTab transactionsTab;
    private final AccountsTab accountsTab;
    private final JournalTab journalTab;
    private final LedgerTab ledgerTab;
    private final BalanceSheetTab balanceSheetTab;

    // Creates the main application frame and its six tabs.
    public MainWindow(DataStore dataStore, User user) {
        this.dataStore = dataStore;
        this.user = user;
        setTitle("Accounting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        newTransactionTab = new NewTransactionTab(dataStore, this);
        transactionsTab = new TransactionsTab(dataStore, this);
        accountsTab = new AccountsTab(dataStore, this);
        journalTab = new JournalTab(dataStore, this);
        ledgerTab = new LedgerTab(dataStore, this);
        balanceSheetTab = new BalanceSheetTab(dataStore, this);
        tabs.addTab("New Transaction", newTransactionTab);
        tabs.addTab("Transactions", transactionsTab);
        tabs.addTab("Accounts", accountsTab);
        tabs.addTab("General Journal", journalTab);
        tabs.addTab("General Ledger", ledgerTab);
        tabs.addTab("Balance Sheet", balanceSheetTab);
        add(tabs, BorderLayout.CENTER);
        statusLabel = new JLabel("Ready.");
        add(statusLabel, BorderLayout.SOUTH);
        refreshAll();
    }

    // Builds the title, subtitle, user name, and file/session buttons.
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel titles = new JPanel(new GridLayout(3, 1));
        titles.add(new JLabel("Accounting System"));
        titles.add(new JLabel("Double-Entry · General Journal · General Ledger · Balance Sheet"));
        titles.add(new JLabel("Logged in as: " + user.getFullName()));
        JPanel buttons = new JPanel();
        JButton loadButton = new JButton("Load");
        JButton saveButton = new JButton("Save");
        JButton csvButton = new JButton("Save CSV");
        JButton logoutButton = new JButton("Logout");
        buttons.add(loadButton);
        buttons.add(saveButton);
        buttons.add(csvButton);
        buttons.add(logoutButton);
        panel.add(titles, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);

        loadButton.addActionListener(event -> loadData());
        saveButton.addActionListener(event -> saveData());
        csvButton.addActionListener(event -> saveCsvData());
        logoutButton.addActionListener(event -> {
            dispose();
            new LoginWindow(dataStore).setVisible(true);
        });
        return panel;
    }

    // Refreshes all six tabs from the current user's data.
    public void refreshAll() {
        newTransactionTab.refresh();
        transactionsTab.refresh();
        accountsTab.refresh();
        journalTab.refresh();
        ledgerTab.refresh();
        balanceSheetTab.refresh();
    }

    // Shows a message in the status bar using green for success and red for errors.
    public void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? java.awt.Color.RED : new java.awt.Color(0, 128, 0));
    }

    // Returns the active username to the tabs.
    public String getUsername() {
        return user.getUsername();
    }

    // Opens a file chooser and saves the active user's data.
    private void saveData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("accounting-data.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            dataStore.saveUserData(user.getUsername(), chooser.getSelectedFile());
            showStatus("Data saved.", false);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Exports the active user's account balances to a CSV file.
    private void saveCsvData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("accounts.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            dataStore.saveAccountsCsv(user.getUsername(), chooser.getSelectedFile());
            showStatus("Accounts exported.", false);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Confirms replacement, then loads the active user's data from a selected file.
    private void loadData() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Loading this file will replace your current accounts and transactions. Continue?",
                "Confirm Load", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dataStore.loadUserData(user.getUsername(), chooser.getSelectedFile());
            refreshAll();
            showStatus("Data loaded.", false);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
