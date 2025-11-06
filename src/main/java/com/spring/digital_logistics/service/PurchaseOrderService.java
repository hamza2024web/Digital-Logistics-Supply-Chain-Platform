package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.purchase.PuchaseOrderCreateDTO;
import com.spring.digital_logistics.dto.response.purchase.PurchaseOrderDTO;
import com.spring.digital_logistics.entity.PurchaseOrder;
import com.spring.digital_logistics.repository.PurchaseOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository){
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(PuchaseOrderCreateDTO createDTO){

    }
}
