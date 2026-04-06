package dev.rubenpari.backend.service;

import dev.rubenpari.backend.client.ExternalBook;
import dev.rubenpari.backend.model.Book;
import dev.rubenpari.backend.model.User;
import dev.rubenpari.backend.repository.BookRepository;
import dev.rubenpari.backend.repository.DiscoveryRepository;
import dev.rubenpari.backend.repository.UserRepository;
import dev.rubenpari.backend.client.IsbnDbClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    IsbnDbClient isbnDbClient;

    @Mock
    BookRepository bookRepository;

    @Mock
    DiscoveryRepository discoveryRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    TranslationService translationService;

    @InjectMocks
    BookService bookService;

    @Test
    void randomBook_throwsWhenApiNeverReturnsUsableBook() {
        when(isbnDbClient.fetchRandom(any())).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> bookService.randomBook(null, "s", Map.of(), null));
    }

    @Test
    void randomBook_persistsAndSkipsTranslationWhenNoTargetLanguage() {
        ExternalBook ext = new ExternalBook();
        ext.setId("ext-99");
        ext.setTitle("Title");
        when(isbnDbClient.fetchRandom(any())).thenReturn(ext);
        when(bookRepository.findByExternalId("ext-99")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book book = bookService.randomBook(null, "sess", Map.of(), null);

        assertEquals("ext-99", book.getExternalId());
        verify(translationService, never()).translatePlainBatch(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void randomBook_invokesBatchTranslationWhenTargetLanguageSet() {
        ExternalBook ext = new ExternalBook();
        ext.setId("ext-100");
        ext.setTitle("T");
        when(isbnDbClient.fetchRandom(any())).thenReturn(ext);
        when(bookRepository.findByExternalId("ext-100")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(translationService.translatePlainBatch(any(), eq("fr"))).thenAnswer(inv -> inv.getArgument(0));

        bookService.randomBook(null, "sess", Map.of(), "fr");

        ArgumentCaptor<java.util.List<String>> captor = ArgumentCaptor.forClass(java.util.List.class);
        verify(translationService).translatePlainBatch(captor.capture(), eq("fr"));
        assertEquals("T", captor.getValue().getFirst());
    }

    @Test
    void randomBook_recordsDiscoveryForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();

        ExternalBook ext = new ExternalBook();
        ext.setId("ext-200");
        ext.setTitle("Book");
        when(isbnDbClient.fetchRandom(any())).thenReturn(ext);
        when(bookRepository.findByExternalId("ext-200")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(discoveryRepository.findByUserIdAndExternalId(userId, "ext-200")).thenReturn(Optional.empty());

        bookService.randomBook(userId, "sid", Map.of(), null);

        verify(discoveryRepository).save(any());
    }
}
