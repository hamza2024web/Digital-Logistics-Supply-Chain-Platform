package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.warehouse.WarehouseCreateDTO;
import com.spring.digital_logistics.dto.response.warehouse.WarehouseDTO;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.exception.WarehouseCodeAlreadyUsedException;
import com.spring.digital_logistics.mapper.WarehouseMapper;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse warehouse;
    private WarehouseCreateDTO createDTO;
    private WarehouseDTO warehouseDTO;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("W-001");
        warehouse.setName("Warehouse Paris");

        createDTO = new WarehouseCreateDTO();
        createDTO.setCode("W-001");
        createDTO.setName("Warehouse Paris");

        warehouseDTO = new WarehouseDTO();
        warehouseDTO.setId(1L);
        warehouseDTO.setCode("W-001");
        warehouseDTO.setName("Warehouse Paris");
    }

    // ========== TESTS getAllWarehouses ==========

    @Test
    void getAllWarehouses_shouldReturnAllWarehouses() {
        Warehouse warehouse2 = new Warehouse();
        warehouse2.setId(2L);
        warehouse2.setCode("W-002");
        warehouse2.setName("Warehouse Lyon");

        WarehouseDTO warehouseDTO2 = new WarehouseDTO();
        warehouseDTO2.setId(2L);
        warehouseDTO2.setCode("W-002");

        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse, warehouse2));
        when(warehouseMapper.toDTO(warehouse)).thenReturn(warehouseDTO);
        when(warehouseMapper.toDTO(warehouse2)).thenReturn(warehouseDTO2);

        List<WarehouseDTO> result = warehouseService.getAllWarehouses();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(warehouseRepository).findAll();
        verify(warehouseMapper, times(2)).toDTO(any(Warehouse.class));
    }

    @Test
    void getAllWarehouses_whenNoWarehouses_shouldReturnEmptyList() {
        when(warehouseRepository.findAll()).thenReturn(List.of());

        List<WarehouseDTO> result = warehouseService.getAllWarehouses();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(warehouseRepository).findAll();
    }

    // ========== TESTS createWarehouse ==========

    @Test
    void createWarehouse_withValidData_shouldCreateWarehouse() {
        when(warehouseRepository.existsByCode(createDTO.getCode())).thenReturn(false);
        when(warehouseMapper.toEntity(createDTO)).thenReturn(warehouse);
        when(warehouseRepository.save(warehouse)).thenReturn(warehouse);
        when(warehouseMapper.toDTO(warehouse)).thenReturn(warehouseDTO);

        WarehouseDTO result = warehouseService.createWarehouse(createDTO);

        assertNotNull(result);
        assertEquals(warehouseDTO.getCode(), result.getCode());
        verify(warehouseRepository).existsByCode(createDTO.getCode());
        verify(warehouseMapper).toEntity(createDTO);
        verify(warehouseRepository).save(warehouse);
        verify(warehouseMapper).toDTO(warehouse);
    }

    @Test
    void createWarehouse_withExistingCode_shouldThrowException() {
        when(warehouseRepository.existsByCode(createDTO.getCode())).thenReturn(true);

        assertThrows(WarehouseCodeAlreadyUsedException.class,
                () -> warehouseService.createWarehouse(createDTO));
        verify(warehouseRepository).existsByCode(createDTO.getCode());
        verify(warehouseRepository, never()).save(any());
    }

    // ========== TESTS deleteWarehouse ==========

    @Test
    void deleteWarehouse_withExistingId_shouldDeleteWarehouse() {
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(warehouseRepository).deleteById(1L);

        warehouseService.deleteWarehouse(1L);

        verify(warehouseRepository).existsById(1L);
        verify(warehouseRepository).deleteById(1L);
    }

    @Test
    void deleteWarehouse_withNonExistingId_shouldThrowException() {
        when(warehouseRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> warehouseService.deleteWarehouse(999L));
        verify(warehouseRepository).existsById(999L);
        verify(warehouseRepository, never()).deleteById(any());
    }
}