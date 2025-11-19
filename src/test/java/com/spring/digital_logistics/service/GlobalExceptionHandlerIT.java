package com.spring.digital_logistics.exception;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.UserRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
public class GlobalExceptionHandlerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String clientToken;

    @BeforeEach
    void setUp() throws Exception {
        // Créer un utilisateur admin
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        // Créer un utilisateur client
        User client = new User();
        client.setEmail("client@test.com");
        client.setPassword(passwordEncoder.encode("password123"));
        client.setFirstName("Client");
        client.setLastName("User");
        client.setRole(Role.CLIENT);
        client.setActive(true);
        userRepository.save(client);

        // Obtenir les tokens JWT
        String loginAdminJson = "{\"email\":\"admin@test.com\",\"password\":\"password123\"}";
        String adminResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginAdminJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        adminToken = extractToken(adminResponse);

        String loginClientJson = "{\"email\":\"client@test.com\",\"password\":\"password123\"}";
        String clientResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginClientJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        clientToken = extractToken(clientResponse);
    }

    // ========== TESTS ResourceNotFoundException ==========

    @Test
    void whenProductNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/products/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Produit non trouvé")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenWarehouseNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/warehouses/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Entrepôt non trouvé")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenUserNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/users/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Utilisateur non trouvé")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== TESTS EmailAlreadyUsedException ==========

    @Test
    void whenEmailAlreadyExists_shouldReturn409() throws Exception {
        // L'email admin@test.com existe déjà
        String registerJson = """
            {
                "email": "admin@test.com",
                "password": "password123",
                "firstName": "Test",
                "lastName": "User"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("email")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== TESTS WarehouseCodeAlreadyUsedException ==========

    @Test
    void whenWarehouseCodeAlreadyExists_shouldReturn409() throws Exception {
        // Créer un entrepôt
        warehouseRepository.save(new Warehouse("W-001", "Warehouse 1"));

        String warehouseJson = """
            {
                "code": "W-001",
                "name": "Duplicate Warehouse"
            }
            """;

        mockMvc.perform(post("/api/warehouses")
                        .header("Authorization", "Bearer " + adminToken)
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

        // Tenter de faire un mouvement sortant sans stock
        String movementJson = String.format("""
            {
                "productId": %d,
                "warehouseId": %d,
                "quantity": 100
            }
            """, product.getId(), warehouse.getId());

        mockMvc.perform(post("/api/inventory/outbound")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movementJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Stock")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== TESTS SecurityException ==========

    @Test
    void whenUnauthorizedOrderAccess_shouldReturn403() throws Exception {
        // Créer une commande pour le client
        User otherClient = new User();
        otherClient.setEmail("other@test.com");
        otherClient.setPassword(passwordEncoder.encode("password"));
        otherClient.setFirstName("Other");
        otherClient.setLastName("Client");
        otherClient.setRole(Role.CLIENT);
        otherClient.setActive(true);
        userRepository.save(otherClient);

        // Le client essaie d'accéder à une commande qui n'est pas la sienne
        // (Vous devrez adapter ce test selon votre implémentation)
        // Exemple : GET /api/sales-orders/1 avec un mauvais client
    }

    // ========== TESTS AccessDeniedException ==========

    @Test
    void whenClientAccessAdminEndpoint_shouldReturn403() throws Exception {
        // Un client essaie d'accéder à un endpoint admin
        String userJson = """
            {
                "email": "newadmin@test.com",
                "password": "password123",
                "firstName": "New",
                "lastName": "Admin",
                "role": "ADMIN"
            }
            """;

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(containsString("Accès refusé")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== TESTS IllegalStateException ==========

    @Test
    void whenInvalidOrderState_shouldReturn400() throws Exception {
        // Créer une commande et essayer de faire une action invalide
        // (Par exemple, expédier une commande non réservée)
        // Vous devrez adapter selon votre implémentation
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
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidProductJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Erreur de validation"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenInvalidRegistrationData_shouldReturn400() throws Exception {
        String invalidRegisterJson = """
            {
                "email": "invalid-email",
                "password": "123",
                "firstName": "",
                "lastName": ""
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRegisterJson))
                .andExpect(status().isBadRequest());
    }

    // ========== TESTS Exception générique ==========

    @Test
    void whenUnexpectedError_shouldReturn500() throws Exception {
        // Pour tester l'exception générique, vous pourriez:
        // 1. Créer un endpoint de test qui lance une exception
        // 2. Ou simuler une erreur de base de données
        // 3. Ou utiliser un mock pour forcer une exception

        // Exemple : essayer de créer un produit avec des données qui causent une erreur DB
    }

    // ========== TESTS sans authentification ==========

    @Test
    void whenNoAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenInvalidToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    // ========== Helper Methods ==========

    private String extractToken(String response) {
        // Extraire le token de la réponse JSON
        // Format: {"token":"..."}
        return response.substring(
                response.indexOf("\"token\":\"") + 9,
                response.indexOf("\"", response.indexOf("\"token\":\"") + 9)
        );
    }
}