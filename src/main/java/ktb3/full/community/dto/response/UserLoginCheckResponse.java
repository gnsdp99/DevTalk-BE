package ktb3.full.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(title = "로그인 여부 검사 응답 DTO")
@Getter
@RequiredArgsConstructor
public class UserLoginCheckResponse {

    @Schema(description = "회원 ID", example = "1")
    private final Long userId;
}
