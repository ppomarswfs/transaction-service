package com.smallworldfs.transactionservice.transaction.business.compliance;

import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS;

import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(4)
@Component
@RequiredArgsConstructor
public class OpenTransactionsValidator implements TransactionValidator {

    private final TransactionProperties transactionProperties;
    private final TransactionDataServiceClient client;


    @Override
    public void validate(Transaction transaction) {
        List<Transaction> transactions =
                client.getOpenTransactionsByUser(transaction.getSenderId(), TransactionStatus.NEW);
        if (transactions.size() >= transactionProperties.getMaxOpenTransactions()) {
            throw CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS
                    .withParameters(transactionProperties.getMaxOpenTransactions())
                    .asException();
        }
    }
}
