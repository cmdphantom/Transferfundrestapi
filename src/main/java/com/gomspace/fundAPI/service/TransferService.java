package com.gomspace.fundAPI.service;

import com.gomspace.fundAPI.entity.Account;
import com.gomspace.fundAPI.entity.Transfer;
import com.gomspace.fundAPI.exception.AccountException;
import com.gomspace.fundAPI.exception.TransferException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Service designed to Calculate the rate and do the transfer between 2 accounts
 */
@Service
public class TransferService {

    final static String XCHANGE_URL = "" +
            "https://api.exchangeratesapi.io/v1/latest" +
            "?access_key=HMNC47tBT0LDa0mTEBYBj9sKvbpf1VL6" +
            "&base=%s" +
            "&symbols=%s";
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);


    @Autowired
    private AccountService accountService;


    public TransferService() { }

    protected BigDecimal getXRate(String source, String target) throws TransferException{
        try {
            String urlStr = String.format(XCHANGE_URL, source, target);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();

            String response = sb.toString();
            System.out.println(response);
            if (conn.getResponseCode() == 200) {
                JSONObject jsonObject = new JSONObject(response);
                BigDecimal rate = BigDecimal.valueOf(jsonObject.getJSONObject("rates").getDouble(target));
                logger.debug(target+" Rate for "+source+"="+rate);
                return rate;
            }else {
                logger.error("Error while retrieving the rate, response code " + conn.getResponseCode());
                throw new TransferException("Not valid response code: "+ conn.getResponseCode() +" received"
                        + " error: "+response);
            }
        } catch (Exception e){
            throw new TransferException(e.getMessage());
        }
    }


    /**
     * Validate the input and get the exchange rate if needed before applying the transfer from account source
     * to account target.
     * @param transfer given input contains ids of 2 accounts and amout of money to transfer
     * @throws TransferException
     * @throws AccountException
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized void transferMoney(Transfer transfer) throws TransferException, AccountException {

        Account source = accountService.findAccount(transfer.getSource());
        if(source == null) {
            throw new TransferException("Source account with id: " + transfer.getSource() + " is not found");
        }

        Account target = accountService.findAccount(transfer.getTarget());
        if(target == null) {
            throw new TransferException("Target account with id: " + transfer.getTarget() + " is not found");
        }

        if(source.equals(target)){
            throw new TransferException("Source and Target account are the same: " + target);
        }

        BigDecimal amount = transfer.getAmount();
        if(amount==null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferException("Given amount " + amount + " is not valid");
        }

        if(source.getBalance().compareTo(amount) < 0) {
            throw new TransferException("Insufficient balance in source account");
        }

        BigDecimal rate;
        //Same currency for both accounts => no need to retrieve rate
        if(source.getCurrency().equals(target.getCurrency())){
            rate = BigDecimal.ONE;
        }else {
            //Different currencies, the rate needs to be retrieved
            rate = getXRate(source.getCurrency(),target.getCurrency());
        }

        logger.info("Transferring " + transfer.getAmount() +" from " +source +" to " +target  );

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount.multiply(rate)));

        accountService.save(source);
        accountService.save(target);

        logger.info("Finish Transferring " + transfer.getAmount() +" from " +source +" to " +target  );
    }

    /**
     * Wrapper Method that keep trying to execute the transfer in case of deadlocks caused by threads concurrency
     * @param transfer given input
     * @throws TransferException
     * @throws AccountException
     */
    public void transferMoneyWithRetry(Transfer transfer) throws TransferException, AccountException {
        logger.info("Start transfer: " + transfer);
        while(true) {
            try {
                transferMoney(transfer);
            } catch (TransferException | AccountException e) {
                logger.error("Error occurred on transfer",e);
                throw e;
            }catch (CannotAcquireLockException e){
                logger.error("dealdock occurred on transfer",e);
                continue;
            }
            break;
        }
        logger.info("Finish transfer: " + transfer);

    }
}
