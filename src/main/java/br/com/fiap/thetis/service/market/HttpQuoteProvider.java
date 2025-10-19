package br.com.fiap.thetis.service.market;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpQuoteProvider implements QuoteProvider {

    private final RestTemplate restTemplate;

    @Value("${market.quotes.url:https://dummy-quote-provider.local/price?symbol=}")
    private String quoteUrl;

    @Override
    public BigDecimal getPrice(String symbol) {
        try {
            String url = quoteUrl + symbol;
            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object price = resp.getBody().get("price");
                if (price != null) return new BigDecimal(price.toString());
            }
            throw new RuntimeException("Resposta inválida do provedor de cotações");
        } catch (Exception e) {
            log.warn("Falha ao buscar cotação de {}: {}. Usando preço fictício.", symbol, e.getMessage());
            // Fallback simples para desenvolvimento
            return BigDecimal.valueOf(100.00);
        }
    }
}
