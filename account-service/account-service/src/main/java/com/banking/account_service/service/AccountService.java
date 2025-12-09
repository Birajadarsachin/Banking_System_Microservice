package com.banking.account_service.service;

import com.banking.account_service.model.Account;
import com.banking.account_service.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    // ------------------ CREATE ACCOUNT ------------------
    public Account createAccount(Account account) {

        logger.info("Creating new account: accountNumber={}, holderName={}",
                account.getAccountNumber(), account.getHolderName());

        account.setCreatedAt(new Date());
        account.setStatus("ACTIVE");

        Account saved = accountRepository.save(account);

        logger.info("Account created successfully: accountNumber={}", saved.getAccountNumber());
        return saved;
    }

    // ------------------ GET ACCOUNT ------------------
    public Optional<Account> getAccount(String accountNumber) {

        logger.info("Fetching account: accountNumber={}", accountNumber);

        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);

        if (account.isPresent()) {
            logger.info("Account fetched successfully: accountNumber={}, balance={}, status={}",
                    account.get().getAccountNumber(),
                    account.get().getBalance(),
                    account.get().getStatus());
        } else {
            logger.warn("Account not found: accountNumber={}", accountNumber);
        }

        return account;
    }

    // ------------------ UPDATE BALANCE ------------------
    public void updateBalance(String accountNumber, double newBalance) {

        logger.info("Updating balance: accountNumber={}, newBalance={}", accountNumber, newBalance);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    logger.error("Cannot update balance — account not found: accountNumber={}", accountNumber);
                    return new RuntimeException("Account not found");
                });

        account.setBalance(newBalance);
        accountRepository.save(account);

        logger.info("Balance updated successfully: accountNumber={}, newBalance={}", accountNumber, newBalance);
    }

    // ------------------ UPDATE STATUS ------------------
    public void updateStatus(String accountNumber, String status) {

        logger.info("Updating status: accountNumber={}, newStatus={}", accountNumber, status);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    logger.error("Cannot update status — account not found: accountNumber={}", accountNumber);
                    return new RuntimeException("Account not found");
                });

        account.setStatus(status);
        accountRepository.save(account);

        logger.info("Status updated successfully: accountNumber={}, newStatus={}", accountNumber, status);
    }
}
