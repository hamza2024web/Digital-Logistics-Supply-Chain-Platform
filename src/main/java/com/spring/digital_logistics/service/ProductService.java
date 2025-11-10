package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.product.ProductCreateDTO;
import com.spring.digital_logistics.dto.response.product.ProductDTO;
import com.spring.digital_logistics.entity.Inventory;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.entity.SalesOrderLine;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.exception.BussinessException;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.ProductMapper;
import com.spring.digital_logistics.repository.InventoryRepository;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.SalesOrderLineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, SalesOrderLineRepository salesOrderLineRepository, InventoryRepository inventoryRepository, ProductMapper productMapper){
        this.productRepository = productRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.inventoryRepository = inventoryRepository;
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
            throw new ResourceNotFoundException("Produit non trouvé avec L'ID : " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public Optional<ProductDTO> desactivateProduct(String sku, User admin){
        if (admin.getRole() != Role.ADMIN){
            throw new SecurityException("vous n'etes pas autorisé de faire cette action .");
        }

        Product product = productRepository.findBySku(sku).orElseThrow(() -> new ResourceNotFoundException("le produit avec sku : " + sku + " non trouvé ."));

        SalesOrderLine salesOrder = salesOrderLineRepository.findByProductId(product.getId());

        if (salesOrder != null){
            if (salesOrder.getStatus() == SalesOrderLineStatus.CREATED || salesOrder.getStatus() == SalesOrderLineStatus.RESERVED){
                throw new BussinessException("Interdit de désactivé ce produit , Il ya une commande  de status Reserver ou crée liée a ce produit avec SKU : " + product.getSku());
            }
        }

        Inventory inventory = inventoryRepository.findByProductId(product.getId());

        if (inventory != null){
            if (inventory.getQtyOnHand()!= 0 || inventory.getQtyReserved() != 0){
                throw new BussinessException("Interdit de désactivé ce produit , Il ya une inventory utilise ce produit avec L'ID : " + product.getId());
            }
        }

        product.setActive(false);
        Product savedProduct = productRepository.save(product);

        return Optional.ofNullable(productMapper.toDto(savedProduct));
    }
}
