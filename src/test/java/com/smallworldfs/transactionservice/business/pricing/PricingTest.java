package com.smallworldfs.transactionservice.business.pricing;

import static com.smallworldfs.transactionservice.Transactions.newTransactionDto;
import static com.smallworldfs.transactionservice.Transactions.newTransactionWithoutId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.smallworldfs.transactionservice.transaction.api.mapper.TransactionDtoMapper;
import com.smallworldfs.transactionservice.transaction.business.pricing.Pricing;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.properties.TransactionProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PricingTest {

    private final TransactionDtoMapper mapper = Mappers.getMapper(TransactionDtoMapper.class);

    @Mock
    private TransactionProperties properties;

    @InjectMocks
    private Pricing pricing;


    @Test
    void calculateFeeWithTransaction() {
        Transaction transaction = mapper.toModel(newTransactionDto());
        assertEquals(2, pricing.calculateFee(transaction));
    }

    @Test
    void calculateFieldsWithTransaction() {
        when(properties.getAgentCommission()).thenReturn(0.2);

        Transaction transaction = mapper.toModel(newTransactionDto());
        pricing.setCalculatedFields(transaction);

        assertEquals(newTransactionWithoutId(), transaction);
    }

}
