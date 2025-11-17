package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.product.ProductCreateDTO;
import com.spring.digital_logistics.dto.response.product.ProductDTO;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class ProductServiceIT extends IntegrationTestBase {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    // ========== TESTS getAllProducts ==========

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        productRepository.save(new Product("SKU-001", "Product 1", "Description 1", new BigDecimal("10.00"), true));
        productRepository.save(new Product("SKU-002", "Product 2", "Description 2", new BigDecimal("20.00"), true));
        productRepository.save(new Product("SKU-003", "Product 3", "Description 3", new BigDecimal("30.00"), false));

        List<ProductDTO> products = productService.getAllProducts();

        assertNotNull(products);
        assertEquals(3, products.size());
    }

    @Test
    void getAllProducts_whenNoProducts_shouldReturnEmptyList() {
        List<ProductDTO> products = productService.getAllProducts();

        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    // ========== TESTS createProduct ==========

    @Test
    void createProduct_withValidData_shouldSaveProduct() {
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setSku("SKU-CREATE-001");
        createDTO.setName("New Product");
        createDTO.setPrice(new BigDecimal("15.99"));
        createDTO.setActive(true);

        ProductDTO result = productService.createProduct(createDTO);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("SKU-CREATE-001", result.getSku());
        assertEquals("New Product", result.getName());
        assertEquals(new BigDecimal("15.99"), result.getPrice());
        assertTrue(result.isActive());

        Product savedProduct = productRepository.findById(result.getId()).orElseThrow();
        assertEquals("SKU-CREATE-001", savedProduct.getSku());
    }

    @Test
    void createProduct_withInactiveProduct_shouldSaveWithCorrectStatus() {
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setSku("SKU-INACTIVE-001");
        createDTO.setName("Inactive Product");
        createDTO.setPrice(new BigDecimal("25.00"));
        createDTO.setActive(false);

        ProductDTO result = productService.createProduct(createDTO);

        assertNotNull(result);
        assertFalse(result.isActive());

        Product savedProduct = productRepository.findById(result.getId()).orElseThrow();
        assertFalse(savedProduct.isActive());
    }

    // ========== TESTS deleteProduct ==========

    @Test
    void deleteProduct_withExistingId_shouldDeleteProduct() {
        Product product = productRepository.save(
                new Product("SKU-DELETE-001", "Product to Delete", "Description", new BigDecimal("10.00"), true)
        );

        Long productId = product.getId();
        assertTrue(productRepository.existsById(productId));

        productService.deleteProduct(productId);

        assertFalse(productRepository.existsById(productId));
    }

    @Test
    void deleteProduct_withNonExistingId_shouldThrowException() {
        Long nonExistingId = 9999L;

        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(nonExistingId));
    }

    @Test
    void deleteProduct_afterDeletion_shouldNotAppearInGetAll() {
        Product product1 = productRepository.save(
                new Product("SKU-001", "Product 1", "Description", new BigDecimal("10.00"), true)
        );
        Product product2 = productRepository.save(
                new Product("SKU-002", "Product 2", "Description", new BigDecimal("20.00"), true)
        );

        assertEquals(2, productService.getAllProducts().size());

        productService.deleteProduct(product1.getId());

        List<ProductDTO> remainingProducts = productService.getAllProducts();
        assertEquals(1, remainingProducts.size());
        assertEquals("SKU-002", remainingProducts.get(0).getSku());
    }
}