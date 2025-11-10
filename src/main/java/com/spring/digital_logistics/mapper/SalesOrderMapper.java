package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.response.salesOrder.SalesOrderDTO;
import com.spring.digital_logistics.dto.response.salesOrder.SalesOrderLineDTO;
import com.spring.digital_logistics.entity.SalesOrder;
import com.spring.digital_logistics.entity.SalesOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ShipmentMapper.class})
public interface SalesOrderMapper {
    SalesOrderMapper INSTANCE = Mappers.getMapper(SalesOrderMapper.class);

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.lastName", target = "clientUsername")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.code", target = "warehouseCode")
    SalesOrderDTO toDto(SalesOrder salesOrder);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.price", target = "price")
    SalesOrderLineDTO lineToDto(SalesOrderLine salesOrderLine);
}
