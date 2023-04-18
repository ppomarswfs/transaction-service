package com.smallworldfs.transactionservice.api;

import static com.smallworldfs.starter.servicetest.error.ErrorDtoResultMatcher.errorDto;
import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_NOT_FOUND;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smallworldfs.transactionservice.transaction.api.TransactionController;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.service.TransactionService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService service;

    @Nested
    class GetTransaction {

        @Test
        void returns_404_when_transaction_not_exist() throws Exception {
            int transactionId = 55;
            wheTransactionIsQueriedThenThrowNotFoundException(transactionId);

            ResultActions result = getTransaction(transactionId);

            result.andExpect(status().isNotFound())
                    .andExpect(errorDto().hasMessage("Transaction with id 55 could be not found.")
                            .hasType("NOT_FOUND")
                            .hasCode("TRANSACTION_NOT_FOUND"));
        }

        @Test
        void returns_transaction_data_when_transaction_exist() throws Exception {
            whenTransactionIsQuierdidThenReturnTransaction(1, newTransaction());

            getTransaction(1)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionId", Matchers.equalTo(1)))
                    .andExpect(jsonPath("$.sendingPrincipal", Matchers.equalTo(100.0)))
                    .andExpect(jsonPath("$.payoutPrincipal", Matchers.equalTo(98.0)))
                    .andExpect(jsonPath("$.fees", Matchers.equalTo(2.0)))
                    .andExpect(jsonPath("$.commission", Matchers.equalTo(1.8)))
                    .andExpect(jsonPath("$.agentCommission", Matchers.equalTo(0.2)))
                    .andExpect(jsonPath("$.senderId", Matchers.equalTo(3)))
                    .andExpect(jsonPath("$.beneficiaryId", Matchers.equalTo(4)))
                    .andExpect(jsonPath("$.status", Matchers.equalTo("NEW")));
        }

        private void whenTransactionIsQuierdidThenThrowNotFound(int id) {
            when(service.getTransaction(id)).thenThrow(TRANSACTION_NOT_FOUND.withParameters(id).asException());
        }

        private void whenTransactionIsQuierdidThenReturnTransaction(int id, Transaction transaction) {
            when(service.getTransaction(id)).thenReturn(transaction);
        }

        private ResultActions getTransaction(int id) throws Exception {
            return mockMvc.perform(MockMvcRequestBuilders.get("/transactions/{id}", id));
        }

        private void wheTransactionIsQueriedThenThrowNotFoundException(int transactionId) {
            when(service.getTransaction(transactionId))
                    .thenThrow(TRANSACTION_NOT_FOUND.withParameters(transactionId).asException());
        }
    }

}

