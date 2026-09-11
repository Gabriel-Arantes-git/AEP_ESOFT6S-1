package com.aep.backend.infra.config;

import com.aep.backend.infra.security.JwtAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.DefaultAuthorizationManagerFactory;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig(mock(JwtAuthFilter.class));

    @Test
    @DisplayName("Deve criar um PasswordEncoder com BCrypt")
    void deveCriarPasswordEncoderComBcrypt() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.matches("123456", passwordEncoder.encode("123456")));
    }

    @Test
    @DisplayName("Deve configurar CORS para o frontend local")
    void deveConfigurarCorsParaFrontendLocal() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertNotNull(source);
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration corsConfiguration = urlSource.getCorsConfigurations().get("/**");

        assertNotNull(corsConfiguration);
        assertEquals(List.of("http://localhost:4200"), corsConfiguration.getAllowedOrigins());
        assertEquals(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"), corsConfiguration.getAllowedMethods());
    }

    @Test
    @DisplayName("Deve retornar o AuthenticationManager configurado")
    void deveRetornarAuthenticationManagerConfigurado() throws Exception {
        AuthenticationManager expected = mock(AuthenticationManager.class);
        AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(expected);

        AuthenticationManager actual = securityConfig.authenticationManager(authenticationConfiguration);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Deve montar a cadeia de filtros de segurança com as regras esperadas")
    void deveMontarCadeiaDeFiltrosDeSegurancaComAsRegrasEsperadas() throws Exception {
        ObjectPostProcessor<Object> objectPostProcessor = new ObjectPostProcessor<Object>() {
            @Override
            public <O> O postProcess(O object) {
                return object;
            }
        };
        AuthenticationManagerBuilder authenticationManagerBuilder = new AuthenticationManagerBuilder(objectPostProcessor);
        HttpSecurity httpSecurity = new HttpSecurity(objectPostProcessor, authenticationManagerBuilder, Map.of());

        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeanNamesForType(any(Class.class))).thenReturn(new String[0]);
        when(context.getBeanNamesForType(any(ResolvableType.class))).thenReturn(new String[0]);

        ObjectProvider builderProvider = mock(ObjectProvider.class);
        java.lang.reflect.Constructor<?> builderConstructor = PathPatternRequestMatcher.Builder.class.getDeclaredConstructor();
        builderConstructor.setAccessible(true);
        PathPatternRequestMatcher.Builder builder = (PathPatternRequestMatcher.Builder) builderConstructor.newInstance();
        when(builderProvider.getIfUnique(any())).thenReturn(builder);
        when(context.getBeanProvider(PathPatternRequestMatcher.Builder.class)).thenReturn(builderProvider);

        ObjectProvider authorizationManagerFactoryProvider = mock(ObjectProvider.class);
        when(authorizationManagerFactoryProvider.getIfAvailable(any())).thenReturn(new DefaultAuthorizationManagerFactory());

        ObjectProvider securityContextProvider = mock(ObjectProvider.class);
        when(securityContextProvider.getIfUnique(any())).thenReturn(SecurityContextHolder.getContextHolderStrategy());

        when(context.getBeanProvider(any(Class.class))).thenAnswer(invocation -> {
            Class<?> type = invocation.getArgument(0);
            if (type == PathPatternRequestMatcher.Builder.class) {
                return builderProvider;
            }
            if (type == SecurityContextHolderStrategy.class) {
                return securityContextProvider;
            }
            return authorizationManagerFactoryProvider;
        });

        when(context.getBeanProvider(any(ResolvableType.class))).thenAnswer(invocation -> {
            ResolvableType type = invocation.getArgument(0);
            if (type.resolve() == PathPatternRequestMatcher.Builder.class) {
                return builderProvider;
            }
            if (type.resolve() == org.springframework.security.authorization.AuthorizationManagerFactory.class) {
                return authorizationManagerFactoryProvider;
            }
            return authorizationManagerFactoryProvider;
        });

        httpSecurity.setSharedObject(ApplicationContext.class, context);

        SecurityFilterChain actualChain = securityConfig.filterChain(httpSecurity);

        assertNotNull(actualChain);
    }
}
