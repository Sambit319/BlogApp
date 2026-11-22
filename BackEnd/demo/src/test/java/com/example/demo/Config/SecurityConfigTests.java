package com.example.demo.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.demo.Controller.BlogController;
import com.example.demo.Controller.UserController;
import com.example.demo.DTO.BlogResponse;
import com.example.demo.DTO.UserResponse;
import com.example.demo.Service.BlogService;
import com.example.demo.Service.UserService;

@WebMvcTest(controllers = { BlogController.class, UserController.class })
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.user.username=user",
        "app.security.user.password=userpass",
        "app.security.admin.username=admin",
        "app.security.admin.password=adminpass"
})
class SecurityConfigTests {

    private static final String BLOG_JSON = "{\"title\":\"T\",\"content\":\"C\"}";
    private static final String USER_JSON = "{\"name\":\"Bob\",\"email\":\"bob@example.com\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsService userDetailsService;

    @MockitoBean
    private BlogService blogService;

    @MockitoBean
    private UserService userService;

    // ---- 2 ----
    @Test
    void anonymousBlogsReturns401WithChallenge() throws Exception {
        mockMvc.perform(get("/api/blogs"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.startsWith("Basic")))
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"demo\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    void anonymousUsersReturns401WithChallenge() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.startsWith("Basic")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    // ---- 3 ----
    @Test
    void wrongPasswordReturns401() throws Exception {
        mockMvc.perform(get("/api/blogs").with(httpBasic("user", "nope")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.startsWith("Basic")))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    void unknownUsernameReturns401() throws Exception {
        mockMvc.perform(get("/api/blogs").with(httpBasic("ghost", "userpass")))
                .andExpect(status().isUnauthorized());
    }

    // ---- 4 ----
    @Test
    void userCanListBlogs() throws Exception {
        when(blogService.getAllBlogs()).thenReturn(List.of(new BlogResponse(1L, "T", "C", "a")));
        mockMvc.perform(get("/api/blogs").with(httpBasic("user", "userpass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("T"));
    }

    @Test
    void userCanGetTotal() throws Exception {
        when(blogService.getTotalWordCount(1L)).thenReturn(Optional.of(5));
        mockMvc.perform(get("/api/blogs/1/total").with(httpBasic("user", "userpass")))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void userCanPostBlogWithoutCsrf() throws Exception {
        when(blogService.createBlog(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new BlogResponse(1L, "T", "C", "a"));
        mockMvc.perform(post("/api/blogs").with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON).content(BLOG_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void userCanPutBlogWithoutCsrf() throws Exception {
        when(blogService.updateBlog(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.of(new BlogResponse(1L, "T", "C", "a")));
        mockMvc.perform(put("/api/blogs/1").with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON).content(BLOG_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void userCanDeleteBlogWithoutCsrf() throws Exception {
        when(blogService.deleteBlog(1L)).thenReturn(true);
        mockMvc.perform(delete("/api/blogs/1").with(httpBasic("user", "userpass")))
                .andExpect(status().isNoContent());
    }

    // ---- 5 ----
    @Test
    void adminCanListUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());
        mockMvc.perform(get("/api/users").with(httpBasic("admin", "adminpass")))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanPostUserWithoutCsrf() throws Exception {
        when(userService.isEmailTaken("bob@example.com", null)).thenReturn(false);
        when(userService.createUser(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new UserResponse());
        mockMvc.perform(post("/api/users").with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON).content(USER_JSON))
                .andExpect(status().isCreated());
    }

    // ---- 6 ----
    @Test
    void userForbiddenOnUsersCollection() throws Exception {
        mockMvc.perform(get("/api/users").with(httpBasic("user", "userpass")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Access denied"));
        verifyNoInteractions(userService);
    }

    @Test
    void userForbiddenOnUserById() throws Exception {
        mockMvc.perform(get("/api/users/1").with(httpBasic("user", "userpass")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
        verifyNoInteractions(userService);
    }

    @Test
    void adminForbiddenOnGetBlogs() throws Exception {
        mockMvc.perform(get("/api/blogs").with(httpBasic("admin", "adminpass")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Access denied"));
        verifyNoInteractions(blogService);
    }

    @Test
    void adminForbiddenOnPostBlogs() throws Exception {
        mockMvc.perform(post("/api/blogs").with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON).content(BLOG_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
        verifyNoInteractions(blogService);
    }

    // ---- 7 ----
    @Test
    void noCookieOrSessionOnAuthenticatedResponse() throws Exception {
        when(blogService.getAllBlogs()).thenReturn(List.of());
        MvcResult result = mockMvc.perform(get("/api/blogs").with(httpBasic("user", "userpass")))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andReturn();
        assertNull(result.getRequest().getSession(false));
    }

    @Test
    void noCookieOrSessionOnAuthenticatedAdminResponse() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());
        MvcResult result = mockMvc.perform(get("/api/users").with(httpBasic("admin", "adminpass")))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andReturn();
        assertNull(result.getRequest().getSession(false));
    }

    // ---- 8 ----
    @Test
    void statelessNoCredentialsAfterSuccessReturns401() throws Exception {
        when(blogService.getAllBlogs()).thenReturn(List.of());
        mockMvc.perform(get("/api/blogs").with(httpBasic("user", "userpass")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/blogs"))
                .andExpect(status().isUnauthorized());
    }

    // ---- 9 ----
    @Test
    void unknownPathAnonymousIs401() throws Exception {
        mockMvc.perform(get("/api/other"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    void unknownPathUserIs403() throws Exception {
        mockMvc.perform(get("/api/other").with(httpBasic("user", "userpass")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    @Test
    void unknownPathAdminIs403() throws Exception {
        mockMvc.perform(get("/api/other").with(httpBasic("admin", "adminpass")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    // ---- 10 ----
    @Test
    void validationStillWorksBehindSecurity() throws Exception {
        mockMvc.perform(post("/api/blogs").with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\",\"content\":\"C\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Post title must not be empty"));
        verifyNoInteractions(blogService);
    }

    // ---- 11 ----
    @Test
    void userDetailsServiceHasHashedPasswordsAndRoles() {
        UserDetails user = userDetailsService.loadUserByUsername("user");
        assertTrue(user.getPassword().startsWith("$2"));
        assertTrue(new BCryptPasswordEncoder().matches("userpass", user.getPassword()));
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        UserDetails admin = userDetailsService.loadUserByUsername("admin");
        assertTrue(admin.getPassword().startsWith("$2"));
        assertTrue(admin.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertEquals(1, admin.getAuthorities().size());
    }

    // ---- 12 ----
    @Test
    void blankCredentialThrowsIllegalState() {
        SecurityConfig config = new SecurityConfig();
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        assertThrows(IllegalStateException.class,
                () -> config.userDetailsService(enc, "", "p", "a", "p"));
        assertThrows(IllegalStateException.class,
                () -> config.userDetailsService(enc, "u", "  ", "a", "p"));
        assertThrows(IllegalStateException.class,
                () -> config.userDetailsService(enc, "u", "p", null, "p"));
        assertThrows(IllegalStateException.class,
                () -> config.userDetailsService(enc, "u", "p", "a", ""));
    }
}
