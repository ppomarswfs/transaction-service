package com.smallworldfs.transactionservice.api;

import static com.smallworldfs.starter.servicetest.error.ErrorDtoResultMatcher.errorDto;
import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static com.smallworldfs.transactionservice.Transactions.newTransactionDto;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_EXCEEDS_SENDING_LIMIT;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_NOT_FOUND;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_SENDING_IS_LESS_THAN_PAYOUT;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smallworldfs.transactionservice.transaction.api.TransactionController;
import com.smallworldfs.transactionservice.transaction.api.mapper.TransactionDtoMapper;
import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.service.TransactionService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = TransactionController.class)
public class TransactionControllerTest {

    private final TransactionDtoMapper mapper = Mappers.getMapper(TransactionDtoMapper.class);

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
                    .andExpect(jsonPath("$.commission", Matchers.equalTo(1.6)))
                    .andExpect(jsonPath("$.agentCommission", Matchers.equalTo(0.4)))
                    .andExpect(jsonPath("$.senderId", Matchers.equalTo(3)))
                    .andExpect(jsonPath("$.beneficiaryId", Matchers.equalTo(4)))
                    .andExpect(jsonPath("$.status", Matchers.equalTo("NEW")));
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

    @Nested
    class CreateTransaction {

        @Test
        void return_400_when_create_without_sending_principal() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setSendingPrincipal(null);

            ResultActions result = postTransaction(transactionDto);

            result.andExpect(status().is4xxClientError());
        }

        @Test
        void return_400_when_create_without_payout_principal() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setPayoutPrincipal(null);

            ResultActions result = postTransaction(transactionDto);

            result.andExpect(status().is4xxClientError());
        }

        @Test
        void return_400_when_create_without_sender_id() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setSenderId(null);

            ResultActions result = postTransaction(transactionDto);

            result.andExpect(status().is4xxClientError());
        }

        @Test
        void return_400_when_create_without_beneficiary_id() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setBeneficiaryId(null);

            ResultActions result = postTransaction(transactionDto);

            result.andExpect(status().is4xxClientError());
        }

        @Test
        void return_400_when_create_with_sending_is_less_payout() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setSendingPrincipal(50.0);
            when(service.createTransaction(mapper.toModel(transactionDto))).thenThrow(
                    TRANSACTION_SENDING_IS_LESS_THAN_PAYOUT.withParameters(50.0, 98.0).asException());

            ResultActions result = postTransaction(transactionDto);

            result.andExpect(status().is4xxClientError())
                    .andExpect(errorDto().hasMessage("Sending amount: 50 is less than payout amount: 98")
                            .hasType("REQUEST_ERROR")
                            .hasCode("TRANSACTION_SENDING_IS_LESS_THAN_PAYOUT"));
        }

        @Test
        void return_400_when_create_with_limit_exceed() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setSendingPrincipal(3001.0);
            when(service.createTransaction(mapper.toModel(transactionDto))).thenThrow(
                    TRANSACTION_EXCEEDS_SENDING_LIMIT.withParameters(3001).asException());

            ResultActions result = postTransaction(transactionDto);

            result.andExpect(status().is4xxClientError())
                    .andExpect(
                            errorDto().hasMessage("Sending principal amount: 3,001 exceeds limit in single operation.")
                                    .hasType("REQUEST_ERROR")
                                    .hasCode("TRANSACTION_EXCEEDS_SENDING_LIMIT"));
        }

        @Test
        void return_400_when_create_where_client_has_five_transaction() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            when(service.createTransaction(mapper.toModel(transactionDto))).thenThrow(
                    CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS.asException());

            ResultActions result = postTransaction(transactionDto);

            result.andExpect(status().is4xxClientError())
                    .andExpect(errorDto().hasMessage("Client cannot has more than 5 transactions in progress")
                            .hasType("REQUEST_ERROR")
                            .hasCode("CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS"));
        }

        @Test
        void return_400_when_create_where_client_cannot_send_more_5000_in_period() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            when(service.createTransaction(mapper.toModel(transactionDto))).thenThrow(
                    CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD.asException());

            ResultActions result = postTransaction(transactionDto);

            result.andExpect(status().is4xxClientError())
                    .andExpect(errorDto().hasMessage("Client cannot has more than 5000$ in a given 30 day period")
                            .hasType("REQUEST_ERROR")
                            .hasCode("CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD"));
        }

        @Test
        void return_201_when_create_with_json_informed() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            when(service.createTransaction(mapper.toModel(transactionDto))).thenReturn(
                    newTransaction());


            ResultActions result = postTransaction(transactionDto);

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionId", Matchers.equalTo(1)))
                    .andExpect(jsonPath("$.sendingPrincipal", Matchers.equalTo(100.0)))
                    .andExpect(jsonPath("$.payoutPrincipal", Matchers.equalTo(98.0)))
                    .andExpect(jsonPath("$.fees", Matchers.equalTo(2.0)))
                    .andExpect(jsonPath("$.commission", Matchers.equalTo(1.6)))
                    .andExpect(jsonPath("$.agentCommission", Matchers.equalTo(0.4)))
                    .andExpect(jsonPath("$.senderId", Matchers.equalTo(3)))
                    .andExpect(jsonPath("$.beneficiaryId", Matchers.equalTo(4)))
                    .andExpect(jsonPath("$.status", Matchers.equalTo("NEW")));
        }

        private ResultActions postTransaction(TransactionDto transactionDto) throws Exception {
            return mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                    .content(asJsonString(transactionDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON));
        }

        private static String asJsonString(final Object obj) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

}

