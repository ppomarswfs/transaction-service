package com.smallworldfs.transactionservice;

import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class TransactionServiceApplicationTests {

    @MockBean
    private TransactionDataServiceClient client;

    @Test
    void contextLoads() {
    }

}
