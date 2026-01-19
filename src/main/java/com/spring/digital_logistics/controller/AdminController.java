package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderCreateDTO;
import com. spring.digital_logistics.dto. request.shipment.ShipmentCreateDTO;
import com.spring.digital_logistics.dto.request.supplier.SupplierCreateDTO;
import com.spring. digital_logistics.dto.request. user.AdminUserCreateDTO;
import com.spring.digital_logistics.dto.request.product.ProductCreateDTO;
import com.spring.digital_logistics.dto.request.warehouse.WarehouseCreateDTO;
import com.spring. digital_logistics.dto.response. product.ProductDTO;
import com.spring.digital_logistics.dto.response.purchase.PurchaseOrderDTO;
import com.spring.digital_logistics.dto.response.salesOrder.SalesOrderDTO;
import com.spring.digital_logistics.dto.response.shipment.ShipmentDTO;
import com.spring.digital_logistics.dto.response.supplier.SupplierDTO;
import com.spring.digital_logistics.dto.response.user.UserDTO;
import com.spring. digital_logistics.dto.response. warehouse.WarehouseDTO;
import com.spring.digital_logistics.service.*;
import io. swagger.v3.oas. annotations.Operation;
import jakarta. validation.Valid;
import org. springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework. security.access.prepost.PreAuthorize;
import org. springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final PurchaseOrderService purchaseOrderService;
    private final ShipmentService shipmentService;
    private final SalesOrderService salesOrderService;

    public AdminController(UserService userService, WarehouseService warehouseService, ProductService productService, SupplierService supplierService, PurchaseOrderService purchaseOrderService, ShipmentService shipmentService, SalesOrderService salesOrderService){
        this.userService = userService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.supplierService = supplierService;
        this.purchaseOrderService = purchaseOrderService;
        this.shipmentService = shipmentService;
        this.salesOrderService = salesOrderService;
    }

    // ========== USERS ==========

    @Operation(summary = "Obtenir tous les utilisateurs")
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDTO>> listAllUsers(){
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity. ok(users);
    }

    @Operation(summary = "Créer un utilisateur")
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody AdminUserCreateDTO createDTO){
        UserDTO createdUser = userService.createUserByAdmin(createDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Modifier le statut d'un utilisateur")
    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> updateUserActivationStatus(@PathVariable Long id, @RequestParam boolean isActive){
        UserDTO updatedUser = userService.updateUserStatus(id, isActive);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }

    // ========== PRODUCTS ==========

    @Operation(summary = "Créer un produit")
    @PostMapping("/products")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO createDTO){
        ProductDTO newProduct = productService.createProduct(createDTO);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtenir tous les produits")
    @GetMapping("/products")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<List<ProductDTO>> getAllProducts(){
        List<ProductDTO> products = productService. getAllProducts();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Obtenir un produit par ID")
    @GetMapping("/products/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id){
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Mettre à jour un produit")
    @PutMapping("/products/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO createDTO){
        ProductDTO updatedProduct = productService.updateProduct(id, createDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(summary = "Changer le statut d'un produit")
    @PatchMapping("/products/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductDTO> toggleProductStatus(@PathVariable Long id, @RequestParam boolean active){
        ProductDTO updatedProduct = productService.updateProductStatus(id, active);
        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(summary = "Supprimer un produit")
    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }

    // ========== WAREHOUSES ==========

    @Operation(summary = "Créer un entrepôt")
    @PostMapping("/warehouses")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<WarehouseDTO> createWarehouse(@Valid @RequestBody WarehouseCreateDTO createDTO){
        WarehouseDTO newWarehouse = warehouseService.createWarehouse(createDTO);
        return new ResponseEntity<>(newWarehouse, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtenir tous les entrepôts")
    @GetMapping("/warehouses")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouses(){
        List<WarehouseDTO> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity. ok(warehouses);
    }

    @Operation(summary = "Supprimer un entrepôt")
    @DeleteMapping("/warehouses/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWarehouse(@PathVariable Long id){
        warehouseService.deleteWarehouse(id);
    }

    @Operation(summary = "Obtenir un entrepôt par ID")
    @GetMapping("/warehouses/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<WarehouseDTO> getWarehouseById(@PathVariable Long id){
        WarehouseDTO warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(warehouse);
    }

    @Operation(summary = "Mettre à jour un entrepôt")
    @PutMapping("/warehouses/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<WarehouseDTO> updateWarehouse(@PathVariable Long id, @Valid @RequestBody WarehouseCreateDTO createDTO){
        WarehouseDTO updatedWarehouse = warehouseService.updateWarehouse(id, createDTO);
        return ResponseEntity.ok(updatedWarehouse);
    }
    // ========== SUPPLIERS ==========

    @Operation(summary = "Créer un fournisseur")
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
        List<SupplierDTO> suppliers = supplierService. getAllSupplier();
        return ResponseEntity.ok(suppliers);
    }

    @Operation(summary = "Obtenir un fournisseur par ID")
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
        SupplierDTO updatedSupplier = supplierService.updateSupplier(id, createDTO);
        return ResponseEntity.ok(updatedSupplier);
    }

    @Operation(summary = "Supprimer un fournisseur")
    @DeleteMapping("/suppliers/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSupplier(@PathVariable Long id){
        supplierService.deleteSupplier(id);
    }

    // ========== PURCHASE ORDERS ==========
    @Operation(summary = "Obtenir tous les bons de commande")
    @GetMapping("/purchase-orders")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<List<PurchaseOrderDTO>> getAllPurchaseOrders(){
        List<PurchaseOrderDTO> orders = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Créer un bon de commande")
    @PostMapping("/purchase-orders")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@Valid @RequestBody PurchaseOrderCreateDTO createDTO){
        PurchaseOrderDTO newPurchaseOrder = purchaseOrderService.createPurchaseOrder(createDTO);
        return new ResponseEntity<>(newPurchaseOrder, HttpStatus.CREATED);
    }

    @Operation(summary = "Envoyer un bon de commande")
    @PatchMapping("/purchase-orders/{id}/send")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PurchaseOrderDTO> sendPurchaseOrder(@PathVariable Long id){
        PurchaseOrderDTO updatedOrder = purchaseOrderService.sendPurchaseOrder(id);
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Obtenir un bon de commande par ID")
    @GetMapping("/purchase-orders/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrderDTO order = purchaseOrderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Annuler un bon de commande")
    @PatchMapping("/purchase-orders/{id}/cancel")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PurchaseOrderDTO> cancelPurchaseOrder(@PathVariable Long id) {
        PurchaseOrderDTO cancelledOrder = purchaseOrderService.cancelPurchaseOrder(id);
        return ResponseEntity.ok(cancelledOrder);
    }

    // ========== SHIPMENTS ==========

    @Operation(summary = "Créer une expédition pour une commande")
    @PostMapping("/sales-orders/{orderId}/shipments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentDTO> createShipmentForOrder(@PathVariable Long orderId, @Valid @RequestBody ShipmentCreateDTO createDTO) {
        ShipmentDTO shipment = shipmentService.createAndPlanShipment(orderId, createDTO);
        return new ResponseEntity<>(shipment, HttpStatus.CREATED);
    }

    @Operation(summary = "Expédier une commande")
    @PatchMapping("/sales-orders/{orderId}/ship")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentDTO> shipOrder(@PathVariable Long orderId) {
        ShipmentDTO shipped = shipmentService.shipOrder(orderId);
        return ResponseEntity.ok(shipped);
    }

    @Operation(summary = "Marquer une commande comme livrée")
    @PatchMapping("/sales-orders/{orderId}/deliver")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentDTO> deliverOrder(@PathVariable Long orderId) {
        ShipmentDTO delivered = shipmentService.deliverOrder(orderId);
        return ResponseEntity.ok(delivered);
    }

    @Operation(summary = "Obtenir toutes les commandes clients")
    @GetMapping("/sales-orders")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<List<SalesOrderDTO>> getAllSalesOrders() {
        List<SalesOrderDTO> orders = salesOrderService.getAllSalesOrders();
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Obtenir une commande par ID")
    @GetMapping("/sales-orders/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<SalesOrderDTO> getSalesOrderById(@PathVariable Long id) {
        SalesOrderDTO order = salesOrderService.getSalesOrderById(id);
        return ResponseEntity.ok(order);
    }
}