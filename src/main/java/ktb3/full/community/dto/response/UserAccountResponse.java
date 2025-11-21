package ktb3.full.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb3.full.community.domain.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Schema(title = "회원 정보 응답 DTO")
@Getter
@RequiredArgsConstructor
public class UserAccountResponse {

    @Schema(description = "회원 ID", example = "1")
    private final long userId;

    @Schema(description = "이메일", example = "test@test.com")
    private final String email;

    @Schema(description = "닉네임", example = "testNick")
    private final String nickname;

    @Schema(description = "프로필 이미지", example = "https://test.kr/test.jpg")
    private final String profileImageName;

    @Schema(description = "생성일", example = "2025-10-14 22:16:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    public static UserAccountResponse from(User user) {
        return new UserAccountResponse(user.getId(), user.getEmail(), user.getNickname(), user.getProfileImageName(), user.getCreatedAt());
    }
}
