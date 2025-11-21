package ktb3.full.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb3.full.community.domain.entity.Comment;
import ktb3.full.community.util.AccountValidator;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Schema(title = "댓글 응답 DTO")
@Getter
@Builder
@RequiredArgsConstructor
public class CommentResponse {

    @Schema(description = "댓글 ID", example = "1")
    private final long commentId;

    @Schema(description = "게시글 ID", example = "1")
    private final long postId;

    @Schema(description = "작성자 ID", example = "1")
    private final Long userId;

    @Schema(description = "작성자 닉네임", example = "testNick")
    private final String authorNickname;

    @Schema(description = "작성자 프로필", example = "https://test.kr/test.jpg")
    private final String authorProfile;

    @Schema(description = "내용", example = "테스트 댓글입니다.")
    private final String content;

    @Schema(description = "작성일", example = "2025-10-14 22:16:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdDate;

    public static CommentResponse from(Comment comment) {
        return builder()
                .commentId(comment.getId())
                .postId(comment.getPost().getId())
                .userId(AccountValidator.getUserId(comment.getUser()))
                .authorNickname(AccountValidator.getAuthorName(comment.getUser()))
                .authorProfile(AccountValidator.getAuthorProfileImageName(comment.getUser()))
                .content(comment.getContent())
                .createdDate(comment.getCreatedAt())
                .build();
    }
}
