package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.response.inventory.InventoryDTO;
import com.spring.digital_logistics.entity.Inventory;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

public interface InventoryMapper {

    InventoryMapper INSTANCE = Mappers.getMapper(InventoryMapper.class);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.code", target = "warehouseCode")
    InventoryDTO toDTO(Inventory inventory);
}
