package com.booksrandomized.backend.auth;

public interface PasswordResetNotifier {
    void send(String email, String token);
}
