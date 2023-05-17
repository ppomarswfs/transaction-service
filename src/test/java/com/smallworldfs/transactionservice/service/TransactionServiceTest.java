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

}

