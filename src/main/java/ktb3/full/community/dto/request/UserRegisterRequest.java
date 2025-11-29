package ktb3.full.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ktb3.full.community.common.annotation.constraint.EmailPattern;
import ktb3.full.community.common.annotation.constraint.NicknamePattern;
import ktb3.full.community.common.annotation.constraint.PasswordPattern;
import ktb3.full.community.domain.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import static ktb3.full.community.common.Constants.*;

@Schema(title = "회원 등록 요청 DTO")
@Getter
@RequiredArgsConstructor
public class UserRegisterRequest {

    @Schema(description = "이메일", example = "test@test.com")
    @NotNull(message = MESSAGE_NOT_NULL_EMAIL)
    @EmailPattern
    private final String email;

    @Schema(description = "비밀번호", example = "Testpassword1!")
    @NotNull(message = MESSAGE_NOT_NULL_PASSWORD)
    @PasswordPattern
    private final String password;

    @Schema(description = "닉네임", example = "testNick")
    @NotNull(message = MESSAGE_NOT_NULL_NICKNAME)
    @NicknamePattern
    private final String nickname;

    @Schema(description = "프로필 이미지", example = "https://test.kr/test.jpg")
    private final MultipartFile profileImage;

    public User toUserEntity(String encodedPassword, String profileImageName) {
        return User.create(email, encodedPassword, nickname, profileImageName, false);
    }
}
