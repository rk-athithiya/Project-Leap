package BankingApplication;

import java.util.HashMap;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BankingApp {

    private static HashMap<String, BankAccount> accounts = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n--- Banking Application ---");
            System.out.println("1. Create Account");
            System.out.println("2. View Account Details");
            System.out.println("3. Update Account Information");
            System.out.println("4. Deposit");
            System.out.println("5. Withdraw");
            System.out.println("6. Transfer");
            System.out.println("7. View Transaction History");
            System.out.println("8. Generate Reports");
            System.out.println("9. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    viewAccountDetails();
                    break;
                case 3:
                    updateAccountInfo();
                    break;
                case 4:
                    deposit();
                    break;
                case 5:
                    withdraw();
                    break;
                case 6:
                    transfer();
                    break;
                case 7:
                    viewTransactionHistory();
                    break;
                case 8:
                	generateReports();
                    break;
                case 9:
                    System.out.println("Exiting the application. Goodbye!");
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private static void createAccount() {
        String accountNumber;
        while (true) {
            System.out.print("Enter Account Number (8-16 digits): ");
            accountNumber = scanner.nextLine().trim();

            if (accounts.containsKey(accountNumber)) {
                System.out.println("Error: Account with this number already exists.");
                continue;
            }

            if (!isValidAccountNumber(accountNumber)) {
                System.out.println("Error: Account number must be between 8 and 16 digits.");
            } else {
                break;
            }
        }

        String accountHolderName;
        while (true) {
            System.out.print("Enter Account Holder Name: ");
            accountHolderName = scanner.nextLine().trim();
            if (!accountHolderName.isEmpty()) {
                break;
            }
            System.out.println("Error: Account holder name cannot be empty.");
        }

        String accountType;
        while (true) {
            System.out.print("Enter Account Type (S for Savings / C for Current): ");
            String accountTypeInput = scanner.nextLine().trim().toLowerCase();
            if (accountTypeInput.equals("s")) {
                accountType = "Savings";
                break;
            } else if (accountTypeInput.equals("c")) {
                accountType = "Current";
                break;
            }
            System.out.println("Error: Invalid input. Please enter 'S' for Savings or 'C' for Current.");
        }

        String address;
        while (true) {
            System.out.print("Enter Address: ");
            address = scanner.nextLine().trim();
            if (address.isEmpty()) {
                System.out.println("Error: Address cannot be empty.");
            } else {
                break;
            }
        }

        String contactNumber;
        while (true) {
            System.out.print("Enter Contact Number (10 digits): ");
            contactNumber = scanner.nextLine().trim();

            if (contactNumber.isEmpty()) {
                System.out.println("Error: Contact number cannot be empty.");
            } else if (!isValidContactNumber(contactNumber)) {
                System.out.println("Error: Invalid contact number. It must be a 10-digit number.");
            } else {
                break;
            }
        }

        try {
            BankAccount newAccount = new BankAccount(accountNumber, accountHolderName, accountType, address, contactNumber);

            accounts.put(accountNumber, newAccount);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO accounts (accountNumber, accountHolderName, accountType, balance, address, contactNumber) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newAccount.getAccountNumber());
                stmt.setString(2, newAccount.getAccountHolderName());
                stmt.setString(3, newAccount.getAccountType());
                stmt.setDouble(4, newAccount.getBalance());
                stmt.setString(5, newAccount.getAddress());
                stmt.setString(6, newAccount.getContactNumber());

                int rowsInserted = stmt.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("Account created successfully and saved to database!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
    // Validation methods
    private static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber.matches("\\d{8,16}");
    }

    private static boolean isValidContactNumber(String contactNumber) {
        return contactNumber.matches("\\d{10}");
    }

    // View account details
    private static void viewAccountDetails() {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = accounts.get(accountNumber);

        if (account != null) {
            account.displayAccountInfo();
        } else {
            System.out.println("Account not found.");
        }
    }
    private static void updateAccountInfo() {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = accounts.get(accountNumber);

        if (account != null) {
            System.out.print("Enter new Address: ");
            String address = scanner.nextLine();

            System.out.print("Enter new Contact Number: ");
            String contactNumber = scanner.nextLine();

            account.setAddress(address);
            account.setContactNumber(contactNumber);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE accounts SET address = ?, contactNumber = ? WHERE accountNumber = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, address);
                stmt.setString(2, contactNumber);
                stmt.setString(3, accountNumber);

                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Account information updated successfully.");
                } else {
                    System.out.println("Error: Account update failed.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Account not found.");
        }
    }
    private static void deposit() {
        System.out.println("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = accounts.get(accountNumber);

        if (account != null) {
            System.out.println("Enter amount to deposit: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // Clear the newline

            account.deposit(amount);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE accounts SET balance = ? WHERE accountNumber = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setDouble(1, account.getBalance());
                stmt.setString(2, account.getAccountNumber());
                stmt.executeUpdate();

                // Insert transaction record
                sql = "INSERT INTO transactions (accountNumber, type, amount) VALUES (?, 'Deposit', ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, accountNumber);
                stmt.setDouble(2, amount);
                stmt.executeUpdate();

                System.out.println("Deposit successful and recorded in database.");
                System.out.println("Current Balance: " + account.getBalance());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Account not found.");
        }
    }
    private static void withdraw() {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = accounts.get(accountNumber);

        if (account != null) {
            System.out.print("Enter amount to withdraw: ");
            double amount = scanner.nextDouble();

            // Check if withdrawal is successful in the BankAccount class
            if (amount > 0 && amount <= account.getBalance()) {
                account.withdraw(amount);

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "UPDATE accounts SET balance = ? WHERE accountNumber = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setDouble(1, account.getBalance());
                    stmt.setString(2, account.getAccountNumber());
                    stmt.executeUpdate();

                    // Insert transaction record
                    sql = "INSERT INTO transactions (accountNumber, type, amount) VALUES (?, 'Withdrawal', ?)";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, accountNumber);
                    stmt.setDouble(2, amount);
                    stmt.executeUpdate();
                    System.out.println("Withdrawal successful and recorded in database.");
                    System.out.println("Current Balance: " + account.getBalance());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Insufficient balance or invalid withdrawal amount.");
            }
        } else {
            System.out.println("Account not found.");
        }
    }
    private static void transfer() {
    	System.out.print("Enter your Account Number: ");
        String fromAccountNumber = scanner.nextLine();
        
        System.out.print("Enter recipient Account Number: ");
        String toAccountNumber = scanner.nextLine();
        
        System.out.print("Enter amount to transfer: ");
        double amount = scanner.nextDouble();
        
        BankAccount fromAccount = accounts.get(fromAccountNumber);
        BankAccount toAccount = accounts.get(toAccountNumber);
        
        if (fromAccount != null && toAccount != null) {
            if (amount > 0 && amount <= fromAccount.getBalance()) {
                fromAccount.withdraw(amount);
                toAccount.deposit(amount);
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    String sql = "UPDATE accounts SET balance = ? WHERE accountNumber = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setDouble(1, fromAccount.getBalance());
                    stmt.setString(2, fromAccount.getAccountNumber());
                    stmt.executeUpdate();
                    
                    stmt.setDouble(1, toAccount.getBalance());
                    stmt.setString(2, toAccount.getAccountNumber());
                    stmt.executeUpdate();
                    
                    sql = "INSERT INTO transactions(account_number, accountType, amount, description) VALUES (?, 'Transfer Out', ?, ?)";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, fromAccountNumber);
                    stmt.setDouble(2, amount);
                    stmt.setString(3, "Transferred to " + toAccountNumber);
                    stmt.executeUpdate();
                    
                    sql = "INSERT INTO transactions(account_number, accountType, amount, description) VALUES (?, 'Transfer In', ?, ?)";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, toAccountNumber);
                    stmt.setDouble(2, amount);
                    stmt.setString(3, "Received from " + fromAccountNumber);
                    stmt.executeUpdate();
                    
                    conn.commit();
                    System.out.println("Amount transferred successfully and recorded in the database.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Transfer failed. Insufficient balance or invalid amount.");
            }
        } else {
            System.out.println("One or both the accounts cannot be found.");
        }
    }
    private static void viewTransactionHistory() {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        
        BankAccount account = accounts.get(accountNumber);
        
        if (account != null) {
            account.displayTransactionHistory();
        } else {
            System.out.println("Account not found.");
        }
    }
    private static void generateReports() {
        System.out.println("\n--- Report Generation ---");
        System.out.println("1. Customer Details");
        System.out.println("2. Transaction History");
        System.out.println("3. Total Balance");
        System.out.println("4. Number of Accounts by Type");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Clear the buffer

        switch (choice) {
            case 1:
                generateCustomerDetailsReport();
                break;

            case 2:
                System.out.print("Enter Account Number: ");
                String accountNumber = scanner.nextLine();
                BankAccount account = accounts.get(accountNumber);

                if (account != null) {
                    account.displayTransactionHistory();
                } else {
                    System.out.println("Account not found.");
                }
                break;

            case 3:
                generateTotalBalanceReport();
                break;

            case 4:
                generateAccountTypeReport();
                break;

            default:
                System.out.println("Invalid choice. Please choose a valid report option.");
                break;
        }
    }
    
    private static void generateCustomerDetailsReport() {
        System.out.println("\nCustomer Details Report:");
        for (BankAccount account : accounts.values()) {
            account.displayAccountInfo();
            System.out.println("-----");
        }
    }

    private static void generateTotalBalanceReport() {
        double totalBalance = accounts.values().stream().mapToDouble(BankAccount::getBalance).sum();
        System.out.println("Total balance across all accounts: $" + totalBalance);
    }

    public static void generateAccountTypeReport() {
        String query = "SELECT accountType, COUNT(*) FROM accounts GROUP BY accountType";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            int savingsCount = 0;
            int currentCount = 0;

            // Process the result set
            while (rs.next()) {
                String accountType = rs.getString("accountType");
                int count = rs.getInt(2);  // The count of each account type

                if ("S".equals(accountType)) {
                    savingsCount = count;
                } else if ("C".equals(accountType)) {
                    currentCount = count;
                }
            }

            // Print the result
            System.out.println("Number of Savings Accounts: " + savingsCount);
            System.out.println("Number of Current Accounts: " + currentCount);

        } catch (SQLException e) {
            e.printStackTrace();																												
        }
    }
   
}
