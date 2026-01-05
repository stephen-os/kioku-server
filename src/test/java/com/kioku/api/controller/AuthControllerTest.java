package com.kioku.api.controller;

import com.kioku.api.dto.request.LoginRequest;
import com.kioku.api.dto.request.RegisterRequest;
import com.kioku.api.dto.response.AuthResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.entity.UserEntity;
import com.kioku.api.security.JwtUtil;
import com.kioku.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for AuthController using RestTestClient (Spring Boot 4.0).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>User registration endpoint</li>
 *   <li>User login endpoint</li>
 *   <li>Request validation</li>
 *   <li>Error handling</li>
 *   <li>Response formats</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthControllerTest.class);

    // Test data constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    RestTestClient client;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up AuthController test");

        testUser = new UserEntity(TEST_EMAIL, "hashedPassword");
        testUser.setId(TEST_USER_ID);

        client = RestTestClient.bindTo(mockMvc).build();
    }

    // Registration Tests

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterSuccess() {
        logger.debug("Test: Successful registration");

        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD);

        when(userService.registerUser(TEST_EMAIL, TEST_PASSWORD)).thenReturn(testUser);
        when(jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL)).thenReturn(TEST_TOKEN);

        AuthResponse response = client.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);

        verify(userService).registerUser(TEST_EMAIL, TEST_PASSWORD);
        verify(jwtUtil).generateToken(TEST_USER_ID, TEST_EMAIL);

        logger.debug("Test passed: User registered successfully");
    }

    /**
     * Tests registration with duplicate email.
     */
    @Test
    @DisplayName("Should return 400 when email already exists")
    void testRegisterDuplicateEmail() {
        logger.debug("Test: Registration with duplicate email");

        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_PASSWORD);

        when(userService.registerUser(TEST_EMAIL, TEST_PASSWORD))
                .thenThrow(new IllegalArgumentException("Email already in use"));

        ErrorResponse response = client.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Email already in use");

        verify(userService).registerUser(TEST_EMAIL, TEST_PASSWORD);
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());

        logger.debug("Test passed: Duplicate email rejected");
    }

    /**
     * Tests registration with invalid email format.
     */
    @Test
    @DisplayName("Should return 400 when email format is invalid")
    void testRegisterInvalidEmail() {
        logger.debug("Test: Registration with invalid email");

        RegisterRequest request = new RegisterRequest("invalid-email", TEST_PASSWORD);

        client.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).registerUser(anyString(), anyString());

        logger.debug("Test passed: Invalid email rejected");
    }

    /**
     * Tests registration with missing email.
     */
    @Test
    @DisplayName("Should return 400 when email is missing")
    void testRegisterMissingEmail() {
        logger.debug("Test: Registration with missing email");

        String requestJson = "{\"password\":\"" + TEST_PASSWORD + "\"}";

        client.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).registerUser(anyString(), anyString());

        logger.debug("Test passed: Missing email rejected");
    }

    /**
     * Tests registration with missing password.
     */
    @Test
    @DisplayName("Should return 400 when password is missing")
    void testRegisterMissingPassword() {
        logger.debug("Test: Registration with missing password");

        String requestJson = "{\"email\":\"" + TEST_EMAIL + "\"}";

        client.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).registerUser(anyString(), anyString());

        logger.debug("Test passed: Missing password rejected");
    }

    /**
     * Tests registration with password that's too short.
     */
    @Test
    @DisplayName("Should return 400 when password is too short")
    void testRegisterPasswordTooShort() {
        logger.debug("Test: Registration with short password");

        RegisterRequest request = new RegisterRequest(TEST_EMAIL, "short");

        client.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).registerUser(anyString(), anyString());

        logger.debug("Test passed: Short password rejected");
    }

    // Login Tests

    /**
     * Tests successful user login.
     */
    @Test
    @DisplayName("Should login user successfully")
    void testLoginSuccess() {
        logger.debug("Test: Successful login");

        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        when(userService.authenticateUser(TEST_EMAIL, TEST_PASSWORD)).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(TEST_USER_ID, TEST_EMAIL)).thenReturn(TEST_TOKEN);

        AuthResponse response = client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);

        verify(userService).authenticateUser(TEST_EMAIL, TEST_PASSWORD);
        verify(jwtUtil).generateToken(TEST_USER_ID, TEST_EMAIL);

        logger.debug("Test passed: User logged in successfully");
    }

    /**
     * Tests login with invalid credentials.
     */
    @Test
    @DisplayName("Should return 401 when credentials are invalid")
    void testLoginInvalidCredentials() {
        logger.debug("Test: Login with invalid credentials");

        LoginRequest request = new LoginRequest(TEST_EMAIL, "WrongPassword123!");

        when(userService.authenticateUser(TEST_EMAIL, "WrongPassword123!")).thenReturn(Optional.empty());

        ErrorResponse response = client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Invalid email or password");

        verify(userService).authenticateUser(TEST_EMAIL, "WrongPassword123!");
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());

        logger.debug("Test passed: Invalid credentials rejected");
    }

    /**
     * Tests login with non-existent user.
     */
    @Test
    @DisplayName("Should return 401 when user doesn't exist")
    void testLoginNonExistentUser() {
        logger.debug("Test: Login with non-existent user");

        LoginRequest request = new LoginRequest("nonexistent@example.com", TEST_PASSWORD);

        when(userService.authenticateUser("nonexistent@example.com", TEST_PASSWORD))
                .thenReturn(Optional.empty());

        ErrorResponse response = client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Invalid email or password");

        verify(jwtUtil, never()).generateToken(anyLong(), anyString());

        logger.debug("Test passed: Non-existent user rejected");
    }

    /**
     * Tests login with invalid email format.
     */
    @Test
    @DisplayName("Should return 400 when login email format is invalid")
    void testLoginInvalidEmail() {
        logger.debug("Test: Login with invalid email format");

        LoginRequest request = new LoginRequest("not-an-email", TEST_PASSWORD);

        client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).authenticateUser(anyString(), anyString());

        logger.debug("Test passed: Invalid email format rejected");
    }

    /**
     * Tests login with missing email.
     */
    @Test
    @DisplayName("Should return 400 when login email is missing")
    void testLoginMissingEmail() {
        logger.debug("Test: Login with missing email");

        String requestJson = "{\"password\":\"" + TEST_PASSWORD + "\"}";

        client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).authenticateUser(anyString(), anyString());

        logger.debug("Test passed: Missing email rejected");
    }

    /**
     * Tests login with missing password.
     */
    @Test
    @DisplayName("Should return 400 when login password is missing")
    void testLoginMissingPassword() {
        logger.debug("Test: Login with missing password");

        String requestJson = "{\"email\":\"" + TEST_EMAIL + "\"}";

        client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).authenticateUser(anyString(), anyString());

        logger.debug("Test passed: Missing password rejected");
    }

    /**
     * Tests login with empty request body.
     */
    @Test
    @DisplayName("Should return 400 when request body is empty")
    void testLoginEmptyBody() {
        logger.debug("Test: Login with empty body");

        client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).authenticateUser(anyString(), anyString());

        logger.debug("Test passed: Empty body rejected");
    }

    /**
     * Tests registration with malformed JSON.
     */
    @Test
    @DisplayName("Should return 400 when JSON is malformed")
    void testMalformedJson() {
        logger.debug("Test: Request with malformed JSON");

        String malformedJson = "{\"email\":\"test@example.com\", \"password\":}";

        client.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(malformedJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).registerUser(anyString(), anyString());

        logger.debug("Test passed: Malformed JSON rejected");
    }
}