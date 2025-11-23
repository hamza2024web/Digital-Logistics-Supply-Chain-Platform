package com.spring.digital_logistics.service;


import com.spring.digital_logistics.dto.request.warehouse.WarehouseCreateDTO;
import com.spring.digital_logistics.dto.response.warehouse.WarehouseDTO;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.exception.WarehouseCodeAlreadyUsedException; // Tu devras créer ce fichier
import com.spring.digital_logistics.mapper.WarehouseMapper;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    public WarehouseService(WarehouseRepository warehouseRepository, WarehouseMapper warehouseMapper) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseMapper = warehouseMapper;
    }

    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(warehouseMapper::toDTO)
                .toList();
    }

    @Transactional
    public WarehouseDTO createWarehouse(WarehouseCreateDTO createDTO) {
        if (warehouseRepository.existsByCode(createDTO.getCode())) {
            throw new WarehouseCodeAlreadyUsedException("Le code d'entrepôt '" + createDTO.getCode() + "' est déjà utilisé.");
        }
        Warehouse warehouse = warehouseMapper.toEntity(createDTO);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toDTO(savedWarehouse);
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Entrepôt non trouvé avec l'ID : " + id);
        }
        warehouseRepository.deleteById(id);
    }
}