package com.polarbookshop.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

//@Configuration
//@EnableWebFluxSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

//  private final PolarProperties polarProperties;
//
//  public SecurityConfig(PolarProperties polarProperties) {
//    this.polarProperties = polarProperties;
//  }

  @Bean
  ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
    return new WebSessionServerOAuth2AuthorizedClientRepository();
  }

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository) {
    /*  책 예제
    return http
      .authorizeExchange(exchange -> exchange
        //.pathMatchers(HttpMethod.GET, "/", "/*.css", "/*.js", "/favicon.ico").permitAll()
        .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico").permitAll()
        .pathMatchers(HttpMethod.GET, "/books/**").permitAll()
        .anyExchange().authenticated()
      )
      .exceptionHandling(exceptionHandling -> exceptionHandling
        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
      .oauth2Login(Customizer.withDefaults())
      .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
      //.csrf().disable()
      .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
      .csrf(ServerHttpSecurity.CsrfSpec::disable)
      .build();
     */
    // 샘플소스 Github sb-3-main 소스 (스프링 부트 3.x)
    return http
      .authorizeExchange(exchange -> exchange
        .pathMatchers("/actuator/**").permitAll()
        .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico").permitAll()
        .pathMatchers(HttpMethod.GET, "/books/**").permitAll()
        //.pathMatchers(HttpMethod.PUT, "/books/**").permitAll()
        //.pathMatchers("/books/**").permitAll()
        .anyExchange().authenticated()
      )
      .exceptionHandling(exceptionHandling -> exceptionHandling
        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
      .oauth2Login(Customizer.withDefaults())
      .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
      .csrf(csrf -> csrf
        .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new XorServerCsrfTokenRequestAttributeHandler()::handle))
      .build();
  }

  private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
    ReactiveClientRegistrationRepository clientRegistrationRepository) {
    var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
    //oidcLogoutSuccessHandler.setPostLogoutRedirectUri("http://192.168.56.40:9000");
    oidcLogoutSuccessHandler.setPostLogoutRedirectUri("http://polarbookshop.example.com:32272");
    //oidcLogoutSuccessHandler.setPostLogoutRedirectUri(polarProperties.getLogoutRedirectUri());
    return oidcLogoutSuccessHandler;
  }

  @Bean
  WebFilter csrfWebFilter() {

    return (exchange, chain) -> {
      exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
        Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
        return csrfToken != null ? csrfToken.then() : Mono.empty();
      }));
      return chain.filter(exchange);
    };
  }
}
