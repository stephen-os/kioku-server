package com.kioku.api.controller;

import com.kioku.api.dto.request.DeleteAccountRequest;
import com.kioku.api.dto.request.UpdateEmailRequest;
import com.kioku.api.dto.request.UpdatePasswordRequest;
import com.kioku.api.dto.request.VerifyEmailChangeRequest;
import com.kioku.api.dto.response.AccountResponse;
import com.kioku.api.dto.response.ErrorResponse;
import com.kioku.api.model.User;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for user account management operations.
 *
 * <p>This controller provides endpoints for authenticated users to manage
 * their account settings, including:
 * <ul>
 *   <li>View account profile</li>
 *   <li>Change email address (with verification)</li>
 *   <li>Update password</li>
 *   <li>Delete account</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong>
 * <ul>
 *   <li>GET /api/account - Get current user's profile</li>
 *   <li>PATCH /api/account/email - Initiate email change</li>
 *   <li>POST /api/account/email/verify - Verify email change</li>
 *   <li>PATCH /api/account/password - Update password</li>
 *   <li>DELETE /api/account - Delete account</li>
 * </ul>
 *
 * <p><strong>Authentication:</strong>
 * All endpoints require authentication via JWT token.
 *
 * <p><strong>Security:</strong>
 * All sensitive operations (email change, password update, account deletion)
 * require the user's current password for verification.
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final UserService userService;

    /**
     * Constructs an AccountController with required dependencies.
     *
     * @param userService the user service for account operations
     */
    public AccountController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets the current user's account profile.
     *
     * <p><strong>Endpoint:</strong> GET /api/account
     *
     * <p><strong>Response:</strong> 200 OK with AccountResponse
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @return ResponseEntity containing the account profile
     */
    @GetMapping
    public ResponseEntity<AccountResponse> getProfile(@CurrentUser Long userId) {
        logger.debug("Getting account profile for user id={}", userId);

        User user = userService.getAccountProfile(userId);

        logger.debug("Account profile retrieved for user id={}", userId);
        return ResponseEntity.ok(new AccountResponse(user));
    }

    /**
     * Initiates an email change for the current user.
     *
     * <p><strong>Endpoint:</strong> PATCH /api/account/email
     *
     * <p>This starts a two-step email change process:
     * <ol>
     *   <li>User provides current password and new email</li>
     *   <li>System generates verification token (24hr expiration)</li>
     *   <li>User must verify using the token via /api/account/email/verify</li>
     * </ol>
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "currentPassword": "current-password",
     *   "newEmail": "new@example.com"
     * }
     * </pre>
     *
     * <p><strong>Response:</strong> 200 OK with verification token
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param request the email change request
     * @return ResponseEntity containing the verification token
     */
    @PatchMapping("/email")
    public ResponseEntity<?> initiateEmailChange(
            @CurrentUser Long userId,
            @Valid @RequestBody UpdateEmailRequest request) {

        logger.debug("Initiating email change for user id={} to {}", userId, request.getNewEmail());

        try {
            String token = userService.initiateEmailChange(
                    userId,
                    request.getCurrentPassword(),
                    request.getNewEmail()
            );

            logger.info("Email change initiated for user id={}", userId);

            // Return token - in production, this would be sent via email instead
            return ResponseEntity.ok(Map.of(
                    "message", "Email change initiated. Please verify with the token.",
                    "token", token
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Email change failed for user id={}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Verifies an email change using the verification token.
     *
     * <p><strong>Endpoint:</strong> POST /api/account/email/verify
     *
     * <p>Completes the email change process by verifying the token sent
     * in the initiation step.
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "token": "verification-token"
     * }
     * </pre>
     *
     * <p><strong>Response:</strong> 200 OK on success, 400 Bad Request if invalid/expired
     *
     * @param request the verification request containing the token
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmailChange(@Valid @RequestBody VerifyEmailChangeRequest request) {
        logger.debug("Verifying email change with token");

        boolean success = userService.confirmEmailChange(request.getToken());

        if (success) {
            logger.info("Email change verified successfully");
            return ResponseEntity.ok(Map.of("message", "Email changed successfully"));
        } else {
            logger.warn("Email change verification failed: invalid or expired token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid or expired verification token"));
        }
    }

    /**
     * Updates the current user's password.
     *
     * <p><strong>Endpoint:</strong> PATCH /api/account/password
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "currentPassword": "current-password",
     *   "newPassword": "new-password"
     * }
     * </pre>
     *
     * <p><strong>Response:</strong> 200 OK on success, 400 Bad Request if current password incorrect
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param request the password update request
     * @return ResponseEntity with success or error message
     */
    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(
            @CurrentUser Long userId,
            @Valid @RequestBody UpdatePasswordRequest request) {

        logger.debug("Updating password for user id={}", userId);

        boolean success = userService.updatePassword(
                userId,
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        if (success) {
            logger.info("Password updated for user id={}", userId);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } else {
            logger.warn("Password update failed for user id={}: incorrect current password", userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Current password is incorrect"));
        }
    }

    /**
     * Deletes the current user's account (soft delete).
     *
     * <p><strong>Endpoint:</strong> DELETE /api/account
     *
     * <p>This performs a soft delete - the account data is preserved but
     * marked as deleted. The user will no longer be able to log in.
     *
     * <p><strong>Request Body:</strong>
     * <pre>
     * {
     *   "currentPassword": "current-password"
     * }
     * </pre>
     *
     * <p><strong>Response:</strong> 204 No Content on success
     *
     * @param userId the authenticated user's ID (auto-resolved)
     * @param request the delete account request
     * @return ResponseEntity with no content on success
     */
    @DeleteMapping
    public ResponseEntity<?> deleteAccount(
            @CurrentUser Long userId,
            @Valid @RequestBody DeleteAccountRequest request) {

        logger.debug("Deleting account for user id={}", userId);

        try {
            userService.softDeleteAccount(userId, request.getCurrentPassword());
            logger.info("Account deleted for user id={}", userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Account deletion failed for user id={}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}
