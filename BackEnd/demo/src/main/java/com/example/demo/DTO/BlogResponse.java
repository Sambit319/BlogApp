package com.example.demo.DTO;

import com.example.demo.Model.Blog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponse {

    private Long id;
    private String title;
    private String content;
    private String author;

    public static BlogResponse from(Blog blog) {
        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getAuthor()
        );
    }
}
