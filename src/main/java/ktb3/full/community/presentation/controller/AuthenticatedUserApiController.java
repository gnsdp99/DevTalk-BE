package ktb3.full.community.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ktb3.full.community.dto.response.UserAccountUpdateResponse;
import ktb3.full.community.security.userdetails.AuthUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ktb3.full.community.dto.request.UserAccountUpdateRequest;
import ktb3.full.community.dto.request.UserPasswordUpdateRequest;
import ktb3.full.community.dto.response.ApiResponse;
import ktb3.full.community.dto.response.UserAccountResponse;
import ktb3.full.community.presentation.api.AuthenticatedUserApi;
import ktb3.full.community.service.UserDeleteService;
import ktb3.full.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class AuthenticatedUserApiController implements AuthenticatedUserApi {

    private final UserService userService;
    private final UserDeleteService userDeleteService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserAccountResponse>> getUserAccount(@AuthenticationPrincipal AuthUserDetails userDetails) {
        UserAccountResponse userAccountResponse = userService.getUserAccount(userDetails.getUserId());
        return ResponseEntity.ok()
                .body(ApiResponse.success(userAccountResponse));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserAccountUpdateResponse>> updateUserAccount(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Valid @RequestBody UserAccountUpdateRequest userAccountUpdateRequest) {
        UserAccountUpdateResponse response = userService.updateAccount(userDetails.getUserId(), userAccountUpdateRequest);
        return ResponseEntity.ok()
                .body(ApiResponse.success(response));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Valid @RequestBody UserPasswordUpdateRequest userPasswordUpdateRequest) {
        userService.updatePassword(userDetails.getUserId(), userPasswordUpdateRequest);
        return ResponseEntity.ok()
                .body(ApiResponse.success());
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUserAccount(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        userDeleteService.deleteAccount(userDetails.getUserId());
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.ok()
                .body(ApiResponse.success());
    }
}
