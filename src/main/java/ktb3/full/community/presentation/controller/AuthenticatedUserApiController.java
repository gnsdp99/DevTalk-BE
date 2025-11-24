package ktb3.full.community.presentation.controller;

import jakarta.validation.Valid;
import ktb3.full.community.dto.response.UserAccountUpdateResponse;
import ktb3.full.community.security.userdetails.AuthUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ktb3.full.community.dto.request.UserAccountUpdateRequest;
import ktb3.full.community.dto.request.UserPasswordUpdateRequest;
import ktb3.full.community.dto.response.ApiSuccessResponse;
import ktb3.full.community.dto.response.UserAccountResponse;
import ktb3.full.community.presentation.api.AuthenticatedUserApi;
import ktb3.full.community.service.UserDeleteService;
import ktb3.full.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class AuthenticatedUserApiController implements AuthenticatedUserApi {

    private final UserService userService;
    private final UserDeleteService userDeleteService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<UserAccountResponse>> getUserAccount(@AuthenticationPrincipal AuthUserDetails userDetails) {
        UserAccountResponse userAccountResponse = userService.getUserAccount(userDetails.getUserId());
        return ResponseEntity.ok()
                .body(ApiSuccessResponse.of(userAccountResponse));
    }

    @PatchMapping
    public ResponseEntity<ApiSuccessResponse<UserAccountUpdateResponse>> updateUserAccount(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Valid @ModelAttribute UserAccountUpdateRequest userAccountUpdateRequest) {
        UserAccountUpdateResponse response = userService.updateAccount(userDetails.getUserId(), userAccountUpdateRequest);
        return ResponseEntity.ok()
                .body(ApiSuccessResponse.of(response));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiSuccessResponse<Void>> updatePassword(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Valid @RequestBody UserPasswordUpdateRequest userPasswordUpdateRequest) {
        userService.updatePassword(userDetails.getUserId(), userPasswordUpdateRequest);
        return ResponseEntity.ok()
                .body(ApiSuccessResponse.getBaseResponse());
    }

    @DeleteMapping
    public ResponseEntity<ApiSuccessResponse<Void>> deleteUserAccount(@AuthenticationPrincipal AuthUserDetails userDetails) {
        userDeleteService.deleteAccount(userDetails.getUserId());
        return ResponseEntity.ok()
                .body(ApiSuccessResponse.getBaseResponse());
    }
}
