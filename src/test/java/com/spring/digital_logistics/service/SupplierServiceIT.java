package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.supplier.SupplierCreateDTO;
import com.spring.digital_logistics.dto.response.supplier.SupplierDTO;
import com.spring.digital_logistics.entity.Supplier;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class SupplierServiceIT extends IntegrationTestBase {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SupplierRepository supplierRepository;

    // ========== TESTS createSupplier ==========

    @Test
    void createSupplier_withValidData_shouldSaveToDatabase() {
        SupplierCreateDTO createDTO = new SupplierCreateDTO();
        createDTO.setName("Fournisseur Test");
        createDTO.setEmail("supplier@test.com");
        createDTO.setPhone("0600000000");

        SupplierDTO result = supplierService.createSupplier(createDTO);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Fournisseur Test", result.getName());
        assertEquals("supplier@test.com", result.getEmail());

        Supplier savedSupplier = supplierRepository.findById(result.getId()).orElseThrow();
        assertEquals("Fournisseur Test", savedSupplier.getName());
    }

    // ========== TESTS getAllSupplier ==========

    @Test
    void getAllSupplier_shouldReturnAllSuppliers() {
        supplierRepository.save(createSupplier("Fournisseur 1", "f1@test.com"));
        supplierRepository.save(createSupplier("Fournisseur 2", "f2@test.com"));
        supplierRepository.save(createSupplier("Fournisseur 3", "f3@test.com"));

        List<SupplierDTO> suppliers = supplierService.getAllSupplier();

        assertNotNull(suppliers);
        assertEquals(3, suppliers.size());
    }

    @Test
    void getAllSupplier_whenNoSuppliers_shouldReturnEmptyList() {
        List<SupplierDTO> suppliers = supplierService.getAllSupplier();

        assertNotNull(suppliers);
        assertTrue(suppliers.isEmpty());
    }

    // ========== TESTS getSupplierById ==========

    @Test
    void getSupplierById_withExistingId_shouldReturnSupplier() {
        Supplier supplier = supplierRepository.save(
                createSupplier("Fournisseur Test", "test@test.com")
        );

        SupplierDTO result = supplierService.getSupplierById(supplier.getId());

        assertNotNull(result);
        assertEquals(supplier.getId(), result.getId());
        assertEquals("Fournisseur Test", result.getName());
    }

    @Test
    void getSupplierById_withNonExistingId_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> supplierService.getSupplierById(9999L));
    }

    // ========== TESTS updateSupplier ==========

    @Test
    void updateSupplier_withValidData_shouldUpdateDatabase() {
        Supplier supplier = supplierRepository.save(
                createSupplier("Ancien Nom", "ancien@test.com")
        );

        SupplierCreateDTO updateDTO = new SupplierCreateDTO();
        updateDTO.setName("Nouveau Nom");
        updateDTO.setEmail("nouveau@test.com");
        updateDTO.setPhone("0611111111");

        SupplierDTO result = supplierService.updateSupplier(supplier.getId(), updateDTO);

        assertNotNull(result);
        assertEquals("Nouveau Nom", result.getName());

        Supplier updatedSupplier = supplierRepository.findById(supplier.getId()).orElseThrow();
        assertEquals("Nouveau Nom", updatedSupplier.getName());
        assertEquals("nouveau@test.com", updatedSupplier.getEmail());
    }

    @Test
    void updateSupplier_withNonExistingId_shouldThrowException() {
        SupplierCreateDTO updateDTO = new SupplierCreateDTO();
        updateDTO.setName("Test");

        assertThrows(ResourceNotFoundException.class,
                () -> supplierService.updateSupplier(9999L, updateDTO));
    }

    // ========== TESTS deleteSupplier ==========

    @Test
    void deleteSupplier_withExistingId_shouldDeleteFromDatabase() {
        Supplier supplier = supplierRepository.save(
                createSupplier("À Supprimer", "delete@test.com")
        );

        Long supplierId = supplier.getId();
        assertTrue(supplierRepository.existsById(supplierId));

        supplierService.deleteSupplier(supplierId);

        assertFalse(supplierRepository.existsById(supplierId));
    }

    @Test
    void deleteSupplier_withNonExistingId_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> supplierService.deleteSupplier(9999L));
    }

    @Test
    void deleteSupplier_shouldNotAffectOtherSuppliers() {
        Supplier supplier1 = supplierRepository.save(createSupplier("Fournisseur 1", "s1@test.com"));
        Supplier supplier2 = supplierRepository.save(createSupplier("Fournisseur 2", "s2@test.com"));

        supplierService.deleteSupplier(supplier1.getId());

        assertFalse(supplierRepository.existsById(supplier1.getId()));
        assertTrue(supplierRepository.existsById(supplier2.getId()));
    }

    // ========== Helper Methods ==========

    private Supplier createSupplier(String name, String email) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhone("0600000000");
        return supplier;
    }
}