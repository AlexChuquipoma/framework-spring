package ec.edu.ups.icc.fundamentos01.products.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;

import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;

import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;

import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDto> create(@Valid @RequestBody CreateProductDto dto) {
        ProductResponseDto created = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ============== ENDPOINTS DE CONSULTA ==============

    /**
     * Listar TODOS los productos (sin paginación) - SOLO ADMIN
     * GET /api/products
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDto>> findAll() {
        List<ProductResponseDto> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    // ============== PAGINACIÓN BÁSICA ==============

    /**
     * Lista todos los productos con paginación básica
     * Ejemplo: GET /api/products/paginated?page=0&size=10&sort=name,asc
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<ProductResponseDto>> findAllPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String[] sort) {

        Page<ProductResponseDto> products = productService.findAll(page, size, sort);
        return ResponseEntity.ok(products);
    }

    // ============== PAGINACIÓN CON SLICE (PERFORMANCE) ==============

    /**
     * Lista productos usando Slice para mejor performance
     * Ejemplo: GET /api/products/slice?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping("/slice")
    public ResponseEntity<Slice<ProductResponseDto>> findAllSlice(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String[] sort) {

        Slice<ProductResponseDto> products = productService.findAllSlice(page, size, sort);
        return ResponseEntity.ok(products);
    }

    // ============== PAGINACIÓN CON FILTROS ==============

    /**
     * Lista productos con filtros y paginación
     * Ejemplo: GET /api/products/search?name=laptop&minPrice=500&page=0&size=5
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> findWithFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String[] sort) {

        Page<ProductResponseDto> products = productService.findWithFilters(
                name, minPrice, maxPrice, categoryId, page, size, sort);

        return ResponseEntity.ok(products);
    }

    // ============== USUARIOS CON SUS PRODUCTOS PAGINADOS ==============

    /**
     * Productos de un usuario específico con paginación
     * Ejemplo: GET /api/products/user/1?page=0&size=5&sort=price,desc
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ProductResponseDto>> findByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String[] sort) {

        Page<ProductResponseDto> products = productService.findByUserIdWithFilters(
                userId, name, minPrice, maxPrice, categoryId, page, size, sort);

        return ResponseEntity.ok(products);
    }

    // ============== OTROS ENDPOINTS EXISTENTES ==============

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> findById(@PathVariable("id") String id) {
        ProductResponseDto product = productService.findById(Long.parseLong(id));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDto>> findByCategoryId(@PathVariable("categoryId") Long categoryId) {
        List<ProductResponseDto> products = productService.findByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        ProductResponseDto updated = productService.update(id, dto, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        productService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}