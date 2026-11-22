package com.example.demo.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.Dto.UserRequest;
import com.example.demo.Dto.UserResponse;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {
        User user = new User();
        applyRequest(user, request);
        return UserResponse.from(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from);
    }

    public Optional<UserResponse> updateUser(Long id, UserRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    applyRequest(user, request);
                    return UserResponse.from(userRepository.save(user));
                });
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    // True if another user already has this email. Pass the user's own id when
    // updating (so keeping the same email is allowed), or null when creating.
    public boolean isEmailTaken(String email, Long excludeUserId) {
        String normalized = normalizeEmail(email);
        return excludeUserId == null
                ? userRepository.existsByEmail(normalized)
                : userRepository.existsByEmailAndIdNot(normalized, excludeUserId);
    }

    private void applyRequest(User user, UserRequest request) {
        user.setName(request.getName().trim());
        user.setEmail(normalizeEmail(request.getEmail()));
        String phone = request.getPhoneNumber();
        user.setPhoneNumber(phone == null || phone.isBlank() ? null : phone.trim());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
