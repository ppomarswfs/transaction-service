package com.smallworldfs.transactionservice.service;

import static com.smallworldfs.error.issue.DefaultIssueType.NOT_FOUND;
import static com.smallworldfs.error.issue.DefaultIssueType.REQUEST_ERROR;
import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static com.smallworldfs.transactionservice.Transactions.newTransactionDto;
import static com.smallworldfs.transactionservice.Transactions.newTransactionWithoutId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.smallworldfs.error.exception.ApplicationException;
import com.smallworldfs.starter.httptest.exception.MockHttpException;
import com.smallworldfs.transactionservice.transaction.api.mapper.TransactionDtoMapper;
import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import com.smallworldfs.transactionservice.transaction.service.TransactionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    private final TransactionDtoMapper mapper = Mappers.getMapper(TransactionDtoMapper.class);

    @Mock
    private TransactionDataServiceClient client;

    @Mock
    private TransactionProperties properties;

    @InjectMocks
    private TransactionService service;


    @Nested
    class GetTransaction {

        @Test
        void throws_transaction_not_found_when_client_returns_404() {
            whenTransactionIsQueriedThenThrowNotFound(55);

            ApplicationException exception = assertThrows(ApplicationException.class, () -> service.getTransaction(55));

            assertThat(exception).hasMessage("Transaction with id 55 could be not found.")
                    .returns(NOT_FOUND, e -> e.getIssue().getType());
        }

        @Test
        void returns_transaction_data_when_transaction_exists() {
            whenTransactionIsQueriedThenReturnTransaction(1, newTransaction());

            Transaction transaction = service.getTransaction(1);


            assertThat(transaction).isEqualTo(newTransaction());
        }

        private void whenTransactionIsQueriedThenThrowNotFound(int id) {
            when(client.getTransaction(id)).thenThrow(MockHttpException.notFound());
        }

        private void whenTransactionIsQueriedThenReturnTransaction(int id, Transaction transaction) {
            when(client.getTransaction(id)).thenReturn(transaction);
        }
    }

    @Nested
    class CreateTransaction {

        @Test
        void throw_error_when_create_with_sending_is_less_payout() {
            Transaction transaction = newTransaction();
            transaction.setSendingPrincipal(50.0);

            ApplicationException exception =
                    assertThrows(ApplicationException.class, () -> service.createTransaction(transaction));

            assertThat(exception).hasMessage("Sending amount: 50 is less than payout amount: 98")
                    .returns(REQUEST_ERROR, e -> e.getIssue().getType());
        }

        @Test
        void throw_error_when_create_with_limit_exceed() {
            when(properties.getMaxTransactionValue()).thenReturn(3000.0);
            Transaction transaction = newTransaction();
            transaction.setSendingPrincipal(3001.0);

            ApplicationException exception =
                    assertThrows(ApplicationException.class, () -> service.createTransaction(transaction));

            assertThat(exception).hasMessage("Sending principal amount: 3,001 exceeds limit in single operation.")
                    .returns(REQUEST_ERROR, e -> e.getIssue().getType());
        }

        @Test
        void throw_error_when_create_where_client_has_five_transaction() {
            // TODO
        }

        @Test
        void throw_error_when_create_where_client_cannot_send_more_5000_in_period() {
            // TODO
        }

        @Test
        void return_transaction_when_is_created() {
            when(properties.getMaxTransactionValue()).thenReturn(3000.0);
            when(properties.getAgentCommission()).thenReturn(0.2);
            when(client.createTransaction(newTransactionWithoutId())).thenReturn(newTransaction());
            TransactionDto transactionDto = newTransactionDto();

            Transaction transaction = service.createTransaction(mapper.toModel(transactionDto));

            assertEquals(newTransaction(), transaction);
        }


    }

}

