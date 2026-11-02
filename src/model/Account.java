package model;

import java.math.BigDecimal;

public class Account {
    private final String username;
    private final String name;
    private final String type;
    private final BigDecimal openingBalance;

    // Creates an account owned by one username.
    public Account(String username, String name, String type) {
        this(username, name, type, BigDecimal.ZERO);
    }

    // Creates an account with an opening balance.
    public Account(String username, String name, String type, BigDecimal openingBalance) {
        this.username = username;
        this.name = name;
        this.type = type;
        this.openingBalance = openingBalance;
    }

    // Returns the username that owns this account.
    public String getUsername() {
        return username;
    }

    // Returns the account name.
    public String getName() {
        return name;
    }

    // Returns the account type.
    public String getType() {
        return type;
    }

    // Returns the account's opening balance.
    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    // Returns whether this account normally has a debit balance.
    public boolean isDebitNormal() {
        return type.equals("ASSET") || type.equals("EXPENSE");
    }

    // Shows the account name and type in account dropdowns.
    @Override
    public String toString() {
        return name + " [" + type + "]";
    }
}
