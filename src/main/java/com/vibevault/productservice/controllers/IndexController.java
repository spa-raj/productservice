package com.vibevault.productservice.controllers;

import com.vibevault.productservice.services.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/index")
@RequiredArgsConstructor
public class IndexController {

    private final ProductIndexingService productIndexingService;

    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reindexAll() {
        long count = productIndexingService.reindexAll();
        return ResponseEntity.ok(Map.of(
                "message", "Reindex completed",
                "productsIndexed", count
        ));
    }
}
