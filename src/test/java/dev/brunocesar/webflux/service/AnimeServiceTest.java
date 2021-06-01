package dev.brunocesar.webflux.service;

import dev.brunocesar.webflux.domain.Anime;
import dev.brunocesar.webflux.repository.AnimeRepository;
import dev.brunocesar.webflux.util.AnimeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class AnimeServiceTest {

    @InjectMocks
    private AnimeService animeService;

    @Mock
    private AnimeRepository animeRepository;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void beforeAll() {
        BlockHound.install();
    }

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    public void findAll_ReturnFluxOfAnime_WhenSuccessful() {

        when(animeRepository.findAll()).thenReturn(Flux.just(anime));

        StepVerifier.create(animeService.findAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono with anime when exists")
    public void findById_ReturnMonoAnime_WhenSuccessful() {

        when(animeRepository.findById(anyInt())).thenReturn(Mono.just(anime));

        StepVerifier.create(animeService.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono Error when anime does not exist")
    public void findById_ReturnMonoError_WhenEmptyMonoIsReturned() {

        when(animeRepository.findById(anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(animeService.findById(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_CreateAnime_WhenSuccessful() {

        when(animeRepository.save(any())).thenReturn(Mono.just(anime));

        doReturn(Mono.just(anime)).when(animeRepository.save(any()));

        var animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeService.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    public void delete_RemovesAnime_WhenSuccessful() {

        when(animeRepository.findById(anyInt())).thenReturn(Mono.just(anime));
        when(animeRepository.delete(any(Anime.class))).thenReturn(Mono.empty());

        StepVerifier.create(animeService.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns Mono error when anime does not exist")
    public void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {

        when(animeRepository.findById(anyInt())).thenReturn(Mono.empty());
        when(animeRepository.delete(any(Anime.class))).thenReturn(Mono.empty());

        StepVerifier.create(animeService.delete(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("update save uppdated anime and returns empty mono when successful")
    public void update_SaveUpdatedAnime_WhenSuccessful() {

        when(animeRepository.findById(anyInt())).thenReturn(Mono.just(AnimeCreator.createAnimeToBeSaved()));
        when(animeRepository.save(AnimeCreator.createAnimeToBeSaved())).thenReturn(Mono.just(AnimeCreator.createValidUpdatedAnime()));

        StepVerifier.create(animeService.update(AnimeCreator.createValidAnime()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update returns Mono error when anime does exist")
    public void update_ReturnMonoError_WhenEmptyMonoIsReturned() {

        when(animeRepository.findById(anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(animeService.update(AnimeCreator.createValidAnime()))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

}