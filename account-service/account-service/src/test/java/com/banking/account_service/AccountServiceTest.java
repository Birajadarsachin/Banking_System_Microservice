package com.banking.account_service;

import com.banking.account_service.model.Account;
import com.banking.account_service.repository.AccountRepository;
import com.banking.account_service.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------------ TEST: Create Account ------------------
    @Test
    void testCreateAccount() {

        Account account = new Account();
        account.setAccountNumber("ACC1001");
        account.setHolderName("Sachin");
        account.setBalance(5000.0);

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account saved = accountService.createAccount(account);

        assertNotNull(saved);
        verify(accountRepository, times(1)).save(account);
    }

    // ------------------ TEST: Get Account (exists) ------------------
    @Test
    void testGetAccountExists() {

        Account account = new Account();
        account.setAccountNumber("ACC1001");

        when(accountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        Optional<Account> result = accountService.getAccount("ACC1001");

        assertTrue(result.isPresent());
        assertEquals("ACC1001", result.get().getAccountNumber());
    }

    // ------------------ TEST: Get Account (not found) ------------------
    @Test
    void testGetAccountNotFound() {

        when(accountRepository.findByAccountNumber("ACC9999"))
                .thenReturn(Optional.empty());

        Optional<Account> result = accountService.getAccount("ACC9999");

        assertFalse(result.isPresent());
    }

    // ------------------ TEST: Update Balance ------------------
    @Test
    void testUpdateBalance() {

        Account account = new Account();
        account.setAccountNumber("ACC1001");
        account.setBalance(5000.0);

        when(accountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        accountService.updateBalance("ACC1001", 7000.0);

        assertEquals(7000.0, account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    // ------------------ TEST: Update Balance - Account Not Found ------------------
    @Test
    void testUpdateBalanceAccountNotFound() {

        when(accountRepository.findByAccountNumber("ACC1111"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            accountService.updateBalance("ACC1111", 5000.0);
        });
    }

    // ------------------ TEST: Update Status ------------------
    @Test
    void testUpdateStatus() {

        Account account = new Account();
        account.setAccountNumber("ACC1001");
        account.setStatus("ACTIVE");

        when(accountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        accountService.updateStatus("ACC1001", "INACTIVE");

        assertEquals("INACTIVE", account.getStatus());
        verify(accountRepository, times(1)).save(account);
    }

    // ------------------ TEST: Update Status - Account Not Found ------------------
    @Test
    void testUpdateStatusAccountNotFound() {

        when(accountRepository.findByAccountNumber("ACC2222"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            accountService.updateStatus("ACC2222", "INACTIVE");
        });
    }
}
