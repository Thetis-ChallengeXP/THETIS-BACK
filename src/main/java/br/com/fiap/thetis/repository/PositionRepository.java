package br.com.fiap.thetis.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.thetis.model.Asset;
import br.com.fiap.thetis.model.Position;
import br.com.fiap.thetis.model.Wallet;

public interface PositionRepository extends JpaRepository<Position, UUID> {
    List<Position> findByWallet(Wallet wallet);
    Optional<Position> findByWalletAndAsset(Wallet wallet, Asset asset);
}
