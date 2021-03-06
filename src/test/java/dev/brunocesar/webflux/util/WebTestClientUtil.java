package dev.brunocesar.webflux.util;

import org.springframework.context.ApplicationContext;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

@Component
public class WebTestClientUtil {

    private final ApplicationContext applicationContext;

    public WebTestClientUtil(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public WebTestClient authenticateClient(String username, String password) {
        return WebTestClient.bindToApplicationContext(applicationContext)
                .apply(SecurityMockServerConfigurers.springSecurity())
                .configureClient()
                .filter(ExchangeFilterFunctions.basicAuthentication(username, password))
                .build();
    }

}
