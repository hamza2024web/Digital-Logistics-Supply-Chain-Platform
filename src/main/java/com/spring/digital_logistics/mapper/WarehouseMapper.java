package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.request.WarehouseCreateDTO;
import com.spring.digital_logistics.dto.response.WarehouseDTO;
import com.spring.digital_logistics.entity.Warehouse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseDTO toDTO(Warehouse warehouse);

    Warehouse toEntity(WarehouseCreateDTO warehouseCreateDTO);
}
