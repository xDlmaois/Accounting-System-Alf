package ui;

import data.DataStore;
import model.Transaction;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

public class JournalTab extends JPanel {
    private final DataStore dataStore;
    private final MainWindow mainWindow;
    private final DefaultTableModel tableModel;

    // Creates the general journal table.
    public JournalTab(DataStore dataStore, MainWindow mainWindow) {
        this.dataStore = dataStore;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(
                new String[]{"Date", "Description", "Account", "Debit", "Credit"}, 0);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
    }

    // Reloads each transaction as one debit row and one indented credit row.
    public void refresh() {
        tableModel.setRowCount(0);
        for (Transaction transaction : dataStore.getTransactionsForUser(mainWindow.getUsername())) {
            String amount = dataStore.formatMoney(transaction.getAmount());
            tableModel.addRow(new Object[]{
                    transaction.getDate().toString(),
                    transaction.getDescription(),
                    transaction.getDebitAccountName(),
                    amount,
                    ""
            });
            tableModel.addRow(new Object[]{
                    "",
                    "",
                    "    " + transaction.getCreditAccountName(),
                    "",
                    amount
            });
        }
    }
}
