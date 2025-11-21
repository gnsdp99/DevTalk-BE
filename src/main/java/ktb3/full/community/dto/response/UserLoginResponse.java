package ktb3.full.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(title = "로그인 응답 DTO")
@Getter
@Builder
@RequiredArgsConstructor
public class UserLoginResponse {

    @Schema(description = "회원 ID", example = "1")
    private final long userId;

    @Schema(description = "프로필 이미지", example = "https://test.kr/test.jpg")
    private final String profileImageName;
}
