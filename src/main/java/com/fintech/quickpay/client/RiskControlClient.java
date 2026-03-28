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
 * 风控服务客户端 —— 封装对风控系统的调用，用于支付风险检查
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskControlClient {

    private final RestTemplate restTemplate;

    @Value("${service.riskcontrol.url}")
    private String riskControlUrl;

    @Value("${service.internal-key}")
    private String internalKey;

    /**
     * 调用风控系统检查交易风险
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> checkTransaction(String customerNo, String transactionType,
                                                 BigDecimal amount, String accountNo) {
        try {
            String url = riskControlUrl + "/internal/risk/check-transaction";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Service-Key", internalKey);

            Map<String, Object> body = Map.of(
                    "customerNo", customerNo,
                    "transactionType", transactionType,
                    "amount", amount,
                    "accountNo", accountNo
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            log.info("Calling RiskControl checkTransaction: customerNo={}, type={}, amount={}, accountNo={}",
                    customerNo, transactionType, amount, accountNo);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("Risk service unavailable, default pass: {}", e.getMessage());
            return Map.of(
                    "approved", true,
                    "riskScore", 0,
                    "message", "Risk service unavailable, default pass"
            );
        }
    }
}
