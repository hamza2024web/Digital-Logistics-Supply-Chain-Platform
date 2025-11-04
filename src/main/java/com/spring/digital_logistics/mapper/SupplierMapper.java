package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.request.supplier.SupplierCreateDTO;
import com.spring.digital_logistics.dto.response.supplier.SupplierDTO;
import com.spring.digital_logistics.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierMapper INSTANCE = Mappers.getMapper(SupplierMapper.class);

    SupplierDTO toDto(Supplier supplier);

    Supplier toEntity(SupplierCreateDTO createDTO);

    void updateFromDto(SupplierCreateDTO dto, @MappingTarget Supplier entity);
}
