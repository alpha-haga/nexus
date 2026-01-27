package nexus.bff.security

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * BFF OAuth2 (Keycloak) 設定
 *
 * application-*.yml の
 * nexus.security.oauth2.issuer-uri
 * を読み取る。
 */
@ConfigurationProperties(prefix = "nexus.security.oauth2")
data class NexusOauth2Properties(
    val issuerUri: String
)
