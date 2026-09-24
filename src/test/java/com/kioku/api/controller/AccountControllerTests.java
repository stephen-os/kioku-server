package com.kioku.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kioku.api.dto.request.DeleteAccountRequest;
import com.kioku.api.dto.request.UpdateEmailRequest;
import com.kioku.api.dto.request.UpdatePasswordRequest;
import com.kioku.api.dto.request.VerifyEmailChangeRequest;
import com.kioku.api.dto.response.AccountResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.model.User;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.security.JwtAuthenticationFilter;
import com.kioku.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Integration tests for AccountController using RestTestClient (Spring Boot 4.0).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Get account profile endpoint</li>
 *   <li>Email change initiation endpoint</li>
 *   <li>Email change verification endpoint</li>
 *   <li>Password update endpoint</li>
 *   <li>Account deletion endpoint</li>
 *   <li>Request validation</li>
 *   <li>Error handling</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(
        controllers = AccountController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@DisplayName("AccountController Integration Tests")
class AccountControllerTests {

    private static final Logger logger = LoggerFactory.getLogger(AccountControllerTests.class);

    // Test data constants
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String TEST_EMAIL = "test@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final String TEST_PASSWORD = "SecurePassword123!";
    private static final String NEW_PASSWORD = "NewSecurePassword456!";
    private static final String VERIFICATION_TOKEN = "verification-token-uuid-12345";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    RestTestClient client;
    private User testUser;

    @TestConfiguration
    static class ControllerTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterAnnotation(CurrentUser.class) != null
                            && parameter.getParameterType().equals(UUID.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              WebDataBinderFactory binderFactory) {
                    return TEST_USER_ID;
                }
            });
        }
    }

    @BeforeEach
    void setUp() {
        logger.debug("Setting up AccountController test");

        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(TEST_USER_ID);
        when(testUser.getEmail()).thenReturn(TEST_EMAIL);
        when(testUser.isEmailVerified()).thenReturn(true);
        when(testUser.getStatus()).thenReturn(User.UserStatus.ACTIVE);
        when(testUser.getCreatedAt()).thenReturn(Instant.now().minus(30, ChronoUnit.DAYS));
        when(testUser.getLastLoginAt()).thenReturn(Instant.now());

        client = RestTestClient.bindTo(mockMvc).build();
    }

    // Get Profile Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should get account profile successfully")
    void testGetProfileSuccess() {
        logger.debug("Test: Successful profile retrieval");

        when(userService.getAccountProfile(TEST_USER_ID)).thenReturn(testUser);

        AccountResponse response = client.get()
                .uri("/api/account")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(response.isEmailVerified()).isTrue();
        assertThat(response.getStatus()).isEqualTo("ACTIVE");

        verify(userService).getAccountProfile(TEST_USER_ID);

        logger.debug("Test passed: Profile retrieved successfully");
    }

    // Email Change Initiation Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should initiate email change successfully")
    void testInitiateEmailChangeSuccess() {
        logger.debug("Test: Successful email change initiation");

        UpdateEmailRequest request = new UpdateEmailRequest(TEST_PASSWORD, NEW_EMAIL);

        when(userService.initiateEmailChange(TEST_USER_ID, TEST_PASSWORD, NEW_EMAIL))
                .thenReturn(VERIFICATION_TOKEN);

        @SuppressWarnings("unchecked")
        Map<String, String> response = client.patch()
                .uri("/api/account/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.get("token")).isEqualTo(VERIFICATION_TOKEN);
        assertThat(response.get("message")).contains("Email change initiated");

        verify(userService).initiateEmailChange(TEST_USER_ID, TEST_PASSWORD, NEW_EMAIL);

        logger.debug("Test passed: Email change initiated successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when initiating email change with incorrect password")
    void testInitiateEmailChangeIncorrectPassword() {
        logger.debug("Test: Email change initiation with incorrect password");

        UpdateEmailRequest request = new UpdateEmailRequest(TEST_PASSWORD, NEW_EMAIL);

        when(userService.initiateEmailChange(TEST_USER_ID, TEST_PASSWORD, NEW_EMAIL))
                .thenThrow(new IllegalArgumentException("Current password is incorrect"));

        ErrorResponse response = client.patch()
                .uri("/api/account/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("Current password is incorrect");

        logger.debug("Test passed: Incorrect password rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when email is already registered")
    void testInitiateEmailChangeEmailExists() {
        logger.debug("Test: Email change with already registered email");

        UpdateEmailRequest request = new UpdateEmailRequest(TEST_PASSWORD, NEW_EMAIL);

        when(userService.initiateEmailChange(TEST_USER_ID, TEST_PASSWORD, NEW_EMAIL))
                .thenThrow(new IllegalArgumentException("Email is already registered"));

        ErrorResponse response = client.patch()
                .uri("/api/account/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("already registered");

        logger.debug("Test passed: Already registered email rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when new email is missing")
    void testInitiateEmailChangeMissingEmail() {
        logger.debug("Test: Email change with missing new email");

        String requestJson = "{\"currentPassword\":\"" + TEST_PASSWORD + "\"}";

        client.patch()
                .uri("/api/account/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).initiateEmailChange(any(UUID.class), anyString(), anyString());

        logger.debug("Test passed: Missing email rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when new email is invalid")
    void testInitiateEmailChangeInvalidEmail() {
        logger.debug("Test: Email change with invalid email format");

        UpdateEmailRequest request = new UpdateEmailRequest(TEST_PASSWORD, "not-an-email");

        client.patch()
                .uri("/api/account/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).initiateEmailChange(any(UUID.class), anyString(), anyString());

        logger.debug("Test passed: Invalid email rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when current password is missing")
    void testInitiateEmailChangeMissingPassword() {
        logger.debug("Test: Email change with missing password");

        String requestJson = "{\"newEmail\":\"" + NEW_EMAIL + "\"}";

        client.patch()
                .uri("/api/account/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).initiateEmailChange(any(UUID.class), anyString(), anyString());

        logger.debug("Test passed: Missing password rejected");
    }

    // Email Change Verification Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should verify email change successfully")
    void testVerifyEmailChangeSuccess() {
        logger.debug("Test: Successful email change verification");

        VerifyEmailChangeRequest request = new VerifyEmailChangeRequest(VERIFICATION_TOKEN);

        when(userService.confirmEmailChange(VERIFICATION_TOKEN)).thenReturn(true);

        @SuppressWarnings("unchecked")
        Map<String, String> response = client.post()
                .uri("/api/account/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.get("message")).contains("Email changed successfully");

        verify(userService).confirmEmailChange(VERIFICATION_TOKEN);

        logger.debug("Test passed: Email change verified successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when verification token is invalid")
    void testVerifyEmailChangeInvalidToken() {
        logger.debug("Test: Email change verification with invalid token");

        VerifyEmailChangeRequest request = new VerifyEmailChangeRequest("invalid-token");

        when(userService.confirmEmailChange("invalid-token")).thenReturn(false);

        ErrorResponse response = client.post()
                .uri("/api/account/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("Invalid or expired");

        logger.debug("Test passed: Invalid token rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when verification token is missing")
    void testVerifyEmailChangeMissingToken() {
        logger.debug("Test: Email change verification with missing token");

        String requestJson = "{}";

        client.post()
                .uri("/api/account/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).confirmEmailChange(anyString());

        logger.debug("Test passed: Missing token rejected");
    }

    // Password Update Tests

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should update password successfully")
    void testUpdatePasswordSuccess() {
        logger.debug("Test: Successful password update");

        UpdatePasswordRequest request = new UpdatePasswordRequest(TEST_PASSWORD, NEW_PASSWORD);

        when(userService.updatePassword(TEST_USER_ID, TEST_PASSWORD, NEW_PASSWORD)).thenReturn(true);

        @SuppressWarnings("unchecked")
        Map<String, String> response = client.patch()
                .uri("/api/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.get("message")).contains("Password updated successfully");

        verify(userService).updatePassword(TEST_USER_ID, TEST_PASSWORD, NEW_PASSWORD);

        logger.debug("Test passed: Password updated successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when current password is incorrect")
    void testUpdatePasswordIncorrectCurrent() {
        logger.debug("Test: Password update with incorrect current password");

        UpdatePasswordRequest request = new UpdatePasswordRequest("WrongPassword", NEW_PASSWORD);

        when(userService.updatePassword(TEST_USER_ID, "WrongPassword", NEW_PASSWORD)).thenReturn(false);

        ErrorResponse response = client.patch()
                .uri("/api/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("Current password is incorrect");

        logger.debug("Test passed: Incorrect password rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when new password is too short")
    void testUpdatePasswordTooShort() {
        logger.debug("Test: Password update with password too short");

        UpdatePasswordRequest request = new UpdatePasswordRequest(TEST_PASSWORD, "short");

        client.patch()
                .uri("/api/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).updatePassword(any(UUID.class), anyString(), anyString());

        logger.debug("Test passed: Short password rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when current password is missing")
    void testUpdatePasswordMissingCurrent() {
        logger.debug("Test: Password update with missing current password");

        String requestJson = "{\"newPassword\":\"" + NEW_PASSWORD + "\"}";

        client.patch()
                .uri("/api/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).updatePassword(any(UUID.class), anyString(), anyString());

        logger.debug("Test passed: Missing current password rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when new password is missing")
    void testUpdatePasswordMissingNew() {
        logger.debug("Test: Password update with missing new password");

        String requestJson = "{\"currentPassword\":\"" + TEST_PASSWORD + "\"}";

        client.patch()
                .uri("/api/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).updatePassword(any(UUID.class), anyString(), anyString());

        logger.debug("Test passed: Missing new password rejected");
    }

    // Account Deletion Tests
    // Note: Using MockMvc directly for DELETE with body since RestTestClient doesn't support it

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should delete account successfully")
    void testDeleteAccountSuccess() throws Exception {
        logger.debug("Test: Successful account deletion");

        DeleteAccountRequest request = new DeleteAccountRequest(TEST_PASSWORD);
        ObjectMapper objectMapper = new ObjectMapper();

        doNothing().when(userService).softDeleteAccount(TEST_USER_ID, TEST_PASSWORD);

        mockMvc.perform(delete("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).softDeleteAccount(TEST_USER_ID, TEST_PASSWORD);

        logger.debug("Test passed: Account deleted successfully");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when deleting with incorrect password")
    void testDeleteAccountIncorrectPassword() throws Exception {
        logger.debug("Test: Account deletion with incorrect password");

        DeleteAccountRequest request = new DeleteAccountRequest("WrongPassword");
        ObjectMapper objectMapper = new ObjectMapper();

        doThrow(new IllegalArgumentException("Current password is incorrect"))
                .when(userService).softDeleteAccount(TEST_USER_ID, "WrongPassword");

        mockMvc.perform(delete("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        logger.debug("Test passed: Incorrect password rejected");
    }

    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when password is missing for deletion")
    void testDeleteAccountMissingPassword() throws Exception {
        logger.debug("Test: Account deletion with missing password");

        String requestJson = "{}";

        mockMvc.perform(delete("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(userService, never()).softDeleteAccount(any(UUID.class), anyString());

        logger.debug("Test passed: Missing password rejected");
    }
}
