package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.UserEntity;
import com.mingeso.travel_agency.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity validUser;

    @BeforeEach
    void setUp() {
        validUser = new UserEntity();
        validUser.setId(1L);
        validUser.setFullName("John Doe");
        validUser.setEmail("john@example.com");
        validUser.setPassword("secret123");
        validUser.setPhone("123456789");
        validUser.setNationality("Chilean");
        validUser.setActive(true);
    }

    // --- registerUser ---

    @Test
    void registerUser_withValidData_savesAndReturnsUser() {
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(validUser);

        UserEntity result = userService.registerUser(validUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(validUser);
    }

    @Test
    void registerUser_withNullFullName_returnsNull() {
        validUser.setFullName(null);

        UserEntity result = userService.registerUser(validUser);

        assertThat(result).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_withNullEmail_returnsNull() {
        validUser.setEmail(null);

        UserEntity result = userService.registerUser(validUser);

        assertThat(result).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_withNullPassword_returnsNull() {
        validUser.setPassword(null);

        UserEntity result = userService.registerUser(validUser);

        assertThat(result).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_withDuplicateEmail_returnsNull() {
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));

        UserEntity result = userService.registerUser(validUser);

        assertThat(result).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_setsActiveTrueBeforeSaving() {
        validUser.setActive(false);
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.registerUser(validUser);

        assertThat(result.isActive()).isTrue();
    }

    // --- getUserById ---

    @Test
    void getUserById_whenExists_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));

        UserEntity result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void getUserById_whenNotFound_returnsNull() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserEntity result = userService.getUserById(99L);

        assertThat(result).isNull();
    }

    // --- updateUser ---

    @Test
    void updateUser_withValidId_updatesAndReturns() {
        UserEntity updatedData = new UserEntity();
        updatedData.setFullName("Jane Doe");
        updatedData.setPhone("987654321");

        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.updateUser(1L, updatedData);

        assertThat(result.getFullName()).isEqualTo("Jane Doe");
        assertThat(result.getPhone()).isEqualTo("987654321");
        verify(userRepository).save(any());
    }

    @Test
    void updateUser_onlyUpdatesNonNullFields() {
        UserEntity updatedData = new UserEntity();
        updatedData.setPhone("999999999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.updateUser(1L, updatedData);

        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getPhone()).isEqualTo("999999999");
    }

    @Test
    void updateUser_updatesNationality() {
        UserEntity updatedData = new UserEntity();
        updatedData.setNationality("Argentine");

        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.updateUser(1L, updatedData);

        assertThat(result.getNationality()).isEqualTo("Argentine");
    }

    @Test
    void updateUser_whenUserNotFound_returnsNull() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserEntity result = userService.updateUser(99L, new UserEntity());

        assertThat(result).isNull();
        verify(userRepository, never()).save(any());
    }

    // --- deleteUserLogical ---

    @Test
    void deleteUserLogical_withExistingUser_setsInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));

        userService.deleteUserLogical(1L);

        assertThat(validUser.isActive()).isFalse();
        verify(userRepository).save(validUser);
    }

    @Test
    void deleteUserLogical_withNonExistingUser_doesNothing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        userService.deleteUserLogical(99L);

        verify(userRepository, never()).save(any());
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(validUser, new UserEntity()));

        List<UserEntity> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllUsers_whenEmpty_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserEntity> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }
}
