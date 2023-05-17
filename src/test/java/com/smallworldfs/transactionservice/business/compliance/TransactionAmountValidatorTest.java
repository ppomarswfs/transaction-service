package com.smallworldfs.transactionservice.business.compliance;

import static com.smallworldfs.error.issue.DefaultIssueType.REQUEST_ERROR;
import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.smallworldfs.error.exception.ApplicationException;
import com.smallworldfs.transactionservice.transaction.business.compliance.TransactionAmountValidator;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TransactionAmountValidatorTest {

    @Mock
    private TransactionProperties properties;

    @InjectMocks
    private TransactionAmountValidator transactionAmountValidator;


    @Test
    void throw_error_when_create_with_limit_exceed() {
        when(properties.getMaxTransactionValue()).thenReturn(3000.0);
        Transaction transaction = newTransaction();
        transaction.setSendingPrincipal(3001.0);

        ApplicationException exception =
                assertThrows(ApplicationException.class, () -> transactionAmountValidator.validate(transaction));

        assertThat(exception).hasMessage("Sending principal amount: 3,001 exceeds limit in single operation.")
                .returns(REQUEST_ERROR, e -> e.getIssue().getType());
    }

    @Test
    void call_create_transaction_when_fee_is_correct() {
        when(properties.getMaxTransactionValue()).thenReturn(3000.0);
        Transaction transaction = newTransaction();

        Assertions.assertDoesNotThrow(
                () -> transactionAmountValidator.validate(transaction));
    }

}
