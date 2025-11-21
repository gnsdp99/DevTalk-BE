package ktb3.full.community.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.util.AccountValidator;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Schema(title = "게시글 응답 DTO")
@Getter
@Builder
@RequiredArgsConstructor
public class PostResponse {

    @Schema(description = "게시글 ID", example = "1")
    private final long postId;

    @Schema(description = "제목", example = "테스트 제목입니다.")
    private final String title;

    @Schema(description = "내용", example = "테스트 게시글입니다.")
    private final String content;

    @Schema(description = "작성자 닉네임", example = "testNick")
    private final String authorNickname;

    @Schema(description = "작성자 프로필", example = "https://test.kr/test.jpg")
    private final String authorProfileImageName;

    @Schema(description = "작성일", example = "2025-10-14 22:16:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdDate;

    @Schema(description = "좋아요 수", example = "83")
    private final int likeCount;

    @Schema(description = "댓글 수", example = "21")
    private final int commentCount;

    @Schema(description = "조회수", example = "210")
    private final int viewCount;

    public static PostResponse from(Post post) {
        return builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(AccountValidator.getAuthorName(post.getUser()))
                .authorProfileImageName(AccountValidator.getAuthorProfileImageName(post.getUser()))
                .createdDate(post.getCreatedAt())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .build();
    }
}
