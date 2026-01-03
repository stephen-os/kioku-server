package com.kioku.api.config;

import com.kioku.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for JWT-based authentication.
 *
 * <p>This configuration class sets up:
 * <ul>
 *   <li>JWT token-based authentication (stateless sessions)</li>
 *   <li>CORS policy for frontend communication</li>
 *   <li>Password encryption using BCrypt</li>
 *   <li>Public and protected endpoint authorization rules</li>
 * </ul>
 *
 * <p><strong>Authentication Flow:</strong>
 * <ol>
 *   <li>Client sends credentials to /api/auth/login or /api/auth/register</li>
 *   <li>Server validates credentials and returns JWT token</li>
 *   <li>Client includes JWT in Authorization header: "Bearer {token}"</li>
 *   <li>{@link JwtAuthenticationFilter} validates token and sets authentication</li>
 *   <li>Request proceeds to controller with authenticated user context</li>
 * </ol>
 *
 * <p><strong>Security Features:</strong>
 * <ul>
 *   <li>Stateless sessions (no server-side session storage)</li>
 *   <li>CSRF protection disabled (safe for JWT-based APIs)</li>
 *   <li>BCrypt password hashing with automatic salt generation</li>
 *   <li>CORS configured for cross-origin frontend communication</li>
 * </ul>
 *
 * <p><strong>⚠️ PRODUCTION DEPLOYMENT REQUIRED CHANGES:</strong>
 * <ul>
 *   <li><strong>CORS Origins:</strong> Update {@code setAllowedOriginPatterns}
 *       from "http://localhost:3000" to your production frontend URL(s):
 *       <pre>configuration.setAllowedOriginPatterns(List.of(
 *     "https://yourdomain.com",
 *     "https://www.yourdomain.com"
 * ));</pre>
 *   </li>
 *   <li><strong>HTTPS Only:</strong> Ensure all allowed origins use HTTPS in production</li>
 *   <li><strong>JWT Secret:</strong> Configure {@link com.kioku.api.security.JwtUtil}
 *       with a strong secret key from environment variables (not hardcoded)</li>
 *   <li><strong>Remove Test Endpoints:</strong> Remove or secure "/api/test/**" endpoints:
 *       <pre>.requestMatchers("/api/test/**").denyAll()</pre>
 *   </li>
 * </ul>
 *
 * <p><strong>Endpoint Authorization Rules:</strong>
 * <ul>
 *   <li><strong>Public (no authentication required):</strong>
 *       <ul>
 *         <li>/api/auth/register - User registration</li>
 *         <li>/api/auth/login - User login</li>
 *         <li>/api/test/** - Test endpoints (⚠️ REMOVE IN PRODUCTION)</li>
 *       </ul>
 *   </li>
 *   <li><strong>Protected (authentication required):</strong>
 *       <ul>
 *         <li>All other endpoints (/api/decks/**, /api/tags/**, etc.)</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 * @see JwtAuthenticationFilter
 * @see com.kioku.api.security.JwtUtil
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs SecurityConfig with required dependencies.
     *
     * @param jwtAuthenticationFilter the JWT authentication filter
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures the security filter chain.
     *
     * <p>This bean defines the core security configuration including:
     * <ul>
     *   <li>CORS policy application</li>
     *   <li>CSRF protection (disabled for stateless JWT authentication)</li>
     *   <li>Session management (stateless)</li>
     *   <li>Request authorization rules (public vs protected endpoints)</li>
     *   <li>JWT authentication filter registration</li>
     * </ul>
     *
     * <p><strong>Filter Order:</strong>
     * JwtAuthenticationFilter runs before UsernamePasswordAuthenticationFilter
     * to validate JWT tokens before standard Spring Security authentication.
     *
     * @param http the HttpSecurity configuration builder
     * @return the configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll() // ⚠️ REMOVE IN PRODUCTION
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) policy.
     *
     * <p>CORS allows the frontend application (running on a different origin)
     * to make requests to this backend API.
     *
     * <p><strong>⚠️ PRODUCTION REQUIRED CHANGE:</strong>
     * Update {@code setAllowedOriginPatterns} to include your production frontend URL(s).
     * Current configuration only allows http://localhost:3000 (development).
     *
     * <p><strong>Configuration Details:</strong>
     * <ul>
     *   <li><strong>Allowed Origins:</strong> http://localhost:3000 (⚠️ UPDATE FOR PROD)</li>
     *   <li><strong>Allowed Methods:</strong> GET, POST, PUT, DELETE, OPTIONS, PATCH</li>
     *   <li><strong>Allowed Headers:</strong> All headers (*)</li>
     *   <li><strong>Allow Credentials:</strong> true (allows cookies and Authorization headers)</li>
     *   <li><strong>Exposed Headers:</strong> Authorization (allows frontend to read JWT)</li>
     *   <li><strong>Max Age:</strong> 3600 seconds (1 hour preflight cache)</li>
     * </ul>
     *
     * <p><strong>Production Example:</strong>
     * <pre>
     * configuration.setAllowedOriginPatterns(List.of(
     *     "https://yourdomain.com",
     *     "https://www.yourdomain.com",
     *     "https://app.yourdomain.com"
     * ));
     * </pre>
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ⚠️ PRODUCTION: Replace with actual frontend URL(s)
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Provides the password encoder for hashing user passwords.
     *
     * <p>Uses BCrypt hashing algorithm with the following properties:
     * <ul>
     *   <li>Adaptive hashing (automatically increases difficulty over time)</li>
     *   <li>Automatic salt generation (unique salt per password)</li>
     *   <li>Default strength: 10 rounds (2^10 = 1024 iterations)</li>
     *   <li>One-way encryption (cannot be reversed)</li>
     * </ul>
     *
     * <p><strong>Security Notes:</strong>
     * <ul>
     *   <li>Never store passwords in plain text</li>
     *   <li>BCrypt automatically handles salt generation and storage</li>
     *   <li>Same password will hash to different values due to unique salts</li>
     *   <li>Verification uses {@code encoder.matches(rawPassword, encodedPassword)}</li>
     * </ul>
     *
     * <p><strong>Production Considerations:</strong>
     * BCrypt strength of 10 is adequate for most applications. Higher values
     * (11-12) provide more security but increase login time. No changes needed
     * for production unless you want to increase strength:
     * <pre>return new BCryptPasswordEncoder(12); // Increase to 12 rounds</pre>
     *
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}