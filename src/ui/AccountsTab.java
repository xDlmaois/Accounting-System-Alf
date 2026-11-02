package ui;

import data.DataStore;
import model.Account;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;

public class AccountsTab extends JPanel {
    private final DataStore dataStore;
    private final MainWindow mainWindow;
    private final DefaultTableModel tableModel;
    private final JTextField nameField;
    private final JComboBox<String> typeDropdown;
    private final JTextField openingBalanceField;

    // Creates the account list and editable account form.
    public AccountsTab(DataStore dataStore, MainWindow mainWindow) {
        this.dataStore = dataStore;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(
                new String[]{"Account", "Type", "Opening Balance", "Balance"}, 0);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(2, 4, 8, 8));
        nameField = new JTextField();
        typeDropdown = new JComboBox<>(
                new String[]{"ASSET", "LIABILITY", "EQUITY", "INCOME", "EXPENSE"});
        openingBalanceField = new JTextField("0.00");
        JButton addButton = new JButton("Add Account");
        form.add(new JLabel("Account name:"));
        form.add(new JLabel("Account type:"));
        form.add(new JLabel("Opening balance:"));
        form.add(new JLabel(""));
        form.add(nameField);
        form.add(typeDropdown);
        form.add(openingBalanceField);
        form.add(addButton);
        add(form, BorderLayout.SOUTH);
        addButton.addActionListener(event -> addAccount());
    }

    // Reloads the chart of accounts and recalculates live balances.
    public void refresh() {
        tableModel.setRowCount(0);
        for (Account account : dataStore.getAccountsForUser(mainWindow.getUsername())) {
            tableModel.addRow(new Object[]{
                    account.getName(),
                    account.getType(),
                    dataStore.formatMoney(account.getOpeningBalance()),
                    dataStore.formatMoney(
                            dataStore.calculateBalance(mainWindow.getUsername(), account))
            });
        }
    }

    // Validates the account form and adds the account to the current user's chart.
    private void addAccount() {
        try {
            BigDecimal openingBalance = new BigDecimal(openingBalanceField.getText().trim());
            dataStore.addAccount(mainWindow.getUsername(), nameField.getText().trim(),
                    (String) typeDropdown.getSelectedItem(), openingBalance);
            nameField.setText("");
            openingBalanceField.setText("0.00");
            mainWindow.refreshAll();
            mainWindow.showStatus("Account added.", false);
        } catch (NumberFormatException exception) {
            mainWindow.showStatus("Enter a valid opening balance.", true);
        } catch (IllegalArgumentException exception) {
            mainWindow.showStatus(exception.getMessage(), true);
        }
    }
}
