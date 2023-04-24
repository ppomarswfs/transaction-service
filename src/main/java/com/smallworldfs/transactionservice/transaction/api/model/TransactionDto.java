package com.smallworldfs.transactionservice.transaction.api.model;


import com.smallworldfs.transactionservice.transaction.entity.TransactionStatus;
import javax.validation.constraints.NotNull;
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
    @NotNull
    private Double sendingPrincipal;
    @NotNull
    private Double payoutPrincipal;
    private Double fees;
    private Double commission;
    private Double agentCommission;
    @NotNull
    private Integer senderId;
    @NotNull
    private Integer beneficiaryId;
    private TransactionStatus status;

}
