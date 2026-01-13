package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;
import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.exceptions.domain.BusinessException;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.SecureUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;

import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.models.Product;
import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;

    public ProductServiceImpl(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepo.findAll()
                .stream()
                .map(Product::fromEntity) // Entity → Domain
                .map(ProductMapper::toResponse) // Domain → DTO
                .toList();
    }

    @Override
    public ProductResponseDto findOne(int id) {
        return productRepo.findById((long) id)
                .map(Product::fromEntity)
                .map(ProductMapper::toResponse)
                .orElseThrow(() -> new IllegalStateException("Producto no encontrado"));
    }

    @Override
    public ProductResponseDto create(CreateProductDto dto) {

        // Regla: nombre único
        if (productRepo.findByName(dto.name).isPresent()) {
            throw new IllegalStateException("El nombre del producto ya está registrado");
        }

        Product product = ProductMapper.fromCreateDto(dto);

        ProductEntity saved = productRepo.save(product.toEntity());

        return ProductMapper.toResponse(Product.fromEntity(saved));
    }

    @Override
    public ProductResponseDto update(int id, UpdateProductDto dto) {
        return productRepo.findById((long) id)
                // Entity → Domain
                .map(Product::fromEntity)

                // Aplicar cambios permitidos en el dominio
                .map(p -> p.update(dto))

                // Domain → Entity
                .map(Product::toEntity)

                // Persistencia
                .map(productRepo::save)

                // Entity → Domain
                .map(Product::fromEntity)

                // Domain → DTO
                .map(ProductMapper::toResponse)

                // Error controlado si no existe
                .orElseThrow(() -> new IllegalStateException("Producto no encontrado"));
    }

    @Override
    public ProductResponseDto partialUpdate(int id, PartialUpdateProductDto dto) {

        return productRepo.findById((long) id)
                // Entity → Domain
                .map(Product::fromEntity)

                // Aplicar solo los cambios presentes
                .map(product -> product.partialUpdate(dto))

                // Domain → Entity
                .map(Product::toEntity)

                // Persistencia
                .map(productRepo::save)

                // Entity → Domain
                .map(Product::fromEntity)

                // Domain → DTO
                .map(ProductMapper::toResponse)

                // Error si no existe
                .orElseThrow(() -> new IllegalStateException("Producto no encontrado"));
    }

    @Override
    public void delete(int id) {

        // Verifica existencia y elimina
        productRepo.findById((long) id)
                .ifPresentOrElse(
                        productRepo::delete,
                        () -> {
                            throw new IllegalStateException("Producto no encontrado");
                        });
    }

    @Override
    public boolean validateName(Integer id, String name) {
        productRepo.findByName(name)
                .ifPresent(existing -> {
                    if (id == null || existing.getId() != id.longValue()) {
                        throw new ConflictException(
                                "Ya existe un producto con el nombre '" + name + "'");
                    }
                });
        return true;
    }

    @Override
    public ProductResponseDto secureUpdate(int id, SecureUpdateProductDto dto) {
        ProductEntity entity = productRepo.findById((long) id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        if (dto.price != null && dto.price > 1000) {
            if (dto.reason == null || dto.reason.isBlank()) {
                throw new BusinessException(
                        "Productos con precio mayor a 1000 requieren justificación");
            }
        }

        Product product = Product.fromEntity(entity);

        if (dto.name != null)
            product.setName(dto.name);
        if (dto.price != null)
            product.setPrice(dto.price);
        if (dto.description != null)
            product.setDescription(dto.description);

        ProductEntity saved = productRepo.save(product.toEntity());

        return ProductMapper.toResponse(Product.fromEntity(saved));
    }

}