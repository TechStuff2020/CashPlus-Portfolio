package com.amey.ameyfintechproject.service;

import com.amey.ameyfintechproject.payload.ApiResponse;
import com.amey.ameyfintechproject.payload.TradeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface PortfolioService {

    ResponseEntity<ApiResponse> processTrades(@RequestBody TradeDto tradeDto);
}
