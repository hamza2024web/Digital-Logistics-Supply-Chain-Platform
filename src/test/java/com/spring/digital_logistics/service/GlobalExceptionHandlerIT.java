package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class GlobalExceptionHandlerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    // ========== TESTS ResourceNotFoundException ==========

    @Test
    void whenProductNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/products/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Produit non trouvé")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenWarehouseNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/warehouses/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Entrepôt non trouvé")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== TESTS WarehouseCodeAlreadyUsedException ==========

    @Test
    void whenWarehouseCodeAlreadyExists_shouldReturn409() throws Exception {
        warehouseRepository.save(new Warehouse("W-001", "Warehouse 1"));

        String warehouseJson = """
            {
                "code": "W-001",
                "name": "Duplicate Warehouse"
            }
            """;

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(warehouseJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("code")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== TESTS StockUnavailableException ==========

    @Test
    void whenStockInsufficient_shouldReturn400() throws Exception {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-STOCK", "Warehouse"));
        Product product = productRepository.save(
                new Product("SKU-STOCK", "Product", "Description", new BigDecimal("10.00"), true)
        );

        String movementJson = String.format("""
            {
                "productId": %d,
                "warehouseId": %d,
                "quantity": 100
            }
            """, product.getId(), warehouse.getId());

        mockMvc.perform(post("/api/inventory/outbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movementJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Stock")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== TESTS Validation Errors ==========

    @Test
    void whenInvalidProductData_shouldReturn400WithValidationErrors() throws Exception {
        String invalidProductJson = """
            {
                "sku": "",
                "name": "",
                "price": -10
            }
            """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidProductJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Erreur de validation"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== TESTS sans authentification ==========

    @Test
    void whenNoAuthenticationOnRestrictedEndpoint_shouldReturn401() throws Exception {
        // Exemple si /api/products nécessite authentification
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }
}