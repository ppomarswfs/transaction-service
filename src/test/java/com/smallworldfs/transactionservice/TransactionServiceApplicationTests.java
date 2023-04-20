package com.smallworldfs.transactionservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "transaction-data-service.url=http://localhost:8080")
class TransactionServiceApplicationTests {

    @Test
    void contextLoads() {}

}
