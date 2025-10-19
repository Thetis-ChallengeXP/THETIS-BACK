package br.com.fiap.thetis.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.thetis.model.Asset;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    Optional<Asset> findBySymbol(String symbol);
}
