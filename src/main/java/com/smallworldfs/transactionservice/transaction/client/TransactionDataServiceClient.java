package com.smallworldfs.transactionservice.transaction.client;

import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "transaction-data-service", url = "${transaction-data-service.url}")
public interface TransactionDataServiceClient {

    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Transaction getTransaction(@PathVariable Integer id);
}
