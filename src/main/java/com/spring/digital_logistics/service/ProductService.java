package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.product.ProductCreateDTO;
import com.spring.digital_logistics.dto.response.product.ProductDTO;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.exception.BusinessException;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.ProductMapper;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.SalesOrderLineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util. List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final SalesOrderLineRepository salesOrderLineRepository;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, SalesOrderLineRepository salesOrderLineRepository){
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.salesOrderLineRepository = salesOrderLineRepository;
    }

    public List<ProductDTO> getAllProducts(){
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    public ProductDTO getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID : " + id));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductCreateDTO createDTO){
        // Vérifier si le SKU existe déjà
        if (productRepository.existsBySku(createDTO.getSku())) {
            throw new BusinessException("Ce SKU est déjà utilisé :  " + createDTO.getSku());
        }

        Product product = productMapper.toEntity(createDTO);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductCreateDTO createDTO){
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID : " + id));

        if (! existingProduct.getSku().equals(createDTO.getSku()) &&
                productRepository.existsBySku(createDTO.getSku())) {
            throw new BusinessException("Ce SKU est déjà utilisé :  " + createDTO.getSku());
        }

        existingProduct.setSku(createDTO.getSku());
        existingProduct.setName(createDTO.getName());
        existingProduct.setImage(createDTO.getImage());
        existingProduct.setPrice(createDTO.getPrice());
        existingProduct.setActive(createDTO.getActive());

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    public ProductDTO updateProductStatus(Long id, boolean active){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID : " + id));

        product.setActive(active);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id){
        if (!productRepository. existsById(id)){
            throw new ResourceNotFoundException("Produit non trouvé avec l'ID : " + id);
        }

        if (salesOrderLineRepository.existsByProductId(id)) {
             throw new BusinessException("Impossible de supprimer ce produit car il est utilisé dans des commandes");
        }

        productRepository.deleteById(id);
    }

    public boolean existsBySku(String sku){
        return productRepository.existsBySku(sku);
    }

    public List<ProductDTO> searchProducts(String keyword){
        List<Product> products = productRepository.findBySkuContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword);
        return products.stream()
                .map(productMapper:: toDto)
                .toList();
    }

    public List<ProductDTO> getActiveProducts(){
        List<Product> products = productRepository.findByActiveTrue();
        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }
}