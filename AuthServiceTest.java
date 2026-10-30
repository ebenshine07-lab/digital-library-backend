package com.library.service;

import com.library.dto.auth.AuthResponse;
import com.library.dto.auth.LoginRequest;
import com.library.dto.auth.RegisterRequest;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.entity.UserStatus;
import com.library.exception.BadRequestException;
import com.library.repository.UserPermissionRepository;
import com.library.repository.UserRepository;
import com.library.security.JwtTokenProvider;
import com.library.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPermissionRepository userPermissionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private Authentication mockAuth;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Abhishek Saha", "abhishek@library.com", "123456", "encoded_hash", Role.ROLE_USER);
        sampleUser.setId(2L);
        sampleUser.setStatus(UserStatus.ACTIVE);

        UserPrincipal principal = UserPrincipal.create(sampleUser);
        mockAuth = mock(Authentication.class);
        lenient().when(mockAuth.getPrincipal()).thenReturn(principal);
    }

    @Test
    @DisplayName("Registration succeeds for new unique email")
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("Abhishek Saha", "abhishek@library.com", "123456", "Password@123");

        when(userRepository.existsByEmail("abhishek@library.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded_hash");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(tokenProvider.generateToken(mockAuth)).thenReturn("mock-access-token");
        when(tokenProvider.generateRefreshToken(mockAuth)).thenReturn("mock-refresh-token");
        when(tokenProvider.getJwtExpirationInMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("Abhishek Saha", response.getName());
        assertEquals("abhishek@library.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userPermissionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Registration throws BadRequestException when email already exists")
    void shouldThrowWhenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("Abhishek Saha", "abhishek@library.com", "123456", "Password@123");
        when(userRepository.existsByEmail("abhishek@library.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login succeeds with valid credentials")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("abhishek@library.com", "Password@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));
        when(tokenProvider.generateToken(mockAuth)).thenReturn("jwt-token");
        when(tokenProvider.generateRefreshToken(mockAuth)).thenReturn("refresh-token");
        when(tokenProvider.getJwtExpirationInMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals(Role.ROLE_USER, response.getRole());
        verify(permissionService, times(1)).validateUserActive(sampleUser);
    }
}

