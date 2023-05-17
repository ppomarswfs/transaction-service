package com.smallworldfs.transactionservice.business.compliance;

import static com.smallworldfs.error.issue.DefaultIssueType.REQUEST_ERROR;
import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static com.smallworldfs.transactionservice.Transactions.newTransactionDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.smallworldfs.error.exception.ApplicationException;
import com.smallworldfs.transactionservice.transaction.api.mapper.TransactionDtoMapper;
import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.business.compliance.OpenTransactionsValidator;
import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OpenTransactionsValidatorTest {

    private final TransactionDtoMapper mapper = Mappers.getMapper(TransactionDtoMapper.class);
    @Mock
    private TransactionProperties properties;
    @Mock
    private TransactionDataServiceClient client;
    @InjectMocks
    private OpenTransactionsValidator openTransactionsValidator;


    @Test
    void call_create_transaction_when_user_has_not_any_transaction_open() {
        when(properties.getMaxOpenTransactions()).thenReturn(5);
        when(client.getOpenTransactionsByUser(3, TransactionStatus.NEW)).thenReturn(createTransactionList(0));
        TransactionDto transactionDto = newTransactionDto();

        Assertions.assertDoesNotThrow(
                () -> openTransactionsValidator.validate(mapper.toModel(transactionDto)));
    }

    @Test
    void call_create_transaction_when_user_has_less_open_transaction_to_limit() {
        when(properties.getMaxOpenTransactions()).thenReturn(5);
        when(client.getOpenTransactionsByUser(3, TransactionStatus.NEW))
                .thenReturn(createTransactionList(4));
        TransactionDto transactionDto = newTransactionDto();


        Assertions.assertDoesNotThrow(
                () -> openTransactionsValidator.validate(mapper.toModel(transactionDto)));
    }

    @Test
    void not_call_create_transaction_when_user_exceeds_open_transaction_limit() {
        when(properties.getMaxOpenTransactions()).thenReturn(5);
        when(client.getOpenTransactionsByUser(3, TransactionStatus.NEW)).thenReturn(createTransactionList(5));
        TransactionDto transactionDto = newTransactionDto();


        ApplicationException exception =
                assertThrows(ApplicationException.class,
                        () -> openTransactionsValidator.validate(
                                mapper.toModel(transactionDto)));

        assertThat(exception).hasMessage("Client cannot has more than 5 transactions in progress")
                .returns(REQUEST_ERROR, e -> e.getIssue().getType());
    }

    private List<Transaction> createTransactionList(int limit) {
        Transaction transaction = newTransaction();
        return Collections.nCopies(limit, transaction);
    }


}
