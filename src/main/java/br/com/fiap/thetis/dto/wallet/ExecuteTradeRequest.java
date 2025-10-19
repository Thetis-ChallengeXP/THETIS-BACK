package br.com.fiap.thetis.dto.wallet;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExecuteTradeRequest(
        @NotNull java.util.UUID walletId,
        @NotBlank String symbol,
        @NotNull TradeSide side,
        @DecimalMin("0.0001") BigDecimal quantity,
        @DecimalMin("0.0001") BigDecimal price
) {
    public enum TradeSide { BUY, SELL }
}
