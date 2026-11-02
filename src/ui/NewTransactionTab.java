package ui;

import data.DataStore;
import model.Account;
import model.Transaction;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;

public class NewTransactionTab extends JPanel {
    private final DataStore dataStore;
    private final MainWindow mainWindow;
    private final JTextField dateField;
    private final JTextField descriptionField;
    private final JComboBox<Account> debitDropdown;
    private final JComboBox<Account> creditDropdown;
    private final JTextField amountField;

    // Creates the transaction entry form.
    public NewTransactionTab(DataStore dataStore, MainWindow mainWindow) {
        this.dataStore = dataStore;
        this.mainWindow = mainWindow;
        setLayout(new GridLayout(6, 2, 8, 8));
        dateField = new JTextField(LocalDate.now().toString());
        descriptionField = new JTextField();
        debitDropdown = new JComboBox<>();
        creditDropdown = new JComboBox<>();
        amountField = new JTextField();
        JButton addButton = new JButton("Add Transaction");

        add(new JLabel("Date (YYYY-MM-DD):"));
        add(dateField);
        add(new JLabel("Description:"));
        add(descriptionField);
        add(new JLabel("Debit Account:"));
        add(debitDropdown);
        add(new JLabel("Credit Account:"));
        add(creditDropdown);
        add(new JLabel("Amount:"));
        add(amountField);
        add(new JLabel(""));
        add(addButton);
        addButton.addActionListener(event -> addTransaction());
    }

    // Reloads the current user's account dropdowns.
    public void refresh() {
        debitDropdown.removeAllItems();
        creditDropdown.removeAllItems();
        for (Account account : dataStore.getAccountsForUser(mainWindow.getUsername())) {
            debitDropdown.addItem(account);
            creditDropdown.addItem(account);
        }
    }

    // Validates the form and adds exactly one debit and one equal credit.
    private void addTransaction() {
        String description = descriptionField.getText().trim();
        String amountText = amountField.getText().trim().replace(",", "");
        Account debitAccount = (Account) debitDropdown.getSelectedItem();
        Account creditAccount = (Account) creditDropdown.getSelectedItem();
        LocalDate date;
        BigDecimal amount;
        try {
            date = LocalDate.parse(dateField.getText().trim());
            amount = new BigDecimal(amountText);
        } catch (RuntimeException exception) {
            mainWindow.showStatus("Enter a valid date and amount.", true);
            return;
        }
        if (description.isEmpty()) {
            showError("Description is required.");
            return;
        }
        if (debitAccount.getName().equalsIgnoreCase(creditAccount.getName())) {
            showError("Debit and credit accounts must be different.");
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            showError("Amount must be greater than zero.");
            return;
        }
        Transaction transaction = new Transaction(mainWindow.getUsername(), date,
                description, debitAccount.getName(),
                creditAccount.getName(), amount);
        dataStore.addTransaction(transaction);
        descriptionField.setText("");
        amountField.setText("");
        mainWindow.refreshAll();
        mainWindow.showStatus("Transaction added.", false);
    }

    // Shows a validation message in the status bar.
    private void showError(String message) {
        mainWindow.showStatus(message, true);
    }
}
