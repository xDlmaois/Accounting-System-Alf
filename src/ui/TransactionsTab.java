package ui;

import data.DataStore;
import model.Transaction;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;

public class TransactionsTab extends JPanel {
    private final DataStore dataStore;
    private final MainWindow mainWindow;
    private final JTextField searchField;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final ArrayList<Transaction> displayedTransactions;

    // Creates the searchable transaction table.
    public TransactionsTab(DataStore dataStore, MainWindow mainWindow) {
        this.dataStore = dataStore;
        this.mainWindow = mainWindow;
        displayedTransactions = new ArrayList<>();
        setLayout(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(25);
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        JButton deleteButton = new JButton("Delete");
        controls.add(new JLabel("Search:"));
        controls.add(searchField);
        controls.add(searchButton);
        controls.add(clearButton);
        controls.add(deleteButton);
        add(controls, BorderLayout.NORTH);
        tableModel = new DefaultTableModel(
                new String[]{"Date", "Description", "Debit Account", "Credit Account", "Amount"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        searchButton.addActionListener(event -> refresh());
        clearButton.addActionListener(event -> {
            searchField.setText("");
            refresh();
        });
        deleteButton.addActionListener(event -> deleteSelected());
    }

    // Reloads the table and applies the current text filter.
    public void refresh() {
        tableModel.setRowCount(0);
        displayedTransactions.clear();
        String query = searchField.getText().trim().toLowerCase();
        ArrayList<Transaction> transactions =
                dataStore.getTransactionsForUser(mainWindow.getUsername());
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction transaction = transactions.get(i);
            String searchable = transaction.getDate() + " " + transaction.getDescription() + " "
                    + transaction.getDebitAccountName() + " " + transaction.getCreditAccountName();
            if (searchable.toLowerCase().contains(query)) {
                displayedTransactions.add(transaction);
                tableModel.addRow(new Object[]{
                        transaction.getDate().toString(),
                        transaction.getDescription(),
                        transaction.getDebitAccountName(),
                        transaction.getCreditAccountName(),
                        dataStore.formatMoney(transaction.getAmount())
                });
            }
        }
    }

    // Confirms and deletes the currently selected transaction.
    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedTransactions.size()) {
            mainWindow.showStatus("Select a transaction to delete.", true);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete the selected transaction?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            dataStore.deleteTransaction(mainWindow.getUsername(),
                    displayedTransactions.get(selectedRow));
            mainWindow.refreshAll();
            mainWindow.showStatus("Transaction deleted.", false);
        }
    }
}
