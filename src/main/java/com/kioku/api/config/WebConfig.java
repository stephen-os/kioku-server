package com.kioku.api.config;

import com.kioku.api.security.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC configuration for custom argument resolvers.
 *
 * <p>This configuration class registers custom method argument resolvers
 * that allow Spring MVC to automatically inject custom-typed parameters
 * into controller methods.
 *
 * <p><strong>Registered Resolvers:</strong>
 * <ul>
 *   <li>{@link CurrentUserArgumentResolver} - Resolves {@code @CurrentUser} annotated parameters
 *       by extracting the authenticated user ID from the Spring Security context</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong>
 * <pre>
 * {@code @GetMapping("/api/decks")}
 * public ResponseEntity{@code <List<DeckResponse>>} getUserDecks({@code @CurrentUser} Long userId) {
 *     // userId is automatically resolved from JWT token
 *     List{@code <DeckEntity>} decks = deckService.getUserDecks(userId);
 *     return ResponseEntity.ok(decks.stream().map(DeckResponse::new).toList());
 * }
 * </pre>
 *
 * <p><strong>How It Works:</strong>
 * <ol>
 *   <li>User makes authenticated request with JWT token in Authorization header</li>
 *   <li>{@link com.kioku.api.security.JwtAuthenticationFilter} validates token
 *       and sets user ID in SecurityContext</li>
 *   <li>{@link CurrentUserArgumentResolver} extracts user ID from SecurityContext</li>
 *   <li>Spring MVC injects the user ID into controller method parameter</li>
 * </ol>
 *
 * <p><strong>Production Considerations:</strong>
 * This configuration is identical for all environments (dev, test, prod).
 * No changes required for production deployment.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 * @see CurrentUserArgumentResolver
 * @see com.kioku.api.security.CurrentUser
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    /**
     * Constructs WebConfig with required dependencies.
     *
     * @param currentUserArgumentResolver the resolver for {@code @CurrentUser} parameters
     */
    public WebConfig(CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    /**
     * Registers custom method argument resolvers.
     *
     * <p>This method is called by Spring MVC during application startup
     * to register custom resolvers that handle specific parameter types
     * in controller methods.
     *
     * @param resolvers the list of resolvers to which custom resolvers are added
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}