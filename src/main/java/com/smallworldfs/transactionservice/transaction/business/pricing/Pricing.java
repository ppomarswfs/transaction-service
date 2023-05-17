package com.smallworldfs.transactionservice.transaction.business.pricing;

import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Pricing {

    private final TransactionProperties transactionProperties;

    public void setCalculatedFields(Transaction transaction) {
        transaction.setFees(calculateFee(transaction));
        transaction.setAgentCommission(
                transaction.getFees() * transactionProperties.getAgentCommission());
        transaction.setCommission(transaction.getFees() - transaction.getAgentCommission());
        transaction.setStatus(TransactionStatus.NEW);
    }

    public double calculateFee(Transaction transaction) {
        return transaction.getSendingPrincipal() - transaction.getPayoutPrincipal();
    }
}
