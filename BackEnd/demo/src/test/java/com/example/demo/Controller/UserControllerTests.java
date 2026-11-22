package com.example.demo.Controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Dto.UserRequest;
import com.example.demo.Dto.UserResponse;
import com.example.demo.Service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserControllerTests {

    private static final String USER_JSON = """
            {"name":"Asha Rao","email":"asha@example.com","phoneNumber":"+919876543210"}
            """;

    private static final UserRequest USER_REQUEST =
            new UserRequest("Asha Rao", "asha@example.com", "+919876543210");
    private static final UserResponse USER_RESPONSE =
            new UserResponse(1L, "Asha Rao", "asha@example.com", "+919876543210");

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = standaloneSetup(new UserController(userService)).build();
    }

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        when(userService.createUser(any(UserRequest.class))).thenReturn(USER_RESPONSE);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Asha Rao"))
                .andExpect(jsonPath("$.email").value("asha@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+919876543210"));

        verify(userService).createUser(USER_REQUEST);
    }

    @Test
    void createUserRejectsMissingName() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"asha@example.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Name must not be empty"));

        verifyNoInteractions(userService);
    }

    @Test
    void createUserRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Asha Rao","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is not valid"));

        verifyNoInteractions(userService);
    }

    @Test
    void createUserRejectsInvalidPhoneNumber() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Asha Rao","email":"asha@example.com","phoneNumber":"12ab"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Phone number must be 7 to 15 digits, optionally starting with +"));

        verifyNoInteractions(userService);
    }

    @Test
    void createUserReturnsConflictWhenEmailIsTaken() throws Exception {
        when(userService.isEmailTaken("asha@example.com", null)).thenReturn(true);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email is already in use"));

        verify(userService, never()).createUser(any(UserRequest.class));
    }

    @Test
    void getAllUsersReturnsUsers() throws Exception {
        UserResponse second = new UserResponse(2L, "Ravi Kumar", "ravi@example.com", null);
        when(userService.getAllUsers()).thenReturn(List.of(USER_RESPONSE, second));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].email").value("ravi@example.com"));
    }

    @Test
    void getUserByIdReturnsUserWhenItExists() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(USER_RESPONSE));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Asha Rao"));
    }

    @Test
    void getUserByIdReturnsNotFoundWhenItDoesNotExist() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserReturnsUpdatedUserWhenItExists() throws Exception {
        UserResponse updated = new UserResponse(1L, "Asha R", "asha.r@example.com", null);
        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Asha R","email":"asha.r@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Asha R"))
                .andExpect(jsonPath("$.email").value("asha.r@example.com"));

        verify(userService).isEmailTaken("asha.r@example.com", 1L);
        verify(userService).updateUser(1L, new UserRequest("Asha R", "asha.r@example.com", null));
    }

    @Test
    void updateUserReturnsNotFoundWhenItDoesNotExist() throws Exception {
        when(userService.updateUser(eq(99L), any(UserRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserRejectsBlankEmail() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Asha Rao","email":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email must not be empty"));

        verifyNoInteractions(userService);
    }

    @Test
    void updateUserReturnsConflictWhenEmailBelongsToAnotherUser() throws Exception {
        when(userService.isEmailTaken(anyString(), eq(1L))).thenReturn(true);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email is already in use"));

        verify(userService, never()).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void deleteUserReturnsNoContentWhenItExists() throws Exception {
        when(userService.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUserReturnsNotFoundWhenItDoesNotExist() throws Exception {
        when(userService.deleteUser(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
