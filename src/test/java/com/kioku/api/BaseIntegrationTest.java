package com.kioku.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 *
 * <p>This class provides common configuration for all integration tests:
 * <ul>
 *   <li>Starts Spring Boot application context</li>
 *   <li>Activates 'test' profile</li>
 *   <li>Configures Testcontainers for PostgreSQL</li>
 * </ul>
 *
 * <p>All repository and service integration tests should extend this class
 * to gain access to a real PostgreSQL database running in a Docker container.
 *
 * <p><strong>Usage:</strong>
 * <pre>
 * {@code
 * @Transactional
 * class UserRepositoryTest extends BaseIntegrationTest {
 *     @Autowired
 *     private UserRepository userRepository;
 *
 *     @Test
 *     void testSaveUser() {
 *         // Test implementation
 *     }
 * }
 * }
 * </pre>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
public abstract class BaseIntegrationTest {
    // All integration tests extend this class
}