package dev.brunocesar.webflux.controller;


import dev.brunocesar.webflux.domain.Anime;
import dev.brunocesar.webflux.service.AnimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("animes")
@SecurityScheme(
        name = "Basic Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class AnimeController {

    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all Animes",
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"Animes"})
    public Flux<Anime> listAll() {
        return animeService.findAll();
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Anime by ID",
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"Animes"})
    public Mono<Anime> findById(@PathVariable int id) {
        return animeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save Anime",
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"Animes"})
    public Mono<Anime> save(@Valid @RequestBody Anime anime) {
        return animeService.save(anime);
    }

    @PostMapping("batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save Animes in Batch",
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"Animes"})
    public Flux<Anime> saveBatch(@RequestBody List<Anime> animes) {
        return animeService.saveAll(animes);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update Anime",
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"Animes"})
    public Mono<Void> update(@Valid @RequestBody Anime anime, @PathVariable Integer id) {
        anime.setId(id);
        return animeService.update(anime);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Anime",
            security = @SecurityRequirement(name = "Basic Authentication"),
            tags = {"Animes"})
    public Mono<Void> delete(@PathVariable Integer id) {
        return animeService.delete(id);
    }

}
