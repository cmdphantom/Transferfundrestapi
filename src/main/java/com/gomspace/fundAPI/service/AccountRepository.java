package com.gomspace.fundAPI.service;

import com.gomspace.fundAPI.entity.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account,Long> {
    List<Account> findAll();
}
