package com.portfolio.doctor.service;

import com.portfolio.doctor.payload.ApiResponse;
import com.portfolio.doctor.payload.TradeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface PortfolioService {

    ResponseEntity<ApiResponse> processTrades(@RequestBody TradeDto tradeDto);
}
