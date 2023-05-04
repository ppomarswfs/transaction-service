package com.smallworldfs.transactionservice.service;

import static com.smallworldfs.error.issue.DefaultIssueType.NOT_FOUND;
import static com.smallworldfs.error.issue.DefaultIssueType.REQUEST_ERROR;
import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static com.smallworldfs.transactionservice.Transactions.newTransactionDto;
import static com.smallworldfs.transactionservice.Transactions.newTransactionWithoutId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smallworldfs.error.exception.ApplicationException;
import com.smallworldfs.starter.httptest.exception.MockHttpException;
import com.smallworldfs.transactionservice.transaction.api.mapper.TransactionDtoMapper;
import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import com.smallworldfs.transactionservice.transaction.service.TransactionService;
import java.util.Collections;
import java.util.List;
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
            whenTransactionIsQueriedThenThrowNotFound();

            ApplicationException exception = assertThrows(ApplicationException.class, () -> service.getTransaction(55));

            assertThat(exception).hasMessage("Transaction with id 55 could be not found.")
                    .returns(NOT_FOUND, e -> e.getIssue().getType());
        }

        @Test
        void returns_transaction_data_when_transaction_exists() {
            whenTransactionIsQueriedThenReturnTransaction(newTransaction());

            Transaction transaction = service.getTransaction(1);


            assertThat(transaction).isEqualTo(newTransaction());
        }

        private void whenTransactionIsQueriedThenThrowNotFound() {
            when(client.getTransaction(55)).thenThrow(MockHttpException.notFound());
        }

        private void whenTransactionIsQueriedThenReturnTransaction(Transaction transaction) {
            when(client.getTransaction(1)).thenReturn(transaction);
        }
    }

    @Nested
    class CreateTransaction {

        @Test
        void throw_error_when_create_with_sending_is_less_payout() {
            when(properties.getMinFee()).thenReturn(1.0);

            Transaction transaction = newTransaction();
            transaction.setPayoutPrincipal(99.01);

            ApplicationException exception =
                    assertThrows(ApplicationException.class, () -> service.createTransaction(transaction));

            assertThat(exception)
                    .hasMessage("The difference between sending (100) and payout (99.01) must be at least 1$")
                    .returns(REQUEST_ERROR, e -> e.getIssue().getType());
        }

        @Test
        void call_create_transaction_when_fee_is_correct() {
            whenDefaultProperties();

            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setPayoutPrincipal(99.0);
            transactionDto.setFees(1.0);
            transactionDto.setCommission(0.8);
            transactionDto.setAgentCommission(0.2);

            service.createTransaction(mapper.toModel(transactionDto));

            Transaction transaction = newTransactionWithoutId();
            transaction.setPayoutPrincipal(99.0);
            transaction.setFees(1.0);
            transaction.setCommission(0.8);
            transaction.setAgentCommission(0.2);

            verify(client, times(1)).createTransaction(transaction);
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
        void call_create_transaction_when_user_has_not_any_transaction_open() {
            whenDefaultProperties();
            when(client.getOpenTransactionsByUser(3, TransactionStatus.NEW)).thenReturn(createTransactionList(0));
            TransactionDto transactionDto = newTransactionDto();

            service.createTransaction(mapper.toModel(transactionDto));

            verify(client, times(1)).createTransaction(newTransactionWithoutId());
        }

        @Test
        void call_create_transaction_when_user_has_less_open_transaction_to_limit() {
            whenDefaultProperties();
            when(client.getOpenTransactionsByUser(3, TransactionStatus.NEW))
                    .thenReturn(createTransactionList(4));
            TransactionDto transactionDto = newTransactionDto();

            service.createTransaction(mapper.toModel(transactionDto));

            verify(client, times(1)).createTransaction(newTransactionWithoutId());
        }

        @Test
        void not_call_create_transaction_when_user_exceeds_open_transaction_limit() {
            whenPropertiesWithoutCommission();
            when(client.getOpenTransactionsByUser(3, TransactionStatus.NEW)).thenReturn(createTransactionList(5));
            TransactionDto transactionDto = newTransactionDto();


            ApplicationException exception =
                    assertThrows(ApplicationException.class,
                            () -> service.createTransaction(mapper.toModel(transactionDto)));

            assertThat(exception).hasMessage("Client cannot has more than 5 transactions in progress")
                    .returns(REQUEST_ERROR, e -> e.getIssue().getType());
        }

        @Test
        void not_call_create_transaction_when_user_not_exist() {
            // TODO not capture error but validate service propagate error
        }

        @Test
        void call_create_transaction_when_user_has_less_limit_by_period() {
            whenDefaultPropertiesWithDaysByPeriod();
            when(client.getTransactionsBySenderIdWithPeriod(3, 30)).thenReturn(createTransactionList(2, 2400));
            TransactionDto transactionDto = newTransactionDto();

            service.createTransaction(mapper.toModel(transactionDto));

            verify(client, times(1)).createTransaction(newTransactionWithoutId());
        }

        @Test
        void call_create_transaction_when_user_has_equal_limit_by_period() {
            whenDefaultPropertiesWithDaysByPeriod();
            when(client.getTransactionsBySenderIdWithPeriod(3, 30)).thenReturn(createTransactionList(2, 2450));
            TransactionDto transactionDto = newTransactionDto();

            service.createTransaction(mapper.toModel(transactionDto));

            verify(client, times(1)).createTransaction(newTransactionWithoutId());
        }

        @Test
        void not_call_create_transaction_when_user_exceeds_limit_by_period() {
            when(properties.getMaxTransactionValue()).thenReturn(3000.0);
            when(properties.getDaysLimitByPeriod()).thenReturn(30);
            when(properties.getMaxTransactionByPeriod()).thenReturn(5000.0);
            when(client.getTransactionsBySenderIdWithPeriod(3, 30)).thenReturn(createTransactionList(2, 2499));
            TransactionDto transactionDto = newTransactionDto();


            ApplicationException exception =
                    assertThrows(ApplicationException.class,
                            () -> service.createTransaction(mapper.toModel(transactionDto)));

            assertThat(exception)
                    .hasMessage(
                            "Client cannot has more than 5,000$ in a given 30 days period. Now sender would has 5,098$")
                    .returns(REQUEST_ERROR, e -> e.getIssue().getType());
        }

        private List<Transaction> createTransactionList(int limit) {
            return createTransactionList(limit, 100.0);
        }

        private List<Transaction> createTransactionList(int limit, double sendingPrincipal) {
            Transaction transaction = newTransaction();
            transaction.setSendingPrincipal(sendingPrincipal);
            return Collections.nCopies(limit, transaction);
        }

        @Test
        void throw_error_when_create_where_client_cannot_send_more_5000_in_period() {
            // TODO
        }

        @Test
        void return_transaction_when_is_created() {
            whenDefaultProperties();
            when(client.createTransaction(newTransactionWithoutId())).thenReturn(newTransaction());
            TransactionDto transactionDto = newTransactionDto();

            Transaction transaction = service.createTransaction(mapper.toModel(transactionDto));

            assertEquals(newTransaction(), transaction);
        }

        private void whenDefaultProperties() {
            when(properties.getMaxTransactionValue()).thenReturn(3000.0);
            when(properties.getAgentCommission()).thenReturn(0.2);
            when(properties.getMaxOpenTransactions()).thenReturn(5);
            when(properties.getMaxTransactionByPeriod()).thenReturn(5000.0);
            when(properties.getMinFee()).thenReturn(1.0);
        }

        private void whenPropertiesWithoutCommission() {
            when(properties.getMaxTransactionValue()).thenReturn(3000.0);
            when(properties.getMaxOpenTransactions()).thenReturn(5);
            when(properties.getMaxTransactionByPeriod()).thenReturn(5000.0);
            when(properties.getMinFee()).thenReturn(1.0);
        }

        private void whenDefaultPropertiesWithDaysByPeriod() {
            whenDefaultProperties();
            when(properties.getDaysLimitByPeriod()).thenReturn(30);
        }

    }

}

