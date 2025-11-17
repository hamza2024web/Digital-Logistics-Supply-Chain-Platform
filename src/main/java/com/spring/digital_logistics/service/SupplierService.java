package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.supplier.SupplierCreateDTO;
import com.spring.digital_logistics.dto.response.supplier.SupplierDTO;
import com.spring.digital_logistics.entity.Supplier;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.SupplierMapper;
import com.spring.digital_logistics.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;


    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    public SupplierDTO createSupplier(SupplierCreateDTO createDTO){
        Supplier supplier = supplierMapper.toEntity(createDTO);
        Supplier savedSupplier = supplierRepository.save(supplier);

        return supplierMapper.toDto(savedSupplier);
    }

    public List<SupplierDTO> getAllSupplier(){
        return supplierRepository.findAll().stream().map(supplierMapper::toDto).toList();
    }

    public SupplierDTO getSupplierById(Long id){
        Supplier supplier = supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));
        return supplierMapper.toDto(supplier);
    }

    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierCreateDTO updateDTO){
        Supplier existingSupplier = supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));

        supplierMapper.updateFromDto(updateDTO, existingSupplier);

        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        return supplierMapper.toDto(updatedSupplier);
    }

    public void deleteSupplier(Long id){
        if (!supplierRepository.existsById(id)){
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id);
        }
        supplierRepository.deleteById(id);
    }
}
