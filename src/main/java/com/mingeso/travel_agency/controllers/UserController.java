package com.mingeso.travel_agency.controllers;

import com.mingeso.travel_agency.entities.UserEntity;
import com.mingeso.travel_agency.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importante para seguridad
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {
    private final UserService userService;

    // ÉPICA 1: Permitir que los usuarios se registren [4]
    // Si esta ruta es para que cualquier persona cree una cuenta,
    // asegúrate de que en SecurityConfig esté como .permitAll()
    @PostMapping("/")
    public ResponseEntity<UserEntity> registerUser(@RequestBody UserEntity user) {
        UserEntity newUser = userService.registerUser(user);
        if (newUser == null) {
            // Retorna error si el correo ya existe o faltan datos obligatorios [2]
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(newUser);
    }

    // ÉPICA 1: Borrado lógico. Solo el ADMIN puede realizar esta acción [3, 6]
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserLogical(id);
        return ResponseEntity.noContent().build();
    }

    // ÉPICA 1: Obtener todos los usuarios. Solo accesible para ADMIN [5]
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        // El controlador le pide los datos a la capa de servicio [6]
        List<UserEntity> users = userService.getAllUsers();
        return ResponseEntity.ok(users); // Spring lo transforma a JSON automáticamente [1]
    }

}
