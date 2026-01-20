package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.reporitory.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.BusinessException;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.SecureUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;

import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.models.Product;
import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.repository.ProductRepository;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public ProductServiceImpl(ProductRepository productRepo, UserRepository userRepo,
            CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepo.findAll()
                .stream()
                .map(Product::fromEntity) // ProductEntity → Product
                .map(Product::toResponseDto) // Product → ProductResponseDto
                .toList();
    }

    @Override
    public ProductResponseDto findById(Long id) {
        return productRepo.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));
    }

    @Override
    public List<ProductResponseDto> findByUserId(Long userId) {

        // Validar que el usuario existe
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + userId);
        }

        return productRepo.findByOwnerId(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public List<ProductResponseDto> findByCategoryId(Long categoryId) {

        // Validar que la categoría existe
        if (!categoryRepo.existsById(categoryId)) {
            throw new NotFoundException("Categoría no encontrada con ID: " + categoryId);
        }

        return productRepo.findByCategoryId(categoryId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto create(CreateProductDto dto) {

        // 1. VALIDAR USER
        UserEntity owner = userRepo.findById(dto.userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // 2. VALIDAR Y OBTENER CATEGORÍAS
        Set<CategoryEntity> categories = validateAndGetCategories(dto.categoryIds);

        // 3. CREAR DOMINIO
        Product product = Product.fromDto(dto);

        // 4. CREAR ENTIDAD CON RELACIONES N:N
        ProductEntity entity = product.toEntity(owner);
        entity.setCategories(categories);

        // 5. PERSISTIR
        ProductEntity saved = productRepo.save(entity);

        return toResponseDto(saved);
    }

    @Override
    public ProductResponseDto update(Long id, UpdateProductDto dto) {

        ProductEntity existing = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        // Validar nuevas categorías
        Set<CategoryEntity> newCategories = validateAndGetCategories(dto.categoryIds);

        // Actualizar usando dominio
        Product product = Product.fromEntity(existing);
        product.update(dto);

        // 3. ACTUALIZAR USANDO Instancia de entidad
        existing.setDescription(dto.description != null ? dto.description : existing.getDescription());
        existing.setName(dto.name != null ? dto.name : existing.getName());
        existing.setPrice(dto.price != null ? dto.price : existing.getPrice());

        // IMPORTANTE: Limpiar categorías existentes y asignar nuevas
        existing.clearCategories();
        existing.setCategories(newCategories);

        ProductEntity saved = productRepo.save(existing);
        return toResponseDto(saved);
    }

    @Override
    public void delete(Long id) {

        ProductEntity product = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));

        // Eliminación física (también se puede implementar lógica)
        productRepo.delete(product);
    }
    // ============== MÉTODOS HELPER ==============

    /**
     * Convierte ProductEntity a DTO incluyendo categorías (N:N)
     * Usa estructura anidada para mejor semántica
     */
    private ProductResponseDto toResponseDto(ProductEntity entity) {
        ProductResponseDto dto = new ProductResponseDto();

        // Campos básicos
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.price = entity.getPrice();
        dto.description = entity.getDescription();

        // Crear objeto User anidado
        ProductResponseDto.UserSummaryDto userDto = new ProductResponseDto.UserSummaryDto();
        userDto.id = entity.getOwner().getId();
        userDto.name = entity.getOwner().getName();
        userDto.email = entity.getOwner().getEmail();
        dto.user = userDto;

        // Convertir Set<CategoryEntity> a List<CategoryResponseDto>
        dto.categories = entity.getCategories().stream()
                .map(this::toCategorySummary)
                .sorted((c1, c2) -> c1.name.compareTo(c2.name)) // Ordenar por nombre
                .toList();

        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();

        return dto;
    }

    private CategoryResponseDto toCategorySummary(CategoryEntity category) {
        CategoryResponseDto summary = new CategoryResponseDto();
        summary.id = category.getId();
        summary.name = category.getName();
        summary.description = category.getDescription();
        return summary;
    }

    // ============== MÉTODOS HELPER ==============

    private Set<CategoryEntity> validateAndGetCategories(Set<Long> categoryIds) {
        Set<CategoryEntity> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {
            CategoryEntity category = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + categoryId));
            categories.add(category);
        }

        return categories;
    }

}