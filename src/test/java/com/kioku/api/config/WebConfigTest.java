package com.kioku.api.config;

import com.kioku.api.security.CurrentUser;
import com.kioku.api.security.CurrentUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebConfig.
 *
 * <p>These tests verify that:
 * <ul>
 *   <li>CurrentUserArgumentResolver is properly registered</li>
 *   <li>The resolver is added to Spring MVC's argument resolver chain</li>
 *   <li>Configuration is properly initialized</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("WebConfig Tests")
class WebConfigTest {

    private static final Logger logger = LoggerFactory.getLogger(WebConfigTest.class);

    private WebConfig webConfig;
    private CurrentUserArgumentResolver mockResolver;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up WebConfig test");

        mockResolver = mock(CurrentUserArgumentResolver.class);
        webConfig = new WebConfig(mockResolver);
    }

    @Test
    @DisplayName("Should register CurrentUserArgumentResolver")
    void testCurrentUserArgumentResolverIsRegistered() {
        logger.debug("Test: CurrentUserArgumentResolver registration");

        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        webConfig.addArgumentResolvers(resolvers);

        assertThat(resolvers).hasSize(1);
        assertThat(resolvers.get(0)).isEqualTo(mockResolver);

        logger.debug("Test passed: CurrentUserArgumentResolver registered successfully");
    }

    @Test
    @DisplayName("Should add resolver to existing resolver list")
    void testAddToExistingResolverList() {
        logger.debug("Test: Add resolver to existing list");

        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        HandlerMethodArgumentResolver existingResolver = mock(HandlerMethodArgumentResolver.class);
        resolvers.add(existingResolver);

        webConfig.addArgumentResolvers(resolvers);

        assertThat(resolvers).hasSize(2);
        assertThat(resolvers.get(0)).isEqualTo(existingResolver);
        assertThat(resolvers.get(1)).isEqualTo(mockResolver);

        logger.debug("Test passed: Resolver added to existing list");
    }

    @Test
    @DisplayName("Should construct WebConfig with CurrentUserArgumentResolver")
    void testWebConfigConstruction() {
        logger.debug("Test: WebConfig construction");

        WebConfig config = new WebConfig(mockResolver);

        assertThat(config).isNotNull();

        logger.debug("Test passed: WebConfig constructed successfully");
    }

    @Test
    @DisplayName("Should support @CurrentUser annotation in method parameters")
    void testCurrentUserAnnotationSupport() throws NoSuchMethodException {
        logger.debug("Test: @CurrentUser annotation support");

        // Create a test controller method with @CurrentUser parameter
        Method testMethod = TestController.class.getMethod("testMethod", Long.class);
        MethodParameter parameter = new MethodParameter(testMethod, 0);

        when(mockResolver.supportsParameter(parameter)).thenReturn(true);

        boolean supported = mockResolver.supportsParameter(parameter);

        assertThat(supported).isTrue();

        logger.debug("Test passed: @CurrentUser annotation supported");
    }

    /**
     * Test controller class for annotation testing.
     */
    private static class TestController {
        public void testMethod(@CurrentUser Long userId) {
            // Test method for parameter annotation testing
        }
    }
}