package ktb3.full.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static ktb3.full.community.common.Constants.MESSAGE_NOT_NULL_EMAIL;
import static ktb3.full.community.common.Constants.MESSAGE_NOT_NULL_PASSWORD;

@Schema(title = "회원 로그인 요청 DTO")
@Getter
@Builder
@RequiredArgsConstructor
public class UserLoginRequest {

    @Schema(description = "이메일", example = "test@test.com")
    @NotBlank(message = MESSAGE_NOT_NULL_EMAIL)
    private final String email;

    @Schema(description = "비밀번호", example = "Testpassword1!")
    @NotBlank(message = MESSAGE_NOT_NULL_PASSWORD)
    private final String password;
}
