package dev.rubenpari.backend.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategorySearchTermsTest {

    @Test
    void splitTrimsAndSkipsBlanks() {
        assertEquals(java.util.List.of("a", "b"), CategorySearchTerms.splitNonBlankParts(" a , b , "));
        assertEquals(2, CategorySearchTerms.splitNonBlankParts("a,b").size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { ",", "  ,  " })
    void pickOneReturnsNullWhenNoTerms(String input) {
        assertNull(CategorySearchTerms.pickOneRandomTerm(input));
    }

    @Test
    void pickOneSingleTermIsDeterministic() {
        assertEquals("Fantasy", CategorySearchTerms.pickOneRandomTerm("Fantasy"));
    }

    @Test
    void pickOneUsesRandomGenerator() {
        RandomGenerator rng = new SplittableRandom(42L);
        String a = CategorySearchTerms.pickOneRandomTerm("x,y", rng);
        assertTrue("x".equals(a) || "y".equals(a));
    }
}
