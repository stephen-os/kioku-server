package com.kioku.api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves controller method parameters annotated with {@link CurrentUser}.
 *
 * <p>This resolver extracts the authenticated user's ID from the Spring Security
 * context and injects it into controller method parameters. The user ID is set
 * in the security context by {@link JwtAuthenticationFilter} after validating
 * the JWT token.
 *
 * <p><strong>Supported Parameter Types:</strong>
 * <ul>
 *   <li>{@code Long} - The user's database ID</li>
 * </ul>
 *
 * <p><strong>Security Note:</strong> This resolver only works for authenticated
 * requests. If no authentication is present or the principal is not a Long,
 * it returns {@code null}.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 * @see CurrentUser
 * @see JwtAuthenticationFilter
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger logger = LoggerFactory.getLogger(CurrentUserArgumentResolver.class);

    /**
     * Determines if this resolver supports the given method parameter.
     *
     * <p>Returns {@code true} if the parameter is annotated with {@link CurrentUser}
     * and the parameter type is {@code Long}.
     *
     * @param parameter the method parameter to check
     * @return {@code true} if this resolver can resolve the parameter, {@code false} otherwise
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.getParameterAnnotation(CurrentUser.class) != null;
        boolean isLongType = parameter.getParameterType().equals(Long.class);

        logger.debug("Checking parameter support: hasAnnotation={}, isLongType={}",
                hasAnnotation, isLongType);

        return hasAnnotation && isLongType;
    }

    /**
     * Resolves the current user's ID from the security context.
     *
     * <p>Extracts the user ID from the authentication principal. The principal
     * is set by {@link JwtAuthenticationFilter} when a valid JWT token is present.
     *
     * @param parameter the method parameter to resolve
     * @param mavContainer the model and view container (not used)
     * @param webRequest the current web request (not used)
     * @param binderFactory the binder factory (not used)
     * @return the authenticated user's ID, or {@code null} if not authenticated
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Long userId) {
            logger.debug("Resolved current user ID: {}", userId);
            return userId;
        }

        logger.debug("No authenticated user found in security context");
        return null;
    }
}