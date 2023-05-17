package com.smallworldfs.transactionservice.transaction.business.compliance;

import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD;

import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(3)
@Component
@RequiredArgsConstructor
public class SenderTransacionsValidator implements TransactionValidator {

    private final TransactionProperties transactionProperties;
    private final TransactionDataServiceClient client;


    @Override
    public void validate(Transaction transaction) {
        double sumAmounts = getTotalAmountsByPeriod(transaction.getSenderId())
                + transaction.getSendingPrincipal();
        if (sumAmounts > transactionProperties.getMaxTransactionByPeriod()) {
            throw CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD
                    .withParameters(transactionProperties.getMaxTransactionByPeriod(),
                            transactionProperties.getDaysLimitByPeriod(), sumAmounts)
                    .asException();
        }
    }

    private double getTotalAmountsByPeriod(int senderId) {
        List<Transaction> transactions = client.getTransactionsBySenderIdWithPeriod(senderId,
                transactionProperties.getDaysLimitByPeriod());
        return transactions.stream().mapToDouble(Transaction::getSendingPrincipal).sum();
    }
}
