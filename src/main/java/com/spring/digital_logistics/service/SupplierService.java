package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.supplier.SupplierCreateDTO;
import com.spring.digital_logistics.dto.response.supplier.SupplierDTO;
import com.spring.digital_logistics.entity.Supplier;
import com.spring.digital_logistics.mapper.SupplierMapper;
import com.spring.digital_logistics.repository.SupplierRepository;
import org.springframework.stereotype.Service;

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

}
