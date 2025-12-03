package ktb3.full.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ktb3.full.community.domain.entity.Comment;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static ktb3.full.community.common.Constants.MESSAGE_NOT_NULL_COMMENT_CONTENT;

@Schema(title = "댓글 생성 요청 DTO")
@Getter
@Builder
@RequiredArgsConstructor
public class CommentCreateRequest {

    @Schema(description = "내용", example = "테스트 댓글입니다.")
    @NotBlank(message = MESSAGE_NOT_NULL_COMMENT_CONTENT)
    private final String content;

    public Comment toEntity(User user, Post post) {
        return Comment.create(user, post, content);
    }
}
