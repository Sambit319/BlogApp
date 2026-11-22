package main.java.com.example.demo.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.example.demo.Dto.BlogRequest;
import com.example.demo.Dto.BlogResponse;
import com.example.demo.Service.BlogService;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    private static final int MAX_CONTENT_LENGTH = 500;

    // Returns an error message if the post is invalid, or null if it is valid
    private String validate(BlogRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            return "Post content must not be empty";
        }
        if (request.getContent().length() >= MAX_CONTENT_LENGTH) {
            return "Post content must be less than " + MAX_CONTENT_LENGTH + " characters";
        }
        return null;
    }

    // POST /api/blogs - create a blog post
    @PostMapping
    public ResponseEntity<?> createBlog(@RequestBody BlogRequest request) {
        String error = validate(request);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        BlogResponse response = blogService.createBlog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/blogs - fetch all blog posts
    @GetMapping
    public List<BlogResponse> getAllBlogs() {
        return blogService.getAllBlogs();
    }

    // GET /api/blogs/{id} - fetch a blog post by id
    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        return blogService.getBlogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/blogs/{id}/total - get the total word count for a blog post
    @GetMapping("/{id}/total")
    public ResponseEntity<Integer> getTotalWordCount(@PathVariable Long id) {
        return blogService.getTotalWordCount(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/blogs/{id} - update a blog post by id
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBlog(@PathVariable Long id, @RequestBody BlogRequest request) {
        String error = validate(request);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        Optional<BlogResponse> response = blogService.updateBlog(id, request);
        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response.get());
    }

    // DELETE /api/blogs/{id} - delete a blog post by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        if (!blogService.deleteBlog(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
