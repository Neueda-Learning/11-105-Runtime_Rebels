package com.portfoliomanager.controller;

import com.portfoliomanager.dto.TransactionRequest;
import com.portfoliomanager.dto.TransactionResponse;
import com.portfoliomanager.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Records buy/sell/deposit/withdraw/interest events against an investment. This is the
 * mechanism through which quantities, average cost and realized P/L are tracked.
 */
@RestController
@Tag(name = "Transactions", description = "Buy/sell/deposit/withdraw/interest history per investment")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "List all transactions across the whole portfolio")
    @GetMapping("/api/transactions")
    public List<TransactionResponse> findAll() {
        return transactionService.findAll();
    }

    @Operation(summary = "List transactions for a specific investment")
    @GetMapping("/api/investments/{investmentId}/transactions")
    public List<TransactionResponse> findByInvestment(@PathVariable Long investmentId) {
        return transactionService.findByInvestment(investmentId);
    }

    @Operation(summary = "Record a new transaction (BUY, SELL, DEPOSIT, WITHDRAW, INTEREST) for an investment")
    @PostMapping("/api/investments/{investmentId}/transactions")
    public ResponseEntity<TransactionResponse> record(@PathVariable Long investmentId,
                                                        @Valid @RequestBody TransactionRequest request) {
        TransactionResponse created = transactionService.record(investmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
