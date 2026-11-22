package com.example.demo.Controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.DTO.BlogRequest;
import com.example.demo.DTO.BlogResponse;
import com.example.demo.Service.BlogService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

class BlogControllerTests {

    private static final String BLOG_JSON = """
            {"title":"Test title","content":"Test content","author":"Test author"}
            """;

    private static final BlogRequest BLOG_REQUEST =
            new BlogRequest("Test title", "Test content", "Test author");
    private static final BlogResponse BLOG_RESPONSE =
            new BlogResponse(1L, "Test title", "Test content", "Test author");

    private BlogService blogService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        blogService = mock(BlogService.class);
        mockMvc = standaloneSetup(new BlogController(blogService)).build();
    }

    @Test
    void createBlogReturnsCreatedBlog() throws Exception {
        when(blogService.createBlog(any(BlogRequest.class))).thenReturn(BLOG_RESPONSE);

        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BLOG_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test title"))
                .andExpect(jsonPath("$.content").value("Test content"))
                .andExpect(jsonPath("$.author").value("Test author"));

        verify(blogService).createBlog(BLOG_REQUEST);
    }

    @Test
    void createBlogRejectsBlankContent() throws Exception {
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Test title","content":" ","author":"Test author"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Post content must not be empty"));

        verifyNoInteractions(blogService);
    }

    @Test
    void createBlogRejectsContentWith500Characters() throws Exception {
        String request = """
                {"title":"Test title","content":"%s","author":"Test author"}
                """.formatted("a".repeat(500));

        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Post content must be less than 500 characters"));

        verifyNoInteractions(blogService);
    }

    @Test
    void createBlogRejectsMissingTitle() throws Exception {
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Test content\",\"author\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Post title must not be empty"));
        verifyNoInteractions(blogService);
    }

    @Test
    void createBlogRejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"content\":\"Test content\",\"author\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Post title must not be empty"));
        verifyNoInteractions(blogService);
    }

    @Test
    void createBlogChecksTitleBeforeContent() throws Exception {
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"content\":\"\",\"author\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Post title must not be empty"));
        verifyNoInteractions(blogService);
    }

    @Test
    void createBlogAccepts499CharacterContent() throws Exception {
        when(blogService.createBlog(any(BlogRequest.class))).thenReturn(BLOG_RESPONSE);
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"content\":\"" + "a".repeat(499) + "\",\"author\":\"a\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createBlogWithEmptyBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(blogService);
    }

    @Test
    void updateBlogRejectsMissingTitle() throws Exception {
        mockMvc.perform(put("/api/blogs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Test content\",\"author\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Post title must not be empty"));
        verifyNoInteractions(blogService);
    }

    @Test
    void updateBlogRejectsBlankTitle() throws Exception {
        mockMvc.perform(put("/api/blogs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"content\":\"Test content\",\"author\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Post title must not be empty"));
        verifyNoInteractions(blogService);
    }

    @Test
    void getAllBlogsReturnsBlogs() throws Exception {
        BlogResponse secondBlog = new BlogResponse(2L, "Second title", "Second content", "Second author");
        when(blogService.getAllBlogs()).thenReturn(List.of(BLOG_RESPONSE, secondBlog));

        mockMvc.perform(get("/api/blogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test title"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Second title"));

        verify(blogService).getAllBlogs();
    }

    @Test
    void getBlogByIdReturnsBlogWhenItExists() throws Exception {
        when(blogService.getBlogById(1L)).thenReturn(Optional.of(BLOG_RESPONSE));

        mockMvc.perform(get("/api/blogs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test title"))
                .andExpect(jsonPath("$.content").value("Test content"))
                .andExpect(jsonPath("$.author").value("Test author"));

        verify(blogService).getBlogById(1L);
    }

    @Test
    void getBlogByIdReturnsNotFoundWhenItDoesNotExist() throws Exception {
        when(blogService.getBlogById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/blogs/99"))
                .andExpect(status().isNotFound());

        verify(blogService).getBlogById(99L);
    }

    @Test
    void updateBlogReturnsUpdatedBlogWhenItExists() throws Exception {
        BlogResponse updatedBlog = new BlogResponse(1L, "Updated title", "Updated content", "Updated author");
        String request = """
                {"title":"Updated title","content":"Updated content","author":"Updated author"}
                """;
        when(blogService.updateBlog(eq(1L), any(BlogRequest.class))).thenReturn(Optional.of(updatedBlog));

        mockMvc.perform(put("/api/blogs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.content").value("Updated content"))
                .andExpect(jsonPath("$.author").value("Updated author"));

        verify(blogService).updateBlog(1L,
                new BlogRequest("Updated title", "Updated content", "Updated author"));
    }

    @Test
    void updateBlogReturnsNotFoundWhenItDoesNotExist() throws Exception {
        when(blogService.updateBlog(eq(99L), any(BlogRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/blogs/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BLOG_JSON))
                .andExpect(status().isNotFound());

        verify(blogService).updateBlog(99L, BLOG_REQUEST);
    }

    @Test
    void updateBlogRejectsBlankContent() throws Exception {
        mockMvc.perform(put("/api/blogs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated title","content":"","author":"Updated author"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Post content must not be empty"));

        verifyNoInteractions(blogService);
    }

    @Test
    void getTotalWordCountReturnsCountWhenBlogExists() throws Exception {
        when(blogService.getTotalWordCount(1L)).thenReturn(Optional.of(5));

        mockMvc.perform(get("/api/blogs/1/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(blogService).getTotalWordCount(1L);
    }

    @Test
    void getTotalWordCountReturnsNotFoundWhenBlogDoesNotExist() throws Exception {
        when(blogService.getTotalWordCount(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/blogs/99/total"))
                .andExpect(status().isNotFound());

        verify(blogService).getTotalWordCount(99L);
    }

    @Test
    void deleteBlogReturnsNoContentWhenItExists() throws Exception {
        when(blogService.deleteBlog(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/blogs/1"))
                .andExpect(status().isNoContent());

        verify(blogService).deleteBlog(1L);
    }

    @Test
    void deleteBlogReturnsNotFoundWhenItDoesNotExist() throws Exception {
        when(blogService.deleteBlog(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/blogs/99"))
                .andExpect(status().isNotFound());

        verify(blogService).deleteBlog(99L);
    }
}
