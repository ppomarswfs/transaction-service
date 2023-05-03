package com.smallworldfs.transactionservice.transaction.client;

import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transaction-data-service", url = "${transaction-data-service.url}")
public interface TransactionDataServiceClient {

    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Transaction getTransaction(@PathVariable Integer id);

    @PostMapping(value = "/transactions/", produces = MediaType.APPLICATION_JSON_VALUE)
    Transaction createTransaction(@RequestBody Transaction transaction);

    @GetMapping(value = "/transactions/client={userId}&status={status}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Transaction> getOpenTransactionsByUser(int userId, String status);
}
