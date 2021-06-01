package dev.brunocesar.webflux.util;

import dev.brunocesar.webflux.domain.Anime;

public class AnimeCreator {

    public static Anime createAnimeToBeSaved() {
        var anime = new Anime();
        anime.setName("Fullmetal");
        return anime;
    }

    public static Anime createValidAnime() {
        var anime = new Anime();
        anime.setId(1);
        anime.setName("Fullmetal");
        return anime;
    }

    public static Anime createValidUpdatedAnime() {
        var anime = new Anime();
        anime.setId(1);
        anime.setName("Fullmetal 2");
        return anime;
    }

}
