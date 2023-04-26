package com.smallworldfs.transactionservice.transaction.api.mapper;

import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper
public interface TransactionDtoMapper {

    TransactionDto toDto(Transaction transaction);

    Transaction toModel(TransactionDto transactionDto);

}
