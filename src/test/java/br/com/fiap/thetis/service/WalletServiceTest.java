package br.com.fiap.thetis.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.fiap.thetis.dto.wallet.CreateWalletRequest;
import br.com.fiap.thetis.dto.wallet.ExecuteTradeRequest;
import br.com.fiap.thetis.model.Asset;
import br.com.fiap.thetis.model.User;
import br.com.fiap.thetis.model.Wallet;
import br.com.fiap.thetis.repository.AssetRepository;
import br.com.fiap.thetis.repository.PositionRepository;
import br.com.fiap.thetis.repository.TradeRepository;
import br.com.fiap.thetis.repository.UserRepository;
import br.com.fiap.thetis.repository.WalletRepository;
import br.com.fiap.thetis.service.market.QuoteProvider;

public class WalletServiceTest {

    private WalletRepository walletRepo;
    private UserRepository userRepo;
    private AssetRepository assetRepo;
    private PositionRepository positionRepo;
    private TradeRepository tradeRepo;
    private QuoteProvider quotes;

    private WalletService service;

    @BeforeEach
    void setup() {
        walletRepo = mock(WalletRepository.class);
        userRepo = mock(UserRepository.class);
        assetRepo = mock(AssetRepository.class);
        positionRepo = mock(PositionRepository.class);
        tradeRepo = mock(TradeRepository.class);
        quotes = mock(QuoteProvider.class);
        service = new WalletService(walletRepo, userRepo, assetRepo, positionRepo, tradeRepo, quotes);
    }

    @Test
    void createWallet_ok() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        Wallet saved = Wallet.builder().id(UUID.randomUUID()).user(user).name("Minha").createdAt(LocalDateTime.now()).build();
        when(walletRepo.save(any(Wallet.class))).thenReturn(saved);

        var resp = service.createWallet(userId, new CreateWalletRequest("Minha"));
        assertEquals("Minha", resp.name());
    }

    @Test
    void executeTrade_buy_createsPosition() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder().id(walletId).build();
        when(walletRepo.findById(walletId)).thenReturn(Optional.of(wallet));

        when(assetRepo.findBySymbol("AAPL")).thenReturn(Optional.empty());
        when(assetRepo.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));
        when(positionRepo.findByWalletAndAsset(any(), any())).thenReturn(Optional.empty());
        when(quotes.getPrice("AAPL")).thenReturn(new BigDecimal("200.00"));
        when(positionRepo.findByWallet(wallet)).thenReturn(java.util.List.of());

        var req = new ExecuteTradeRequest(walletId, "AAPL", ExecuteTradeRequest.TradeSide.BUY,
                new BigDecimal("10"), new BigDecimal("100.00"));
        var resp = service.executeTrade(req);
        assertNotNull(resp);
    }
}
