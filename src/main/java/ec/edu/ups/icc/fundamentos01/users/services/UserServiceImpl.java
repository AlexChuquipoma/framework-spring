package ec.edu.ups.icc.fundamentos01.users.services;

import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.models.Product;
import ec.edu.ups.icc.fundamentos01.products.repository.ProductRepository;
import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.mappers.UserMapper;
import ec.edu.ups.icc.fundamentos01.users.models.User;
import ec.edu.ups.icc.fundamentos01.users.models.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    public UserServiceImpl(UserRepository userRepo, ProductRepository productRepo) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    @Override
    public List<UserResponseDto> findAll() {
        return userRepo.findAll()
                .stream()
                .map(User::fromEntity) // Entity → Domain
                .map(UserMapper::toResponse) // Domain → DTO
                .toList();
    }

    @Override
    public UserResponseDto findOne(int id) {
        return userRepo.findById((long) id)
                .map(User::fromEntity)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    @Override
    public UserResponseDto create(CreateUserDto dto) {

        // Regla: email único
        if (userRepo.findByEmail(dto.email).isPresent()) {
            throw new IllegalStateException("El email ya está registrado");
        }

        User user = UserMapper.fromCreateDto(dto);

        UserEntity saved = userRepo.save(user.toEntity());

        return UserMapper.toResponse(User.fromEntity(saved));

    }

    @Override
    public UserResponseDto update(int id, UpdateUserDto dto) {
        // Optional<UserEntity> userEntity= userRepo.findById((long) id);
        // if(!userEntity.isPresent()){
        // throw new IllegalStateException("Usuario no encontrado");
        // }
        // userEntity.get().setName(dto.name);
        // userEntity.get().setEmail(dto.email);

        // userRepo.save(userEntity.get());

        // User responseDto = User.fromEntity(userEntity.get());
        // UserResponseDto dtoResponse = UserMapper.toResponse(responseDto);
        // return dtoResponse;

        return userRepo.findById((long) id)
                // Entity → Domain
                .map(User::fromEntity)

                // Aplicar cambios permitidos en el dominio
                .map(u -> u.update(dto))

                // Domain → Entity
                .map(User::toEntity)

                // Persistencia
                .map(userRepo::save)

                // Entity → Domain
                .map(User::fromEntity)

                // Domain → DTO
                .map(UserMapper::toResponse)

                // Error controlado si no existe
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
    }

    @Override
    public UserResponseDto partialUpdate(int id, PartialUpdateUserDto dto) {

        return userRepo.findById((long) id)
                // Entity → Domain
                .map(User::fromEntity)

                // Aplicar solo los cambios presentes
                .map(user -> user.partialUpdate(dto))

                // Domain → Entity
                .map(User::toEntity)

                // Persistencia
                .map(userRepo::save)

                // Entity → Domain
                .map(User::fromEntity)

                // Domain → DTO
                .map(UserMapper::toResponse)

                // Error si no existe
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
    }

    @Override
    public void delete(int id) {

        // Verifica existencia y elimina
        userRepo.findById((long) id)
                .ifPresentOrElse(
                        userRepo::delete,
                        () -> {
                            throw new IllegalStateException("Usuario no encontrado");
                        });
    }

    @Override
    public List<ProductResponseDto> getProductsByUserId(Long userId) {
        // Verificar que el usuario existe
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado");
        }

        // Consultar productos del usuario usando el repositorio de productos
        return productRepo.findByOwnerId(userId)
                .stream()
                .map(Product::fromEntity) // Entity → Domain
                .map(ProductMapper::toResponse) // Domain → DTO
                .toList();
    }

}