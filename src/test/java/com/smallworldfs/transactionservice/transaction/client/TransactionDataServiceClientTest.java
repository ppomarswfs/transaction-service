package com.smallworldfs.transactionservice.transaction.client;

import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static com.smallworldfs.transactionservice.Transactions.newTransactionWithoutId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.smallworldfs.starter.http.error.exception.HttpException;
import com.smallworldfs.starter.httptest.HttpClientTest;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import java.util.List;
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
            assertEquals(newTransaction(), transaction);
        }
    }

    @Nested
    class CreateTransaction {

        @Test
        void throws_error_when_server_returns_400() {
            Transaction transaction = newTransactionWithoutId();
            transaction.setSenderId(9999);
            assertThrows(HttpException.BadRequest.class,
                    () -> client.createTransaction(transaction));
        }

        @Test
        void returns_transaction_when_server_returns_transaction_data() {
            Transaction transaction = client.createTransaction(newTransactionWithoutId());
            assertEquals(newTransaction(), transaction);
        }
    }

    @Nested
    class GetOpenTransactionsByUser {

        @Test
        void throws_error_when_server_returns_400() {
            assertThrows(HttpException.NotFound.class,
                    () -> client.getOpenTransactionsByUser(9999, TransactionStatus.NEW));
        }

        @Test
        void return_empty_list_when_user_has_not_open_transactions() {
            List<Transaction> transactions = client.getOpenTransactionsByUser(10, TransactionStatus.NEW);
            assertEquals(0, transactions.size());
        }

        @Test
        void return_transaction_list_when_user_has_open_transactions() {
            List<Transaction> transactions = client.getOpenTransactionsByUser(3, TransactionStatus.NEW);
            assertEquals(3, transactions.size());
        }
    }

    @Nested
    class GetTransactionsBySenderIdWithPeriod {
        @Test
        void throws_error_when_server_returns_400() {
            assertThrows(HttpException.NotFound.class,
                    () -> client.getTransactionsBySenderIdWithPeriod(9999, 30));
        }

        @Test
        void return_empty_list_when_user_has_not_open_transactions() {
            List<Transaction> transactions = client.getTransactionsBySenderIdWithPeriod(10, 30);
            assertEquals(0, transactions.size());
        }

        @Test
        void return_transaction_list_when_user_has_open_transactions() {
            List<Transaction> transactions = client.getTransactionsBySenderIdWithPeriod(3,30);
            assertEquals(3, transactions.size());
        }

    }
}
