package com.example.demo.Service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.DTO.UserRequest;
import com.example.demo.DTO.UserResponse;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTests {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(1L);
            }
            return user;
        });
    }

    private static User existingUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Old Name");
        user.setEmail("old@example.com");
        user.setPhoneNumber("+911234567890");
        return user;
    }

    @Test
    void createUserSavesTrimmedNameAndLowercaseEmail() {
        UserResponse response = userService.createUser(
                new UserRequest("  Asha Rao  ", "  Asha@Example.COM ", " +919876543210 "));

        assertEquals(1L, response.getId());
        assertEquals("Asha Rao", response.getName());
        assertEquals("asha@example.com", response.getEmail());
        assertEquals("+919876543210", response.getPhoneNumber());
    }

    @Test
    void createUserStoresBlankPhoneNumberAsNull() {
        UserResponse response = userService.createUser(new UserRequest("Asha", "asha@example.com", "  "));

        assertNull(response.getPhoneNumber());
    }

    @Test
    void getUserByIdReturnsUserWhenItExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));

        Optional<UserResponse> result = userService.getUserById(1L);

        assertEquals("old@example.com", result.orElseThrow().getEmail());
    }

    @Test
    void getUserByIdReturnsEmptyWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(userService.getUserById(99L).isEmpty());
    }

    @Test
    void updateUserOverwritesFieldsWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));

        UserResponse response = userService.updateUser(1L,
                new UserRequest("New Name", "New@Example.com", null)).orElseThrow();

        assertEquals(1L, response.getId());
        assertEquals("New Name", response.getName());
        assertEquals("new@example.com", response.getEmail());
        assertNull(response.getPhoneNumber());
    }

    @Test
    void updateUserReturnsEmptyWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(userService.updateUser(99L, new UserRequest("Name", "a@b.com", null)).isEmpty());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserDeletesAndReturnsTrueWhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertTrue(userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserReturnsFalseWhenUserDoesNotExist() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertFalse(userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(99L);
    }

    @Test
    void isEmailTakenChecksNormalizedEmailOnCreate() {
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(true);

        assertTrue(userService.isEmailTaken(" Asha@Example.com ", null));
    }

    @Test
    void isEmailTakenIgnoresTheUserBeingUpdated() {
        when(userRepository.existsByEmailAndIdNot("asha@example.com", 1L)).thenReturn(false);

        assertFalse(userService.isEmailTaken("asha@example.com", 1L));
    }
}
