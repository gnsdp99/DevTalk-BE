package ktb3.full.community.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Comment extends AuditTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public static Comment create(User user, Post post, String content) {
        return Comment.builder()
                .user(user)
                .post(post)
                .content(content)
                .isDeleted(false)
                .build();
    }

    @Builder
    private Comment(User user, Post post, String content, boolean isDeleted) {
        this.user = user;
        this.post = post;
        this.content = content;
        this.isDeleted = isDeleted;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void delete() {
        if (isDeleted) {
            throw new IllegalStateException("이미 삭제된 댓글입니다.");
        }

        this.isDeleted = true;
        this.auditDeletedAt();
    }
}
