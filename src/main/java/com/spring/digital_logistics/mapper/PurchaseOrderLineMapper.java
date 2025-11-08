package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderLineCreateDTO;
import com.spring.digital_logistics.dto.response.purchase.PurchaseOrderLineDTO;
import com.spring.digital_logistics.entity.PurchaseOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PurchaseOrderLineMapper {

    PurchaseOrderLineMapper INSTANCE = Mappers.getMapper(PurchaseOrderLineMapper.class);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "quantity", target = "quantityOrdered")
    PurchaseOrderLineDTO toDto(PurchaseOrderLine line);

    PurchaseOrderLine toEntity(PurchaseOrderLineCreateDTO createDTO);
}