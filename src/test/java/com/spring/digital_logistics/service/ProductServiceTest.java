package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.product.ProductCreateDTO;
import com.spring.digital_logistics.dto.response.product.ProductDTO;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.ProductMapper;
import com.spring.digital_logistics.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;
    private ProductCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setSku("PROD-001");
        product.setName("Test Product");

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setSku("PROD-001");
        productDTO.setName("Test Product");

        createDTO = new ProductCreateDTO();
        createDTO.setSku("PROD-001");
        createDTO.setName("Test Product");
        createDTO.setPrice(new BigDecimal("99.99"));
        createDTO.setActive(true);
    }

    // ========== TESTS getAllProducts ==========

    @Test
    void getAllProducts_shouldReturnListOfProducts() {
        Product product2 = new Product();
        product2.setId(2L);
        product2.setSku("PROD-002");

        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setId(2L);
        productDTO2.setSku("PROD-002");

        when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));
        when(productMapper.toDto(product)).thenReturn(productDTO);
        when(productMapper.toDto(product2)).thenReturn(productDTO2);

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PROD-001", result.get(0).getSku());
        assertEquals("PROD-002", result.get(1).getSku());
        verify(productRepository).findAll();
        verify(productMapper, times(2)).toDto(any(Product.class));
    }

    @Test
    void getAllProducts_whenNoProducts_shouldReturnEmptyList() {
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findAll();
    }

    // ========== TESTS createProduct ==========

    @Test
    void createProduct_shouldCreateAndReturnProduct() {
        when(productMapper.toEntity(createDTO)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDTO);

        ProductDTO result = productService.createProduct(createDTO);

        assertNotNull(result);
        assertEquals("PROD-001", result.getSku());
        assertEquals("Test Product", result.getName());

        verify(productMapper).toEntity(createDTO);
        verify(productRepository).save(product);
        verify(productMapper).toDto(product);
    }

    @Test
    void createProduct_shouldSaveWithCorrectData() {
        when(productMapper.toEntity(createDTO)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDTO);

        productService.createProduct(createDTO);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertNotNull(savedProduct);
    }

    // ========== TESTS deleteProduct ==========

    @Test
    void deleteProduct_whenProductExists_shouldDeleteSuccessfully() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_whenProductDoesNotExist_shouldThrowException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(999L));

        assertTrue(exception.getMessage().contains("Produit non trouvé"));
        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProduct_shouldNotDeleteWhenExceptionThrown() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(1L));

        verify(productRepository, never()).deleteById(1L);
    }
}