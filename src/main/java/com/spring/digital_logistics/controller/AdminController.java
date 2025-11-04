package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.user.AdminUserCreateDTO;
import com.spring.digital_logistics.dto.request.product.ProductCreateDTO;
import com.spring.digital_logistics.dto.request.warehouse.WarehouseCreateDTO;
import com.spring.digital_logistics.dto.response.product.ProductDTO;
import com.spring.digital_logistics.dto.response.user.UserDTO;
import com.spring.digital_logistics.dto.response.warehouse.WarehouseDTO;
import com.spring.digital_logistics.service.ProductService;
import com.spring.digital_logistics.service.UserService;
import com.spring.digital_logistics.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    public AdminController(UserService userService, WarehouseService warehouseService, ProductService productService){
        this.userService = userService;
        this.warehouseService = warehouseService;
        this.productService = productService;
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
}
