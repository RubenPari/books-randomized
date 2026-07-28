package com.booksrandomized.backend.catalog;

import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DiscoveryConfiguration {
    @Bean
    RandomGenerator discoveryRandom(@Value("${discovery.random-seed:}") String seed) {
        return seed.isBlank() ? new SecureRandom() : new Random(Long.parseLong(seed));
    }
}
