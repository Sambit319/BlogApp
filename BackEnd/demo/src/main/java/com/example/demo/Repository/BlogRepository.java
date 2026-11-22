package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}
