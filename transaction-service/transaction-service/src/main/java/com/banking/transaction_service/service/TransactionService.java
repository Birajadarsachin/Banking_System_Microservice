package com.banking.transaction_service.service;

import com.banking.transaction_service.client.NotificationClient;
import com.banking.transaction_service.model.Transaction;
import com.banking.transaction_service.repository.TransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NotificationClient notificationClient;

    private final String ACCOUNT_SERVICE_URL = "http://ACCOUNT-SERVICE/api/accounts";

    // ------------------ Utility: Generate Transaction ID ------------------
    private String generateTransactionId() {
        long time = System.currentTimeMillis();
        int randomNum = new Random().nextInt(999);
        return "TXN-" + time + "-" + randomNum;
    }

    // ------------------ DEPOSIT ------------------
    public Transaction deposit(String accountNumber, double amount) {

        logger.info("Starting DEPOSIT: account={}, amount={}", accountNumber, amount);

        Transaction txn = createBaseTransaction("DEPOSIT", amount, accountNumber, null);

        try {
            logger.info("Fetching current balance for account={}", accountNumber);
            Double currentBalance = getBalanceFromAccountService(accountNumber);

            double updatedBalance = currentBalance + amount;

            logger.info("Updating balance for account={} newBalance={}", accountNumber, updatedBalance);
            updateBalanceInAccountService(accountNumber, updatedBalance);

            txn.setStatus("SUCCESS");
            transactionRepository.save(txn);

            logger.info("DEPOSIT SUCCESS: account={}, amount={}, txnId={}", accountNumber, amount, txn.getTransactionId());

            notificationClient.sendNotification(
                    "Deposit of " + amount + " to account " + accountNumber + " was successful."
            );

        } catch (Exception e) {

            logger.error("DEPOSIT FAILED: account={}, amount={}, error={}", accountNumber, amount, e.getMessage());
            txn.setStatus("FAILED");
            transactionRepository.save(txn);
        }

        return txn;
    }

    // ------------------ WITHDRAW ------------------
    public Transaction withdraw(String accountNumber, double amount) {

        logger.info("Starting WITHDRAW: account={}, amount={}", accountNumber, amount);

        Transaction txn = createBaseTransaction("WITHDRAW", amount, accountNumber, null);

        try {
            logger.info("Fetching current balance for account={}", accountNumber);
            Double currentBalance = getBalanceFromAccountService(accountNumber);

            if (currentBalance < amount) {
                logger.warn("WITHDRAW FAILED - insufficient funds: account={}, balance={}, amount={}",
                        accountNumber, currentBalance, amount);

                txn.setStatus("FAILED");
                return transactionRepository.save(txn);
            }

            logger.info("Updating balance for account={} newBalance={}", accountNumber, currentBalance - amount);
            updateBalanceInAccountService(accountNumber, currentBalance - amount);

            txn.setStatus("SUCCESS");
            transactionRepository.save(txn);

            logger.info("WITHDRAW SUCCESS: account={}, amount={}, txnId={}",
                    accountNumber, amount, txn.getTransactionId());

            notificationClient.sendNotification(
                    "Withdrawal of " + amount + " from account " + accountNumber + " was successful."
            );

        } catch (Exception e) {

            logger.error("WITHDRAW FAILED: account={}, amount={}, error={}", accountNumber, amount, e.getMessage());
            txn.setStatus("FAILED");
            transactionRepository.save(txn);
        }

        return txn;
    }

    // ------------------ TRANSFER ------------------
    public Transaction transfer(String sourceAcc, String destAcc, double amount) {

        logger.info("Starting TRANSFER: from={}, to={}, amount={}", sourceAcc, destAcc, amount);

        Transaction txn = createBaseTransaction("TRANSFER", amount, sourceAcc, destAcc);

        try {
            logger.info("Fetching balance for sourceAcc={}", sourceAcc);
            Double sourceBalance = getBalanceFromAccountService(sourceAcc);

            if (sourceBalance < amount) {
                logger.warn("TRANSFER FAILED - insufficient funds: from={}, balance={}, amount={}",
                        sourceAcc, sourceBalance, amount);

                txn.setStatus("FAILED");
                return transactionRepository.save(txn);
            }

            logger.info("Debiting sourceAcc={} newBalance={}", sourceAcc, sourceBalance - amount);
            updateBalanceInAccountService(sourceAcc, sourceBalance - amount);

            logger.info("Fetching balance for destAcc={}", destAcc);
            Double destBalance = getBalanceFromAccountService(destAcc);

            logger.info("Crediting destAcc={} newBalance={}", destAcc, destBalance + amount);
            updateBalanceInAccountService(destAcc, destBalance + amount);

            txn.setStatus("SUCCESS");
            transactionRepository.save(txn);

            logger.info("TRANSFER SUCCESS: from={}, to={}, amount={}, txnId={}",
                    sourceAcc, destAcc, amount, txn.getTransactionId());

            notificationClient.sendNotification(
                    "Transfer of " + amount + " from " + sourceAcc + " to " + destAcc + " was successful."
            );

        } catch (Exception e) {

            logger.error("TRANSFER FAILED: from={}, to={}, amount={}, error={}",
                    sourceAcc, destAcc, amount, e.getMessage());

            txn.setStatus("FAILED");
            transactionRepository.save(txn);
        }

        return txn;
    }

    // ------------------ HELPER: CREATE BASE TXN ------------------
    private Transaction createBaseTransaction(String type, double amount, String src, String dest) {
        Transaction txn = new Transaction();
        txn.setTransactionId(generateTransactionId());
        txn.setType(type);
        txn.setAmount(amount);
        txn.setTimestamp(new Date());
        txn.setSourceAccount(src);
        txn.setDestinationAccount(dest);
        return txn;
    }


    // ===================================================================
    // CIRCUIT BREAKER METHODS
    // ===================================================================

    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackGetBalance")
    private Double getBalanceFromAccountService(String accNo) {
        logger.info("Calling ACCOUNT-SERVICE for getBalance: account={}", accNo);

        String url = ACCOUNT_SERVICE_URL + "/" + accNo;
        Map response = restTemplate.getForObject(url, Map.class);

        logger.info("ACCOUNT-SERVICE responded with balance={} for account={}",
                response.get("balance"), accNo);

        return Double.valueOf(response.get("balance").toString());
    }

    private Double fallbackGetBalance(String accNo, Throwable t) {
        logger.warn("Fallback triggered for getBalance (ACCOUNT-SERVICE DOWN): account={}, error={}",
                accNo, t.getMessage());
        return 0.0;
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackUpdateBalance")
    private void updateBalanceInAccountService(String accNo, double newBalance) {
        logger.info("Calling ACCOUNT-SERVICE to update balance: account={}, newBalance={}", accNo, newBalance);

        String url = ACCOUNT_SERVICE_URL + "/" + accNo + "/balance?newBalance=" + newBalance;
        restTemplate.put(url, null);

        logger.info("ACCOUNT-SERVICE balance update completed: account={}, newBalance={}", accNo, newBalance);
    }

    private void fallbackUpdateBalance(String accNo, double newBalance, Throwable t) {
        logger.warn("Fallback triggered for updateBalance (ACCOUNT-SERVICE DOWN): account={}, newBalance={}, error={}",
                accNo, newBalance, t.getMessage());
    }

    // ------------------ GET ALL TXNS FOR ACCOUNT ------------------
    public List<Transaction> getTransactionsForAccount(String accountNumber) {

        logger.info("Fetching ALL transactions for account={}", accountNumber);

        List<Transaction> outgoing = transactionRepository.findBySourceAccount(accountNumber);
        List<Transaction> incoming = transactionRepository.findByDestinationAccount(accountNumber);

        outgoing.addAll(incoming);

        logger.info("Found {} transactions for account={}", outgoing.size(), accountNumber);

        return outgoing;
    }
}
