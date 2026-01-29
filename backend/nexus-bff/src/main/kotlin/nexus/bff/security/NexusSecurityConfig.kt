package nexus.bff.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableConfigurationProperties(NexusOAuth2Properties::class)
class NexusSecurityConfig(
    private val properties: NexusOAuth2Properties,
    private val authorizationContextFilter: NexusAuthorizationContextFilter, // ★ DI 注入
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/v1/**").authenticated()
                    .anyRequest().permitAll()
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }

        // ★ new せず、Bean を差し込む
        http.addFilterAfter(authorizationContextFilter, BearerTokenAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withIssuerLocation(properties.issuerUri).build()
    }
}

@ConfigurationProperties(prefix = "nexus.security.oauth2")
data class NexusOAuth2Properties(
    val issuerUri: String,
)
