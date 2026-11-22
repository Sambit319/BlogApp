package com.example.demo.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.UserRequest;
import com.example.demo.DTO.UserResponse;
import com.example.demo.Service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Returns an error message if the user is invalid, or null if it is valid
    private String validate(UserRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return "Name must not be empty";
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return "Email must not be empty";
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            return "Email is not valid";
        }
        String phone = request.getPhoneNumber();
        if (phone != null && !phone.isBlank() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return "Phone number must be 7 to 15 digits, optionally starting with +";
        }
        return null;
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    // POST /api/users - create a user
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        String error = validate(request);
        if (error != null) {
            return error(HttpStatus.BAD_REQUEST, error);
        }
        if (userService.isEmailTaken(request.getEmail(), null)) {
            return error(HttpStatus.CONFLICT, "Email is already in use");
        }

        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/users - fetch all users
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET /api/users/{id} - fetch a user by id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/users/{id} - update a user by id
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        String error = validate(request);
        if (error != null) {
            return error(HttpStatus.BAD_REQUEST, error);
        }
        if (userService.isEmailTaken(request.getEmail(), id)) {
            return error(HttpStatus.CONFLICT, "Email is already in use");
        }

        Optional<UserResponse> response = userService.updateUser(id, request);
        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response.get());
    }

    // DELETE /api/users/{id} - delete a user by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.deleteUser(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
