package com.gomspace.fundAPI.service;

import com.gomspace.fundAPI.entity.Account;
import com.gomspace.fundAPI.exception.AccountException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class created to ensure the logic of AccountService CRUD methods
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Test
    void findAll() {
        int currentSize = accountService.findAll().size();
        Account tobeSaved = new Account(11L,"CHF", BigDecimal.valueOf(500));
        accountService.save(tobeSaved);
        assertEquals(accountService.findAll().size(),currentSize + 1);
    }

    @Test
    void findAccount() throws AccountException {
        Account tobeSaved = new Account(66L,"CHF", BigDecimal.valueOf(500));
        Long tobeSaved_id = accountService.save(tobeSaved).getId();

        Assertions.assertDoesNotThrow(() -> accountService.findAccount(tobeSaved_id));
        accountService.delete(tobeSaved_id);
        assertThrows(AccountException.class, () -> accountService.findAccount(666L),"should have thrown an error id 666 not exist");
    }

    @Test
    void save() throws AccountException {
        Account tobeSaved = new Account(66L,"CHF", BigDecimal.valueOf(500));
        Long tobeSaved_id = accountService.save(tobeSaved).getId();
        Assertions.assertDoesNotThrow(()->accountService.findAccount(tobeSaved_id),"Account 66 should be saved");
        accountService.delete(tobeSaved_id);
    }

    @Test
    void createAccount() throws AccountException {
        Account tobeCreated = new Account();
        assertThrows(AccountException.class, ()->accountService.createAccount(tobeCreated),"no owner specified");

        tobeCreated.setOwner(-1L);
        assertThrows(AccountException.class, ()->accountService.createAccount(tobeCreated),"owner not valid");

        tobeCreated.setOwner(1L);
        assertThrows(AccountException.class, ()-> accountService.createAccount(tobeCreated),"no balance");

        tobeCreated.setBalance(BigDecimal.valueOf(-5));
        assertThrows(AccountException.class, ()-> accountService.createAccount(tobeCreated),"balance not valid");

        tobeCreated.setBalance(BigDecimal.valueOf(500));
        assertThrows(AccountException.class, ()-> accountService.createAccount(tobeCreated),"no currency specified");

        tobeCreated.setCurrency("ABC");
        assertThrows(AccountException.class, ()-> accountService.createAccount(tobeCreated),"currency not valid");

        tobeCreated.setCurrency("EUR");
        Account created = accountService.createAccount(tobeCreated);

        assertEquals(tobeCreated.getCurrency(),created.getCurrency());
        assertEquals(tobeCreated.getBalance(),created.getBalance());
        assertEquals(tobeCreated.getOwner(),created.getOwner());
        assertNotNull(created.getId());

        accountService.delete(created.getId());
    }

    @Test
    void delete() {
        Account tobeDeleted = new Account(99L,"CHF", BigDecimal.valueOf(500));
        Long tobeDeleted_id = accountService.save(tobeDeleted).getId();
        Assertions.assertDoesNotThrow(()->accountService.findAccount(tobeDeleted_id));
        assertDoesNotThrow(()->accountService.delete(tobeDeleted_id));
        assertThrows(AccountException.class,()->accountService.findAccount(tobeDeleted_id), "should throw an exception, account was deleted");
    }
}