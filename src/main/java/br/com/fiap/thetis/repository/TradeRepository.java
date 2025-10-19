package br.com.fiap.thetis.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.thetis.model.Trade;
import br.com.fiap.thetis.model.Wallet;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findByWallet(Wallet wallet);
}
