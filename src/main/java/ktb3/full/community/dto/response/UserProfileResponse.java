package ktb3.full.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb3.full.community.domain.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(title = "회원 응답 DTO")
@Getter
@RequiredArgsConstructor
public class UserProfileResponse {

    @Schema(description = "회원 ID", example = "1")
    private final long userId;

    @Schema(description = "닉네임", example = "testNick")
    private final String nickname;

    @Schema(description = "프로필 이미지", example = "https://test.kr/test.jpg")
    private final String profileImageName;

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(user.getId(), user.getNickname(), user.getProfileImageName());
    }
}
