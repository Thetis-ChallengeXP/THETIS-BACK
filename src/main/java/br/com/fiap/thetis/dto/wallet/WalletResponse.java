package br.com.fiap.thetis.dto.wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String name,
        BigDecimal totalValue,
        BigDecimal totalPnl,
        List<PositionResponse> positions
) {}
