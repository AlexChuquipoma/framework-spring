package ec.edu.ups.icc.fundamentos01.users.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dtos.UserResponseDto;

import ec.edu.ups.icc.fundamentos01.users.services.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDto> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponseDto findOne(@PathVariable("id") int id) {
        return userService.findOne(id);
    }

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    public UserResponseDto update(@PathVariable("id") int id, @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }

    @PatchMapping("/{id}")
    public UserResponseDto partialUpdate(@PathVariable("id") int id, @RequestBody PartialUpdateUserDto dto) {
        return userService.partialUpdate(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") int id) {
        userService.delete(id);
    }

    @GetMapping("/{id}/products")
    public List<ProductResponseDto> getUserProducts(@PathVariable("id") Long id) {
        return userService.getProductsByUserId(id);
    }
}
