package com.library.service;

import com.library.dto.auth.AuthResponse;
import com.library.dto.auth.LoginRequest;
import com.library.dto.auth.RegisterRequest;
import com.library.dto.auth.TokenRefreshRequest;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.entity.UserPermission;
import com.library.entity.UserStatus;
import com.library.exception.BadRequestException;
import com.library.exception.UnauthorizedException;
import com.library.repository.UserPermissionRepository;
import com.library.repository.UserRepository;
import com.library.security.JwtTokenProvider;
import com.library.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final PermissionService permissionService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       UserPermissionRepository userPermissionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       PermissionService permissionService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.permissionService = permissionService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with email " + request.getEmail() + " already exists.");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_USER
        );
        user.setStatus(UserStatus.ACTIVE);
        // Default avatar with UI initials style
        user.setAvatarUrl("https://ui-avatars.com/api/?name=" + request.getName().replace(" ", "+") + "&background=1A242E&color=EBB036");

        User savedUser = userRepository.save(user);

        // Provision baseline default permissions
        UserPermission permission = new UserPermission(savedUser, true, true, true, true);
        userPermissionRepository.save(permission);

        // Auto authenticate and generate tokens
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return new AuthResponse(
                accessToken,
                refreshToken,
                tokenProvider.getJwtExpirationInMs(),
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getAvatarUrl()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Validate user account status and restrictions
        permissionService.validateUserActive(user);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return new AuthResponse(
                accessToken,
                refreshToken,
                tokenProvider.getJwtExpirationInMs(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token. Please log in again.");
        }

        Long userId = tokenProvider.getUserIdFromJWT(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));

        permissionService.validateUserActive(user);

        String newAccessToken = tokenProvider.generateTokenFromUserId(
                user.getId(), user.getEmail(), user.getRole().name(), tokenProvider.getJwtExpirationInMs()
        );

        return new AuthResponse(
                newAccessToken,
                request.getRefreshToken(),
                tokenProvider.getJwtExpirationInMs(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl()
        );
    }
}

