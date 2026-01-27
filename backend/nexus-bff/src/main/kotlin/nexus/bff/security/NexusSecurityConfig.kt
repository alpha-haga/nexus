package nexus.bff.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

/**
 * P1-1: BFF で Keycloak token を Resource Server として受け、
 * claim（nexus_db_access）による認可と Context set を行う。
 */
@Configuration
@EnableConfigurationProperties(NexusOauth2Properties::class)
class NexusSecurityConfig(
    private val props: NexusOauth2Properties,
    private val authorizationContextFilter: NexusAuthorizationContextFilter,
) {

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return JwtDecoders.fromIssuerLocation(props.issuerUri)
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    // health check / swagger
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }

        // 認証後に実施する（SecurityContext に Jwt が入った状態で動作させる）
        http.addFilterAfter(authorizationContextFilter, BearerTokenAuthenticationFilter::class.java)

        return http.build()
    }
}
