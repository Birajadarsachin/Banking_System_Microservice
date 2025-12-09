
package com.banking.transaction_service;

import com.banking.transaction_service.client.NotificationClient;
import com.banking.transaction_service.model.Transaction;
import com.banking.transaction_service.repository.TransactionRepository;
import com.banking.transaction_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Fix randomly generated IDs for test stability
        Mockito.doReturn("TXN-TEST-ID").when(transactionService).toString();
    }


    // =====================================================================================
    // DEPOSIT TESTS
    // =====================================================================================

    @Test
    void testDepositSuccess() {
        String accountNumber = "ACC1001";
        double startingBalance = 5000.0;
        double depositAmount = 1000.0;

        Map<String, Object> accountResponse = new HashMap<>();
        accountResponse.put("balance", startingBalance);

        // Mock getBalanceFromAccountService()
        when(restTemplate.getForObject("http://ACCOUNT-SERVICE/api/accounts/" + accountNumber, Map.class))
                .thenReturn(accountResponse);

        // Mock repository save()
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.deposit(accountNumber, depositAmount);

        assertEquals("SUCCESS", result.getStatus());
        verify(notificationClient, times(1)).sendNotification(anyString());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }


    // =====================================================================================
    // WITHDRAW TESTS
    // =====================================================================================

    @Test
    void testWithdrawSuccess() {

        String accountNumber = "ACC1001";
        double startingBalance = 5000.0;
        double withdrawAmount = 1000.0;

        Map<String, Object> accountResponse = new HashMap<>();
        accountResponse.put("balance", startingBalance);

        when(restTemplate.getForObject("http://ACCOUNT-SERVICE/api/accounts/" + accountNumber, Map.class))
                .thenReturn(accountResponse);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.withdraw(accountNumber, withdrawAmount);

        assertEquals("SUCCESS", result.getStatus());
        verify(notificationClient, times(1)).sendNotification(anyString());
    }


    @Test
    void testWithdrawInsufficientFunds() {

        String accountNumber = "ACC1001";
        double startingBalance = 300.0;
        double withdrawAmount = 1000.0;

        Map<String, Object> accountResponse = new HashMap<>();
        accountResponse.put("balance", startingBalance);

        when(restTemplate.getForObject("http://ACCOUNT-SERVICE/api/accounts/" + accountNumber, Map.class))
                .thenReturn(accountResponse);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        Transaction result = transactionService.withdraw(accountNumber, withdrawAmount);

        assertEquals("FAILED", result.getStatus());
        verify(notificationClient, times(0)).sendNotification(anyString());
    }


    // =====================================================================================
    // TRANSFER TESTS
    // =====================================================================================

    @Test
    void testTransferSuccess() {

        String sourceAccount = "ACC1001";
        String destAccount = "ACC2002";

        double sourceBalance = 7000.0;
        double destBalance = 2000.0;
        double transferAmount = 1000.0;

        // First call = source account balance
        Map<String, Object> srcResponse = new HashMap<>();
        srcResponse.put("balance", sourceBalance);

        // Second call = dest account balance
        Map<String, Object> destResponse = new HashMap<>();
        destResponse.put("balance", destBalance);

        when(restTemplate.getForObject("http://ACCOUNT-SERVICE/api/accounts/" + sourceAccount, Map.class))
                .thenReturn(srcResponse);
        when(restTemplate.getForObject("http://ACCOUNT-SERVICE/api/accounts/" + destAccount, Map.class))
                .thenReturn(destResponse);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        Transaction result = transactionService.transfer(sourceAccount, destAccount, transferAmount);

        assertEquals("SUCCESS", result.getStatus());
        verify(notificationClient, times(1)).sendNotification(anyString());
    }


    @Test
    void testTransferInsufficientFunds() {

        String sourceAccount = "ACC1001";
        String destAccount = "ACC2002";

        double sourceBalance = 500.0;
        double transferAmount = 2000.0;

        Map<String, Object> srcResponse = new HashMap<>();
        srcResponse.put("balance", sourceBalance);

        when(restTemplate.getForObject("http://ACCOUNT-SERVICE/api/accounts/" + sourceAccount, Map.class))
                .thenReturn(srcResponse);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        Transaction result = transactionService.transfer(sourceAccount, destAccount, transferAmount);

        assertEquals("FAILED", result.getStatus());
        verify(notificationClient, times(0)).sendNotification(anyString());
    }


    // =====================================================================================
    // GET TRANSACTIONS TEST
    // =====================================================================================

    @Test
    void testGetTransactions() {

        String account = "ACC1001";

        List<Transaction> outgoing = Arrays.asList(new Transaction(), new Transaction());
        List<Transaction> incoming = Collections.singletonList(new Transaction());

        when(transactionRepository.findBySourceAccount(account)).thenReturn(outgoing);
        when(transactionRepository.findByDestinationAccount(account)).thenReturn(incoming);

        List<Transaction> result = transactionService.getTransactionsForAccount(account);

        assertEquals(3, result.size());
    }
}
