package dev.brunocesar.webflux.controller;

import dev.brunocesar.webflux.domain.Anime;
import dev.brunocesar.webflux.service.AnimeService;
import dev.brunocesar.webflux.util.AnimeCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AnimeControllerTest {

    @InjectMocks
    private AnimeController animeController;

    @Mock
    private AnimeService animeService;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeEach
    public void beforeEach() {
        BDDMockito.when(animeService.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeService.findById(anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeService.save(any()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeService.delete(anyInt()))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeService.update(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("listAll returns a flux of anime")
    public void listAll_ReturnFluxOfAnime_WhenSuccessful() {
        StepVerifier.create(animeController.listAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono with anime when exists")
    public void findById_ReturnMonoAnime_WhenSuccessful() {
        StepVerifier.create(animeController.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_CreateAnime_WhenSuccessful() {
        var animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeController.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("saveBatch creates a list of anime when successful")
    public void saveBatch_CreateListOfAnime_WhenSuccessful() {

        when(animeService.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));

        var animes = List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved());

        StepVerifier.create(animeController.saveBatch(animes))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    public void delete_RemovesAnime_WhenSuccessful() {
        StepVerifier.create(animeController.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful")
    public void update_SaveUpdatedAnime_WhenSuccessful() {
        StepVerifier.create(animeController.update(AnimeCreator.createValidAnime(), 1))
                .expectSubscription()
                .verifyComplete();
    }

}