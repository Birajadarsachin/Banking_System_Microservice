package com.banking.account_service.controller;

import com.banking.account_service.model.Account;
import com.banking.account_service.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    // ------------------ CREATE ACCOUNT ------------------
    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        logger.info("Received CREATE ACCOUNT request: accountNumber={}, holderName={}",
                account.getAccountNumber(), account.getHolderName());

        Account saved = accountService.createAccount(account);

        logger.info("ACCOUNT CREATED successfully: accountNumber={}", saved.getAccountNumber());
        return saved;
    }

    // ------------------ GET ACCOUNT ------------------
    @GetMapping("/{accountNumber}")
    public Optional<Account> getAccount(@PathVariable String accountNumber) {

        logger.info("Received GET ACCOUNT request: accountNumber={}", accountNumber);

        Optional<Account> account = accountService.getAccount(accountNumber);

        if (account.isPresent()) {
            logger.info("ACCOUNT FOUND: accountNumber={}, balance={}",
                    account.get().getAccountNumber(), account.get().getBalance());
        } else {
            logger.warn("ACCOUNT NOT FOUND: accountNumber={}", accountNumber);
        }

        return account;
    }

    // ------------------ UPDATE BALANCE ------------------
    @PutMapping("/{accountNumber}/balance")
    public String updateBalance(@PathVariable String accountNumber,
                                @RequestParam double newBalance) {

        logger.info("Received BALANCE UPDATE request: accountNumber={}, newBalance={}",
                accountNumber, newBalance);

        accountService.updateBalance(accountNumber, newBalance);

        logger.info("BALANCE UPDATED successfully: accountNumber={}, newBalance={}",
                accountNumber, newBalance);

        return "Balance updated";
    }

    // ------------------ UPDATE STATUS ------------------
    @PutMapping("/{accountNumber}/status")
    public String updateStatus(@PathVariable String accountNumber,
                               @RequestParam String status) {

        logger.info("Received STATUS UPDATE request: accountNumber={}, newStatus={}",
                accountNumber, status);

        accountService.updateStatus(accountNumber, status);

        logger.info("STATUS UPDATED successfully: accountNumber={}, newStatus={}",
                accountNumber, status);

        return "Status updated";
    }
}
