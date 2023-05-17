package com.smallworldfs.transactionservice.transaction.business.compliance;

import com.smallworldfs.transactionservice.transaction.entity.Transaction;

public interface TransactionValidator {

    void validate(Transaction transaction);


}
