package dev.rubenpari.backend.service;

import dev.rubenpari.backend.model.Discovery;
import dev.rubenpari.backend.repository.DiscoveryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** Service providing read-only access to a user's book discovery history. */
@Service
public class DiscoveryService {
    private final DiscoveryRepository discoveryRepository;

    public DiscoveryService(DiscoveryRepository discoveryRepository) {
        this.discoveryRepository = discoveryRepository;
    }

    public List<Discovery> listHistory(UUID userId) {
        return discoveryRepository.findByUserId(userId);
    }
}
