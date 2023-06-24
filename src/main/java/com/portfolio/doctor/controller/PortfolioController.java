package com.portfolio.doctor.controller;

import com.portfolio.doctor.payload.ApiResponse;
import com.portfolio.doctor.payload.TradeDto;
import com.portfolio.doctor.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/portfolio")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<ApiResponse> processTrades(@RequestBody @Valid TradeDto tradeRequest) {
        return portfolioService.processTrades(tradeRequest);
    }

}

