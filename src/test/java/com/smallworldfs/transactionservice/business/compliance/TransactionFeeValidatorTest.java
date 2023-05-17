package com.smallworldfs.transactionservice.business.compliance;

import static com.smallworldfs.error.issue.DefaultIssueType.REQUEST_ERROR;
import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.smallworldfs.error.exception.ApplicationException;
import com.smallworldfs.transactionservice.transaction.business.compliance.TransactionFeeValidator;
import com.smallworldfs.transactionservice.transaction.business.pricing.Pricing;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TransactionFeeValidatorTest {

    @Mock
    private TransactionProperties properties;
    @Mock
    private Pricing pricing;
    @InjectMocks
    private TransactionFeeValidator transactionFeeValidator;


    @Test
    void throw_error_when_create_with_sending_is_less_minimum() {
        when(properties.getMinFee()).thenReturn(1.0);
        Transaction transaction = newTransaction();
        transaction.setPayoutPrincipal(99.01);
        when(pricing.calculateFee(transaction)).thenReturn(0.99);

        ApplicationException exception =
                assertThrows(ApplicationException.class, () -> transactionFeeValidator.validate(transaction));

        assertThat(exception)
                .hasMessage("The difference between sending (100) and payout (99.01) must be at least 1$")
                .returns(REQUEST_ERROR, e -> e.getIssue().getType());
    }

    @Test
    void call_create_transaction_when_fee_is_correct() {
        when(properties.getMinFee()).thenReturn(1.0);
        Transaction transaction = newTransaction();
        when(pricing.calculateFee(transaction)).thenReturn(1.0);

        Assertions.assertDoesNotThrow(
                () -> transactionFeeValidator.validate(transaction));
    }

}
