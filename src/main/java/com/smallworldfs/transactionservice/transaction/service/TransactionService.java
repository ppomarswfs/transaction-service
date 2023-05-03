package com.smallworldfs.transactionservice.transaction.service;

import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_EXCEEDS_SENDING_LIMIT;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_NOT_FOUND;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_SENDING_IS_LESS_THAN_PAYOUT;

import com.smallworldfs.starter.http.error.exception.HttpException;
import com.smallworldfs.transactionservice.transaction.client.TransactionDataServiceClient;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionProperties transactionProperties;

    private final TransactionDataServiceClient client;

    public Transaction getTransaction(Integer id) {
        try {
            return client.getTransaction(id);
        } catch (HttpException.NotFound exception) {
            throw TRANSACTION_NOT_FOUND.withParameters(id).causedBy(exception).asException();
        }
    }

    public Transaction createTransaction(Transaction transaction) {
        businessRules(transaction);
        setCalculatedFields(transaction);
        return client.createTransaction(transaction);
    }

    private void setCalculatedFields(Transaction transaction) {
        transaction.setFees(transaction.getSendingPrincipal() - transaction.getPayoutPrincipal());
        transaction.setAgentCommission(transaction.getFees() * transactionProperties.getAgentCommission());
        transaction.setCommission(transaction.getFees() - transaction.getAgentCommission());
        transaction.setStatus(TransactionStatus.NEW);
    }

    private void businessRules(Transaction transaction) {
        validateSendingIsGreaterPayout(transaction);
        validateSendingNotExceedsLimit(transaction.getSendingPrincipal());
        validateSenderNotExceedsLimitByPeriod(transaction);
        validateClientNotExceedsLimitOpenTransactions(transaction);
    }

    private void validateClientNotExceedsLimitOpenTransactions(Transaction transaction) {
        List<Transaction> transactions =
                client.getOpenTransactionsByUser(transaction.getSenderId(), TransactionStatus.NEW);
        if (transactions.size() >= transactionProperties.getMaxOpenTransactions()) {
            throw CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS
                    .withParameters(transactionProperties.getMaxOpenTransactions())
                    .asException();
        }
    }

    private void validateSenderNotExceedsLimitByPeriod(Transaction transaction) {
        double sumAmounts = getTotalAmountsByPeriod(transaction.getSenderId()) + transaction.getSendingPrincipal();
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

    private void validateSendingNotExceedsLimit(Double sendingPrincipal) {
        if (sendingPrincipal > transactionProperties.getMaxTransactionValue()) {
            throw TRANSACTION_EXCEEDS_SENDING_LIMIT
                    .withParameters(sendingPrincipal)
                    .asException();
        }
    }

    private void validateSendingIsGreaterPayout(Transaction transaction) {
        if (transaction.getSendingPrincipal() < transaction.getPayoutPrincipal()) {
            throw TRANSACTION_SENDING_IS_LESS_THAN_PAYOUT
                    .withParameters(transaction.getSendingPrincipal(), transaction.getPayoutPrincipal())
                    .asException();
        }
    }
}
