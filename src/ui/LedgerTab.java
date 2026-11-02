package ui;

import data.DataStore;
import model.Account;
import model.Transaction;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;

public class LedgerTab extends JPanel {
    private final DataStore dataStore;
    private final MainWindow mainWindow;
    private final JComboBox<Account> accountDropdown;
    private final DefaultTableModel tableModel;

    // Creates the account selector and running ledger table.
    public LedgerTab(DataStore dataStore, MainWindow mainWindow) {
        this.dataStore = dataStore;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());
        accountDropdown = new JComboBox<>();
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Select Account:"));
        controls.add(accountDropdown);
        add(controls, BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new String[]{
                "Date", "Description", "Debit Account", "Credit Account", "Amount", "Balance"
        }, 0);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        accountDropdown.addActionListener(event -> refreshLedger());
    }

    // Reloads the account selector and displays the selected account's ledger.
    public void refresh() {
        accountDropdown.removeAllItems();
        for (Account account : dataStore.getAccountsForUser(mainWindow.getUsername())) {
            accountDropdown.addItem(account);
        }
        refreshLedger();
    }

    // Shows all transactions that touch the selected account with its running balance.
    private void refreshLedger() {
        tableModel.setRowCount(0);
        Account selected = (Account) accountDropdown.getSelectedItem();
        if (selected == null) {
            return;
        }
        boolean debitNormal = selected.isDebitNormal();
        BigDecimal balance = selected.getOpeningBalance();
        for (Transaction transaction : dataStore.getTransactionsForUser(mainWindow.getUsername())) {
            boolean debitMatch = transaction.getDebitAccountName().equalsIgnoreCase(selected.getName());
            boolean creditMatch = transaction.getCreditAccountName().equalsIgnoreCase(selected.getName());
            if (!debitMatch && !creditMatch) {
                continue;
            }
            if (debitMatch) {
                balance = debitNormal ? balance.add(transaction.getAmount())
                        : balance.subtract(transaction.getAmount());
            } else {
                balance = debitNormal ? balance.subtract(transaction.getAmount())
                        : balance.add(transaction.getAmount());
            }
            tableModel.addRow(new Object[]{
                    transaction.getDate().toString(),
                    transaction.getDescription(),
                    transaction.getDebitAccountName(),
                    transaction.getCreditAccountName(),
                    dataStore.formatMoney(transaction.getAmount()),
                    dataStore.formatMoney(balance)
            });
        }
    }
}
