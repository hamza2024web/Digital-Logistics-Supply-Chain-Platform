package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.request.product.ProductCreateDTO;
import com.spring.digital_logistics.dto.response.product.ProductDTO;
import com.spring.digital_logistics.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDto(Product product);
    Product toEntity(ProductCreateDTO productCreateDTO);
}
