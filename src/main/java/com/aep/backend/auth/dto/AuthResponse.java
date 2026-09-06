package com.aep.backend.auth.dto;

public record AuthResponse(String token, String email, String nome, String perfil) {}
