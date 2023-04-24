package com.smallworldfs.transactionservice;

import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;

public class Transactions {
    public static Transaction newTransaction() {
        return Transaction.builder()
                .transactionId(1)
                .sendingPrincipal(100.0)
                .payoutPrincipal(98.0)
                .fees(2.0)
                .commission(1.8)
                .agentCommission(0.2)
                .senderId(3)
                .beneficiaryId(4)
                .status(TransactionStatus.NEW)
                .build();
    }

    public static TransactionDto newTransactionDto() {
        return TransactionDto.builder()
                .sendingPrincipal(100.0)
                .payoutPrincipal(98.0)
                .senderId(3)
                .beneficiaryId(4)
                .build();
    }

}
