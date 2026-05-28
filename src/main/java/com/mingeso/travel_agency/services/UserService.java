package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.UserEntity;
import com.mingeso.travel_agency.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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