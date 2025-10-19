package br.com.fiap.thetis.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.thetis.dto.wallet.CreateWalletRequest;
import br.com.fiap.thetis.dto.wallet.ExecuteTradeRequest;
import br.com.fiap.thetis.dto.wallet.WalletResponse;
import br.com.fiap.thetis.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService service;

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse create(@PathVariable UUID userId, @Valid @RequestBody CreateWalletRequest req) {
        return service.createWallet(userId, req);
    }

    @GetMapping("/{walletId}")
    public WalletResponse get(@PathVariable UUID walletId) {
        return service.getWallet(walletId);
    }

    @PostMapping("/trade")
    public WalletResponse trade(@Valid @RequestBody ExecuteTradeRequest req) {
        return service.executeTrade(req);
    }
}
