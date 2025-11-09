package com.spring.digital_logistics.mapper;

import com.spring.digital_logistics.dto.response.shipment.ShipmentDTO;
import com.spring.digital_logistics.entity.Shipment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {
    ShipmentDTO toDto(Shipment shipment);
}
