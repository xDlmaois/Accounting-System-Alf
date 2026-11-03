# Java Code Explanation

This guide explains the Java source files under `src/`. It follows each file
from top to bottom and explains its declarations and statements. Wrapped
statements are explained together. Blank lines only separate sections, and
braces mark the start or end of a class, method, loop, or condition.

The compiled `.class` files and `users.txt` are not Java source code, so they
are not explained here.

## `src/Main.java`

- `import data.DataStore` and `import ui.LoginWindow` make the app's data manager
  and first screen available to this file.
- `import javax.swing.SwingUtilities` provides Swing's event-thread helper.
- `public class Main` declares the application's entry-point class.
- `main(String[] args)` is where Java starts the program. `throws Exception`
  allows startup errors to be reported to the runtime.
- `new DataStore()` creates the central in-memory data manager and loads the
  saved user list.
- `SwingUtilities.invokeLater(...)` schedules UI creation on Swing's event
  thread. The lambda creates the login window and displays it.

## `src/model/User.java`

- `package model` places this class in the model package.
- `public class User` represents one registered user.
- `fullName`, `username`, and `password` are final fields: each is set once
  when the object is created. The password is stored as plain text in this
  learning project.
- The constructor receives those three values and assigns each to its field.
- `getFullName()`, `getUsername()`, and `getPassword()` return the matching
  stored value. They let other classes read the user's details.

## `src/model/Account.java`

- `package model` and the `BigDecimal` import identify the package and exact
  decimal type used for money.
- `Account` represents a user's ledger account.
- Its final fields store the owning username, account name, category, and
  opening balance.
- The three-argument constructor delegates to the four-argument constructor
  with `BigDecimal.ZERO`, making the default opening balance zero.
- The four-argument constructor stores the supplied account details.
- `getUsername()`, `getName()`, `getType()`, and `getOpeningBalance()` return
  those stored values.
- `isDebitNormal()` returns true for assets and expenses; those categories
  normally increase on a debit. Other categories normally increase on a credit.
- `@Override` indicates that `toString()` replaces Java's default object text.
- `toString()` returns the account name and type, which makes dropdown choices
  readable.

## `src/model/Transaction.java`

- The package and imports provide the model namespace, precise decimal values,
  and `LocalDate` for dates without a time-of-day.
- `Transaction` represents one double-entry event.
- Its final fields store the owner's username, date, description, debit account
  name, credit account name, and amount.
- The constructor takes all six values and assigns them to the matching fields.
  The transaction uses one amount for both sides, so the entry is balanced by
  construction.
- `getUsername()`, `getDate()`, `getDescription()`, `getDebitAccountName()`,
  `getCreditAccountName()`, and `getAmount()` each return one stored value.

## `src/data/UserRepository.java`

- `package data` marks this as a data-layer class. Imports provide the `User`
  model, file reading/writing, checked I/O errors, and a list.
- `class UserRepository` is package-private: the rest of the app can use it
  from the `data` package, but it is not exposed as a public API.
- `users` holds user objects in memory. `usersFile` points to `users.txt` in
  the program's current working directory.
- The constructor calls `loadUsers()` so previously registered users are
  available when the application starts.
- `usernameExists(username)` loops through registered users and compares
  usernames without case sensitivity. It returns true on a match and false
  after checking all users.
- `checkLogin(username, password)` searches for a case-insensitive username
  and an exact password match. It returns that user or `null` if credentials
  do not match.
- `addUser(...)` creates a `User` and appends it to the in-memory list.
- `loadUsers()` returns immediately if `users.txt` does not exist. Otherwise,
  it reads the file line by line, splits each row at `|`, and creates a user
  from the three fields. The try-with-resources block closes the reader.
- `saveUsers()` opens the file for writing, loops over users, and writes each
  user's full name, username, and password separated by `|`. Opening the file
  this way replaces its previous contents; try-with-resources closes it.

## `src/data/UserDataFileStore.java`

- The package and imports provide account/transaction models, buffered text
  file I/O, decimal amounts, dates, lists, and a function used for balance
  calculation during export.
- `UserDataFileStore` is package-private and handles ledger file formats.
- `saveUserData(username, file, accounts, transactions)` opens the selected
  file for writing. It writes only accounts owned by the requested username
  as `ACCOUNT|name|type|openingBalance` records.
