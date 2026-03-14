package com.vibevault.productservice.events;

import com.vibevault.productservice.services.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIndexingEventListener {

    private final ProductIndexingService productIndexingService;

    @Async
    @EventListener
    public void handleProductChanged(ProductChangedEvent event) {
        try {
            switch (event.getActionType()) {
                case CREATED, UPDATED, DELETED ->
                        productIndexingService.indexProduct(event.getProduct());
            }
        } catch (Exception e) {
            log.warn("Failed to index product {} to Elasticsearch: {}",
                    event.getProduct().getId(), e.getMessage());
        }
    }
}
