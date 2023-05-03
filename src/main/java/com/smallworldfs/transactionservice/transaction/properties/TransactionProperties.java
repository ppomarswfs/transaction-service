package com.smallworldfs.transactionservice.transaction.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "transaction")
public class TransactionProperties {

    private double maxTransactionValue = 3000.0;
    private double agentCommission = 0.2;
    private int maxOpenTransactions = 5;

}
