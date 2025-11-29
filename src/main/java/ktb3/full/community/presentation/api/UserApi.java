package ktb3.full.community.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import ktb3.full.community.common.annotation.constraint.EmailPattern;
import ktb3.full.community.common.annotation.constraint.NicknamePattern;
import ktb3.full.community.dto.request.UserRegisterRequest;
import ktb3.full.community.dto.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User", description = "회원 API")
public interface UserApi {

    @Operation(summary = "이메일 중복 검사", description = "이메일이 중복되는지 검사합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<ApiSuccessResponse<UserValidationResponse>> validateEmailAvailable(
            @EmailPattern @RequestParam("email") @Parameter(description = "이메일") String email);


    @Operation(summary = "닉네임 중복 검사", description = "닉네임이 중복되는지 검사합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<ApiSuccessResponse<UserValidationResponse>> validateNicknameAvailable(
            @NicknamePattern @RequestParam("nickname") @Parameter(description = "닉네임") String nickname);

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이메일, 닉네임 중복",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<ApiSuccessResponse<Void>> signUp(@Valid @ModelAttribute UserRegisterRequest userRegisterRequest);

    @Operation(summary = "사용자 조회", description = "ID를 이용해 특정 사용자를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
    })
    ResponseEntity<ApiSuccessResponse<UserProfileResponse>> getUserProfile(
            @Positive @PathVariable("userId") @Parameter(description = "회원 ID") long userId);

    @Operation(summary = "로그인 여부 조회", description = "회원의 로그인 여부를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/check")
    ResponseEntity<ApiSuccessResponse<UserLoginCheckResponse>> checkLogin(Authentication authentication);
}
