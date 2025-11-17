package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.warehouse.WarehouseCreateDTO;
import com.spring.digital_logistics.dto.response.warehouse.WarehouseDTO;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.exception.WarehouseCodeAlreadyUsedException;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class WarehouseServiceIT extends IntegrationTestBase {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseRepository warehouseRepository;

    // ========== TESTS getAllWarehouses ==========

    @Test
    void getAllWarehouses_shouldReturnAllWarehouses() {
        warehouseRepository.save(new Warehouse("W-001", "Warehouse Paris"));
        warehouseRepository.save(new Warehouse("W-002", "Warehouse Lyon"));
        warehouseRepository.save(new Warehouse("W-003", "Warehouse Marseille"));

        List<WarehouseDTO> warehouses = warehouseService.getAllWarehouses();

        assertNotNull(warehouses);
        assertEquals(3, warehouses.size());
    }

    @Test
    void getAllWarehouses_whenNoWarehouses_shouldReturnEmptyList() {
        List<WarehouseDTO> warehouses = warehouseService.getAllWarehouses();

        assertNotNull(warehouses);
        assertTrue(warehouses.isEmpty());
    }

    // ========== TESTS createWarehouse ==========

    @Test
    void createWarehouse_withValidData_shouldSaveToDatabase() {
        WarehouseCreateDTO createDTO = new WarehouseCreateDTO();
        createDTO.setCode("W-NEW-001");
        createDTO.setName("New Warehouse");

        WarehouseDTO result = warehouseService.createWarehouse(createDTO);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("W-NEW-001", result.getCode());
        assertEquals("New Warehouse", result.getName());

        Warehouse savedWarehouse = warehouseRepository.findById(result.getId()).orElseThrow();
        assertEquals("W-NEW-001", savedWarehouse.getCode());
    }

    @Test
    void createWarehouse_withExistingCode_shouldThrowException() {
        warehouseRepository.save(new Warehouse("W-EXIST", "Existing Warehouse"));

        WarehouseCreateDTO createDTO = new WarehouseCreateDTO();
        createDTO.setCode("W-EXIST");
        createDTO.setName("Duplicate Warehouse");

        assertThrows(WarehouseCodeAlreadyUsedException.class,
                () -> warehouseService.createWarehouse(createDTO));
    }

    // ========== TESTS deleteWarehouse ==========

    @Test
    void deleteWarehouse_withExistingId_shouldDeleteFromDatabase() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-DELETE", "To Delete"));
        Long warehouseId = warehouse.getId();

        assertTrue(warehouseRepository.existsById(warehouseId));

        warehouseService.deleteWarehouse(warehouseId);

        assertFalse(warehouseRepository.existsById(warehouseId));
    }

    @Test
    void deleteWarehouse_withNonExistingId_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> warehouseService.deleteWarehouse(9999L));
    }

    @Test
    void deleteWarehouse_shouldNotAffectOtherWarehouses() {
        Warehouse warehouse1 = warehouseRepository.save(new Warehouse("W-001", "Warehouse 1"));
        Warehouse warehouse2 = warehouseRepository.save(new Warehouse("W-002", "Warehouse 2"));

        warehouseService.deleteWarehouse(warehouse1.getId());

        assertFalse(warehouseRepository.existsById(warehouse1.getId()));
        assertTrue(warehouseRepository.existsById(warehouse2.getId()));
    }
}