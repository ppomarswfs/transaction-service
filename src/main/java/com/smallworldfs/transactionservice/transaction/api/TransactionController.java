package com.smallworldfs.transactionservice.transaction.api;

import com.smallworldfs.transactionservice.transaction.api.mapper.TransactionDtoMapper;
import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionDtoMapper mapper = Mappers.getMapper(TransactionDtoMapper.class);
    private final TransactionService service;

    @GetMapping("/{id}")
    public TransactionDto getTransaction(@PathVariable Integer id) {
        return mapper.toDto(service.getTransaction(id));
    }

}
