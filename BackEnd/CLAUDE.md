# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Layout

The backend is a single Spring Boot app in `demo/` (Maven, Java 21, Spring Boot 4.2). The `BackEnd/` folder itself holds no build files; run all Maven commands from `demo/`.

## Commands

Run from `demo/`:

```
mvn spring-boot:run                          # start the app on http://localhost:8080
mvn clean package                            # compile + run tests + package the jar
mvn compile                                  # compile only
mvn test                                     # run all tests (JUnit 5)
mvn test "-Dtest=DemoApplicationTests"        # one test class
mvn test "-Dtest=DemoApplicationTests#contextLoads" # one test method
```

There is no linter configured.

## Database

`demo/src/main/resources/application.properties` points at a local PostgreSQL: `jdbc:postgresql://localhost:5432/postgres`, user `postgres` / password `postgres`. `spring.jpa.hibernate.ddl-auto=update` means Hibernate creates and alters tables from the `@Entity` classes, so there are no migration files. Postgres must be running for `spring-boot:run` and for `DemoApplicationTests` (a `@SpringBootTest` that loads the full context, including the datasource).

## Architecture

The code follows the Controller → Service → Repository → Model layers under `com.example.demo`, with one subpackage per layer (`Controller`, `Service`, `Repository`, `Model`; the package names are capitalized). Controllers call services rather than repositories directly and contain the request validation (e.g. `BlogController.validate`, which rejects blog content that is empty or 500+ characters and returns `400` with `{"error": "..."}`). `BlogService` handles repository access and maps between blog DTOs and entities.

The blog REST API is mounted at `/api/blogs` (`POST`, `GET`, `GET /{id}`, `GET /{id}/total`, `PUT /{id}`, `DELETE /{id}`). `Blog` is the JPA entity mapped to the `blogs` table. API request and response bodies use `BlogRequest` and `BlogResponse` from the `DTO` package instead of exposing the entity directly.

## Gotchas

- Package declarations must be `com.example.demo...`. If the editor opens `BackEnd/` instead of the Maven project, Java tooling treats `src` as the source root and suggests `main.java.com.example.demo...` as the package. Applying that quick fix breaks the build and moves the class out of `@SpringBootApplication` component scanning.
