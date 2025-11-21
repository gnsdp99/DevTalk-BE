package ktb3.full.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb3.full.community.common.annotation.constraint.NicknamePattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Schema(title = "회원 정보 수정 요청 DTO")
@Getter
@RequiredArgsConstructor
public class UserAccountUpdateRequest {

    @Schema(description = "닉네임", example = "testNick")
    @NicknamePattern
    private final String nickname;

    @Schema(description = "프로필 이미지", example = "https://test.kr/test.jpg")
    private final MultipartFile profileImage;
}
