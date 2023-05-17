package com.smallworldfs.transactionservice.transaction.business.compliance;

import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.MIN_FEE_IS_TOO_SMALL;

import com.smallworldfs.transactionservice.transaction.business.pricing.Pricing;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
@RequiredArgsConstructor
public class TransactionFeeValidator implements TransactionValidator {

    private final TransactionProperties transactionProperties;
    private final Pricing pricing;


    @Override
    public void validate(Transaction transaction) {
        if (feeIsLessToMin(transaction)) {
            throw MIN_FEE_IS_TOO_SMALL
                    .withParameters(transaction.getSendingPrincipal(), transaction.getPayoutPrincipal(),
                            transactionProperties.getMinFee())
                    .asException();
        }
    }

    private boolean feeIsLessToMin(Transaction transaction) {
        return pricing.calculateFee(transaction) < transactionProperties.getMinFee();
    }
}
