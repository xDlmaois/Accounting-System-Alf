package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private final String username;
    private final LocalDate date;
    private final String description;
    private final String debitAccountName;
    private final String creditAccountName;
    private final BigDecimal amount;

    // Creates one balanced debit-and-credit transaction.
    public Transaction(String username, LocalDate date, String description,
                       String debitAccountName, String creditAccountName, BigDecimal amount) {
        this.username = username;
        this.date = date;
        this.description = description;
        this.debitAccountName = debitAccountName;
        this.creditAccountName = creditAccountName;
        this.amount = amount;
    }

    // Returns the username that owns this transaction.
    public String getUsername() {
        return username;
    }

    // Returns the transaction date.
    public LocalDate getDate() {
        return date;
    }

    // Returns the transaction description.
    public String getDescription() {
        return description;
    }

    // Returns the debited account name.
    public String getDebitAccountName() {
        return debitAccountName;
    }

    // Returns the credited account name.
    public String getCreditAccountName() {
        return creditAccountName;
    }

    // Returns the equal debit and credit amount.
    public BigDecimal getAmount() {
        return amount;
    }
}
