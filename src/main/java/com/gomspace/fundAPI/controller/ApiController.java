package com.gomspace.fundAPI.controller;

import com.gomspace.fundAPI.entity.Account;
import com.gomspace.fundAPI.entity.Transfer;
import com.gomspace.fundAPI.exception.AccountException;
import com.gomspace.fundAPI.exception.TransferException;
import com.gomspace.fundAPI.service.AccountService;
import com.gomspace.fundAPI.service.TransferService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping("api/v1")
public class ApiController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;

    private Gson gson = new Gson();
    private TypeToken lists = new TypeToken<ArrayList<Account>>(){};

    @GetMapping(value = "/accounts")
    public ResponseEntity<ApiResponse> retrieveAllAccounts() {
        return getSuccessResponse(HttpStatus.OK, gson.toJson(accountService.findAll(), lists.getType()));
    }

    @PostMapping(value = "/accounts", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> createAccount(@RequestBody Account account) throws AccountException {
        return getSuccessResponse(HttpStatus.OK,gson.toJson(accountService.createAccount(account),Account.class));
    }

    @PostMapping(value = "/transfer", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> doTransfer(@RequestBody Transfer transfer) throws TransferException, AccountException {
        transferService.transferMoneyWithRetry(transfer);
        return getSuccessResponse(HttpStatus.OK,null);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse> getAccount(@PathVariable long id) throws AccountException {
        Account account = accountService.findAccount(id);
        return getSuccessResponse(HttpStatus.OK,gson.toJson(account,Account.class));
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse> deleteAccount(@PathVariable long id) throws AccountException {
        accountService.delete(id);
        return getSuccessResponse(HttpStatus.OK,null);
    }

    private  ResponseEntity<ApiResponse> getSuccessResponse(HttpStatus status, String payload) {
        ApiResponse response = new ApiResponse();
        if(payload != null && ! payload.isEmpty()){
            response.setPayload(payload.replaceAll("\"",""));
        }
        response.setResponseCode(status.getReasonPhrase());
        response.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(response, status);
    }
}
