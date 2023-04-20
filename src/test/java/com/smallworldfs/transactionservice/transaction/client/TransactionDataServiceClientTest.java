package com.smallworldfs.transactionservice.transaction.client;

import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.smallworldfs.starter.http.error.exception.HttpException;
import com.smallworldfs.starter.httptest.HttpClientTest;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@HttpClientTest(classes = TransactionDataServiceClient.class)
class TransactionDataServiceClientTest {

    @Autowired
    private TransactionDataServiceClient client;

    @Nested
    class GetTransaction {

        @Test
        void throws_not_found_when_server_returns_404() {
            assertThrows(HttpException.NotFound.class, () -> client.getTransaction(55));
        }

        @Test
        void returns_transaction_when_server_returns_transaction_data() {
            Transaction transaction = client.getTransaction(1);
            assertThat(transaction).isEqualTo(newTransaction());
        }
    }
}
