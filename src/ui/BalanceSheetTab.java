package ui;

import data.DataStore;
import model.Account;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;

public class BalanceSheetTab extends JPanel {
    private final DataStore dataStore;
    private final MainWindow mainWindow;
    private final DefaultTableModel assetsModel;
    private final DefaultTableModel liabilitiesModel;
    private final JLabel balanceLabel;

    // Creates the side-by-side assets and liabilities/equity tables.
    public BalanceSheetTab(DataStore dataStore, MainWindow mainWindow) {
        this.dataStore = dataStore;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());
        JPanel tables = new JPanel(new GridLayout(1, 2));
        assetsModel = new DefaultTableModel(new String[]{"Assets", "Amount"}, 0);
        liabilitiesModel = new DefaultTableModel(
                new String[]{"Liabilities & Equity", "Amount"}, 0);
        tables.add(new JScrollPane(new JTable(assetsModel)));
        tables.add(new JScrollPane(new JTable(liabilitiesModel)));
        add(tables, BorderLayout.CENTER);
        balanceLabel = new JLabel(" ");
        add(balanceLabel, BorderLayout.SOUTH);
    }

    // Reloads non-zero balances, adds net income to equity, and checks the sheet totals.
    public void refresh() {
        assetsModel.setRowCount(0);
        liabilitiesModel.setRowCount(0);
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilitiesAndEquity = BigDecimal.ZERO;
        for (Account account : dataStore.getAccountsForUser(mainWindow.getUsername())) {
            BigDecimal balance = dataStore.calculateBalance(mainWindow.getUsername(), account);
            if (balance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (account.getType().equals("ASSET")) {
                assetsModel.addRow(new Object[]{account.getName(), dataStore.formatMoney(balance)});
                totalAssets = totalAssets.add(balance);
            } else if (account.getType().equals("LIABILITY")
                    || account.getType().equals("EQUITY")) {
                liabilitiesModel.addRow(new Object[]{account.getName(), dataStore.formatMoney(balance)});
                totalLiabilitiesAndEquity = totalLiabilitiesAndEquity.add(balance);
            }
        }
        BigDecimal netIncome = dataStore.calculateNetIncome(mainWindow.getUsername());
        if (netIncome.compareTo(BigDecimal.ZERO) != 0) {
            liabilitiesModel.addRow(new Object[]{"Net Income", dataStore.formatMoney(netIncome)});
            totalLiabilitiesAndEquity = totalLiabilitiesAndEquity.add(netIncome);
        }
        assetsModel.addRow(new Object[]{"Total Assets", dataStore.formatMoney(totalAssets)});
        liabilitiesModel.addRow(new Object[]{
                "Total Liabilities & Equity", dataStore.formatMoney(totalLiabilitiesAndEquity)
        });
        if (totalAssets.compareTo(totalLiabilitiesAndEquity) == 0) {
            balanceLabel.setText("Balanced ✔");
        } else {
            balanceLabel.setText("Not balanced ✘");
        }
    }
}
