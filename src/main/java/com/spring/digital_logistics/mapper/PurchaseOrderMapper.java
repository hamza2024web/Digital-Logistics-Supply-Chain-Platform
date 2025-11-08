package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.response.purchase.PurchaseOrderDTO;
import com.spring.digital_logistics.entity.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {PurchaseOrderLineMapper.class})
public interface PurchaseOrderMapper {

    PurchaseOrderMapper INSTANCE = Mappers.getMapper(PurchaseOrderMapper.class);

    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    @Mapping(source = "destinationWarehouse.id", target = "destinationWarehouseId")
    @Mapping(source = "destinationWarehouse.code", target = "destinationWarehouseCode")
    @Mapping(source = "creationDate", target = "createdAt")
    PurchaseOrderDTO toDto(PurchaseOrder purchaseOrder);

}