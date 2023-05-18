package com.smallworldfs.transactionservice.service;

import static com.smallworldfs.error.issue.DefaultIssueType.NOT_FOUND;
import static com.smallworldfs.error.issue.DefaultIssueType.REQUEST_ERROR;
import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static com.smallworldfs.transactionservice.Transactions.newTransactionDto;
import static com.smallworldfs.transactionservice.Transactions.newTransactionWithoutId;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_EXCEEDS_SENDING_LIMIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.smallworldfs.error.exception.ApplicationException;
import com.smallworldfs.starter.httptest.exception.MockHttpException;
import com.smallworldfs.transactionservice.transaction.api.mapper.TransactionDtoMapper;
import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.business.compliance.TransactionAmountValidator;
import com.smallworldfs.transactionservice.transaction.business.pricing.Pricing;
import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import com.smallworldfs.transactionservice.transaction.service.TransactionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


// @ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {TransactionService.class, Pricing.class, TransactionProperties.class})
public class TransactionServiceTest {

    private final TransactionDtoMapper mapper = Mappers.getMapper(TransactionDtoMapper.class);

    @MockBean
    private TransactionDataServiceClient client;

    @MockBean
    private TransactionAmountValidator validator;


    @Autowired
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


    }

    @Nested
    class CreateTransaction {

        @Test
        void throw_error_when_compliance_is_not_accepted() {
            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setPayoutPrincipal(3005.0);
            Transaction transaction = mapper.toModel(transactionDto);
            doThrow(TRANSACTION_EXCEEDS_SENDING_LIMIT.withParameters(3005.0).asException()).when(validator)
                    .validate(transaction);

            ApplicationException exception =
                    assertThrows(ApplicationException.class, () -> service.createTransaction(transaction));

            assertThat(exception).hasMessage("Sending principal amount: 3,005 exceeds limit in single operation.")
                    .returns(REQUEST_ERROR, e -> e.getIssue().getType());
        }

        @Test
        void return_transaction_when_is_created() {
            when(client.createTransaction(newTransactionWithoutId())).thenReturn(newTransaction());
            TransactionDto transactionDto = newTransactionDto();

            Transaction transaction = service.createTransaction(mapper.toModel(transactionDto));

            assertEquals(newTransaction(), transaction);
        }
    }

    @Nested
    class Payout {

        @Test
        void throws_transaction_not_found_when_client_returns_404() {
            whenTransactionIsQueriedThenThrowNotFound();

            ApplicationException exception =
                    assertThrows(ApplicationException.class, () -> service.changeStatusPayout(55));

            assertThat(exception).hasMessage("Transaction with id 55 could be not found.")
                    .returns(NOT_FOUND, e -> e.getIssue().getType());
        }

        @Test
        void throws_error_when_transaction_was_payout() {
            whenTransactionIsQueriedThenReturnTransactionWithPayoutStatus();

            ApplicationException exception =
                    assertThrows(ApplicationException.class, () -> service.changeStatusPayout(100));

            assertThat(exception).hasMessage("Transaction with id 100 was already payout.")
                    .returns(REQUEST_ERROR, e -> e.getIssue().getType());
        }

        @Test
        void returns_transaction_data_when_with_status_payout_when_transaction_is_correct() {
            Transaction transactionRequested = newTransaction();
            whenTransactionIsQueriedThenReturnTransaction(transactionRequested);
            whenTransactionIsPayoutThenReturnTransactionWithPayoutStatus();

            Transaction transaction = service.changeStatusPayout(1);

            transactionRequested.setStatus(TransactionStatus.PAY_OUT);
            assertThat(transaction).isEqualTo(transactionRequested);
        }

        private void whenTransactionIsQueriedThenReturnTransactionWithPayoutStatus() {
            Transaction transaction = newTransaction();
            transaction.setStatus(TransactionStatus.PAY_OUT);
            when(client.getTransaction(100)).thenReturn(transaction);
        }

        private void whenTransactionIsPayoutThenReturnTransactionWithPayoutStatus() {
            Transaction transaction = newTransaction();
            transaction.setStatus(TransactionStatus.PAY_OUT);
            when(client.payout(transaction.getTransactionId(), transaction)).thenReturn(transaction);
        }

    }

    private void whenTransactionIsQueriedThenThrowNotFound() {
        when(client.getTransaction(55)).thenThrow(MockHttpException.notFound());
    }

    private void whenTransactionIsQueriedThenReturnTransaction(Transaction transaction) {
        when(client.getTransaction(1)).thenReturn(transaction);
    }

}

