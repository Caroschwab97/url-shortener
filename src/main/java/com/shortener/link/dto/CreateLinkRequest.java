package com.shortener.link.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLinkRequest(
        @NotBlank(message = "es obligatoria")
        @Size(max = 2048, message = "no puede superar los 2048 caracteres")
        String url) {
}