- It then writes only that user's transactions as
  `TRANSACTION|date|description|debit|credit|amount`. Descriptions replace
  `|` with `/` so the delimiter does not break the record. Decimal values use
  `toPlainString()` to avoid scientific notation.
- `saveAccountsCsv(accounts, file, balanceCalculator)` writes a CSV header,
  then one row per account. `csvField()` quotes text values, and the supplied
  function calculates each current balance.
- `loadUserData(username, file)` creates empty lists for the parsed accounts
  and transactions, then reads the selected file one line at a time.
- `split("\\|", -1)` splits each record at `|` and retains empty trailing
  fields so malformed rows can be detected.
- An `ACCOUNT` row with three or four fields becomes an `Account`. A legacy
  three-field row gets a zero opening balance; a four-field row parses its
  balance as `BigDecimal`.
- A six-field `TRANSACTION` row becomes a `Transaction`; its date is parsed
  as `LocalDate` and amount as `BigDecimal`.
- Any other row shape or record type throws an `IOException`. The loaded lists
  are returned only after the whole file is read successfully.
- `csvField(value)` wraps a string in quotes and doubles embedded quotes, as
  required for CSV text fields.
- `LoadedData` is a small nested result class holding the two loaded lists.
  Its constructor stores the lists; `accounts()` and `transactions()` return
  them to `DataStore`.

## `src/data/DataStore.java`

- The imports provide account, transaction, and user models; files and I/O;
  decimal calculations; number formatting; lists; sorting; locale settings;
  and a function callback.
- `DataStore` is the central in-memory application data manager.
- `userRepository` handles registered users; `userDataFileStore` handles
  ledger file formats. `accounts` and `transactions` hold the current
  application's ledger data in memory.
- The constructor creates both helpers and starts with empty account and
  transaction lists. The repository loads `users.txt` during its own setup.
- `getAccountsForUser(username)` builds and returns a list containing only
  accounts whose owner matches the username, ignoring letter case.
- `getTransactionsForUser(username)` does the same for transactions.
- `usernameExists(username)` forwards the check to `UserRepository`.
- `checkLogin(username, password)` forwards credential checking to the
  repository and returns its matching user or `null`.
- `addUser(...)` adds the user to the repository, creates their default
  accounts by calling `seedAccounts()`, then saves the user list.
