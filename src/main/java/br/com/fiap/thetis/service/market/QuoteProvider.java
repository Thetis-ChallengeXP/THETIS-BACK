package br.com.fiap.thetis.service.market;

import java.math.BigDecimal;

public interface QuoteProvider {
    BigDecimal getPrice(String symbol);
}
