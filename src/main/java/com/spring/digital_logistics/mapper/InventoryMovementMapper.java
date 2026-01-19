package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.response.inventory.InventoryMovementDTO;
import com.spring.digital_logistics.entity.InventoryMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface InventoryMovementMapper  {
    InventoryMovementMapper INSTANCE =
            Mappers.getMapper(InventoryMovementMapper.class);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.code", target = "warehouseCode")
    InventoryMovementDTO toDto(InventoryMovement movement);
}
