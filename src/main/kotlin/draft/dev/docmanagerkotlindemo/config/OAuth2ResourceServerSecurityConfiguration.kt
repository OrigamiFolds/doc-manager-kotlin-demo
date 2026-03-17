package draft.dev.docmanagerkotlindemo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class OAuth2ResourceServerSecurityConfiguration {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/api/fetchDocuments").hasAuthority("SCOPE_read:documents")

                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                    jwt.decoder(jwtDecoder())         // Added custom JwtDecoder to the code
                }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = mutableListOf<GrantedAuthority>()

            // Map scopes
            val scopes = (jwt.claims["scope"] as? String)?.split(" ") ?: emptyList()
            authorities.addAll(scopes.map { SimpleGrantedAuthority("SCOPE_$it") })

            // Map roles
            val roles = jwt.claims["roles"] as? Collection<*> ?: emptyList<Any>()
            authorities.addAll(roles.map { SimpleGrantedAuthority("ROLE_$it") })

            // Map permissions
            val permissions = jwt.claims["permissions"] as? Collection<*> ?: emptyList<Any>()
            authorities.addAll(permissions.map { SimpleGrantedAuthority(it.toString()) })

            authorities
        }
        return converter
    }


    class AudienceValidator(private val audience: String) : OAuth2TokenValidator<Jwt> {
        override fun validate(token: Jwt): OAuth2TokenValidatorResult {
            return if (token.audience.contains(audience)) {
                OAuth2TokenValidatorResult.success()
            } else {
                OAuth2TokenValidatorResult.failure(
                    OAuth2Error("invalid_token", "The required audience is missing", null)
                )
            }
        }
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val issuer = "https://myapp.com/auth"           // Replace with your own official issuer URI
        val audience = "http://localhost:8080/api/"

        val decoder = JwtDecoders.fromIssuerLocation<NimbusJwtDecoder>(issuer)

        // Add audience validation
        val audienceValidator = AudienceValidator(audience)
        val issuerValidator = JwtValidators.createDefaultWithIssuer(issuer)

        val validator = DelegatingOAuth2TokenValidator(listOf(issuerValidator, audienceValidator))
        (decoder as NimbusJwtDecoder).setJwtValidator(validator)

        return decoder
    }

}