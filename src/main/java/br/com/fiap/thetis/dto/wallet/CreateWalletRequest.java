package br.com.fiap.thetis.dto.wallet;

import jakarta.validation.constraints.NotBlank;

public record CreateWalletRequest(@NotBlank String name) {}
