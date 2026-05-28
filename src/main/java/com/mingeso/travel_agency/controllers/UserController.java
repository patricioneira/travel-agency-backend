package com.mingeso.travel_agency.controllers;

import com.mingeso.travel_agency.entities.UserEntity;
import com.mingeso.travel_agency.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {
    private final UserService userService;

    // Público: cualquiera puede registrarse
    @PostMapping("/")
    public ResponseEntity<UserEntity> registerUser(@RequestBody UserEntity user) {
        UserEntity newUser = userService.registerUser(user);
        if (newUser == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(newUser);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id) {
        UserEntity user = userService.getUserById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    // Un cliente solo puede modificar sus propios datos
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable Long id,
                                                  @RequestBody UserEntity updatedData,
                                                  @AuthenticationPrincipal Jwt jwt) {
        UserEntity existing = userService.getUserById(id);
        if (existing == null) return ResponseEntity.notFound().build();

        // Si no es ADMIN, verificar que el email del JWT coincide con el usuario a editar
        if (!isAdmin(jwt)) {
            String jwtEmail = jwt.getClaimAsString("email");
            if (jwtEmail == null || !jwtEmail.equals(existing.getEmail())) {
                return ResponseEntity.status(403).build();
            }
        }

        UserEntity updated = userService.updateUser(id, updatedData);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserLogical(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null) return false;
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles != null && roles.contains("ADMIN");
        } catch (Exception e) {
            return false;
        }
    }
}
