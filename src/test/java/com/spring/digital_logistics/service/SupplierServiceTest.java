package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.supplier.SupplierCreateDTO;
import com.spring.digital_logistics.dto.response.supplier.SupplierDTO;
import com.spring.digital_logistics.entity.Supplier;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.SupplierMapper;
import com.spring.digital_logistics.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;
    private SupplierCreateDTO createDTO;
    private SupplierDTO supplierDTO;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Fournisseur Test");
        supplier.setEmail("supplier@test.com");
        supplier.setPhone("0600000000");

        createDTO = new SupplierCreateDTO();
        createDTO.setName("Fournisseur Test");
        createDTO.setEmail("supplier@test.com");
        createDTO.setPhone("0600000000");

        supplierDTO = new SupplierDTO();
        supplierDTO.setId(1L);
        supplierDTO.setName("Fournisseur Test");
        supplierDTO.setEmail("supplier@test.com");
    }

    // ========== TESTS createSupplier ==========

    @Test
    void createSupplier_withValidData_shouldCreateSupplier() {
        when(supplierMapper.toEntity(createDTO)).thenReturn(supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDTO);

        SupplierDTO result = supplierService.createSupplier(createDTO);

        assertNotNull(result);
        assertEquals(supplierDTO.getName(), result.getName());
        verify(supplierMapper).toEntity(createDTO);
        verify(supplierRepository).save(supplier);
        verify(supplierMapper).toDto(supplier);
    }

    // ========== TESTS getAllSupplier ==========

    @Test
    void getAllSupplier_shouldReturnAllSuppliers() {
        Supplier supplier2 = new Supplier();
        supplier2.setId(2L);
        supplier2.setName("Fournisseur 2");

        SupplierDTO supplierDTO2 = new SupplierDTO();
        supplierDTO2.setId(2L);
        supplierDTO2.setName("Fournisseur 2");

        List<Supplier> suppliers = List.of(supplier, supplier2);
        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDTO);
        when(supplierMapper.toDto(supplier2)).thenReturn(supplierDTO2);

        List<SupplierDTO> result = supplierService.getAllSupplier();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(supplierRepository).findAll();
        verify(supplierMapper, times(2)).toDto(any(Supplier.class));
    }

    @Test
    void getAllSupplier_whenNoSuppliers_shouldReturnEmptyList() {
        when(supplierRepository.findAll()).thenReturn(List.of());

        List<SupplierDTO> result = supplierService.getAllSupplier();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository).findAll();
    }

    // ========== TESTS getSupplierById ==========

    @Test
    void getSupplierById_withExistingId_shouldReturnSupplier() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDTO);

        SupplierDTO result = supplierService.getSupplierById(1L);

        assertNotNull(result);
        assertEquals(supplierDTO.getId(), result.getId());
        verify(supplierRepository).findById(1L);
        verify(supplierMapper).toDto(supplier);
    }

    @Test
    void getSupplierById_withNonExistingId_shouldThrowException() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> supplierService.getSupplierById(999L));
        verify(supplierRepository).findById(999L);
    }

    // ========== TESTS updateSupplier ==========

    @Test
    void updateSupplier_withValidData_shouldUpdateSupplier() {
        SupplierCreateDTO updateDTO = new SupplierCreateDTO();
        updateDTO.setName("Fournisseur Modifié");
        updateDTO.setEmail("nouveau@test.com");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        doNothing().when(supplierMapper).updateFromDto(updateDTO, supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.toDto(supplier)).thenReturn(supplierDTO);

        SupplierDTO result = supplierService.updateSupplier(1L, updateDTO);

        assertNotNull(result);
        verify(supplierRepository).findById(1L);
        verify(supplierMapper).updateFromDto(updateDTO, supplier);
        verify(supplierRepository).save(supplier);
    }

    @Test
    void updateSupplier_withNonExistingId_shouldThrowException() {
        SupplierCreateDTO updateDTO = new SupplierCreateDTO();
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> supplierService.updateSupplier(999L, updateDTO));
        verify(supplierRepository).findById(999L);
        verify(supplierRepository, never()).save(any());
    }

    // ========== TESTS deleteSupplier ==========

    @Test
    void deleteSupplier_withExistingId_shouldDeleteSupplier() {
        when(supplierRepository.existsById(1L)).thenReturn(true);
        doNothing().when(supplierRepository).deleteById(1L);

        supplierService.deleteSupplier(1L);

        verify(supplierRepository).existsById(1L);
        verify(supplierRepository).deleteById(1L);
    }

    @Test
    void deleteSupplier_withNonExistingId_shouldThrowException() {
        when(supplierRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> supplierService.deleteSupplier(999L));
        verify(supplierRepository).existsById(999L);
        verify(supplierRepository, never()).deleteById(any());
    }
}