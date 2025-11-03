package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.ProductCreateDTO;
import com.spring.digital_logistics.dto.response.ProductDTO;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.exception.ProductNotFoundException;
import com.spring.digital_logistics.mapper.ProductMapper;
import com.spring.digital_logistics.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,ProductMapper productMapper){
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductDTO> getAllProducts(){
        return productRepository.findAll().stream().map(productMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(ProductCreateDTO createDTO){
        Product product = productMapper.toEntity(createDTO);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id){
        if (!productRepository.existsById(id)){
            throw new ProductNotFoundException("Produit non trouvé avec L'ID : " + id);
        }
        productRepository.deleteById(id);
    }
}
