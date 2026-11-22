package com.example.demo.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.Dto.BlogRequest;
import com.example.demo.Dto.BlogResponse;
import com.example.demo.Model.Blog;
import com.example.demo.Repository.BlogRepository;

@Service
public class BlogService {

    private final BlogRepository blogRepository;

    public BlogService(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    public BlogResponse createBlog(BlogRequest request) {
        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setAuthor(request.getAuthor());

        Blog saved = blogRepository.save(blog);
        return BlogResponse.from(saved);
    }

    public List<BlogResponse> getAllBlogs() {
        return blogRepository.findAll().stream()
                .map(BlogResponse::from)
                .toList();
    }

    public Optional<BlogResponse> getBlogById(Long id) {
        return blogRepository.findById(id)
                .map(BlogResponse::from);
    }

    public Optional<Integer> getTotalWordCount(Long id) {
        return blogRepository.findById(id)
                .map(blog -> {
                    String content = blog.getContent();
                    if (content == null || content.isBlank()) {
                        return 0;
                    }
                    return content.trim().split("\\s+").length;
                });
    }

    public Optional<BlogResponse> updateBlog(Long id, BlogRequest request) {
        return blogRepository.findById(id)
                .map(blog -> {
                    blog.setTitle(request.getTitle());
                    blog.setContent(request.getContent());
                    blog.setAuthor(request.getAuthor());
                    return BlogResponse.from(blogRepository.save(blog));
                });
    }

    public boolean deleteBlog(Long id) {
        if (!blogRepository.existsById(id)) {
            return false;
        }
        blogRepository.deleteById(id);
        return true;
    }
}
