package com.portfolio.doctor.controller;

import com.portfolio.doctor.payload.ApiResponse;
import com.portfolio.doctor.payload.TradeDto;
import com.portfolio.doctor.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<ApiResponse> processTrades(@RequestBody @Valid TradeDto tradeRequest) {
        return portfolioService.processTrades(tradeRequest);
    }

}

