package com.kioku.api.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RegisterRequest DTO.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Field validation (required fields, email format, password length)</li>
 *   <li>Constructor initialization</li>
 *   <li>Getter and setter methods</li>
 *   <li>Edge cases (invalid emails, short passwords)</li>
 * </ul>
 *
 * @author Stephen Watson
 * @version 1.0
 * @since 1.0
 */
@DisplayName("RegisterRequest DTO Tests")
class RegisterRequestTest {

    private static final Logger logger = LoggerFactory.getLogger(RegisterRequestTest.class);

    private Validator validator;

    // Test data constants
    private static final String VALID_EMAIL = "newuser@example.com";
    private static final String VALID_PASSWORD = "SecurePassword123!";

    @BeforeEach
    void setUp() {
        logger.debug("Setting up RegisterRequest test");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Constructor Tests

    @Test
    @DisplayName("Should create request with default constructor")
    void testDefaultConstructor() {
        logger.debug("Test: Default constructor");

        RegisterRequest request = new RegisterRequest();

        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();

        logger.debug("Test passed: Default constructor works");
    }

    @Test
    @DisplayName("Should create request with parameterized constructor")
    void testParameterizedConstructor() {
        logger.debug("Test: Parameterized constructor");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, VALID_PASSWORD);

        assertThat(request.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(request.getPassword()).isEqualTo(VALID_PASSWORD);

        logger.debug("Test passed: Parameterized constructor works");
    }

    // Validation Tests - Valid Cases

    @Test
    @DisplayName("Should validate successfully with all valid fields")
    void testValidRequest() {
        logger.debug("Test: Valid request");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Valid request has no violations");
    }

    @Test
    @DisplayName("Should validate successfully with minimum password length")
    void testMinimumPasswordLength() {
        logger.debug("Test: Password with minimum length (6 characters)");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: 6 character password is valid");
    }

    @Test
    @DisplayName("Should validate successfully with long password")
    void testLongPassword() {
        logger.debug("Test: Long password");

        String longPassword = "a".repeat(100);
        RegisterRequest request = new RegisterRequest(VALID_EMAIL, longPassword);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Long password is valid (no max length)");
    }

    // Validation Tests - Email Field

    @Test
    @DisplayName("Should fail validation when email is null")
    void testEmailNull() {
        logger.debug("Test: Email is null");

        RegisterRequest request = new RegisterRequest(null, VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email is required");

        logger.debug("Test passed: Null email rejected");
    }

    @Test
    @DisplayName("Should fail validation when email is empty")
    void testEmailEmpty() {
        logger.debug("Test: Email is empty");

        RegisterRequest request = new RegisterRequest("", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email is required"));

        logger.debug("Test passed: Empty email rejected");
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void testEmailBlank() {
        logger.debug("Test: Email is blank");

        RegisterRequest request = new RegisterRequest("   ", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email is required"));

        logger.debug("Test passed: Blank email rejected");
    }

    @Test
    @DisplayName("Should fail validation when email has invalid format - no @")
    void testEmailInvalidNoAt() {
        logger.debug("Test: Email invalid - no @ symbol");

        RegisterRequest request = new RegisterRequest("invalid-email", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be valid");

        logger.debug("Test passed: Email without @ rejected");
    }

    @Test
    @DisplayName("Should fail validation when email has invalid format - no domain")
    void testEmailInvalidNoDomain() {
        logger.debug("Test: Email invalid - no domain");

        RegisterRequest request = new RegisterRequest("user@", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be valid");

        logger.debug("Test passed: Email without domain rejected");
    }

    @Test
    @DisplayName("Should fail validation when email has invalid format - no local part")
    void testEmailInvalidNoLocalPart() {
        logger.debug("Test: Email invalid - no local part");

        RegisterRequest request = new RegisterRequest("@example.com", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be valid");

        logger.debug("Test passed: Email without local part rejected");
    }

    @Test
    @DisplayName("Should validate successfully with subdomain email")
    void testEmailWithSubdomain() {
        logger.debug("Test: Email with subdomain");

        RegisterRequest request = new RegisterRequest("user@mail.example.com", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Subdomain email is valid");
    }

    @Test
    @DisplayName("Should validate successfully with plus sign in email")
    void testEmailWithPlusSign() {
        logger.debug("Test: Email with plus sign");

        RegisterRequest request = new RegisterRequest("user+test@example.com", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Email with + is valid");
    }

    @Test
    @DisplayName("Should validate successfully with dots in email")
    void testEmailWithDots() {
        logger.debug("Test: Email with dots");

        RegisterRequest request = new RegisterRequest("first.last@example.com", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Email with dots is valid");
    }

    @Test
    @DisplayName("Should validate successfully with hyphen in domain")
    void testEmailWithHyphenInDomain() {
        logger.debug("Test: Email with hyphen in domain");

        RegisterRequest request = new RegisterRequest("user@my-domain.com", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Email with hyphen in domain is valid");
    }

    @Test
    @DisplayName("Should validate successfully with numbers in email")
    void testEmailWithNumbers() {
        logger.debug("Test: Email with numbers");

        RegisterRequest request = new RegisterRequest("user123@example456.com", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Email with numbers is valid");
    }

    // Validation Tests - Password Field

    @Test
    @DisplayName("Should fail validation when password is null")
    void testPasswordNull() {
        logger.debug("Test: Password is null");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password is required");

        logger.debug("Test passed: Null password rejected");
    }

    @Test
    @DisplayName("Should fail validation when password is empty")
    void testPasswordEmpty() {
        logger.debug("Test: Password is empty");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Password is required"));

        logger.debug("Test passed: Empty password rejected");
    }

    @Test
    @DisplayName("Should fail validation when password is blank")
    void testPasswordBlank() {
        logger.debug("Test: Password is blank");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "   ");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Password is required"));

        logger.debug("Test passed: Blank password rejected");
    }

    @Test
    @DisplayName("Should fail validation when password is too short")
    void testPasswordTooShort() {
        logger.debug("Test: Password too short (5 characters)");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "12345");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password must be at least 6 characters");

        logger.debug("Test passed: 5 character password rejected");
    }

    @Test
    @DisplayName("Should fail validation when password is 1 character")
    void testPasswordSingleCharacter() {
        logger.debug("Test: Password is single character");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "a");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password must be at least 6 characters");

        logger.debug("Test passed: Single character password rejected");
    }

    @Test
    @DisplayName("Should validate successfully with special characters in password")
    void testPasswordWithSpecialCharacters() {
        logger.debug("Test: Password with special characters");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "P@ssw0rd!#$%");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Password with special characters is valid");
    }

    @Test
    @DisplayName("Should validate successfully with spaces in password")
    void testPasswordWithSpaces() {
        logger.debug("Test: Password with spaces");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "pass word 123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Password with spaces is valid");
    }

    @Test
    @DisplayName("Should validate successfully with unicode characters in password")
    void testPasswordWithUnicode() {
        logger.debug("Test: Password with unicode characters");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "パスワード123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Password with unicode is valid");
    }

    // Getter and Setter Tests

    @Test
    @DisplayName("Should set and get email correctly")
    void testSetAndGetEmail() {
        logger.debug("Test: Set and get email");

        RegisterRequest request = new RegisterRequest();
        request.setEmail(VALID_EMAIL);

        assertThat(request.getEmail()).isEqualTo(VALID_EMAIL);

        logger.debug("Test passed: Email setter and getter work");
    }

    @Test
    @DisplayName("Should set and get password correctly")
    void testSetAndGetPassword() {
        logger.debug("Test: Set and get password");

        RegisterRequest request = new RegisterRequest();
        request.setPassword(VALID_PASSWORD);

        assertThat(request.getPassword()).isEqualTo(VALID_PASSWORD);

        logger.debug("Test passed: Password setter and getter work");
    }

    // Edge Case Tests

    @Test
    @DisplayName("Should handle both fields null")
    void testBothFieldsNull() {
        logger.debug("Test: Both fields null");

        RegisterRequest request = new RegisterRequest(null, null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email is required"));
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Password is required"));

        logger.debug("Test passed: Both null fields rejected with 2 violations");
    }

    @Test
    @DisplayName("Should handle both fields empty")
    void testBothFieldsEmpty() {
        logger.debug("Test: Both fields empty");

        RegisterRequest request = new RegisterRequest("", "");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);

        logger.debug("Test passed: Both empty fields rejected");
    }

    @Test
    @DisplayName("Should handle invalid email and short password together")
    void testInvalidEmailAndShortPassword() {
        logger.debug("Test: Invalid email and short password");

        RegisterRequest request = new RegisterRequest("not-an-email", "123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Email must be valid"));
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Password must be at least 6 characters"));

        logger.debug("Test passed: Multiple validation failures detected");
    }

    @Test
    @DisplayName("Should handle uppercase email domain")
    void testUppercaseEmailDomain() {
        logger.debug("Test: Uppercase email domain");

        RegisterRequest request = new RegisterRequest("user@EXAMPLE.COM", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Uppercase domain is valid");
    }

    @Test
    @DisplayName("Should handle mixed case email")
    void testMixedCaseEmail() {
        logger.debug("Test: Mixed case email");

        RegisterRequest request = new RegisterRequest("User@Example.Com", VALID_PASSWORD);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: Mixed case email is valid");
    }

    @Test
    @DisplayName("Should handle exactly 6 character password with only digits")
    void testSixDigitPassword() {
        logger.debug("Test: Six digit password");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: 6 digit password is valid");
    }

    @Test
    @DisplayName("Should handle exactly 6 character password with only letters")
    void testSixLetterPassword() {
        logger.debug("Test: Six letter password");

        RegisterRequest request = new RegisterRequest(VALID_EMAIL, "abcdef");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();

        logger.debug("Test passed: 6 letter password is valid");
    }
}