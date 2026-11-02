# Accounting System

A beginner-friendly Java 17+ desktop bookkeeping application. It uses Swing and
only the Java standard library; it has no database, build tool, or external jars.

## Project structure

```text
Accounting System/
├── src/
│   ├── Main.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Account.java
│   │   └── Transaction.java
│   ├── data/
│   │   ├── DataStore.java
│   │   ├── UserRepository.java
│   │   └── UserDataFileStore.java
│   └── ui/
│       ├── LoginWindow.java
│       ├── MainWindow.java
│       ├── NewTransactionTab.java
│       ├── TransactionsTab.java
│       ├── AccountsTab.java
│       ├── JournalTab.java
│       ├── LedgerTab.java
│       └── BalanceSheetTab.java
├── compile.bat
├── run.bat
├── compile.sh
├── run.sh
└── README.md
```

## Compile and run

Install a JDK version 17 or newer and open a terminal in the project directory.

### Windows

```bat
compile.bat
run.bat
```

Or compile all Java files under `src` directly in PowerShell:

```powershell
$sources = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force out
javac -encoding UTF-8 -d out $sources
java -cp out Main
```

### macOS or Linux

```sh
chmod +x compile.sh run.sh
./compile.sh
./run.sh
```

## Data files

Signups are automatically written to `users.txt` in the current working
directory, one `fullName|username|password` record per line. Passwords are plain
text to keep this learning example simple; a real application should hash
passwords and protect the user file.

Use **Save** to export the logged-in user's accounts and transactions to a file
you choose. **Load** asks for confirmation and replaces only that user's current
accounts and transactions. Account records include an opening balance:
`ACCOUNT|name|type|openingBalance`. The loader also accepts older three-field
account records. Transactions use
`TRANSACTION|date|description|debitAccountName|creditAccountName|amount`.
**Save CSV** exports the current account balances. Data remains in memory until
the user saves it; only the user list is automatically persisted.

The account form accepts an opening balance, and new accounts are added to the
logged-in user's chart. The tabs use plain Swing layouts so their controls and
layout code can be edited directly.

## Quick test steps

1. Start the app, open **Sign Up**, and create an account with a password of at
   least eight characters. The app opens the ledger after signup; log out and
   back in to exercise the login screen.
2. On **New Transaction**, enter today's date, description `invest cash in
   company`, debit **Cash**, credit **Owner's Capital**, and amount `50,000`.
3. Add `purchased equipment`, debit **Equipment**, credit **Cash**, amount
   `10,000`.
4. Confirm **Accounts** shows Cash `40,000.00`, Equipment `10,000.00`, and
   Owner's Capital `50,000.00`.
5. Confirm **Balance Sheet** shows Total Assets `50,000.00`, Total Liabilities &
   Equity `50,000.00`, and **Balanced ✔**.
6. Save to a file, load it again, and confirm the same accounts and balances
   remain.

## File structure

The app generates the following files in the `out/` directory:

- `*.class` files for the compiled Java classes
- `users.txt` for the user signups

The user signups are also written to `src/users.txt` for reference.
