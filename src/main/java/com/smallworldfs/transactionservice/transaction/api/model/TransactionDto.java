package com.smallworldfs.transactionservice.transaction.api.model;


import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    private Integer transactionId;
    private Double sendingPrincipal;
    private Double payoutPrincipal;
    private Double fees;
    private Double commission;
    private Double agentCommission;
    private Integer senderId;
    private Integer beneficiaryId;
    private TransactionStatus status;

}
