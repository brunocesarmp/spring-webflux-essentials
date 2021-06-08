package dev.brunocesar.webflux.integration;

import dev.brunocesar.webflux.domain.Anime;
import dev.brunocesar.webflux.repository.AnimeRepository;
import dev.brunocesar.webflux.repository.UserRepository;
import dev.brunocesar.webflux.service.AnimeUserDetailsService;
import dev.brunocesar.webflux.util.AnimeCreator;
import dev.brunocesar.webflux.util.UserCreator;
import dev.brunocesar.webflux.util.WebTestClientUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class AnimeControllerIT {

    @Autowired
    private UserCreator userCreator;

    @MockBean
    private AnimeRepository animeRepositoryMock;

    @Autowired
    private WebTestClientUtil webTestClientUtil;

    @MockBean
    private UserRepository userRepository;

    private WebTestClient testClientUser;

    private WebTestClient testClientAdmin;

    private WebTestClient testClientInvalid;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeEach
    public void beforeEach() {
        var user = userCreator.createUser();
        var admin = userCreator.createAdmin();

        testClientUser = webTestClientUtil.authenticateClient(user.getUsername(), UserCreator.getUserPassword());
        BDDMockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Mono.just(user));

        testClientAdmin = webTestClientUtil.authenticateClient(admin.getUsername(), UserCreator.getAdminPassword());
        BDDMockito.when(userRepository.findByUsername(admin.getUsername()))
                .thenReturn(Mono.just(admin));

        testClientInvalid = webTestClientUtil.authenticateClient("x", "x");
        BDDMockito.when(userRepository.findByUsername("x"))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepositoryMock.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepositoryMock.findById(anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepositoryMock.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepositoryMock
                .saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));

        BDDMockito.when(animeRepositoryMock.delete(any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepositoryMock.save(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("listAll returns unauthorized when user is not authenticated")
    public void listAll_ReturnUnauthorized_WhenUserIsNotAuthenticated() {
        testClientInvalid
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("listAll returns forbidden when user is successfully authenticated and does not have role ADMIN")
    public void listAll_ReturnForbidden_WhenUserDoesNotHaveRoleAdmin() {
        testClientUser
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("listAll returns a flux of anime when USER is successfully authenticated and has role ADMIN")
    public void listAll_ReturnFluxOfAnime_WhenSuccessful() {
        testClientAdmin
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("listAll returns a flux of anime and user is successfully authenticated and has role ADMIN")
    public void listAll_Flavor2_ReturnFluxOfAnime_WhenSuccessful() {
        testClientAdmin
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists and user is successfully authenticated and has role USER")
    public void findById_ReturnMonoAnime_WhenSuccessful() {
        testClientUser
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns Mono Error when anime does not exist and user is successfully authenticated and has role USER")
    public void findById_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepositoryMock.findById(anyInt()))
                .thenReturn(Mono.empty());

        testClientUser
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException happened");
    }

    @Test
    @DisplayName("save creates an anime when successful and when user is successfully authenticated and has role ADMIN")
    public void save_CreateAnime_WhenSuccessful() {
        var animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        testClientAdmin
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("save returns mono error with bad request when name is empty and when user is successfully authenticated and has role ADMIN")
    public void save_ReturnsError_WhenNameIsEmpty() {
        var animeToBeSaved = new Anime();

        testClientAdmin
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("saveBatch creates a list of anime when successful and when user is successfully authenticated and has role ADMIN")
    public void saveBatch_CreateListOfAnime_WhenSuccessful() {
        var animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        testClientAdmin
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);
    }

    @Test
    @DisplayName("saveBatch returns mono error when one of the objects in the list contains null or empty name and when user is successfully authenticated and has role ADMIN")
    public void saveBatch_ReturnMonoError_WhenContainsInvalidName() {
        var animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        BDDMockito.when(animeRepositoryMock
                .saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), new Anime())))
                .thenReturn(Flux.just(anime, new Anime()));

        testClientAdmin
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, new Anime())))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("delete removes the anime when successful and when user is successfully authenticated and has role ADMIN")
    public void delete_RemovesAnime_WhenSuccessful() {
        testClientAdmin
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete returns Mono error when anime does not exist and when user is successfully authenticated and has role ADMIN")
    public void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepositoryMock.findById(anyInt()))
                .thenReturn(Mono.empty());

        testClientAdmin
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException happened");
    }

    @Test
    @DisplayName("update save updated anime and returns empty mono when successful and when user is successfully authenticated and has role ADMIN")
    public void update_SaveUpdatedAnime_WhenSuccessful() {
        testClientAdmin
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("update returns Mono error when anime does exist and when user is successfully authenticated and has role ADMIN")
    public void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepositoryMock.findById(anyInt()))
                .thenReturn(Mono.empty());

        testClientAdmin
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException happened");
    }

}
