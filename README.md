# Rest API
## API
### Endpoints

### 1 - Show Accounts

***Method*** : `GET` 

***URL*** : `/api/v1/accounts/`


### 2 - Show Account Detail

***Method*** : `GET`

***URL*** : `/api/v1/accounts/:id`

### 3 - Create Account

***Method*** : `POST`

***URL*** : `/api/v1/accounts

### 4 - Delete Account

***Method*** : `DELETE`

***URL*** : `/api/v1/accounts/:id



### 5 - Transfer Money

***Method*** : `POST`

***URL*** : `/api/v1/transfer

***curl*** : ```curl -i -H "Content-Type: application/json" -X POST  -d '{"source":"2","target":"3","amount":"100"}'  http://localhost:8080/api/v1/transfer ```

***HEADER*** : `Content-Type: application/json`

***DATA*** : ```{"source":"2","target":"199","amount":"10"}```

***Success Response***

***Code*** : `200 OK`



