package com.fintech.quickpay.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 核心银行服务客户端 —— 封装对核心银行内部接口的调用，用于支付清算
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoreBankingClient {

    private final RestTemplate restTemplate;

    @Value("${service.corebanking.url}")
    private String coreBankingUrl;

    @Value("${service.internal-key}")
    private String internalKey;

    /**
     * 调用核心银行内部存款接口，用于支付清算
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> settle(String accountNo, BigDecimal amount, String remark) {
        String url = coreBankingUrl + "/internal/account/deposit";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Service-Key", internalKey);

        Map<String, Object> body = Map.of(
                "accountNo", accountNo,
                "amount", amount,
                "remark", remark
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("Calling CoreBanking settle: accountNo={}, amount={}, remark={}", accountNo, amount, remark);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        return response.getBody();
    }
}
