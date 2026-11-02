package data;

import model.Account;
import model.Transaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.Function;

class UserDataFileStore {
    void saveUserData(String username, File file, ArrayList<Account> accounts,
                      ArrayList<Transaction> transactions) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            for (Account account : accounts) {
                if (account.getUsername().equalsIgnoreCase(username)) {
                    writer.println("ACCOUNT|" + account.getName() + "|" + account.getType() + "|"
                            + account.getOpeningBalance().toPlainString());
                }
            }
            for (Transaction transaction : transactions) {
                if (transaction.getUsername().equalsIgnoreCase(username)) {
                    String description = transaction.getDescription().replace("|", "/");
                    writer.println("TRANSACTION|" + transaction.getDate() + "|" + description + "|"
                            + transaction.getDebitAccountName() + "|" + transaction.getCreditAccountName()
                            + "|" + transaction.getAmount().toPlainString());
                }
            }
        }
    }

    void saveAccountsCsv(ArrayList<Account> accounts, File file,
                         Function<Account, BigDecimal> balanceCalculator) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            writer.println("Account Name,Type,Balance");
            for (Account account : accounts) {
                writer.println(csvField(account.getName()) + "," + csvField(account.getType()) + ","
                        + balanceCalculator.apply(account).toPlainString());
            }
        }
    }

    LoadedData loadUserData(String username, File file) throws IOException {
        ArrayList<Account> loadedAccounts = new ArrayList<>();
        ArrayList<Transaction> loadedTransactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if ((parts.length == 3 || parts.length == 4) && parts[0].equals("ACCOUNT")) {
                    BigDecimal openingBalance = parts.length == 4
                            ? new BigDecimal(parts[3]) : BigDecimal.ZERO;
                    loadedAccounts.add(new Account(username, parts[1], parts[2], openingBalance));
                } else if (parts.length == 6 && parts[0].equals("TRANSACTION")) {
                    loadedTransactions.add(new Transaction(username, LocalDate.parse(parts[1]),
                            parts[2], parts[3], parts[4], new BigDecimal(parts[5])));
                } else {
                    throw new IOException("The selected file contains an invalid line.");
                }
            }
        }
        return new LoadedData(loadedAccounts, loadedTransactions);
    }

    private String csvField(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    static final class LoadedData {
        private final ArrayList<Account> accounts;
        private final ArrayList<Transaction> transactions;

        LoadedData(ArrayList<Account> accounts, ArrayList<Transaction> transactions) {
            this.accounts = accounts;
            this.transactions = transactions;
        }

        ArrayList<Account> accounts() {
            return accounts;
        }

        ArrayList<Transaction> transactions() {
            return transactions;
        }
    }
}
