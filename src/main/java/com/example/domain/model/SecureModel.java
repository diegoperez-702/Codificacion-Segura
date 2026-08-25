package com.example.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SecureModel(@NotBlank @Size(min = 3, max = 10) String input) {}