package ktb3.full.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb3.full.community.domain.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(title = "회원정보수정 응답 DTO")
@Getter
@RequiredArgsConstructor
public class UserAccountUpdateResponse {

    @Schema(description = "프로필 이미지", example = "https://test.kr/test.jpg")
    private final String profileImageName;

    public static UserAccountUpdateResponse from(User user) {
        return new UserAccountUpdateResponse(user.getProfileImageName());
    }
}