- `seedAccounts(username)` creates the starter chart of accounts: asset
  accounts (Cash, Receivable, Inventory, Supplies, Prepaid Expenses,
  Equipment, Furniture and Fixtures); liability accounts (Accounts Payable,
  Notes Payable, Salaries Payable); equity accounts (Owner's Capital and
  Owner's Drawing); income accounts (Sales Revenue and Service Revenue); and
  expense accounts (Cost of Goods Sold, Rent, Salaries, and Utilities).
- The three-argument `addAccount(...)` overload calls the four-argument
  version with a zero opening balance.
- The four-argument `addAccount(...)` trims the name, rejects a blank name,
  rejects a duplicate name for that user, and then creates/stores the account.
- `findAccountByName(username, name)` searches for a matching account name
  owned by that user. Both comparisons ignore case. It returns the account or
  `null`.
- `addTransaction(transaction)` appends the transaction and sorts the shared
  list by date.
- `deleteTransaction(username, transaction)` removes the supplied transaction
  from the list. The username parameter is present, but this method removes
  by transaction object and does not check the username itself.
- `calculateBalance(username, account)` starts with the account's opening
  balance and determines its normal side. For each transaction belonging to
  that user, it adds or subtracts the amount depending on whether the account
  is debited or credited and whether the account is debit-normal.
- `calculateNetIncome(username)` loops over that user's accounts, adds the
  balances of income accounts, adds the balances of expense accounts, and
  returns income minus expenses.
- `formatMoney(amount)` formats a decimal with US grouping separators and
  exactly two fractional digits.
- `saveUserData(username, file)` forwards the current lists and requested
  user's name to the file store.
- `saveAccountsCsv(username, file)` obtains that user's accounts and supplies
  a lambda that calculates each account's current balance to the CSV exporter.
- `loadUserData(username, file)` asks the file store to parse the file first.
  Only after parsing succeeds does it call `replaceUserData()`.
- `replaceUserData(...)` walks both lists backwards and removes existing data
  for that user, then appends the newly loaded accounts and transactions.
  Other users' data remains in the lists.

## `src/ui/LoginWindow.java`

- The imports provide the data manager, user model, Swing form controls, and
  layout managers.
- `LoginWindow extends JFrame` makes the login screen a desktop window.
- The `dataStore` field lets login and sign-up actions access application data.
- The constructor stores that reference, sets the window title, close behavior,
  size, and centered position, then creates Login and Sign Up tabs.
- `createLoginPanel()` creates a panel with a form grid, username field,
  password field, show-password checkbox, error label, and Login button.
- The `form.add(...)` statements place the labels and controls into the grid;
  the form is placed in the center and the error label at the bottom.
- The show-password listener sets the password field's echo character to zero
  when selected, revealing text, and restores `*` when unchecked.
- The login listener trims the entered username, reads the password, and asks
  `DataStore` to check the credentials. A missing user produces a red error;
  a valid user closes this window and opens `MainWindow`.
- `createSignupPanel()` constructs the full-name, username, password,
  confirmation, error, and submit controls and arranges them in a grid.
- Its submit listener reads and trims values, calls `validateSignup()`, and
  displays any returned error. If valid, it registers the user, closes the
  login window, gets the newly registered user, and opens the main window.
  Exceptions are displayed in red in the form.
- `validateSignup(...)` returns an error message for any empty field, an
  already-used username, a password shorter than eight characters, or
  mismatched password confirmation. It returns `null` when validation passes.

## `src/ui/MainWindow.java`

- The imports provide shared data, current user, Swing controls/dialogs, layout
  managers, and file selection.
- `MainWindow extends JFrame` is the main application window.
- Its fields keep references to the shared data manager, logged-in user,
  status label, and six tab panels so they can be refreshed.
- The constructor stores the data and user, configures the frame, adds the
  top bar, creates all six tab panels, adds them to a `JTabbedPane`, creates
  the status label, and calls `refreshAll()` to show current data.
- `createTopPanel()` builds the title and logged-in-user labels plus Load,
  Save, Save CSV, and Logout buttons.
- Each button's action listener calls the matching operation. Logout closes
  the main window and opens a new login window with the same data manager.
- `refreshAll()` calls `refresh()` on every tab so all views display current
  data.
- `showStatus(message, isError)` updates the status text and sets red for an
  error or green for a success.
- `getUsername()` returns the active user's username to the tab panels.
- `saveData()` opens a Save dialog with a suggested file name. If the user
  cancels it returns; otherwise it saves that user's ledger, updates the
  status, and shows an error dialog if saving fails.
- `saveCsvData()` follows the same flow for exporting account balances to CSV.
- `loadData()` opens a file chooser, returns on cancel, and asks the user to
  confirm replacement. On approval it loads the file, refreshes all tabs, and
  updates the status. An error is shown if loading fails.

## `src/ui/NewTransactionTab.java`

- The imports provide the data manager, account and transaction models,
  Swing form controls, layout, decimal amounts, and current-date support.
- The class extends `JPanel` and stores references to the data manager,
  main window, date/description/amount inputs, and debit/credit dropdowns.
- The constructor stores the references, uses a grid layout, initializes the
  date to today, creates the fields and dropdowns, adds labels and controls,
  and connects the Add Transaction button to `addTransaction()`.
- `refresh()` clears both dropdowns and fills each with the active user's
  accounts.
- `addTransaction()` reads the form, trims text, removes commas from the
  amount, and gets the selected accounts.
- Its `try` block parses the date and decimal amount. A parse failure displays
  an error status and stops the method.
- The following conditions reject an empty description, identical debit and
  credit account names, and an amount that is zero or negative.
- If valid, it creates a transaction for the active user, adds it to
  `DataStore`, clears description and amount inputs, refreshes all tabs, and
  displays a success status.
- `showError(message)` is a small helper that displays a validation message
  using the main window's status label.

## `src/ui/TransactionsTab.java`

- The imports provide transaction data, Swing table/search controls, layouts,
  and a list.
- Fields hold the data manager, main window, search box, table model/table,
  and a list mapping visible rows back to transaction objects.
- The constructor initializes those fields, creates the search/clear/delete
  controls, builds a five-column table, and places controls and table in the
  panel.
- The Search listener reloads the table using the current query.
- The Clear listener empties the query and reloads all transactions.
- The Delete listener calls `deleteSelected()`.
- `refresh()` clears the visible rows and row mapping, lowercases the query,
  obtains this user's transactions, and iterates newest-to-oldest through the
  date-sorted list.
- For each transaction it builds searchable text from date, description, and
  both account names. If that text contains the query, it adds the transaction
  to the row mapping and adds formatted values to the table.
- `deleteSelected()` checks that a valid row is selected. If not, it shows an
  error status. Otherwise it asks for confirmation; on Yes it removes the
  corresponding transaction, refreshes every tab, and reports success.

## `src/ui/AccountsTab.java`

- The imports provide account data, Swing controls/table models/layouts, and
  decimal amounts.
- Fields store the shared data manager, main window, table model, account-name
  input, type dropdown, and opening-balance input.
- The constructor creates a four-column table, puts it in a scroll pane, and
  builds a form with a name, one of five account types, opening balance, and
  Add Account button.
- The button listener calls `addAccount()`.
- `refresh()` clears the table, loops through the active user's accounts, and
  adds each account's name, type, opening balance, and calculated current
  balance using money formatting.
- `addAccount()` parses the opening balance, asks `DataStore` to add the
  account, clears/reset inputs, refreshes all tabs, and reports success.
- A `NumberFormatException` produces a message about the balance format.
- An `IllegalArgumentException` from account validation is shown as an error
  status.

## `src/ui/JournalTab.java`

- The imports provide the data manager, transaction model, Swing table
  controls, and border layout.
- Fields retain the data manager, main window, and table model.
- The constructor sets the layout, creates a five-column journal table, and
  places it in a scroll pane.
- `refresh()` clears old rows, gets the current user's transactions, and
  formats each amount.
- The first `addRow(...)` writes the date, description, debit account, and
  debit amount.
- The second `addRow(...)` writes the credit account on an indented row and
  puts the same amount in the credit column. Empty cells make it visually
  follow the debit line.

## `src/ui/LedgerTab.java`

- The imports provide accounts and transactions, Swing dropdown/table
  controls, layouts, and `BigDecimal`.
- Fields retain the data manager, main window, account selector, and table
  model.
- The constructor builds a selector at the top and a ledger table below it.
  Changing the selected account calls `refreshLedger()`.
- `refresh()` clears and repopulates the selector with the current user's
  accounts, then refreshes the ledger display.
- `refreshLedger()` clears the table and returns if there is no selected
  account.
- It reads whether the account is debit-normal and starts the running balance
  at the account's opening balance.
- It loops over this user's transactions and checks whether each debits or
  credits the selected account.
- Transactions that affect neither side of the selected account are skipped.
- For a debit match, the amount is added for debit-normal accounts and
  subtracted otherwise. For a credit match, the reverse is applied.
- Each matching transaction is then added to the table with its date,
  description, both accounts, amount, and new running balance.

## `src/ui/BalanceSheetTab.java`

- The imports provide account data, Swing labels/tables/layouts, and decimal
  arithmetic.
- Fields store the data manager, main window, separate table models for assets
  and liabilities/equity, and the balance-status label.
- The constructor creates two side-by-side tables and a label beneath them.
- `refresh()` clears the two tables and initializes total assets and total
  liabilities/equity to zero.
- It loops through the current user's accounts and calculates each balance.
- A zero-balance account is skipped.
- An asset is added to the asset table and asset total.
- A liability or equity account is added to the other table and its total.
- The method calculates net income. If nonzero, it adds net income to the
  liabilities/equity display and total.
- It appends total rows to both tables.
- It compares the totals numerically with `compareTo()`: equal totals show
  "Balanced"; unequal totals show "Not balanced".

## How the parts work together

1. `Main` creates one `DataStore` and opens `LoginWindow`.
2. `LoginWindow` authenticates or creates a user through `DataStore`.
3. `MainWindow` creates the feature tabs and passes each a reference to the
   shared data and active session.
4. Tabs read or change data through `DataStore`; `refreshAll()` updates the
   displayed tables after changes.
5. `DataStore` keeps accounts and transactions in memory and delegates file
   reading/writing to `UserDataFileStore`. User registrations are handled by
   `UserRepository`.

## Important project limitations

- Accounts and transactions are in memory until the user saves them to a file.
- User records are stored in plain text, including passwords; this is suitable
  only for a simple class project, not for real credentials.
- This guide describes the current code. It does not change or fix the
  implementation.
