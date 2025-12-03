package ktb3.full.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb3.full.community.common.Constants;
import ktb3.full.community.presentation.validator.NullableNotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(title = "댓글 수정 요청 DTO")
@Getter
@Builder
@RequiredArgsConstructor
public class CommentUpdateRequest {

    @Schema(description = "내용", example = "테스트 댓글입니다.")
    @NullableNotBlank(message = Constants.MESSAGE_NULLABLE_NOT_BLANK_COMMENT_CONTENT)
    private final String content;
}
