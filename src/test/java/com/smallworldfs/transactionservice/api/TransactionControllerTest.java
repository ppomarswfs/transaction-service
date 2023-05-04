package com.smallworldfs.transactionservice.api;

import static com.smallworldfs.starter.servicetest.error.ErrorDtoResultMatcher.errorDto;
import static com.smallworldfs.transactionservice.Transactions.newTransaction;
import static com.smallworldfs.transactionservice.Transactions.newTransactionDto;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.MIN_FEE_IS_TOO_SMALL;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_EXCEEDS_SENDING_LIMIT;
import static com.smallworldfs.transactionservice.transaction.error.TransactionIssue.TRANSACTION_NOT_FOUND;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smallworldfs.error.model.FieldErrorDto;
import com.smallworldfs.transactionservice.transaction.api.TransactionController;
import com.smallworldfs.transactionservice.transaction.api.mapper.TransactionDtoMapper;
import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.entity.Transaction;
import com.smallworldfs.transactionservice.transaction.service.TransactionService;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
            whenTransactionIsQueriedIdThenReturnTransaction(1, newTransaction());

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

        private void whenTransactionIsQueriedIdThenReturnTransaction(int id, Transaction transaction) {
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
            executePostWithoutAnyField("sendingPrincipal");
        }

        @Test
        void return_400_when_create_without_payout_principal() throws Exception {
            executePostWithoutAnyField("payoutPrincipal");
        }

        @Test
        void return_400_when_create_without_sender_id() throws Exception {
            executePostWithoutAnyField("senderId");
        }

        @Test
        void return_400_when_create_without_beneficiary_id() throws Exception {
            executePostWithoutAnyField("beneficiaryId");
        }

        @Test
        void return_400_when_create_with_sending_is_less_payout() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setSendingPrincipal(50.0);
            when(service.createTransaction(mapper.toModel(transactionDto))).thenThrow(
                    MIN_FEE_IS_TOO_SMALL.withParameters(100, 99.01, 1).asException());

            post("businessRules", "transactionSendingIsLessThanPayout")
                    .andExpect(status().isBadRequest())
                    .andExpect(errorDto()
                            .hasMessage("The difference between sending (100) and payout (99.01) must be at least 1$")
                            .hasType("REQUEST_ERROR")
                            .hasCode("MIN_FEE_IS_TOO_SMALL"));
        }

        @Test
        void return_400_when_create_with_limit_exceed() throws Exception {
            TransactionDto transactionDto = newTransactionDto();
            transactionDto.setSendingPrincipal(3001.0);
            when(service.createTransaction(mapper.toModel(transactionDto))).thenThrow(
                    TRANSACTION_EXCEEDS_SENDING_LIMIT.withParameters(3001.0).asException());

            post("businessRules", "sendingPrincipalExceedLimit")
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            errorDto().hasMessage("Sending principal amount: 3,001 exceeds limit in single operation.")
                                    .hasType("REQUEST_ERROR")
                                    .hasCode("TRANSACTION_EXCEEDS_SENDING_LIMIT"));
        }

        @Test
        void return_400_when_create_where_client_has_five_transaction() throws Exception {
            when(service.createTransaction(mapper.toModel(newTransactionDto()))).thenThrow(
                    CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS.withParameters(5).asException());
            post("correct", "transaction")
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            errorDto().hasMessage("Client cannot has more than 5 transactions in progress")
                                    .hasType("REQUEST_ERROR")
                                    .hasCode("CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS"));
        }

        @Test
        void return_400_when_create_where_client_cannot_send_more_5000_in_period() throws Exception {
            when(service.createTransaction(mapper.toModel(newTransactionDto()))).thenThrow(
                    CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD.withParameters(5000, 30, 6000).asException());

            post("correct", "transaction")
                    .andExpect(status().isBadRequest())
                    .andExpect(errorDto().hasMessage(
                            "Client cannot has more than 5,000$ in a given 30 days period. Now sender would has 6,000$")
                            .hasType("REQUEST_ERROR")
                            .hasCode("CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD"));
        }

        @Test
        void return_201_when_create_with_json_informed() throws Exception {
            when(service.createTransaction(mapper.toModel(newTransactionDto()))).thenReturn(
                    newTransaction());


            post("correct", "transaction").andExpect(status().isOk())
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

        private void executePostWithoutAnyField(String field) throws Exception {
            post("specError/withoutField", field)
                    .andExpect(status().isBadRequest())
                    .andExpect(errorDto()
                            .hasField(FieldErrorDto.builder()
                                    .path(field)
                                    .message("must not be null")
                                    .build()));
        }

        private ResultActions post(String directory, String jsonFile) throws Exception {
            return mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loadRequest("mvc/requests/" + directory + "/", jsonFile + ".json")));
        }

        private byte[] loadRequest(String path, String resource) throws IOException {
            return IOUtils.toByteArray(new ClassPathResource(path + resource).getInputStream());
        }
    }

}

