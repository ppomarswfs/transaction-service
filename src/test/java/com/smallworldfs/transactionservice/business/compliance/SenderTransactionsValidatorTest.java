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
import com.smallworldfs.transactionservice.transaction.business.compliance.SenderTransacionsValidator;
import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
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
public class SenderTransactionsValidatorTest {

    private final TransactionDtoMapper mapper = Mappers.getMapper(TransactionDtoMapper.class);
    @Mock
    private TransactionProperties properties;
    @Mock
    private TransactionDataServiceClient client;
    @InjectMocks
    private SenderTransacionsValidator senderTransacionsValidator;

    private static final int PERIOD = 30;


    @Test
    void call_create_transaction_when_user_has_less_limit_by_period() {
        whenProperties();
        when(client.getTransactionsBySenderIdWithPeriod(3, PERIOD))
                .thenReturn(createTransactionListWithTwoTransactions(2400));
        TransactionDto transactionDto = newTransactionDto();


        Assertions.assertDoesNotThrow(
                () -> senderTransacionsValidator.validate(mapper.toModel(transactionDto)));
    }

    @Test
    void call_create_transaction_when_user_has_equal_limit_by_period() {
        whenProperties();
        when(client.getTransactionsBySenderIdWithPeriod(3, 30))
                .thenReturn(createTransactionListWithTwoTransactions(2450));
        TransactionDto transactionDto = newTransactionDto();

        Assertions.assertDoesNotThrow(
                () -> senderTransacionsValidator.validate(mapper.toModel(transactionDto)));
    }

    @Test
    void not_call_create_transaction_when_user_exceeds_limit_by_period() {
        whenProperties();
        when(client.getTransactionsBySenderIdWithPeriod(3, 30))
                .thenReturn(createTransactionListWithTwoTransactions(2499));
        TransactionDto transactionDto = newTransactionDto();


        ApplicationException exception =
                assertThrows(ApplicationException.class,
                        () -> senderTransacionsValidator.validate(mapper.toModel(transactionDto)));

        assertThat(exception)
                .hasMessage(
                        "Client cannot has more than 5,000$ in a given 30 days period. Now sender would has 5,098$")
                .returns(REQUEST_ERROR, e -> e.getIssue().getType());
    }

    private List<Transaction> createTransactionListWithTwoTransactions(double sendingPrincipal) {
        Transaction transaction = newTransaction();
        transaction.setSendingPrincipal(sendingPrincipal);
        return Collections.nCopies(2, transaction);
    }

    private void whenProperties() {
        when(properties.getMaxTransactionByPeriod()).thenReturn(5000.0);
        when(properties.getDaysLimitByPeriod()).thenReturn(PERIOD);
    }


}
