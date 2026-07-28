package com.booksrandomized.backend.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CatalogClient catalog;

    CatalogController(CatalogClient catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/search")
    List<Book> search(
            @RequestParam @NotBlank @Size(max = 120) String query,
            @RequestParam(defaultValue = "20") @Min(1) @Max(40) int limit) {
        return catalog.search(query.trim(), limit);
    }
}
