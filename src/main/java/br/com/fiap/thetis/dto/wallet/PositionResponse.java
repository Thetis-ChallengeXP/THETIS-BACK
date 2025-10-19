package br.com.fiap.thetis.dto.wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record PositionResponse(
        UUID assetId,
        String symbol,
        String assetName,
        BigDecimal quantity,
        BigDecimal avgPrice,
        BigDecimal marketPrice,
        BigDecimal pnl
) {}
