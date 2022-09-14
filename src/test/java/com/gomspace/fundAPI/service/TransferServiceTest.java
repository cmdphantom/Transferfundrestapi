package com.gomspace.fundAPI.service;

import com.gomspace.fundAPI.entity.Account;
import com.gomspace.fundAPI.entity.Transfer;
import com.gomspace.fundAPI.exception.AccountException;
import com.gomspace.fundAPI.exception.TransferException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountService accountService;

    /**
     * Ensure the correct rate is provided by Testing the call to exchange api.
     * @throws TransferException
     */
    @Test
    void getXRate() throws TransferException {
        Account accountA = new Account(11L,"USD", BigDecimal.valueOf(500));
        Account accountB = new Account(22L,"USD", BigDecimal.valueOf(500));
        Account accountC = new Account(33L,"EUR", BigDecimal.valueOf(500));

       BigDecimal sameCurrencyRate =  transferService.getXRate(accountA.getCurrency(),accountB.getCurrency());
      assertEquals(sameCurrencyRate.intValue(),1);

        BigDecimal diffCurrencyRate =  transferService.getXRate(accountA.getCurrency(),accountC.getCurrency());
        assertTrue(diffCurrencyRate.compareTo(BigDecimal.valueOf(1))<0);
    }

    /**
     * Testing transferMoney logic with different input
     * ensure Exception is thrown in case of wrong input.
     * @throws AccountException
     * @throws TransferException
     */
    @Test
    void transferMoney() throws AccountException, TransferException {
        Account accountA = new Account(11L,"USD", BigDecimal.valueOf(500));
        Account accountB = new Account(22L,"USD", BigDecimal.valueOf(500));
        Account accountC = new Account(33L,"EUR", BigDecimal.valueOf(500));
        accountService.save(accountA);
        accountService.save(accountB);
        accountService.save(accountC);

        Transfer tobeTransfered = new Transfer();
        assertThrows(AccountException.class, ()-> transferService.transferMoney(tobeTransfered),"source does not exist");
        tobeTransfered.setSource(accountA.getId());

        assertThrows(AccountException.class, ()-> transferService.transferMoney(tobeTransfered),"target does not exist");
        tobeTransfered.setTarget(accountB.getId());

        assertThrows(TransferException.class, ()-> transferService.transferMoney(tobeTransfered),"amount not given");
        tobeTransfered.setAmount(BigDecimal.valueOf(-20));

        assertThrows(TransferException.class, ()-> transferService.transferMoney(tobeTransfered),"amount is not valid");
        tobeTransfered.setAmount(BigDecimal.valueOf(600));

        assertThrows(TransferException.class, ()-> transferService.transferMoney(tobeTransfered),"Error insufficient balance");
        tobeTransfered.setAmount(BigDecimal.valueOf(200));

        assertDoesNotThrow(()-> transferService.transferMoney(tobeTransfered));

        assertTrue(accountService.findAccount(accountA.getId()).getBalance().compareTo(BigDecimal.valueOf(300))==0);
        assertTrue(accountService.findAccount(accountB.getId()).getBalance().compareTo(BigDecimal.valueOf(700))==0);

        tobeTransfered.setSource(accountC.getId());
        assertDoesNotThrow(()-> transferService.transferMoney(tobeTransfered));
        assertTrue(accountService.findAccount(accountC.getId()).getBalance().compareTo(BigDecimal.valueOf(300))==0);

        assertTrue(accountService.findAccount(accountB.getId()).getBalance().compareTo(BigDecimal.valueOf(900))>=0);

    }

    /**
     * Testing Thread Concurrency, handling of deadlock (CannotAcquireLockException) is done in test method
     * starts by creating 2 accounts, the first with 0$, the second with 500$
     * allocate 500 thread to transfer money $ by $
     *
     * Deadlocks can be seen in logs
     *
     * Concurrency test to be run against Multi-Threaded DB
     * UnComment the annotation
     * @throws InterruptedException
     * @throws AccountException
     */
    //@Test
    public void transferMoneyConcurrent() throws InterruptedException, AccountException {

        Account accountA = new Account(11L,"USD", BigDecimal.valueOf(0));
        Account accountB = new Account(22L,"USD", BigDecimal.valueOf(500));
        Long id1 = accountService.save(accountA).getId();
        Long id2 = accountService.save(accountB).getId();

        int numberOfThreads = 500;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                while(true) {
                    try {
                        transferService.transferMoney(new Transfer(id2,id1,BigDecimal.valueOf(1)));
                    } catch (TransferException e) {
                        e.printStackTrace();
                    } catch (AccountException e) {
                        e.printStackTrace();
                    }catch (CannotAcquireLockException e){
                        continue;
                    }
                    break;
                }
            latch.countDown();
            });
        }
        latch.await();
        assertTrue(accountService.findAccount(id2).getBalance().compareTo(BigDecimal.ZERO)==0);
        assertTrue(accountService.findAccount(id1).getBalance().compareTo(BigDecimal.valueOf(500))==0);
    }

    /**
     * Testing Thread Concurrency, handling of deadlock is done in service wrapper method
     * starts by creating 2 accounts, the first with 0$, the second with 500$
     * allocate 500 thread to transfer money $ by $
     *
     * Deadlocks can be seen in logs
     *
     * Concurrency test to be run against Multi-Threaded DB
     * UnComment the annotation
     * @throws InterruptedException
     * @throws AccountException
     */
    //@Test
    public void transferMoneyConcurrentWrapper() throws InterruptedException, AccountException {

        Account accountA = new Account(11L,"USD", BigDecimal.valueOf(0));
        Account accountB = new Account(22L,"USD", BigDecimal.valueOf(500));
        Long id1 = accountService.save(accountA).getId();
        Long id2 = accountService.save(accountB).getId();

        int numberOfThreads = 500;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    transferService.transferMoneyWithRetry(new Transfer(id2,id1,BigDecimal.valueOf(1)));
                } catch (TransferException | AccountException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }
        latch.await();
        assertTrue(accountService.findAccount(id2).getBalance().compareTo(BigDecimal.ZERO)==0);
        assertTrue(accountService.findAccount(id1).getBalance().compareTo(BigDecimal.valueOf(500))==0);
    }

}