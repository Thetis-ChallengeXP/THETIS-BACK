package br.com.fiap.thetis.service;

import br.com.fiap.thetis.dto.wallet.CreateWalletRequest;
import br.com.fiap.thetis.dto.wallet.ExecuteTradeRequest;
import br.com.fiap.thetis.dto.wallet.PositionResponse;
import br.com.fiap.thetis.dto.wallet.WalletResponse;
import br.com.fiap.thetis.exception.BusinessException;
import br.com.fiap.thetis.exception.NotFoundException;
import br.com.fiap.thetis.model.*;
import br.com.fiap.thetis.repository.*;
import br.com.fiap.thetis.service.market.QuoteProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepo;
    private final UserRepository userRepo;
    private final AssetRepository assetRepo;
    private final PositionRepository positionRepo;
    private final TradeRepository tradeRepo;
    private final QuoteProvider quotes;

    @Transactional
    public WalletResponse createWallet(UUID userId, CreateWalletRequest req) {
        User user = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Wallet wallet = Wallet.builder()
                .user(user)
                .name(req.name())
                .createdAt(LocalDateTime.now())
                .build();
        walletRepo.save(wallet);
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID walletId) {
        Wallet wallet = walletRepo.findById(walletId).orElseThrow(() -> new NotFoundException("Carteira não encontrada"));
        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse executeTrade(ExecuteTradeRequest req) {
        Wallet wallet = walletRepo.findById(req.walletId())
                .orElseThrow(() -> new NotFoundException("Carteira não encontrada"));

        Asset asset = assetRepo.findBySymbol(req.symbol())
                .orElseGet(() -> assetRepo.save(Asset.builder()
                        .symbol(req.symbol().toUpperCase())
                        .name(req.symbol().toUpperCase())
                        .type(Asset.AssetType.STOCK)
                        .build()));

        Position position = positionRepo.findByWalletAndAsset(wallet, asset).orElse(null);
        BigDecimal qty = req.quantity();
        BigDecimal price = req.price();

        if (req.side() == ExecuteTradeRequest.TradeSide.BUY) {
            if (position == null) {
                position = Position.builder()
                        .wallet(wallet)
                        .asset(asset)
                        .quantity(qty)
                        .avgPrice(price)
                        .build();
            } else {
                BigDecimal totalCost = position.getAvgPrice().multiply(position.getQuantity())
                        .add(price.multiply(qty));
                BigDecimal newQty = position.getQuantity().add(qty);
                BigDecimal newAvg = totalCost.divide(newQty, 4, RoundingMode.HALF_UP);
                position.setQuantity(newQty);
                position.setAvgPrice(newAvg);
            }
            positionRepo.save(position);
        } else { // SELL
            if (position == null || position.getQuantity().compareTo(qty) < 0) {
                throw new BusinessException("Quantidade para venda excede a posição");
            }
            BigDecimal newQty = position.getQuantity().subtract(qty);
            position.setQuantity(newQty);
            if (newQty.compareTo(BigDecimal.ZERO) == 0) {
                positionRepo.delete(position);
            } else {
                positionRepo.save(position);
            }
        }

        tradeRepo.save(Trade.builder()
                .wallet(wallet)
                .asset(asset)
                .side(req.side() == ExecuteTradeRequest.TradeSide.BUY ? Trade.Side.BUY : Trade.Side.SELL)
                .quantity(qty)
                .price(price)
                .executedAt(LocalDateTime.now())
                .build());

        return toResponse(walletRepo.findById(wallet.getId()).orElse(wallet));
    }

    private WalletResponse toResponse(Wallet wallet) {
        List<Position> positions = positionRepo.findByWallet(wallet);
        List<PositionResponse> pos = positions.stream().map(p -> {
            BigDecimal market = quotes.getPrice(p.getAsset().getSymbol());
            BigDecimal pnl = market.subtract(p.getAvgPrice()).multiply(p.getQuantity());
            return new PositionResponse(
                    p.getAsset().getId(),
                    p.getAsset().getSymbol(),
                    p.getAsset().getName(),
                    p.getQuantity(),
                    p.getAvgPrice(),
                    market,
                    pnl
            );
        }).collect(Collectors.toList());

        BigDecimal totalValue = pos.stream()
                .map(x -> x.marketPrice().multiply(x.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPnl = pos.stream()
                .map(PositionResponse::pnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new WalletResponse(wallet.getId(), wallet.getName(), totalValue, totalPnl, pos);
    }
}
