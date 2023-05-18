package com.smallworldfs.transactionservice.transaction.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.smallworldfs.error.model.ErrorDto;
import com.smallworldfs.transactionservice.transaction.api.mapper.TransactionDtoMapper;
import com.smallworldfs.transactionservice.transaction.api.model.TransactionDto;
import com.smallworldfs.transactionservice.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("")
    public TransactionDto postTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        return mapper.toDto(service.createTransaction(mapper.toModel(transactionDto)));
    }

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Success"),
            @ApiResponse(
                    responseCode = "422",
                    description = "Bad Request",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping("/{transactionId}/payout")
    public TransactionDto payout(@PathVariable Integer transactionId) {
        return mapper.toDto(service.changeStatusPayout(transactionId));
    }
}
