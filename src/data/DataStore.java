package data;

import model.Account;
import model.Transaction;
import model.User;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

public class DataStore {
    private final UserRepository userRepository;
    private final UserDataFileStore userDataFileStore;
    private final ArrayList<Account> accounts;
    private final ArrayList<Transaction> transactions;

    // Loads the persistent user list and starts with an empty in-memory ledger.
    public DataStore() throws IOException {
        userRepository = new UserRepository();
        userDataFileStore = new UserDataFileStore();
        accounts = new ArrayList<>();
        transactions = new ArrayList<>();
    }

    // Returns a copy of the accounts belonging to one user.
    public ArrayList<Account> getAccountsForUser(String username) {
        ArrayList<Account> result = new ArrayList<>();
        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                result.add(account);
            }
        }
        return result;
    }

    // Returns a copy of the transactions belonging to one user.
    public ArrayList<Transaction> getTransactionsForUser(String username) {
        ArrayList<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getUsername().equalsIgnoreCase(username)) {
                result.add(transaction);
            }
        }
        return result;
    }

    // Checks whether a username is already registered.
    public boolean usernameExists(String username) {
        return userRepository.usernameExists(username);
    }

    // Checks a login and returns the matching user, or null when it fails.
    public User checkLogin(String username, String password) {
        return userRepository.checkLogin(username, password);
    }

    // Registers a user, seeds the chart of accounts, and automatically saves users.txt.
    public void addUser(String fullName, String username, String password) throws IOException {
        userRepository.addUser(fullName, username, password);
        seedAccounts(username);
        userRepository.saveUsers();
    }

    // Adds the default chart of accounts for a newly registered user.
    private void seedAccounts(String username) {
        addAccount(username, "Cash", "ASSET");
        addAccount(username, "Accounts Receivable", "ASSET");
        addAccount(username, "Inventory", "ASSET");
        addAccount(username, "Supplies", "ASSET");
        addAccount(username, "Prepaid Expenses", "ASSET");
        addAccount(username, "Equipment", "ASSET");
        addAccount(username, "Furniture and Fixtures", "ASSET");
        addAccount(username, "Accounts Payable", "LIABILITY");
        addAccount(username, "Notes Payable", "LIABILITY");
        addAccount(username, "Salaries Payable", "LIABILITY");
        addAccount(username, "Owner's Capital", "EQUITY");
        addAccount(username, "Owner's Drawing", "EQUITY");
        addAccount(username, "Sales Revenue", "INCOME");
        addAccount(username, "Service Revenue", "INCOME");
        addAccount(username, "Cost of Goods Sold", "EXPENSE");
        addAccount(username, "Rent Expense", "EXPENSE");
        addAccount(username, "Salaries Expense", "EXPENSE");
        addAccount(username, "Utilities Expense", "EXPENSE");
    }

    // Adds a valid account for the specified user.
    public void addAccount(String username, String name, String type) {
        addAccount(username, name, type, BigDecimal.ZERO);
    }

    // Adds an account with an opening balance for the specified user.
    public void addAccount(String username, String name, String type, BigDecimal openingBalance) {
        String accountName = name.trim();
        if (accountName.isEmpty()) {
            throw new IllegalArgumentException("Enter an account name.");
        }
        if (findAccountByName(username, accountName) != null) {
            throw new IllegalArgumentException("An account with that name already exists.");
        }
        accounts.add(new Account(username, accountName, type, openingBalance));
    }

    // Finds one account by name without exposing another user's accounts.
    public Account findAccountByName(String username, String name) {
        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username)
                    && account.getName().equalsIgnoreCase(name)) {
                return account;
            }
        }
        return null;
    }

    // Adds a transaction to the list.
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transactions.sort(Comparator.comparing(Transaction::getDate));
    }

    // Removes a transaction from the list.
    public void deleteTransaction(String username, Transaction transaction) {
        transactions.remove(transaction);
    }

    // Calculates an account's balance using the normal-balance rule for its type.
    public BigDecimal calculateBalance(String username, Account account) {
        BigDecimal balance = account.getOpeningBalance();
        boolean debitNormal = account.isDebitNormal();
        for (Transaction transaction : transactions) {
            if (!transaction.getUsername().equalsIgnoreCase(username)) {
                continue;
            }
            if (transaction.getDebitAccountName().equalsIgnoreCase(account.getName())) {
                if (debitNormal) {
                    balance = balance.add(transaction.getAmount());
                } else {
                    balance = balance.subtract(transaction.getAmount());
                }
            }
            if (transaction.getCreditAccountName().equalsIgnoreCase(account.getName())) {
                if (debitNormal) {
                    balance = balance.subtract(transaction.getAmount());
                } else {
                    balance = balance.add(transaction.getAmount());
                }
            }
        }
        return balance;
    }

    // Calculates total income less total expenses.
    public BigDecimal calculateNetIncome(String username) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        for (Account account : getAccountsForUser(username)) {
            if (account.getType().equals("INCOME")) {
                income = income.add(calculateBalance(username, account));
            } else if (account.getType().equals("EXPENSE")) {
                expenses = expenses.add(calculateBalance(username, account));
            }
        }
        return income.subtract(expenses);
    }

    // Formats an amount with US-style thousands separators and two decimal places.
    public String formatMoney(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(amount);
    }

    // Saves only the selected user's accounts and transactions in the specified text format.
    public void saveUserData(String username, File file) throws IOException {
        userDataFileStore.saveUserData(username, file, accounts, transactions);
    }

    // Exports the selected user's account balances as a CSV file.
    public void saveAccountsCsv(String username, File file) throws IOException {
        userDataFileStore.saveAccountsCsv(getAccountsForUser(username), file,
                account -> calculateBalance(username, account));
    }

    // Reads a user's records and replaces only that user's current data.
    public void loadUserData(String username, File file) throws IOException {
        UserDataFileStore.LoadedData loadedData = userDataFileStore.loadUserData(username, file);
        replaceUserData(username, loadedData.accounts(), loadedData.transactions());
    }

    // Replaces only the selected user's data after a complete successful load.
    private void replaceUserData(String username, ArrayList<Account> loadedAccounts,
                                 ArrayList<Transaction> loadedTransactions) {
        for (int i = accounts.size() - 1; i >= 0; i--) {
            if (accounts.get(i).getUsername().equalsIgnoreCase(username)) {
                accounts.remove(i);
            }
        }
        for (int i = transactions.size() - 1; i >= 0; i--) {
            if (transactions.get(i).getUsername().equalsIgnoreCase(username)) {
                transactions.remove(i);
            }
        }
        accounts.addAll(loadedAccounts);
        transactions.addAll(loadedTransactions);
    }
}
