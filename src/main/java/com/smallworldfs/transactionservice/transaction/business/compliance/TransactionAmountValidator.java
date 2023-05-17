package com.smallworldfs.transactionservice.transaction.business.compliance;

import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_EXCEEDS_SENDING_LIMIT;

import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@RequiredArgsConstructor
public class TransactionAmountValidator implements TransactionValidator {

    private final TransactionProperties transactionProperties;


    @Override
    public void validate(Transaction transaction) {
        if (transaction.getSendingPrincipal() > transactionProperties.getMaxTransactionValue()) {
            throw TRANSACTION_EXCEEDS_SENDING_LIMIT
                    .withParameters(transaction.getSendingPrincipal())
                    .asException();
        }
    }
}
