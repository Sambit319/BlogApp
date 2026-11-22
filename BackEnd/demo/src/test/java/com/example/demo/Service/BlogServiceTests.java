package com.example.demo.Service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.Model.Blog;
import com.example.demo.Repository.BlogRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlogServiceTests {

    private BlogRepository blogRepository;
    private BlogService blogService;

    @BeforeEach
    void setUp() {
        blogRepository = mock(BlogRepository.class);
        blogService = new BlogService(blogRepository);
    }

    @Test
    void getTotalWordCountCountsWordsSeparatedByWhitespace() {
        Blog blog = new Blog();
        blog.setContent("  Hello world\nfrom\tJUnit tests  ");
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));

        Optional<Integer> result = blogService.getTotalWordCount(1L);

        assertEquals(5, result.orElseThrow());
    }

    @Test
    void getTotalWordCountReturnsZeroForBlankContent() {
        Blog blog = new Blog();
        blog.setContent("   ");
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));

        Optional<Integer> result = blogService.getTotalWordCount(1L);

        assertEquals(0, result.orElseThrow());
    }

    @Test
    void getTotalWordCountReturnsEmptyWhenBlogDoesNotExist() {
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Integer> result = blogService.getTotalWordCount(99L);

        assertTrue(result.isEmpty());
    }
}
