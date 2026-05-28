package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.UserEntity;
import com.mingeso.travel_agency.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserEntity registerUser(UserEntity user) {
        if (user.getFullName() == null || user.getEmail() == null || user.getPassword() == null) {
            return null;
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return null;
        }
        user.setActive(true);
        return userRepository.save(user);
    }

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // El cliente solo puede modificar sus propios datos (validación de autorización en el controller)
    public UserEntity updateUser(Long id, UserEntity updatedData) {
        Optional<UserEntity> existing = userRepository.findById(id);
        if (existing.isPresent()) {
            return null;
        }
        UserEntity user = existing.get();
        if (updatedData.getFullName() != null) user.setFullName(updatedData.getFullName());
        if (updatedData.getPhone() != null) user.setPhone(updatedData.getPhone());
        if (updatedData.getNationality() != null) user.setNationality(updatedData.getNationality());
        if (updatedData.getIdDocument() != null) user.setIdDocument(updatedData.getIdDocument());
        return userRepository.save(user);
    }

    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // Borrado lógico: no elimina usuarios con historial de reservas, solo los marca inactivos
    public void deleteUserLogical(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
