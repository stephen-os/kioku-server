package com.kioku.api.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the current authenticated user's ID into controller method parameters.
 *
 * <p>This annotation works in conjunction with {@link CurrentUserArgumentResolver} to
 * automatically resolve the authenticated user's ID from the JWT token in the security context.
 *
 * <p><strong>Usage:</strong>
 * <pre>
 * {@code
 * @GetMapping("/profile")
 * public ResponseEntity<UserProfile> getProfile(@CurrentUser Long userId) {
 *     // userId is automatically resolved from JWT token
 *     return userService.getProfile(userId);
 * }
 * }
 * </pre>
 *
 * <p><strong>Requirements:</strong>
 * <ul>
 *   <li>User must be authenticated with a valid JWT token</li>
 *   <li>Parameter type must be {@code Long}</li>
 *   <li>Returns {@code null} if no authenticated user</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 * @see CurrentUserArgumentResolver
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}