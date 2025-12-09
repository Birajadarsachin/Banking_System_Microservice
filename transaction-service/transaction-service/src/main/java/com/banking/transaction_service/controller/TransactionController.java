package com.banking.transaction_service.controller;

import com.banking.transaction_service.model.Transaction;
import com.banking.transaction_service.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    // ------------------ DEPOSIT ------------------
    @PostMapping("/deposit")
    public Transaction deposit(@RequestParam String accountNumber,
                               @RequestParam double amount) {

        logger.info("Received DEPOSIT request: accountNumber={}, amount={}", accountNumber, amount);

        Transaction txn = transactionService.deposit(accountNumber, amount);

        logger.info("DEPOSIT completed: accountNumber={}, amount={}, status={}, txnId={}",
                accountNumber, amount, txn.getStatus(), txn.getTransactionId());

        return txn;
    }

    // ------------------ WITHDRAW ------------------
    @PostMapping("/withdraw")
    public Transaction withdraw(@RequestParam String accountNumber,
                                @RequestParam double amount) {

        logger.info("Received WITHDRAW request: accountNumber={}, amount={}", accountNumber, amount);

        Transaction txn = transactionService.withdraw(accountNumber, amount);

        logger.info("WITHDRAW completed: accountNumber={}, amount={}, status={}, txnId={}",
                accountNumber, amount, txn.getStatus(), txn.getTransactionId());

        return txn;
    }

    // ------------------ TRANSFER ------------------
    @PostMapping("/transfer")
    public Transaction transfer(@RequestParam String sourceAccount,
                                @RequestParam String destinationAccount,
                                @RequestParam double amount) {

        logger.info("Received TRANSFER request: sourceAccount={}, destinationAccount={}, amount={}",
                sourceAccount, destinationAccount, amount);

        Transaction txn = transactionService.transfer(sourceAccount, destinationAccount, amount);

        logger.info("TRANSFER completed: sourceAccount={}, destinationAccount={}, amount={}, status={}, txnId={}",
                sourceAccount, destinationAccount, amount, txn.getStatus(), txn.getTransactionId());

        return txn;
    }

    // ------------------ GET ALL TRANSACTIONS OF AN ACCOUNT ------------------
    @GetMapping("/account/{accountNumber}")
    public List<Transaction> getTransactions(@PathVariable String accountNumber) {

        logger.info("Fetching transactions for accountNumber={}", accountNumber);

        List<Transaction> txns = transactionService.getTransactionsForAccount(accountNumber);

        logger.info("Fetched {} transactions for accountNumber={}", txns.size(), accountNumber);

        return txns;
    }
}
