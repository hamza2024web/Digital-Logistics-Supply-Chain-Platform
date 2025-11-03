package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.request.WarehouseCreateDTO;
import com.spring.digital_logistics.dto.response.WarehouseDTO;
import com.spring.digital_logistics.entity.Warehouse;

public interface WarehouseMapper {
    WarehouseCreateDTO toDto(Warehouse warehouse);

    Warehouse toEntity(WarehouseCreateDTO warehouseCreateDTO);
}
