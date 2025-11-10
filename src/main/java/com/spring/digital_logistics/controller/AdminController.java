package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderCreateDTO;
import com.spring.digital_logistics.dto.request.shipment.ShipmentCreateDTO;
import com.spring.digital_logistics.dto.request.supplier.SupplierCreateDTO;
import com.spring.digital_logistics.dto.request.user.AdminUserCreateDTO;
import com.spring.digital_logistics.dto.request.product.ProductCreateDTO;
import com.spring.digital_logistics.dto.request.warehouse.WarehouseCreateDTO;
import com.spring.digital_logistics.dto.response.product.ProductDTO;
import com.spring.digital_logistics.dto.response.purchase.PurchaseOrderDTO;
import com.spring.digital_logistics.dto.response.shipment.ShipmentDTO;
import com.spring.digital_logistics.dto.response.supplier.SupplierDTO;
import com.spring.digital_logistics.dto.response.user.UserDTO;
import com.spring.digital_logistics.dto.response.warehouse.WarehouseDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.service.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final PurchaseOrderService purchaseOrderService;
    private final ShipmentService shipmentService;

    public AdminController(UserService userService, WarehouseService warehouseService, ProductService productService, SupplierService supplierService, PurchaseOrderService purchaseOrderService, ShipmentService shipmentService){
        this.userService = userService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.supplierService = supplierService;
        this.purchaseOrderService = purchaseOrderService;
        this.shipmentService = shipmentService;
    }

    @Operation(summary = "Obtenir Tous les Utilisateurs")
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDTO>> listAllUsers(){
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Crée un utilisateur")
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody AdminUserCreateDTO createDTO){
        UserDTO createdUser = userService.createUserByAdmin(createDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Modifier un utilisateur")
    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> updateUserActivationStatus(@PathVariable Long id, @RequestParam boolean isActive){
        UserDTO updatedUser = userService.updateUserStatus(id,isActive);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }

    @Operation(summary = "Crée un produit")
    @PostMapping("/prodcuts")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO createDTO){
        ProductDTO newProduct = productService.createProduct(createDTO);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtenir tous les produits")
    @GetMapping("/products")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ProductDTO>> getAllProducts(){
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "supprimer un produit")
    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }

    @Operation(summary = "Crée un entrepôts")
    @PostMapping("/Warehouses")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<WarehouseDTO> createWarehouse(@Valid @RequestBody WarehouseCreateDTO createDTO){
        WarehouseDTO newWarehouse = warehouseService.createWarehouse(createDTO);
        return new ResponseEntity<>(newWarehouse, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtenir les entrepôts")
    @GetMapping("/Warehouse")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouse(){
        List<WarehouseDTO> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(warehouses);
    }

    @Operation(summary = "Supprimer un entrepôts")
    @DeleteMapping("Warehouse/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWarehouse(@PathVariable Long id){
        warehouseService.deleteWarehouse(id);
    }

    @Operation(summary = "Crée un Fournisseur")
    @PostMapping("/suppliers")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierCreateDTO createDTO){
        SupplierDTO newSupplier = supplierService.createSupplier(createDTO);
        return new ResponseEntity<>(newSupplier, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtenir tous les fournisseurs")
    @GetMapping("/suppliers")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers(){
        List<SupplierDTO> suppliers = supplierService.getAllSupplier();
        return ResponseEntity.ok(suppliers);
    }

    @Operation(summary = "Obtenir un Fournisseur")
    @GetMapping("/suppliers/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id){
        SupplierDTO supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @Operation(summary = "Modifier un fournisseur")
    @PutMapping("/suppliers/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierCreateDTO createDTO){
        SupplierDTO updateSupplier = supplierService.updateSupplier(id,createDTO);
        return ResponseEntity.ok(updateSupplier);
    }

    @Operation(summary = "Supprimer un Fournisseur")
    @DeleteMapping("/suppliers/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteSupplier(@PathVariable Long id){
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/purchase-orders/")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@Valid @RequestBody PurchaseOrderCreateDTO createDTO){
        PurchaseOrderDTO newPurchaseOrder = purchaseOrderService.createPurchaseOrder(createDTO);
        return new ResponseEntity<>(newPurchaseOrder, HttpStatus.CREATED);
    }

    @PatchMapping("/purchase-order/{id}/send")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PurchaseOrderDTO> sendPurchaseOrder(@PathVariable Long id){
        PurchaseOrderDTO updateOrder = purchaseOrderService.sendPurchaseOrder(id);
        return ResponseEntity.ok(updateOrder);
    }

    @PostMapping("/sales-orders/{orderId}/shipments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentDTO> createShipmentForOrder(@PathVariable Long orderId, @Valid @RequestBody ShipmentCreateDTO createDTO) {
        ShipmentDTO shipment = shipmentService.createAndPlanShipment(orderId, createDTO);
        return new ResponseEntity<>(shipment, HttpStatus.CREATED);
    }

    @PatchMapping("/sales-orders/{orderId}/ship")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentDTO> shipOrder(@PathVariable Long orderId) {
        ShipmentDTO shipped = shipmentService.shipOrder(orderId);
        return ResponseEntity.ok(shipped);
    }

    @PatchMapping("/sales-orders/{orderId}/deliver")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentDTO> deliverOrder(@PathVariable Long orderId) {
        ShipmentDTO delivered = shipmentService.deliverOrder(orderId);
        return ResponseEntity.ok(delivered);
    }

    @PatchMapping("/products/{sku}/desactiver")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Optional<ProductDTO>> desactivateProduct(@PathVariable String sku , @AuthenticationPrincipal User admin){
        Optional<ProductDTO> desactivateProduct = productService.desactivateProduct(sku,admin);
        return ResponseEntity.ok(desactivateProduct);
    }
}
