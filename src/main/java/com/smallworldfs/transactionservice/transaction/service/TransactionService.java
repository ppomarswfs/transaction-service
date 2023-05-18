package com.smallworldfs.transactionservice.transaction.service;

import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_NOT_FOUND;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_WAS_PAYOUT;

import com.smallworldfs.starter.http.error.exception.HttpException;
import com.smallworldfs.transactionservice.transaction.business.compliance.TransactionValidator;
import com.smallworldfs.transactionservice.transaction.business.pricing.Pricing;
import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionDataServiceClient client;
    private final Pricing pricing;

    private final List<TransactionValidator> transactionValidators;


    public Transaction getTransaction(Integer transactionId) {
        return callGetTransaction(transactionId);
    }

    public Transaction createTransaction(Transaction transaction) {
        transactionValidators.forEach(transactionValidator -> transactionValidator.validate(transaction));
        pricing.setCalculatedFields(transaction);
        return client.createTransaction(transaction);
    }

    public Transaction changeStatusPayout(Integer transactionId) {
        Transaction transaction = callGetTransaction(transactionId);
        if (TransactionStatus.PAY_OUT.equals(transaction.getStatus())) {
            // TODO mover a un validador? No podria extender de TransactionValidator, no?
            throw TRANSACTION_WAS_PAYOUT.withParameters(transactionId).asException();
        }
        transaction.setStatus(TransactionStatus.PAY_OUT);
        return client.payout(transaction.getTransactionId(), transaction);
    }

    private Transaction callGetTransaction(Integer transactionId) {
        try {
            return client.getTransaction(transactionId);
        } catch (HttpException.NotFound exception) {
            throw TRANSACTION_NOT_FOUND.withParameters(transactionId).causedBy(exception).asException();
        }
    }
}
