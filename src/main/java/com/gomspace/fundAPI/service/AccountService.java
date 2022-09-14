package com.gomspace.fundAPI.service;

import com.gomspace.fundAPI.entity.Account;
import com.gomspace.fundAPI.exception.AccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;

@Service
@Transactional
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account findAccount(Long id) throws AccountException {
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null ){
            logger.error("No account exist with the given id: " + id);
            throw new AccountException("No account exist with the given id: " + id);
        }
        return account;
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Account createAccount(Account input) throws AccountException {
        //check owner (against DB or other method
        if(input.getOwner() == null || input.getOwner() <= 0){
            logger.error("Given Owner " + input.getOwner()+ " is not valid");
            throw new AccountException("Given Owner " + input.getOwner()+ " is not valid");
        }
        if(input.getBalance() == null || input.getBalance().longValue() < 0){
            logger.error("Given Balance " + input.getBalance() +" is not valid");
            throw new AccountException("Given Balance " + input.getBalance() +" is not valid");
        }

        if(input.getCurrency() == null){
            logger.error("Currency is not specified");
            throw new AccountException("Currency is not specified");
        }else{
            try {
                Currency.getInstance(input.getCurrency());
            }catch(Exception e){
                logger.error("Given currency " + input.getCurrency() + " is not valid");
                throw new AccountException("Given currency " + input.getCurrency() + " is not valid");
            }

        }
        Account newAccount = new Account();
        newAccount.setOwner(input.getOwner());
        newAccount.setBalance(input.getBalance());
        newAccount.setCurrency(input.getCurrency());
        this.save(newAccount);

        logger.info("new Account created " + newAccount);
        return newAccount;
    }

    public void delete(long id) throws AccountException {
        Account tobeDeleted = findAccount(id);
        accountRepository.deleteById(id);
        logger.info("Account deleted " + tobeDeleted);
    }
}
