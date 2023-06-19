package com.amey.ameyfintechproject.controller;

import com.amey.ameyfintechproject.payload.ApiResponse;
import com.amey.ameyfintechproject.payload.TradeDto;
import com.amey.ameyfintechproject.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

