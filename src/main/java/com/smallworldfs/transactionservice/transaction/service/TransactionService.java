package com.smallworldfs.transactionservice.transaction.service;

import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_NOT_FOUND;

import com.smallworldfs.starter.http.error.exception.HttpException;
import com.smallworldfs.transactionservice.transaction.business.compliance.TransactionValidator;
import com.smallworldfs.transactionservice.transaction.business.pricing.Pricing;
import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionDataServiceClient client;
    private final Pricing pricing;

    private final List<TransactionValidator> transactionValidators;


    public Transaction getTransaction(Integer id) {
        try {
            return client.getTransaction(id);
        } catch (HttpException.NotFound exception) {
            throw TRANSACTION_NOT_FOUND.withParameters(id).causedBy(exception).asException();
        }
    }

    public Transaction createTransaction(Transaction transaction) {
        transactionValidators.forEach(transactionValidator -> transactionValidator.validate(transaction));
        pricing.setCalculatedFields(transaction);
        return client.createTransaction(transaction);
    }

    public Transaction changeStatusPayout(Integer transactionId) {
        return null;
    }
}
