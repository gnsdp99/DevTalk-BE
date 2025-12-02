package ktb3.full.community.presentation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import ktb3.full.community.common.annotation.constraint.EmailPattern;
import ktb3.full.community.common.annotation.constraint.NicknamePattern;
import ktb3.full.community.dto.request.UserRegisterRequest;
import ktb3.full.community.dto.response.*;
import ktb3.full.community.presentation.api.UserApi;
import ktb3.full.community.security.userdetails.AuthUserDetails;
import ktb3.full.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RequiredArgsConstructor
@RequestMapping("/users")
@RestController
public class UserApiController implements UserApi {

    private final UserService userService;

    @GetMapping("/email-validation")
    public ResponseEntity<ApiResponse<UserValidationResponse>> validateEmailAvailable(@EmailPattern @RequestParam("email") String email) {
        UserValidationResponse userValidationResponse = userService.validateEmailAvailable(email);
        return ResponseEntity.ok()
                .body(ApiResponse.success(userValidationResponse));
    }

    @GetMapping("/nickname-validation")
    public ResponseEntity<ApiResponse<UserValidationResponse>> validateNicknameAvailable(@NicknamePattern @RequestParam("nickname") String nickname) {
        UserValidationResponse userValidationResponse = userService.validateNicknameAvailable(nickname);
        return ResponseEntity.ok()
                .body(ApiResponse.success(userValidationResponse));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @ModelAttribute UserRegisterRequest userRegisterRequest) {
        long userId = userService.register(userRegisterRequest);
        return ResponseEntity.created(URI.create(String.format("/users/%d", userId)))
                .body(ApiResponse.success());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@Positive @PathVariable("userId") long userId) {
        UserProfileResponse userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok()
                .body(ApiResponse.success(userProfile));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<UserLoginCheckResponse>> checkLogin(Authentication authentication) {
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
            userId = principal.getUserId();

        }
        return ResponseEntity.ok()
                .body(ApiResponse.success(new UserLoginCheckResponse(userId)));
    }
}
