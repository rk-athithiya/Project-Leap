CREATE DATABASE banking_app;
USE banking_app;
CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    accountNumber VARCHAR(20) UNIQUE,
    accountHolderName VARCHAR(100),
    accountType VARCHAR(10),
    balance DOUBLE,
    address VARCHAR(255),
    contactNumber VARCHAR(20)
);
CREATE TABLE transactions (
    transactionID INT AUTO_INCREMENT PRIMARY KEY,
    accountNumber VARCHAR(20),
    type VARCHAR(20),
    amount DOUBLE,
    description VARCHAR(255),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (accountNumber) REFERENCES accounts(accountNumber)
);